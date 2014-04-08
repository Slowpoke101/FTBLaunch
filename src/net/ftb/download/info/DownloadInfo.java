package net.ftb.download.info;

import java.io.File;
import java.net.URL;

import lombok.Getter;
import lombok.Setter;

public class DownloadInfo {
    public URL url;
    public File local;
    public String name;
    public long size = 0;
    public String hash;
    public String hashType;
    @Getter
    @Setter
    private DLType primaryDLType = DLType.ETag;
    @Getter
    @Setter
    private DLType backupDLType = DLType.NONE;

    public DownloadInfo() {
    }

    public DownloadInfo(URL url, File local, String name, Boolean ftbServers) {
        this(url, local, name, null, "md5");
        if (ftbServers) {
            primaryDLType = DLType.ContentMD5;
            backupDLType = DLType.FTBBackup;
        }
    }

    public DownloadInfo(URL url, File local, String name) {
        this(url, local, name, null, "md5");
    }

    public DownloadInfo(URL url, File local, String name, String hash, String hashType, DLType primary, DLType backup) {
        this(url, local, name, hash, hashType);
        if (primary != null)
            this.primaryDLType = primary;
        if (backup != null)
            this.backupDLType = backup;
    }

    public DownloadInfo(URL url, File local, String name, String hash, String hashType) {
        this.url = url;
        this.local = local;
        this.name = name;
        this.hash = hash;
        this.hashType = hashType;
    }

    public enum DLType {
        ETag, ContentMD5, FTBBackup, NONE
    }
}