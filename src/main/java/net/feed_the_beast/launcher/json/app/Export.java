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

import com.google.common.collect.Lists;
import lombok.Data;
import net.ftb.data.ModPack;
import net.ftb.log.Logger;

import java.io.File;
import java.util.List;

@Data
public class Export {
    private String basePath;
    private String libraryPath;
    private String launcherVersion;
    private List<LauncherPack> pack;

    public Export () {

    }

    public Export (List<ModPack> packs, String launcherVersion, String libraryPath, String basepath) {
        this.launcherVersion = launcherVersion;
        this.libraryPath = libraryPath;
        this.basePath = basepath;
        pack = Lists.newArrayList();
        for (ModPack p : packs) {
            File verFile = new File(basepath, p.getDir() + File.separator + "version");
            if (verFile.exists()) {
                String storedVersion = p.getStoredVersion(verFile);
                Logger.logDebug("Exporting data for installed Pack " + p.getName() + " version " + storedVersion);
                pack.add(new LauncherPack(p.getDir(), storedVersion, p.getMcVersion(storedVersion), p.getParentXml(), p.getCurseId(), new File(basepath, p.getDir()).getAbsolutePath()));
            }
        }

    }
}
