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
package net.ftb.workers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.SwingWorker;

import net.ftb.data.Settings;
import net.ftb.log.Logger;
import net.ftb.util.OSUtils;

/**
 * SwingWorker that downloads Minecraft. Returns true if successful, false if it
 * fails.
 */
public class GameUpdateWorker extends SwingWorker<Boolean, Void> {
    protected String status, reqVersion;
    protected File binDir;
    protected URL[] jarURLs;
    protected boolean debugVerbose = Settings.getSettings().getDebugLauncher();
    protected String debugTag = "debug: GameUpdateWorker: ";

    public GameUpdateWorker(String packVersion, String binDir) {
        reqVersion = packVersion;
        this.binDir = new File(binDir);
        this.status = "";
    }

    @Override
    protected Boolean doInBackground () {
        if (debugVerbose) {
            Logger.logInfo(debugTag + "Loading MC assets...");
        }
        setStatus("Downloading jars...");
        if (!loadJarURLs()) {
            return false;
        }
        if (!binDir.exists()) {
            if (debugVerbose) {
                Logger.logWarn(debugTag + "binDir not found, creating: " + binDir.getPath());
            }
            binDir.mkdirs();
        }
        Logger.logInfo("Downloading Jars");
        if (!downloadJars()) {
            Logger.logError("Download Failed");
            return false;
        }
        setStatus("Extracting files...");
        Logger.logInfo("Extracting Files");
        if (!extractNatives()) {
            Logger.logError("Extraction Failed");
            return false;
        }
        return true;
    }

    protected boolean loadJarURLs () {
        Logger.logInfo("Loading Jar URLs");
        String[] jarList = { "lwjgl.jar", "lwjgl_util.jar", "jinput.jar" };
        jarURLs = new URL[jarList.length + 2];
        try {
            jarURLs[0] = new URL("http://assets.minecraft.net/" + reqVersion.replace(".", "_") + "/minecraft.jar");
            for (int i = 0; i < jarList.length; i++) {
                jarURLs[i + 1] = new URL("http://s3.amazonaws.com/MinecraftDownload/" + jarList[i]);
            }
            switch (OSUtils.getCurrentOS()) {
            case WINDOWS:
                jarURLs[jarURLs.length - 1] = new URL("http://s3.amazonaws.com/MinecraftDownload/windows_natives.jar");
                break;
            case MACOSX:
                jarURLs[jarURLs.length - 1] = new URL("http://s3.amazonaws.com/MinecraftDownload/macosx_natives.jar");
                break;
            case UNIX:
                jarURLs[jarURLs.length - 1] = new URL("http://s3.amazonaws.com/MinecraftDownload/linux_natives.jar");
                break;
            default:
                return false;
            }
        } catch (MalformedURLException e) {
            Logger.logError(e.getMessage(), e);
            return false;
        }
        return true;
    }

    //TODO ASAP- thread this!!!!
    protected boolean downloadJars () {
        double totalDownloadSize = 0, totalDownloadedSize = 0;
        int[] fileSizes = new int[jarURLs.length];
        for (int i = 0; i < jarURLs.length; i++) {
            try {
                fileSizes[i] = jarURLs[i].openConnection().getContentLength();
                totalDownloadSize += fileSizes[i];
            } catch (IOException e) {
                Logger.logError(e.getMessage(), e);
                return false;
            }
        }
        for (int i = 0; i < jarURLs.length; i++) {
            int attempt = 0;
            final int attempts = 5;
            int lastfile = -1;
            boolean downloadSuccess = false;
            while (!downloadSuccess && (attempt < attempts)) {
                try {
                    attempt++;
                    if (debugVerbose || lastfile == i) {
                        Logger.logInfo("Connecting.. Try " + attempt + " of " + attempts + " for: " + jarURLs[i].toURI());
                    }
                    lastfile = i;
                    URLConnection dlConnection = jarURLs[i].openConnection();
                    if (dlConnection instanceof HttpURLConnection) {
                        dlConnection.setRequestProperty("Cache-Control", "no-cache");
                        dlConnection.connect();
                    }
                    String jarFileName = getFilename(jarURLs[i]);
                    if (new File(binDir, jarFileName).exists()) {
                        new File(binDir, jarFileName).delete();
                    }
                    InputStream dlStream = dlConnection.getInputStream();
                    FileOutputStream outStream = new FileOutputStream(new File(binDir, jarFileName));
                    setStatus("Downloading " + jarFileName + "...");
                    byte[] buffer = new byte[24000];
                    int readLen;
                    int currentDLSize = 0;
                    while ((readLen = dlStream.read(buffer, 0, buffer.length)) != -1) {
                        outStream.write(buffer, 0, readLen);
                        currentDLSize += readLen;
                        totalDownloadedSize += readLen;
                        int prog = (int) ((totalDownloadedSize / totalDownloadSize) * 100);
                        if (prog > 100) {
                            prog = 100;
                        } else if (prog < 0) {
                            prog = 0;
                        }
                        setProgress(prog);
                    }
                    dlStream.close();
                    outStream.close();
                    if (dlConnection instanceof HttpURLConnection && (currentDLSize == fileSizes[i] || fileSizes[i] <= 0)) {
                        downloadSuccess = true;
                    }
                } catch (Exception e) {
                    downloadSuccess = false;
                    Logger.logWarn("Connection failed, trying again");
                }
            }
            if (!downloadSuccess) {
                return false;
            }
        }
        return true;
    }

    protected boolean extractNatives () {
        setStatus("Extracting natives...");
        File nativesJar = new File(binDir, getFilename(jarURLs[jarURLs.length - 1]));
        File nativesDir = new File(binDir, "natives");
        if (!nativesDir.isDirectory()) {
            nativesDir.mkdirs();
        }
        FileInputStream input = null;
        ZipInputStream zipIn = null;
        try {
            input = new FileInputStream(nativesJar);
            zipIn = new ZipInputStream(input);
            ZipEntry currentEntry = zipIn.getNextEntry();
            while (currentEntry != null) {
                if (currentEntry.getName().contains("META-INF")) {
                    currentEntry = zipIn.getNextEntry();
                    continue;
                }
                setStatus("Extracting " + currentEntry + "...");
                FileOutputStream outStream = new FileOutputStream(new File(nativesDir, currentEntry.getName()));
                int readLen;
                byte[] buffer = new byte[1024];
                while ((readLen = zipIn.read(buffer, 0, buffer.length)) > 0) {
                    outStream.write(buffer, 0, readLen);
                }
                outStream.close();
                currentEntry = zipIn.getNextEntry();
            }
        } catch (IOException e) {
            Logger.logError(e.getMessage(), e);
            return false;
        } finally {
            try {
                zipIn.close();
                input.close();
            } catch (IOException e) {
                Logger.logError(e.getMessage(), e);
            }
        }
        nativesJar.delete();
        return true;
    }

    protected String getFilename (URL url) {
        String string = url.getFile();
        if (string.contains("?")) {
            string = string.substring(0, string.indexOf('?'));
        }
        return string.substring(string.lastIndexOf('/') + 1);
    }

    protected void setStatus (String newStatus) {
        String oldStatus = status;
        status = newStatus;
        firePropertyChange("status", oldStatus, newStatus);
    }

    public String getStatus () {
        return status;
    }
}