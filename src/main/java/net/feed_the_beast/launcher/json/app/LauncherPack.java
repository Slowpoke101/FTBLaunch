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
package net.feed_the_beast.launcher.json.app;

import lombok.Data;

@Data
public class LauncherPack {
    private String id;//dir
    private String version;
    private String mcversion;
    private String xml; // XML file this came from
    private String curseProjectId; //easy to provide
    private String location;//location on disk

    public LauncherPack () {
    }

    public LauncherPack (String id, String version, String mcversion, String xml, String curseProjectId, String location) {
        this.id = id;
        this.version = version;
        this.mcversion = mcversion;
        this.xml = xml;
        this.curseProjectId = curseProjectId;
        this.location = location;
    }
}
