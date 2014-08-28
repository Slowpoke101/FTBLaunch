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
package net.ftb.main;

import net.ftb.data.Constants;
import net.ftb.data.Settings;
import net.ftb.log.Logger;
import net.ftb.util.ErrorUtils;
import net.ftb.util.OSUtils;
import net.ftb.util.TrackerUtils;
import net.ftb.util.winreg.JavaInfo;

import java.io.*;

import javax.swing.JOptionPane;

public class MainHelpers {
    public static void printInfo() {
        // + version + " starting up based on launcher (version " + "1.3.7"
        Logger.logInfo("FTBLaunch PAXPRIME2014 "+ Constants.version + " starting based on launcher (version 1.4.4 Build: " + Constants.buildNumber + ")");
        Logger.logInfo("Java version: " + System.getProperty("java.version"));
        Logger.logInfo("Java vendor: " + System.getProperty("java.vendor"));
        Logger.logInfo("Java home: " + System.getProperty("java.home"));
        Logger.logInfo("Java specification: " + System.getProperty("java.vm.specification.name") + " version: " + System.getProperty("java.vm.specification.version") + " by "
                + System.getProperty("java.vm.specification.vendor"));
        Logger.logInfo("Java vm: " + System.getProperty("java.vm.name") + " version: " + System.getProperty("java.vm.version") + " by " + System.getProperty("java.vm.vendor"));
        Logger.logInfo("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " (" + (OSUtils.is64BitOS() ? "64-bit" : "32-bit") + ")");
        Logger.logInfo("Launcher Install Dir: " + Settings.getSettings().getInstallPath());
        Logger.logInfo("System memory: " + OSUtils.getOSFreeMemory() + "M free, " + OSUtils.getOSTotalMemory() + "M total");

        //hack: I want to trigger JavaFinder here:
        String selectedJavaPath = Settings.getSettings().getJavaPath();
        //then test if preferred and selected java paths differs
        if (!selectedJavaPath.equals(Settings.getSettings().getDefaultJavaPath())) {
            Logger.logInfo("Using Java path entered by user: " + selectedJavaPath);
        }

        if (!OSUtils.is64BitOS()) {
            Logger.logError("32-bit operating system. 64-bit is required for most mod packs. If you have issues, please try the FTB Lite 2 pack.");
        }

        if (OSUtils.is64BitOS() && !Settings.getSettings().getCurrentJava().is64bits) {//unfortunately the easy to find DL links are for 32 bit java
            Logger.logError("32-bit Java in 64-bit operating system. 64-bit Java is required for most mod packs. If you have issues, please try the FTB Lite 2 pack.");
        }

        JavaInfo java = Settings.getSettings().getCurrentJava();
        if(java.getMajor() < 1 || (java.getMajor() == 1 && java.getMinor() < 7)){
            Logger.logError("Java 6 detected. Java 7 is recommended for most mod packs.");
        }

    }


    public static void tossNag(String setting, String message) {
        if (!Settings.getSettings().getBoolean(setting)) {
            int result = ErrorUtils.tossOKIgnoreDialog(message, JOptionPane.WARNING_MESSAGE);
            if (result != 0 && result != JOptionPane.CLOSED_OPTION) {
                Settings.getSettings().setBoolean(setting, true);
                Settings.getSettings().save();
            }
        } else {
            Logger.logDebug("ignored: " + setting);
        }

    }

}
