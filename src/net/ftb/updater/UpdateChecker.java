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
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import net.ftb.gui.dialogs.LauncherUpdateDialog;
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
    private int latest;
    private int  minUsable;
    public static String verString = "";
    private String downloadAddress = "";

    public UpdateChecker(int version, int minUsable) {
        this.version = version;
        this.minUsable = minUsable;
    }

    @Override
    protected Boolean doInBackground() {
        //try{Thread.sleep(10000);}catch (Exception e){}
        loadInfo();
        try {
            FileUtils.delete(new File(OSUtils.getCacheStorageLocation(), "updatetemp"));
        } catch (Exception ignored) {
            Logger.logError(ignored.getMessage(), ignored);
        }
        return this.shouldUpdate();
    }

    @Override
    protected void done() {
        try {
            if (get()) {
                LauncherUpdateDialog p = new LauncherUpdateDialog(this, minUsable);
                p.setVisible(true);
            }
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
        }
    }

    private void loadInfo () {
        try {
            Document doc = AppUtils.downloadXML(new URL(DownloadUtils.getStaticCreeperhostLink("version.xml")));
            if (doc == null) {
                return;
            }
            NamedNodeMap updateAttributes = doc.getDocumentElement().getAttributes();
            latest = Integer.parseInt(updateAttributes.getNamedItem("currentBuild").getTextContent());
            char[] temp = String.valueOf(latest).toCharArray();
            for (int i = 0; i < (temp.length - 1); i++) {
                verString += temp[i] + ".";
            }
            verString += temp[temp.length - 1];
            downloadAddress = updateAttributes.getNamedItem("downloadURL").getTextContent();
        } catch (Exception e) {
            Logger.logError(e.getMessage(), e);
        }
    }

    public boolean shouldUpdate () {
        if (version < latest) {
            Logger.logInfo("New version found. version: " + version + ", latest: " + latest );
            return true;
        } else {
            return false;
        }
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
            URL updateURL = new URL(DownloadUtils.getCreeperhostLink(downloadAddress + "." + extension));
            File temporaryUpdate = new File(temporaryUpdatePath);
            temporaryUpdate.getParentFile().mkdir();
            DownloadUtils.downloadToFile(updateURL, temporaryUpdate);
            SelfUpdate.runUpdate(path, temporaryUpdatePath);
        } catch (Exception e) {
            Logger.logError(e.getMessage(), e);
        }
    }
}
