package net.ftb.download.workers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.List;

import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import net.ftb.download.info.DownloadInfo;
import net.ftb.download.info.DownloadInfo.DLType;
import net.ftb.log.Logger;
import net.ftb.util.DownloadUtils;

public class AssetDownloader extends SwingWorker<Boolean, Void> {
    private List<DownloadInfo> downloads;
    private final ProgressMonitor monitor;
    private String status;
    private int progressIndex = 0;

    public AssetDownloader(final ProgressMonitor monitor, List<DownloadInfo> downloads) {
        this.downloads = downloads;
        this.monitor = monitor;

        monitor.setMaximum(downloads.size() * 100);

        addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange (PropertyChangeEvent evt) {
                if (monitor.isCanceled())
                    AssetDownloader.this.cancel(false);
            }
        });
    }

    @Override
    protected Boolean doInBackground () throws Exception {
        boolean allDownloaded = true;

        byte[] buffer = new byte[24000];
        for (int x = 0; x < downloads.size(); x++) {
            DownloadInfo asset = downloads.get(x);
            String remoteHash = null;
            int attempt = 0;
            final int attempts = 5;
            boolean downloadSuccess = false;
            while (!downloadSuccess && (attempt < attempts)) {
                try {
                    if (attempt++ > 0) {
                        Logger.logInfo("Connecting.. Try " + attempt + " of " + attempts + " for: " + asset.url);
                    }
                    URLConnection con = asset.url.openConnection();
                    if (con instanceof HttpURLConnection) {
                        con.setRequestProperty("Cache-Control", "no-cache");
                        con.connect();
                    }
                    this.status = "Downloading " + asset.name + "...";
                    asset.local.getParentFile().mkdirs();
                    InputStream input = con.getInputStream();
                    FileOutputStream output = new FileOutputStream(asset.local);
                    int readLen;
                    int currentSize = 0;
                    int size = Integer.parseInt(con.getHeaderField("Content-Length"));
                    if (asset.getPrimaryDLType() == DLType.ETag)
                        remoteHash = con.getHeaderField("ETag");
                    if (asset.getPrimaryDLType() == DLType.ContentMD5)
                        remoteHash = con.getHeaderField("Content-MD5");

                    setProgress(0);
                    while ((readLen = input.read(buffer, 0, buffer.length)) != -1) {
                        output.write(buffer, 0, readLen);
                        currentSize += readLen;
                        int prog = (int) ((currentSize / size) * 100);
                        if (prog > 100)
                            prog = 100;
                        if (prog < 0)
                            prog = 0;

                        setProgress(prog);

                        prog = (progressIndex * 100) + prog;

                        monitor.setProgress(prog);
                        monitor.setNote(this.status);
                    }
                    input.close();
                    output.close();
                    String hash = DownloadUtils.fileHash(asset.local, asset.hashType).toLowerCase();
                    String assetHash = asset.hash;
                    if (asset.hash == null) {
                        if (remoteHash != null)
                            assetHash = remoteHash;
                        else if (asset.getBackupDLType() == DLType.FTBBackup && DownloadUtils.backupIsValid(asset.local, asset.url.getPath().replace("/FTB2", ""))) {
                            remoteHash = asset.hash;
                        }

                    }
                    if (con instanceof HttpURLConnection && (currentSize == asset.size || asset.size <= 0)) {
                        if ((assetHash != null && !assetHash.toLowerCase().equals(hash))) {
                            asset.local.delete();
                        } else {
                            downloadSuccess = true;
                        }
                    }
                    progressIndex += 1;
                } catch (Exception e) {
                    e.printStackTrace();
                    downloadSuccess = false;
                    Logger.logWarn("Connection failed, trying again");
                }
            }
            if (!downloadSuccess) {
                allDownloaded = false;
            }
        }
        status = allDownloaded ? "Success" : "Downloads failed";
        return allDownloaded;
    }
}