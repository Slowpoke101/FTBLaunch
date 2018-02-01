/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2017, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
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
package net.ftb.download.workers;

import static com.google.common.net.HttpHeaders.CACHE_CONTROL;
import static com.google.common.net.HttpHeaders.CONTENT_LENGTH;
import static com.google.common.net.HttpHeaders.CONTENT_MD5;
import static com.google.common.net.HttpHeaders.ETAG;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.ftb.download.Locations;
import net.ftb.download.info.DownloadInfo;
import net.ftb.download.info.DownloadInfo.DLType;
import net.ftb.log.Logger;
import net.ftb.util.DownloadUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import javax.swing.*;

public class AssetDownloader extends SwingWorker<Boolean, Void> {
    private List<DownloadInfo> downloads;
    private final ProgressMonitor monitor;
    private int progressIndex = 0;
    private boolean allDownloaded = true;

    @Getter
    private String status;
    @Getter
    private int ready = 0;

    public AssetDownloader (final ProgressMonitor monitor, List<DownloadInfo> downloads) {
        this.downloads = downloads;
        this.monitor = monitor;
    }

    @Override
    protected Boolean doInBackground () throws Exception {
        for (DownloadInfo download : downloads) {
            if (isCancelled()) {
                return false;
            }
            doDownload(download);
        }
        setStatus(allDownloaded ? "Success" : "Downloads failed");
        return allDownloaded;
    }

    public synchronized void setReady (int newReady) {
        int oldReady = ready;
        ready = newReady;
        firePropertyChange("ready", oldReady, ready);
    }

    public synchronized void setStatus (String newStatus) {
        String oldStatus = status;
        status = newStatus;
        firePropertyChange("note", oldStatus, status);
    }

    private void doDownload (DownloadInfo assetOrig) {
        DownloadInfo asset = assetOrig;
        if(asset.url.getProtocol().equals("http") && asset.url.getHost().startsWith(Locations.oldMasterRepoNoHTTP)) {
            try {//move all links to HTTPS on our repo
                asset.url = new URL(assetOrig.url.toString().replace(Locations.oldMasterRepo, Locations.masterRepo));
            } catch (MalformedURLException e) {
                Logger.logError("error creating url", e);
            }
        }
        byte[] buffer = new byte[24000];
        boolean downloadSuccess = false;
        List<String> remoteHash = asset.hash;
        String hashType;
        int attempt = 0;
        final int attempts = 5;

        while (!downloadSuccess && (attempt < attempts)) {
            try {
                if (remoteHash == null) {
                    remoteHash = Lists.newArrayList();
                }
                hashType = asset.hashType;
                if (attempt++ > 0) {
                    Logger.logInfo("Connecting.. Try " + attempt + " of " + attempts + " for: " + asset.url);
                }

                // Will this break something?
                //HTTPURLConnection con = (HttpURLConnection) asset.url.openConnection();
                URLConnection con = asset.url.openConnection();
                if (con instanceof HttpURLConnection) {
                    con.setRequestProperty(CACHE_CONTROL, "no-cache, no-transform");
                    ((HttpURLConnection) con).setRequestMethod("HEAD");
                    con.connect();
                }

                // gather data for basic checks
                long remoteSize = Long.parseLong(con.getHeaderField(CONTENT_LENGTH));
                if (asset.hash == null && asset.getPrimaryDLType() == DLType.ETag) {
                    remoteHash.clear();
                    remoteHash.add(con.getHeaderField(ETAG).replace("\"", ""));
                    hashType = "md5";
                }
                if (asset.hash == null && asset.getPrimaryDLType() == DLType.ContentMD5) {
                    remoteHash.clear();
                    remoteHash.add(con.getHeaderField(CONTENT_MD5).replace("\"", ""));
                    hashType = "md5";
                }

                Logger.logDebug(asset.name);
                Logger.logDebug("RemoteSize: " + remoteSize);
                Logger.logDebug("asset.hash: " + asset.hash);
                Logger.logDebug("remoteHash: " + remoteHash);

                // existing file are only added when we want to check file integrity with force update
                if (asset.local.exists()) {
                    long localSize = asset.local.length();
                    if (!(con instanceof HttpURLConnection && localSize == remoteSize)) {
                        asset.local.delete();
                        Logger.logWarn("Local asset size differs from remote size: " + asset.name + " remote: " + remoteSize + " local: " + localSize);
                    }
                }

                if (asset.local.exists()) {
                    doHashCheck(asset, remoteHash);
                }

                if (asset.local.exists()) {
                    downloadSuccess = true;
                    progressIndex += 1;
                    continue;
                }

                //download if needed
                setProgress(0);
                setStatus("Downloading " + asset.name + "...");
                con = asset.url.openConnection();
                if (con instanceof HttpURLConnection) {
                    con.setRequestProperty(CACHE_CONTROL, "no-cache, no-transform");
                    ((HttpURLConnection) con).setRequestMethod("GET");
                    con.connect();
                }
                asset.local.getParentFile().mkdirs();
                int readLen;
                int currentSize = 0;
                InputStream input = con.getInputStream();
                FileOutputStream output = new FileOutputStream(asset.local);
                while ((readLen = input.read(buffer, 0, buffer.length)) != -1) {
                    output.write(buffer, 0, readLen);
                    currentSize += readLen;
                    int prog = (int) ((currentSize / remoteSize) * 100);
                    if (prog > 100) {
                        prog = 100;
                    }
                    if (prog < 0) {
                        prog = 0;
                    }

                    setProgress(prog);

                    prog = (progressIndex * 100) + prog;

                    setReady(prog);
                    //monitor.setProgress(prog);
                    //monitor.setNote(status);
                }
                input.close();
                output.close();

                //file downloaded check size
                if (!(con instanceof HttpURLConnection && currentSize > 0 && currentSize == remoteSize)) {
                    asset.local.delete();
                    Logger.logWarn("Local asset size differs from remote size: " + asset.name + " remote: " + remoteSize + " local: " + currentSize);
                }

                if (downloadSuccess = doHashCheck(asset, remoteHash)) {
                    progressIndex += 1;
                }
            } catch (Exception e) {
                downloadSuccess = false;
                Logger.logWarn("Connection failed, trying again", e);
            }
        }
        if (!downloadSuccess) {
            allDownloaded = false;
        }
    }

    public boolean doHashCheck (DownloadInfo asset, final List<String> remoteHash) throws IOException {
        String hash = DownloadUtils.fileHash(asset.local, asset.hashType).toLowerCase();
        List<String> assetHash = asset.hash;
        boolean good = false;
        if (asset.hash == null) {
            if (remoteHash != null) {
                assetHash = remoteHash;
            } else if (asset.getBackupDLType() == DLType.FTBBackup && DownloadUtils.backupIsValid(asset.local, asset.url.getPath().replace("/FTB2", ""))) {
                good = true;
            }
        }
        if (good || assetHash != null && assetHash.contains(hash)) {
            return true;
        }
        Logger.logWarn("Asset hash checking failed: " + asset.name + " " + asset.hashType + " " + hash);//unhashed DL's are not allowed!!!
        asset.local.delete();
        return false;
    }
}
