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
package net.feed_the_beast.launcher.json.versions;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@ToString
public class Version {
    public String id;
    public Date time;
    public Date releaseTime;
    public String type;
    public String minecraftArguments;
    @Getter @Setter
    private Arguments arguments;
    public List<Library> libraries;
    public String mainClass;
    public int minimumLauncherVersion;
    public String incompatibilityReason;
    public List<OSRule> rules;
    public String assets;
    public String inheritsFrom;
    public String jar;
    private List<Library> _libraries;
    public Asset assetIndex;
    private Map<DownloadType, Downloadable> downloads = Maps.newEnumMap(DownloadType.class);

    public boolean hasDownloads(){
        return !downloads.isEmpty();
    }
    public Downloadable getDownload(DownloadType dlt){
        return downloads.get(dlt);
    }
    public List<Library> getLibraries () {
        if (_libraries == null) {
            _libraries = new ArrayList<Library>();
            if (libraries == null) {
                return _libraries;
            }
            for (Library lib : libraries) {
                if (lib.applies()) {
                    _libraries.add(lib);
                }
            }
        }
        return _libraries;
    }

    public String getAssets () {
        return assets == null ? "legacy" : assets;
    }
}
