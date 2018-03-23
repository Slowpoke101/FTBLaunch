/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2018, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
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
package net.ftb.download.info;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.feed_the_beast.launcher.json.versions.Artifact;
import net.feed_the_beast.launcher.json.versions.Downloadable;

import java.io.File;
import java.net.URL;
import java.util.List;

public class DownloadInfo {
    public URL url;
    public File local;
    public String name;
    public long size = 0;
    public List<String> hash;
    public String hashType;
    @Getter
    @Setter
    private DLType primaryDLType = DLType.ETag;
    @Getter
    @Setter
    private DLType backupDLType = DLType.NONE;

    public DownloadInfo () {
    }

    public DownloadInfo (URL url, File local, String name, Boolean ftbServers) {
        this(url, local, name, null, "md5");
        if (ftbServers) {
            primaryDLType = DLType.ContentMD5;
            backupDLType = DLType.FTBBackup;
        }
    }

    public DownloadInfo (URL url, File local, String name) {
        this(url, local, name, null, "md5");
    }

    public DownloadInfo(Downloadable down, File local) {
        this (down.getUrl(), local, local.getName(), Lists.newArrayList(down.getSha1()), "sha1");
        this.size = down.getSize();
    }


    public DownloadInfo(Artifact down, File local) {
        this (down.getUrl(), local, down.getPath(), Lists.newArrayList(down.getSha1()), "sha1");
        this.size = down.getSize();
    }

    public DownloadInfo (URL url, File local, String name, List<String> hash, String hashType, DLType primary, DLType backup) {
        this(url, local, name, hash, hashType);
        if (primary != null) {
            this.primaryDLType = primary;
        }
        if (backup != null) {
            this.backupDLType = backup;
        }
    }

    public DownloadInfo (URL url, File local, String name, List<String> hash, String hashType) {
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