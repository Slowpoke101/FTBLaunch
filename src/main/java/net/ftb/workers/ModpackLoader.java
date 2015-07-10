/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2014, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
 * FTB Launcher is licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ftb.workers;

import static net.ftb.download.Locations.MODPACKXML;
import static net.ftb.download.Locations.THIRDPARTYXML;

import com.google.common.collect.Lists;
import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.gui.LaunchFrame;
import net.ftb.log.Logger;
import net.ftb.util.*;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ModpackLoader extends Thread {
    private ArrayList<String> xmlFiles = Lists.newArrayList();
    private boolean disableOtherLoader = false;
    private static int offset = 0;

    public ModpackLoader (ArrayList<String> xmlFiles, boolean disableOtherLoaders) {
        this.xmlFiles = xmlFiles;
        this.disableOtherLoader = disableOtherLoaders;
    }

    @Override
    public void run () {
        Benchmark.start("ModpackLoader");

        ExecutorService executor = new ThreadPoolExecutor(4, 4, 5L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(8), new ThreadPoolExecutor.CallerRunsPolicy());

        for (String xmlFile : xmlFiles) {
            executor.submit(new XmlHtmlRunnable(xmlFile, offset));
            offset = offset + 100;
        }

        executor.shutdown();
        try {
            executor.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Logger.logError("failed", e);
        }

        // everything added sort packs
        ModPack.sortPacks();

        Benchmark.logBenchAs("ModpackLoader", "All modpack lists loaded");
        LaunchFrame.checkDoneLoading();
    }

    private class XmlHtmlRunnable implements Runnable {
        private String xmlFile;
        private int offset;
        public XmlHtmlRunnable(String xmlFile, int offset) {
            this.xmlFile = xmlFile;
            this.offset = offset;
        }

        @Override public void run () {
            handleXML(xmlFile, offset);
        }

        private void handleXML (String xmlFile, int offset) {
            boolean privatePack = !xmlFile.equalsIgnoreCase(MODPACKXML) && !xmlFile.equalsIgnoreCase(THIRDPARTYXML);//this is for stuff that is stored under privatepacks on the repo
            boolean isThirdParty = !xmlFile.equalsIgnoreCase(THIRDPARTYXML);
            File modPackFile = new File(OSUtils.getCacheStorageLocation(), "ModPacks" + File.separator + xmlFile);
            try {
                modPackFile.getParentFile().mkdirs();
                String s = DownloadUtils.getStaticCreeperhostLink(xmlFile);
                DownloadUtils.downloadToFile(new URL(s), modPackFile);
                Benchmark.logBenchAs("ModpackLoader", "Modpack Loader " + s.replace(".creeperrepo.net/FTB2/static", "").replace(".cursecdn.com/FTB2/static", ""));
            } catch (IOException e) {
                Logger.logWarn("Failed to load modpacks, loading from backup", e);
            }
            Logger.logInfo("Loading modpack information for " + xmlFile + "...");

            InputStream modPackStream = null;
            try {
                modPackStream = new FileInputStream(modPackFile);
                if (Settings.getSettings().getDebugLauncher()) {
                    Logger.logDebug(xmlFile + " MD5: " + DownloadUtils.fileMD5(modPackFile) + " Size: " + modPackFile.length());
                }
            } catch (IOException e) {
                Logger.logWarn("Failed to read modpack file - falling back to direct download", e);
            }
            if (modPackStream == null) {
                try {
                    modPackStream = new URL(DownloadUtils.getStaticCreeperhostLink(xmlFile)).openStream();
                } catch (IOException e) {
                    Logger.logError("Completely unable to download the modpack file - check your connection", e);
                }
            }
            if (modPackStream != null) {
                Document doc;
                try {
                    doc = AppUtils.getXML(modPackStream);
                } catch (Exception e) {
                    Logger.logError("Exception reading modpack file", e);
                    return;
                }
                if (doc == null) {
                    Logger.logError("Error: could not load modpack data!");
                    return;
                }
                NodeList modPacks = doc.getElementsByTagName("modpack");
                ArrayList<ModPack> mp = Lists.newArrayList();
                // ATT: this is not thread safe. Only one thread can run rest of the code!
                // proper fix: ModPack.add() should assign indexes for modpacks

                for (int i = 0; i < modPacks.getLength(); i++) {
                    Node modPackNode = modPacks.item(i);
                    NamedNodeMap modPackAttr = modPackNode.getAttributes();
                    try {
                        if (modPackAttr.getNamedItem("author") != null) {
                            isThirdParty = !modPackAttr.getNamedItem("author").getTextContent().equalsIgnoreCase("the ftb team");
                        }
                        mp.add(new ModPack(modPackAttr.getNamedItem("name").getTextContent(), modPackAttr.getNamedItem("author").getTextContent(), modPackAttr.getNamedItem("version")
                                .getTextContent(), modPackAttr.getNamedItem("logo").getTextContent(), modPackAttr.getNamedItem("url").getTextContent(), modPackAttr.getNamedItem("image")
                                .getTextContent(), modPackAttr.getNamedItem("dir").getTextContent(), modPackAttr.getNamedItem("mcVersion").getTextContent(), modPackAttr.getNamedItem("serverPack")
                                .getTextContent(), modPackAttr.getNamedItem("description") == null ? null : modPackAttr.getNamedItem("description").getTextContent().replace("\\n", "\n"),
                                modPackAttr.getNamedItem("mods") != null ? modPackAttr.getNamedItem("mods")
                                        .getTextContent() : "", modPackAttr.getNamedItem("oldVersions") != null ? modPackAttr.getNamedItem("oldVersions").getTextContent() : "", modPackAttr
                                .getNamedItem("animation") != null ? modPackAttr.getNamedItem("animation").getTextContent() : "", modPackAttr.getNamedItem("maxPermSize") != null ? modPackAttr
                                .getNamedItem("maxPermSize").getTextContent() : "", offset + i,
                                (isThirdParty && !privatePack) ? (modPackAttr.getNamedItem("private") != null) : privatePack, xmlFile, modPackAttr
                                .getNamedItem("bundledMap") != null, modPackAttr.getNamedItem("customTP") != null, modPackAttr
                                .getNamedItem("minJRE") != null ? modPackAttr.getNamedItem("minJRE").getTextContent() : "1.6", isThirdParty, modPackAttr
                                .getNamedItem("minLaunchSpec") == null ? 0 : Integer.parseInt(modPackAttr.getNamedItem("minLaunchSpec").getTextContent()), modPackAttr
                                .getNamedItem("warning") == null ? null : modPackAttr.getNamedItem("warning").getTextContent().replace("\\n", "\n"), modPackAttr
                                .getNamedItem("customMCVersions") == null ? null : modPackAttr.getNamedItem("customMCVersions").getTextContent()
                        ));
                    } catch (Exception e) {
                        Logger.logError("Error while updating modpack info", e);
                    }
                }

                ModPack.addPacks(mp);
                try {
                    modPackStream.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
