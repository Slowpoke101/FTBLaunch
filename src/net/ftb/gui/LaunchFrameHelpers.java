/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2013, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
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
package net.ftb.gui;

import net.ftb.data.Settings;
import net.ftb.log.Logger;
import net.ftb.util.OSUtils;
import net.ftb.util.TrackerUtils;

import java.io.*;

public class LaunchFrameHelpers {
    public static void printInfo() {
        Logger.logInfo("FTBLaunch starting up (version " + LaunchFrame.getVersion() + ")");
        Logger.logInfo("Java version: " + System.getProperty("java.version") + " (" + (OSUtils.is64BitOS() ? "64-bit" : "32-bit") + ")");
        Logger.logInfo("Java vendor: " + System.getProperty("java.vendor"));
        Logger.logInfo("Java home: " + System.getProperty("java.home"));
        Logger.logInfo("Java specification: " + System.getProperty("java.vm.specification.name") + " version: " + System.getProperty("java.vm.specification.version") + " by "
                + System.getProperty("java.vm.specification.vendor"));
        Logger.logInfo("Java vm: " + System.getProperty("java.vm.name") + " version: " + System.getProperty("java.vm.version") + " by " + System.getProperty("java.vm.vendor"));
        Logger.logInfo("OS: " + (OSUtils.is64BitOS() ? "64-bit" : "32-bit") + " " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        Logger.logInfo("Launcher Install Dir: " + Settings.getSettings().getInstallPath());
        Logger.logInfo("System memory: " + OSUtils.getOSFreeMemory() + "M free, " + OSUtils.getOSTotalMemory() + "M total");

        if (!OSUtils.is64BitOS()) {
            Logger.logWarn("Warning: 32 Bit operating system. 64 Bit is encouraged for most mod packs. If you have issues, please try the FTB Lite 2 pack.");
        }

        if (OSUtils.is64BitOS() && !OSUtils.is64BitVM()) {//unfortunately the easy to find DL links are for 32 bit java
            Logger.logWarn("Warning: 32 Bit Java in 64 Bit operating system. 64 Bit Java is encouraged for most mod packs. If you have issues, please try the FTB Lite 2 pack.");
        }
    }

    public static void googleAnalytics() {
        File credits = new File(OSUtils.getDynamicStorageLocation(), "credits.txt");
        try {
            if (!credits.exists()) {
                FileOutputStream fos = new FileOutputStream(credits);
                OutputStreamWriter osw = new OutputStreamWriter(fos);

                osw.write("FTB Launcher and Modpack Credits " + System.getProperty("line.separator"));
                osw.write("-------------------------------" + System.getProperty("line.separator"));
                osw.write("Launcher Developers:" + System.getProperty("line.separator"));
                osw.write("jjw123" + System.getProperty("line.separator"));
                osw.write("unv_annihilator" + System.getProperty("line.separator"));
                osw.write("ProgWML6" + System.getProperty("line.separator"));
                osw.write("Major Launcher Dev Contributors" + System.getProperty("line.separator"));
                osw.write("LexManos" + System.getProperty("line.separator"));
                osw.write("Viper-7" + System.getProperty("line.separator") + System.getProperty("line.separator"));
                osw.write("Vbitz" + System.getProperty("line.separator") + System.getProperty("line.separator"));
                osw.write("Web Developers:" + System.getProperty("line.separator"));
                osw.write("Captainnana" + System.getProperty("line.separator"));
                osw.write("Rob" + System.getProperty("line.separator") + System.getProperty("line.separator"));
                osw.write("Modpack Team:" + System.getProperty("line.separator"));
                osw.write("Lathanael" + System.getProperty("line.separator"));
                osw.write("Watchful11" + System.getProperty("line.separator"));
                osw.write("Jadedcat" + System.getProperty("line.separator"));
                osw.write("Eyamaz" + System.getProperty("line.separator"));

                osw.flush();

                TrackerUtils.sendPageView("net/ftb/gui/LaunchFrame.java", "Unique User (Credits)");
            }


        } catch (FileNotFoundException e1) {
            Logger.logError(e1.getMessage());
        } catch (IOException e1) {
            Logger.logError(e1.getMessage());
        }

        if (!Settings.getSettings().getLoaded()) {
            TrackerUtils.sendPageView("net/ftb/gui/LaunchFrame.java", "OS: " + System.getProperty("os.name") + " : " + System.getProperty("os.arch"));
            TrackerUtils.sendPageView("net/ftb/gui/LaunchFrame.java", "Unique User (Settings)");
            Settings.getSettings().setLoaded(true);
        }

        File stamp = new File(OSUtils.getDynamicStorageLocation(), "stamp");
        long unixTime = System.currentTimeMillis() / 1000L;
        long unixts=0;
        try {
            if (!stamp.exists()) {
                FileOutputStream fos = new FileOutputStream(stamp);
                OutputStreamWriter osw = new OutputStreamWriter(fos);

                osw.write(String.valueOf(unixTime));
                osw.flush();
                Logger.logInfo("Reporting daily use");
                TrackerUtils.sendPageView("net/ftb/gui/LaunchFrame.java", "Daily User (Flat)");
            } else {
                FileInputStream fis = new FileInputStream(stamp);
                int content;
                StringBuilder timeBuilder = new StringBuilder();
                while ((content = fis.read()) != -1) {
                    char c = (char) content;
                    timeBuilder.append(String.valueOf(c));
                }
                String time = timeBuilder.toString();
                try {
                    unixts = Long.valueOf(time);
                } catch (NumberFormatException e) {
                    Logger.logWarn("Malformed stamp-file. Will be fixed automatically");
                }
                unixts = unixts + (24 * 60 * 60);
                if (unixts < unixTime) {
                    FileOutputStream fos = new FileOutputStream(stamp);
                    OutputStreamWriter osw = new OutputStreamWriter(fos);

                    osw.write(String.valueOf(unixTime));
                    osw.flush();
                    Logger.logInfo("Reporting daily use");
                    TrackerUtils.sendPageView("net/ftb/gui/LaunchFrame.java", "Daily User (Flat)");

                }
            }
        } catch (FileNotFoundException e1) {
            Logger.logError(e1.getMessage());
        } catch (IOException e1) {
            Logger.logError(e1.getMessage());
        }
    }

}
