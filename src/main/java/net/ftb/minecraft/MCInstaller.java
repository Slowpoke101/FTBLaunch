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
package net.ftb.minecraft;

import com.google.common.collect.Lists;
import net.feed_the_beast.launcher.json.JsonFactory;
import net.feed_the_beast.launcher.json.assets.AssetIndex;
import net.feed_the_beast.launcher.json.versions.Library;
import net.feed_the_beast.launcher.json.versions.Version;
import net.ftb.data.LauncherStyle;
import net.ftb.data.LoginResponse;
import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.download.Locations;
import net.ftb.download.info.DownloadInfo;
import net.ftb.download.workers.AssetDownloader;
import net.ftb.events.EnableObjectsEvent;
import net.ftb.gui.LauncherFrame;
import net.ftb.gui.panes.OptionsPane;
import net.ftb.log.LogEntry;
import net.ftb.log.LogLevel;
import net.ftb.log.Logger;
import net.ftb.log.StreamLogger;
import net.ftb.main.Main;
import net.ftb.tools.ProcessMonitor;
import net.ftb.util.*;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MCInstaller {
    public static void setupNewStyle (final String installPath, final ModPack pack, final boolean isLegacy, final LoginResponse RESPONSE) {
        List<DownloadInfo> assets = gatherAssets(new File(installPath), pack.getMcVersion(Settings.getSettings().getPackVer(pack.getDir())),installPath);
        if (assets != null && assets.size() > 0) {
            Logger.logInfo("Checking/Downloading " + assets.size() + " assets, this may take a while...");

            final ProgressMonitor prog = new ProgressMonitor(LauncherFrame.getInstance(), "Downloading Files...", "", 0, 100);
            prog.setMaximum(assets.size() * 100);

            final AssetDownloader downloader = new AssetDownloader(prog, assets) {
                @Override
                public void done () {
                    try {
                        prog.close();
                        if (get()) {
                            Logger.logInfo("Asset downloading complete");
                            launchMinecraft(installPath, pack, RESPONSE, isLegacy);
                        } else {
                            ErrorUtils.tossError("Error occurred during downloading the assets");
                        }
                    } catch (CancellationException e) {
                        Logger.logInfo("Asset download interrupted by user");
                    } catch (Exception e) {
                        ErrorUtils.tossError("Failed to download files.", e);
                    } finally {
                        Main.getEventBus().post(new EnableObjectsEvent());
                    }
                }
            };

            downloader.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange (PropertyChangeEvent evt) {
                    if (prog.isCanceled()) {
                        downloader.cancel(false);
                        prog.close();
                    } else if (!downloader.isCancelled()) {
                        if ("ready".equals(evt.getPropertyName()))
                            prog.setProgress(downloader.getReady());
                        if ("status".equals(evt.getPropertyName()))
                            prog.setNote(downloader.getStatus());
                    }
                }
            });

            downloader.execute();
        } else if (assets == null) {
            Main.getEventBus().post(new EnableObjectsEvent());
        } else {
            launchMinecraft(installPath, pack, RESPONSE, isLegacy);
        }
    }

    /**
     * Gather assets to check/download. If force update is enabled this will return all assets
     *
     * @return null if failed and MC can't be started, otherwise, ArrayList containing assets to be checked/downloaded
     *              Normally, if offline mode works, setupNewStyle() and gatherAssets() are not called and error situation is impossible
     *              Returning null just in case of network breakge after authentication process
     */
    private static List<DownloadInfo> gatherAssets (final File root, String mcVersion, String installDir) {
        try {
            Logger.logInfo("Checking local assets file, for MC version" + mcVersion + " Please wait! ");
            List<DownloadInfo> list = Lists.newArrayList();
            Boolean forceUpdate = Settings.getSettings().isForceUpdateEnabled();

            /*
             * vanilla minecraft.jar
             */

            File local = new File(root, "versions/{MC_VER}/{MC_VER}.jar".replace("{MC_VER}", mcVersion));
            if (!local.exists() || forceUpdate) {
                list.add(new DownloadInfo(new URL(Locations.mc_dl + "versions/{MC_VER}/{MC_VER}.jar".replace("{MC_VER}", mcVersion)), local, local.getName()));
            }

            /*
             * <ftb installation location>/libraries/*
             */
            //check if our copy exists of the version json if not backup to mojang's copy
            Logger.logDebug("Checking minecraft.jar");
            URL url = new URL(DownloadUtils.getStaticCreeperhostLinkOrBackup("mcjsons/versions/{MC_VER}/{MC_VER}.json".replace("{MC_VER}", mcVersion), Locations.mc_dl
                    + "versions/{MC_VER}/{MC_VER}.json".replace("{MC_VER}", mcVersion)));
            File json = new File(root, "versions/{MC_VER}/{MC_VER}.json".replace("{MC_VER}", mcVersion));

            DownloadUtils.downloadToFile(url, json, 3);
            if (!json.exists()) {
                Logger.logError("library JSON not found");
                return null;
            }

            Version version = JsonFactory.loadVersion(json);
            //TODO make sure to  setup lib DL's for pack.json!!!
            Logger.logDebug("checking minecraft libraries");
            for (Library lib : version.getLibraries()) {
                if (lib.natives == null) {
                    local = new File(root, "libraries/" + lib.getPath());
                    if (!local.exists() || forceUpdate) {
                        if (!lib.getUrl().toLowerCase().equalsIgnoreCase(Locations.ftb_maven)) {//DL's shouldn't be coming from maven repos but ours or mojang's
                            list.add(new DownloadInfo(new URL(lib.getUrl() + lib.getPath()), local, lib.getPath()));
                        } else {
                            list.add(new DownloadInfo(new URL(DownloadUtils.getCreeperhostLink(lib.getUrl() + lib.getPath())), local, lib.getPath(), true));
                        }
                    }
                } else {
                    local = new File(root, "libraries/" + lib.getPathNatives());
                    if (!local.exists() || forceUpdate) {
                        list.add(new DownloadInfo(new URL(lib.getUrl() + lib.getPathNatives()), local, lib.getPathNatives()));
                    }

                }
            }

            //Pack JSON Libraries
            Logger.logDebug("Checking pack libararies");
            ModPack pack = ModPack.getSelectedPack();
            File packDir = new File(installDir, pack.getDir());
            File gameDir = new File(packDir, "minecraft");
            File libDir = new File(installDir, "libraries");
            if (!pack.getDir().equals("mojang_vanilla")) {
                if (new File(gameDir, "pack.json").exists()) {
                    Version packjson = JsonFactory.loadVersion(new File(gameDir, "pack.json"));
                    for (Library lib : packjson.getLibraries()) {
                        //Logger.logError(new File(libDir, lib.getPath()).getAbsolutePath());
                        // These files are shipped inside pack.zip, can't do force update check yet
                        local = new File(root, "libraries/" + lib.getPath());
                        if(!new File(libDir, lib.getPath()).exists()){
                            if (lib.checksums!= null)
                                list.add(new DownloadInfo(new URL(lib.getUrl() + lib.getPath()), local, lib.getPath(), lib.checksums, "sha1", DownloadInfo.DLType.NONE, DownloadInfo.DLType.NONE));
                            else if(lib.download != null && lib.download)
                                list.add(new DownloadInfo(new URL(lib.getUrl() + lib.getPath()), local, lib.getPath()));
                        }
                    }
                }
            } else {
                //TODO handle vanilla packs w/ tweakers w/ this stuffs !!!
            }

            // Move the old format to the new:
            File test = new File(root, "assets/READ_ME_I_AM_VERY_IMPORTANT.txt");
            if (test.exists()) {
                Logger.logDebug("Moving old format");
                File assets = new File(root, "assets");
                Set<File> old = FileUtils.listFiles(assets);
                File objects = new File(assets, "objects");
                String[] skip = new String[] { objects.getAbsolutePath(), new File(assets, "indexes").getAbsolutePath(), new File(assets, "virtual").getAbsolutePath() };

                for (File f : old) {
                    String path = f.getAbsolutePath();
                    boolean move = true;
                    for (String prefix : skip) {
                        if (path.startsWith(prefix))
                            move = false;
                    }
                    if (move) {
                        String hash = DownloadUtils.fileSHA(f);
                        File cache = new File(objects, hash.substring(0, 2) + "/" + hash);
                        Logger.logInfo("Caching Asset: " + hash + " - " + f.getAbsolutePath().replace(assets.getAbsolutePath(), ""));
                        if (!cache.exists()) {
                            cache.getParentFile().mkdirs();
                            f.renameTo(cache);
                        }
                        f.delete();
                    }
                }

                List<File> dirs = FileUtils.listDirs(assets);
                for (File dir : dirs) {
                    if (dir.listFiles().length == 0) {
                        dir.delete();
                    }
                }
            }

            /*
             * assets/*
             */
            Logger.logDebug("Checking minecraft assets");
            url = new URL(Locations.mc_dl + "indexes/{INDEX}.json".replace("{INDEX}", version.getAssets()));
            json = new File(root, "assets/indexes/{INDEX}.json".replace("{INDEX}", version.getAssets()));

            DownloadUtils.downloadToFile(url, json, 3);
            if (!json.exists()) {
                Logger.logError("asset JSON not found");
                return null;
            }

            AssetIndex index = JsonFactory.loadAssetIndex(json);

            Benchmark.start("threading");
            long size = list.size();
            Collection<DownloadInfo> tmp;
            Logger.logDebug("Starting TaskHandler to check MC assets");
            Parallel.TaskHandler th = new Parallel.ForEach(index.objects.entrySet())
                    .withFixedThreads(2*OSUtils.getNumCores())
                            //.configurePoolSize(2*2*OSUtils.getNumCores(), 10)
                    .apply( new Parallel.F<Map.Entry<String, AssetIndex.Asset>, DownloadInfo>() {
                        public DownloadInfo apply(Map.Entry<String, AssetIndex.Asset> e) {
                            try {
                                //Logger.logDebug("YYYY" + System.currentTimeMillis());
                                String name = e.getKey();
                                AssetIndex.Asset asset = e. getValue();
                                String path = asset.hash.substring(0, 2) + "/" + asset.hash;
                                final File local = new File(root, "assets/objects/" + path);
                                if (local.exists() && !asset.hash.equals(DownloadUtils.fileSHA(local))) {
                                    local.delete();
                                }
                                if (!local.exists()) {
                                    return(new DownloadInfo(new URL(Locations.mc_res + path), local, name, Lists.newArrayList(asset.hash), "sha1"));
                                }
                            } catch (Exception ex) {
                                Logger.logError("Asset hash check failed", ex);
                            }
                            // values() will drop null entries
                            return null;
                        }
                    });
            tmp = th.values();
            list.addAll(tmp);
            // kill executorservice
            th.shutdown();
            Benchmark.logBenchAs("threading", "parallel asset check");

            return list;
        } catch (Exception e) {
            Logger.logError("Error while gathering assets", e);
        }
        return null;
    }

    public static void launchMinecraft(String installDir, ModPack pack, LoginResponse resp, boolean isLegacy) {
        try {
            File packDir = new File(installDir, pack.getDir());
            String gameFolder = installDir + File.separator + pack.getDir() + File.separator + "minecraft";
            File gameDir = new File(packDir, "minecraft");
            File assetDir = new File(installDir, "assets");
            File libDir = new File(installDir, "libraries");
            File natDir = new File(packDir, "natives");
            final String packVer = Settings.getSettings().getPackVer(pack.getDir());

            Logger.logInfo("Setting up native libraries for " + pack.getName() + " v " + packVer + " MC " + pack.getMcVersion(packVer));
            if(!gameDir.exists())
                gameDir.mkdirs();
            
            if (natDir.exists()) {
                natDir.delete();
            }
            natDir.mkdirs();
            if (isLegacy)
                extractLegacy();
            Version base = JsonFactory.loadVersion(new File(installDir, "versions/{MC_VER}/{MC_VER}.json".replace("{MC_VER}", pack.getMcVersion(packVer))));
            byte[] buf = new byte[1024];
            for (Library lib : base.getLibraries()) {
                if (lib.natives != null) {
                    File local = new File(libDir, lib.getPathNatives());
                    ZipInputStream input = null;
                    try {
                        input = new ZipInputStream(new FileInputStream(local));
                        ZipEntry entry = input.getNextEntry();
                        while (entry != null) {
                            String name = entry.getName();
                            int n;
                            if (lib.extract == null || !lib.extract.exclude(name)) {
                                File output = new File(natDir, name);
                                output.getParentFile().mkdirs();
                                FileOutputStream out = new FileOutputStream(output);
                                while ((n = input.read(buf, 0, 1024)) > -1) {
                                    out.write(buf, 0, n);
                                }
                                out.close();
                            }
                            input.closeEntry();
                            entry = input.getNextEntry();
                        }
                    } catch (Exception e) {
                        ErrorUtils.tossError("Error extracting native libraries");
                        Logger.logError("", e);
                    } finally {
                        try {
                            input.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }
            List<File> classpath = new ArrayList<File>();
            Version packjson = new Version();
            if (!pack.getDir().equals("mojang_vanilla")) {
                if (isLegacy) {
                    extractLegacyJson(new File(gameDir, "pack.json"));
                }
                if (new File(gameDir, "pack.json").exists()) {
                    packjson = JsonFactory.loadVersion(new File(gameDir, "pack.json"));
                    for (Library lib : packjson.getLibraries()) {
                        //Logger.logError(new File(libDir, lib.getPath()).getAbsolutePath());
                        classpath.add(new File(libDir, lib.getPath()));
                    }
                }
            } else {
                packjson = base;
            }
            if (!isLegacy) //we copy the jar to a new location for legacy
                classpath.add(new File(installDir, "versions/{MC_VER}/{MC_VER}.jar".replace("{MC_VER}", pack.getMcVersion(packVer))));
            else {
                FileUtils.copyFile(new File(installDir, "versions/{MC_VER}/{MC_VER}.jar".replace("{MC_VER}", pack.getMcVersion(packVer))), new File(gameDir, "bin/" + Locations.OLDMCJARNAME));
                FileUtils.killMetaInf();
            }
            for (Library lib : base.getLibraries()) {
                classpath.add(new File(libDir, lib.getPath()));
            }

            Process minecraftProcess = MCLauncher.launchMinecraft(Settings.getSettings().getJavaPath(), gameFolder, assetDir, natDir, classpath,
                    packjson.mainClass != null ? packjson.mainClass : base.mainClass, packjson.minecraftArguments != null ? packjson.minecraftArguments : base.minecraftArguments,
                    packjson.assets != null ? packjson.assets : base.getAssets(), Settings.getSettings().getRamMax(), pack.getMaxPermSize(), pack.getMcVersion(packVer), resp.getAuth(), isLegacy);
            LauncherFrame.MCRunning = true;
            if (LauncherFrame.con != null)
                LauncherFrame.con.minecraftStarted();
            StreamLogger.prepare(minecraftProcess.getInputStream(), new LogEntry().level(LogLevel.UNKNOWN));
            String[] ignore = {"Session ID is token"};
            StreamLogger.setIgnore(ignore);
            StreamLogger.doStart();
            String curVersion = (Settings.getSettings().getPackVer().equalsIgnoreCase("recommended version") ? pack.getVersion() : Settings.getSettings().getPackVer()).replace(".", "_");
            TrackerUtils.sendPageView(ModPack.getSelectedPack().getName(), "Launched / " + ModPack.getSelectedPack().getName() + " / " + curVersion.replace('_', '.'));
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
            }
            try {
                minecraftProcess.exitValue();
            } catch (IllegalThreadStateException e) {
                LauncherFrame.getInstance().setVisible(false);
                LauncherFrame.setProcMonitor(ProcessMonitor.create(minecraftProcess, new Runnable() {
                    @Override
                    public void run() {
                        if (!Settings.getSettings().getKeepLauncherOpen()) {
                            System.exit(0);
                        } else {
                            if (LauncherFrame.con != null)
                                LauncherFrame.con.minecraftStopped();
                            LauncherFrame launchFrame = LauncherFrame.getInstance();
                            launchFrame.setVisible(true);
                            Main.getEventBus().post(new EnableObjectsEvent());
                            try {
                                Settings.getSettings().load(new FileInputStream(Settings.getSettings().getConfigFile()));
                                LauncherFrame.getInstance().tabbedPane.remove(1);
                                LauncherFrame.getInstance().optionsPane = new OptionsPane(Settings.getSettings());
                                LauncherFrame.getInstance().tabbedPane.add(LauncherFrame.getInstance().optionsPane, 1);
                                LauncherFrame.getInstance().tabbedPane.setIconAt(1, LauncherStyle.getCurrentStyle().filterHeaderIcon(this.getClass().getResource("/image/tabs/options.png")));
                            } catch (Exception e1) {
                                Logger.logError("Failed to reload settings after launcher closed", e1);
                            }
                        }
                        LauncherFrame.MCRunning = false;
                    }
                }));
            }
        } catch (Exception e) {
            Logger.logError("Error while running launchMinecraft()", e);
        }
    }

    /**
     * @param modPackName - The pack to install (should already be downloaded)
     * @throws IOException
     */
    public static void installMods (String modPackName) throws IOException {
        String installpath = Settings.getSettings().getInstallPath();
        String temppath = OSUtils.getCacheStorageLocation();

        ModPack pack;
        if (LauncherFrame.currentPane == LauncherFrame.Panes.THIRDPARTY)
            pack = ModPack.getPack(LauncherFrame.getInstance().thirdPartyPane.getSelectedThirdPartyModIndex());
        else
            pack = ModPack.getPack(LauncherFrame.getInstance().modPacksPane.getSelectedFTBModIndex());

        String packDir = pack.getDir();

        Logger.logInfo("dirs mk'd");

        File source = new File(temppath, "ModPacks/" + packDir + "/.minecraft");
        if (!source.exists()) {
            source = new File(temppath, "ModPacks/" + packDir + "/minecraft");
        }

        Logger.logDebug("install path: " + installpath);
        Logger.logDebug("temp path: " + temppath);
        Logger.logDebug("source: " + source);
        Logger.logDebug("packDir: " + packDir);

        FileUtils.copyFolder(source, new File(installpath, packDir + "/minecraft/"));
        FileUtils.copyFolder(new File(temppath, "ModPacks/" + packDir + "/instMods/"), new File(installpath, packDir + "/instMods/"));
        FileUtils.copyFolder(new File(temppath, "ModPacks/" + packDir + "/libraries/"), new File(installpath, "/libraries/"), false);
    }


    public static void extractLegacy () {
        try {
            File f = new File(Settings.getSettings().getInstallPath() + File.separator + "libraries" + File.separator + "net.ftb.legacylaunch.FTBLegacyLaunch".replace(".", File.separator)
                    + File.separator + "0.0.1" + File.separator + "FTBLegacyLaunch-0.0.1.jar");
            //Logger.logError("Extracting Legacy launch code to " + f.getAbsolutePath());
            if (!new File(f.getParent()).exists())
                new File(f.getParent()).mkdirs();
            if (f.exists())
                f.delete();//we want to have the current version always!!!
            URL u = LauncherFrame.class.getResource("/launch/FTBLegacyLaunch-0.0.1.jar");
            org.apache.commons.io.FileUtils.copyURLToFile(u, f);
        } catch (Exception e) {
            Logger.logError("Error extracting legacy launch to maven directory");
        }
    }

    public static void extractLegacyJson (File newLoc) {
        try {
            if (!new File(newLoc.getParent()).exists())
                new File(newLoc.getParent()).mkdirs();
            if (newLoc.exists())
                newLoc.delete();//we want to have the current version always!!!
            URL u = LauncherFrame.class.getResource("/launch/legacypack.json");
            org.apache.commons.io.FileUtils.copyURLToFile(u, newLoc);
        } catch (Exception e) {
            Logger.logError("Error extracting legacy json to maven directory");
        }
    }
}
