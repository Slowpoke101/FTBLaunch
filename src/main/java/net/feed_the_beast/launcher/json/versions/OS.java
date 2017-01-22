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

import java.util.Locale;

public enum OS {
    LINUX("linux", "linux", "unix"), WINDOWS("windows", "win"), OSX("osx", "mac"), UNKNOWN("unknown");

    private String name;
    private String[] aliases;

    public static final OS CURRENT = getCurrentPlatform();
    public static final String VERSION = System.getProperty("os.version");

    private OS (String name, String... aliases) {
        this.name = name;
        this.aliases = aliases;
    }

    public static OS getCurrentPlatform () {
        String osName = System.getProperty("os.name").toLowerCase(Locale.US);
        for (OS os : values()) {
            if (osName.contains(os.name)) {
                return os;
            }
            for (String alias : os.aliases) {
                if (osName.contains(alias)) {
                    return os;
                }
            }
        }
        return UNKNOWN;
    }
}
