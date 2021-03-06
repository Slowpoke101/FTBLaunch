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

import lombok.Data;
import net.ftb.data.Settings;

@Data
public class GameRule {
    public Action action = Action.ALLOW;
    public Features features;

    public class Features {
        private boolean is_demo_user = false;
        private boolean has_custom_resolution = false;
    }

    public boolean applies () {
        if (features == null) {
            return false;
        }
        if (features != null && features.has_custom_resolution && Settings.getSettings().getLastDimension() != null) {
            return true;
        }

        return false;
    }

}
