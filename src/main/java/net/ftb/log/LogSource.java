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
package net.ftb.log;

public enum LogSource {
    ALL, LAUNCHER, EXTERNAL("Minecraft");
    private String humanReadableName;

    private LogSource () {
        this(null);
    }

    private LogSource (String humanReadableName) {
        this.humanReadableName = humanReadableName;
    }

    public String toString () {
        return (humanReadableName == null) ? name().substring(0, 1) + name().substring(1).toLowerCase() : humanReadableName;
    }
}