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
package net.feed_the_beast.launcher.json.versions;

import java.util.regex.Pattern;

public class OSRule {
    public Action action = Action.ALLOW;
    public OSInfo os;

    public class OSInfo {
        private OS name;
        private String version;
    }

    public boolean applies () {
        if (os == null) {
            return true;
        }
        if (os.name != null && os.name != OS.getCurrentPlatform()) {
            return false;
        }
        if (os.version != null) {
            try {
                if (!Pattern.compile(os.version).matcher(OS.VERSION).matches()) {
                    return false;
                }
            } catch (Throwable e) {
            }
        }
        return true;
    }
}
