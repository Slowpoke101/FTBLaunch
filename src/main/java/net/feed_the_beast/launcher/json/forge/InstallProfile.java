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
package net.feed_the_beast.launcher.json.forge;

import lombok.Data;
import net.feed_the_beast.launcher.json.versions.Library;

import java.util.List;
import java.util.Map;

@Data
public class InstallProfile {
    //private String _comment_;
    private int spec;
    private String version;
    private String json;
    private String path;
    private String logo;
    private String minecraft;
    private String welcome;
    private Map<String, InstallerData> data;
    private List<InstallerProcessor> processors;
    private List<Library> libraries;
    private Library installerjar;
}
