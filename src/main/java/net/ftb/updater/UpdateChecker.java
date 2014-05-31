/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2013, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
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
package net.ftb.updater;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;

import javax.swing.SwingWorker;

import net.ftb.data.Settings;
import net.ftb.download.Locations;
import net.ftb.gui.LaunchFrame;
import net.ftb.log.Logger;
import net.ftb.util.AppUtils;
import net.ftb.util.DownloadUtils;
import net.ftb.util.FileUtils;
import net.ftb.util.OSUtils;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

public class UpdateChecker extends SwingWorker<Boolean, Void> {
    private int version;
    private int buildJenk;
    private int beta;//beta release target
    private int betaJenk;//beta CI build #
    private int relJenk;//beta CI build #

    private int latest;
    private int minUsable;
    public static String verString = "";
    public static String betaStr = "";
    private String downloadAddress = "";
    private String betaAddress = "";
    private boolean allowBeta;
    private boolean betaLatest;
    private boolean useBeta;

    public UpdateChecker(int version, int minUsable, int buildJenk) {
        this.version = version;
        this.minUsable = minUsable;
        this.allowBeta = Settings.getSettings().isBetaChannel();
        this.buildJenk = buildJenk;
        if (buildJenk == 9999999)
            this.allowBeta = false;
    }

    @Override
    protected Boolean doInBackground () {
        loadInfo();
        try {
            FileUtils.delete(new File(OSUtils.getCacheStorageLocation(), "updatetemp"));
        } catch (Exception ignored) { }
        return this.shouldUpdate();
    }

    private void loadInfo () {
        try {
            Document doc = AppUtils.downloadXML(new URL(DownloadUtils.getStaticCreeperhostLink("version.xml")));
            if (doc == null) {
                return;
            }
            NamedNodeMap updateAttributes = doc.getDocumentElement().getAttributes();
            int latest_ = latest = Integer.parseInt(updateAttributes.getNamedItem("currentBuild").getTextContent());
            verString += latest_ / (100 * 100) + ".";
            latest_ = latest_ % (100 * 100);
            verString += latest_ / (100) + ".";
            latest_ = latest_ % 100;
            verString += latest_;
            downloadAddress = updateAttributes.getNamedItem("downloadURL").getTextContent();
            if(updateAttributes.getNamedItem("releaseJenkins")!= null) {
                relJenk = Integer.parseInt(updateAttributes.getNamedItem("releaseJenkins").getTextContent());
                int beta_ = beta = Integer.parseInt(updateAttributes.getNamedItem("betaBuild").getTextContent());
                betaJenk = Integer.parseInt(updateAttributes.getNamedItem("betaJenkins").getTextContent());
                betaStr += beta_ / (100 * 100) + ".";
                beta_ = beta_ % (100 * 100);
                betaStr += beta_ / (100) + ".";
                beta_ = beta_ % 100;
                betaStr += beta_;
                betaAddress = Locations.FTBMAVENFULL + "net/ftb/FTB Launcher/" + betaStr + "-" + betaJenk + "/FTB_Launcher-" + betaStr + "-" + betaJenk;
                betaLatest = Boolean.parseBoolean(updateAttributes.getNamedItem("betaLatest").getTextContent());
            } else {
                Logger.logInfo("Beta channel hasn't been activated yet!");
            }
        } catch (Exception e) {
            Logger.logError("Error while loading launcher update info", e);
        }
    }

    public boolean shouldUpdate () {
        if (allowBeta && !betaLatest && (buildJenk < betaJenk || version < beta)) {
            Logger.logInfo("New beta version found. version: " + version + "-" + buildJenk + ", latest: " + beta + "-" + betaJenk);
            useBeta = true;
            return true;
        } else if (version == latest && buildJenk < relJenk) {
            Logger.logInfo("Release version found. version: " + version + "-"+ buildJenk+ ", latest: " + latest);
        } else if (version < latest) {
            Logger.logInfo("New version found. version: " + version + ", latest: " + latest);
            useBeta = false;
            return true;
        } else {
            return false;
        }
        return false;
    }
    public void update () {
        String path = null;
        try {
            path = new File(LaunchFrame.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getCanonicalPath();
            path = URLDecoder.decode(path, "UTF-8");
        } catch (IOException e) {
            Logger.logError("Couldn't get path to current launcher jar/exe", e);
        }
        String temporaryUpdatePath = OSUtils.getCacheStorageLocation() + File.separator + "updatetemp" + File.separator + path.substring(path.lastIndexOf(File.separator) + 1);
        String extension = path.substring(path.lastIndexOf('.') + 1);
        extension = "exe".equalsIgnoreCase(extension) ? extension : "jar";
        try {
            URL updateURL = new URL(useBeta ? DownloadUtils.getCreeperhostLink(downloadAddress + "." + extension) : betaAddress + "." + extension);
            File temporaryUpdate = new File(temporaryUpdatePath);
            temporaryUpdate.getParentFile().mkdir();
            DownloadUtils.downloadToFile(updateURL, temporaryUpdate);//TODO hash check this !!!!
            SelfUpdate.runUpdate(path, temporaryUpdatePath);
        } catch (Exception e) {
            Logger.logError("Error while updating launcher", e);
        }
    }
}
