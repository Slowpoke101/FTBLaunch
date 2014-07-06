/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2014, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
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
package net.ftb.util;

import java.io.File;

import net.ftb.data.Settings;
import net.ftb.log.Logger;
import net.ftb.util.OSUtils;
import net.ftb.util.OSUtils.OS;

public class CheckInstallPath {
    public String installPath;
    // What kind of action should caller do
    public Action action;
    // Error/Warn message
    public String message;
    // Localized message to show in dialog
    public String localizedMessage;
    // name of the Setting which controls if check is skipped
    // all settings starts with "CIP_" => we can later add
    // button to clear those settings
    public String setting;

    public static enum Action {
        OK, WARN, BLOCK,
    }

    public CheckInstallPath(String path) {
        this(path, false);
    }

    public CheckInstallPath(String path, boolean calledFromLaunchFrame) {
        installPath = path;
        File f = new File(path);

        // TODO: add more tests! (Unicode test)\
        if (OSUtils.getCurrentOS()==OS.WINDOWS && System.getenv("ProgramFiles")!=null && path.contains(System.getenv("ProgramFiles"))) {
            setting = "CIP_programfiles";
            if (!Settings.getSettings().getBoolean(setting)) {
                action = Action.BLOCK;
                message = "Installing under C:\\Program Files\\ or similar is not supported.";
                localizedMessage = "CIP_PROGRAMFILES";
                Logger.logError(message);
            } else {
                action = Action.OK;
                Logger.logDebug("ignored: " + setting);
            }
        }
        else if (OSUtils.getCurrentOS()==OS.WINDOWS && System.getenv("USERPROFILE")!=null && path.contains(System.getenv("USERPROFILE"))) {
            setting = "CIP_userprofile";
            if (!Settings.getSettings().getBoolean(setting)) {
                action = Action.WARN;
                message = ("Installing under C:\\Users\\<username> is not recommended and can cause problems.");
                localizedMessage = "CIP_USERPROFILE";
                Logger.logWarn(message);
            } else {
                action = Action.OK;
                Logger.logDebug("ignored: " + setting);
            }
        }
        else if (f.isDirectory() && !f.canWrite()) {
            setting = "CIP_writeprotect";
            if (!Settings.getSettings().getBoolean(setting)) {
                action = Action.BLOCK;
                message = "No write access to FTB installation directory.";
                localizedMessage = "CIP_WRITEPROTECT";
                Logger.logError(message);
            } else {
                action = Action.OK;
                Logger.logDebug("ignored: " + setting);
            }
        }
        // special case. This must be ignored at InstallDirectoryDialog
        else if (calledFromLaunchFrame && !f.exists()) {
            setting = "CIP_exists";
            if (!Settings.getSettings().getBoolean(setting)) {
                action = Action.BLOCK;
                message = "FTB installation directory not found!";
                localizedMessage = "CIP_EXISTS";
                Logger.logWarn(message);
            } else {
                action = Action.OK;
                Logger.logDebug("ignored: " + setting);
            }
        }
        else if ( !calledFromLaunchFrame && !f.exists()) {
            f.mkdirs();
            if (!f.exists() || !f.canWrite()) {
                setting = "CIP_create";
                if (!Settings.getSettings().getBoolean(setting)) {
                    action = Action.BLOCK;
                    message = "Could not create FTB installation location";
                    localizedMessage = "CIP_CREATE";
                    Logger.logWarn(message);
                } else {
                    action = Action.OK;
                    Logger.logDebug("ignored: " + setting);
                }
            }
        } else {
            action = Action.OK;
        }
    }
}