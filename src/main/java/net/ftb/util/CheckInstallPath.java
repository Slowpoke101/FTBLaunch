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
package net.ftb.util;

import net.ftb.locale.I18N;
import net.ftb.log.Logger;
import net.ftb.util.OSUtils.OS;

import java.io.File;

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

    public CheckInstallPath (String path) {
        this(path, false);
    }

    public CheckInstallPath (String path, boolean calledFromLaunchFrame) {
        installPath = path;
        File f = new File(path);
        String defaultLocation = "C:\\FTB";

        /**
         *  Messages are shown by FirstRunDialog and by LaunchFrame
         *  E.g.
         *  FirstRunDialog opens tossError with         message + "\nPlease select again"
         *  LaunchFrame opens tossOKIgnoreDialog with   message
         *
         */
        String pathRegex = "[\\w:\\\\/ \\-\\.]+";

        if (OSUtils.getCurrentOS() == OS.WINDOWS && System.getenv("ProgramFiles") != null && path.contains(System.getenv("ProgramFiles"))) {
            message = "Installing under C:\\Program Files\\ or similar is not supported. Please select a new location such as " + defaultLocation;
            localizedMessage = I18N.getLocaleString("CIP_PROGRAMFILES") + defaultLocation;
            action = Action.BLOCK;
            Logger.logError(message);
        } else if (!path.matches(pathRegex)) {
            String s = path.replaceAll(pathRegex, "");
            message = "Unsupported installation directory. Forge does not support following character(s): " + s + " Please select a new location such as " + defaultLocation;
            localizedMessage = I18N.getLocaleString("CIP_BADPATH").replace("LIST", s) + defaultLocation;
            action = Action.BLOCK;
            Logger.logError(message);
        } else if (OSUtils.getCurrentOS() == OS.WINDOWS && path.contains("Content.IE5")) {
            setting = "CIP_internetfiles";
            message = "You cannot install FTB to your Temporary Internet Files directory. Please select a new location such as " + defaultLocation;
            localizedMessage = I18N.getLocaleString("CIP_INTERNETFILES") + defaultLocation;
            action = Action.BLOCK;
            Logger.logError(message);
        } /*
        else if (OSUtils.getCurrentOS()==OS.WINDOWS && System.getenv("USERPROFILE")!=null && path.contains(System.getenv("USERPROFILE"))) {
            setting = "CIP_userprofile";
            message = ("Installing under C:\\Users\\<username> is not recommended and can cause problems. We suggest you select a new location such as " + defaultLocation);
            localizedMessage = I18N.getLocaleString("CIP_USERPROFILE") + defaultLocation;
            if (!Settings.getSettings().getBoolean(setting)) {
                action = Action.WARN;
                Logger.logWarn(message);
            } else {
                action = Action.OK;
                Logger.logDebug("ignored: " + setting);
            }
        }*/ else if (f.isDirectory() && !f.canWrite()) {
            message = "Could not write to the FTB installation directory. Please select a folder which you have permission to write to.";
            localizedMessage = I18N.getLocaleString("CIP_WRITEPROTECT");
            action = Action.BLOCK;
            Logger.logError(message);
        }
        // special case. This must be ignored at InstallDirectoryDialog
        else if (calledFromLaunchFrame && !f.exists()) {
            message = "FTB installation directory not found!";
            localizedMessage = I18N.getLocaleString("CIP_EXISTS");
            action = Action.BLOCK;
            Logger.logError(message);
        } else if (!calledFromLaunchFrame && !f.exists()) {
            f.mkdirs();
            if (!f.exists() || !f.canWrite()) {
                message = "Could not create FTB installation location";
                localizedMessage = I18N.getLocaleString("CIP_CREATE");
                action = Action.BLOCK;
                Logger.logError(message);
            }
        } else {
            action = Action.OK;
        }
    }
}