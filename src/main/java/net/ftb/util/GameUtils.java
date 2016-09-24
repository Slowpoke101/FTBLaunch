/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2016, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
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

import net.ftb.gui.LaunchFrame;
import net.ftb.gui.dialogs.YNDialog;
import net.ftb.locale.I18N;
import net.ftb.log.Logger;

import javax.swing.*;

public class GameUtils {

    private GameUtils() {
    }

    public static void killMC () {
        //if Mc is running
        if (LaunchFrame.MCRunning) {
            //open confirm dialog for closing MC
            YNDialog yn = new YNDialog("KILL_MC_MESSAGE", "KILL_MC_CONFIRM", "KILL_MC_TITLE");
            yn.setVisible(true);
            yn.toFront();

            if (yn.ready && yn.ret && LaunchFrame.MCRunning && LaunchFrame.getProcMonitor() != null) {
                Logger.logWarn("MC Killed by the user!");
                LaunchFrame.getProcMonitor().stop();
            }

            yn.setVisible(false);
        } else {
            Logger.logInfo("No Minecraft Process currently running to kill");
        }
    }

    public static void threadDumpMC () {
        boolean ret = true;
        //if Mc is running
        if (LaunchFrame.MCRunning) {
            //open confirm dialog for closing MC
            YNDialog yn = new YNDialog("TD_MC_MESSAGE", "TD_MC_CONFIRM", "TD_MC_TITLE");
            yn.setVisible(true);
            yn.toFront();

            if (yn.ready && yn.ret && LaunchFrame.MCRunning && LaunchFrame.getProcMonitor() != null) {
                Logger.logWarn("Getting thread dump from MC");
                ret = OSUtils.genThreadDump(LaunchFrame.getProcMonitor().getPid());
            }

            yn.setVisible(false);

            if (ret == false) {
                ErrorUtils.showClickableMessage(I18N.getLocaleString("TD_MC_FAIL_MESSAGE"), JOptionPane.ERROR_MESSAGE);
            }
        } else {
            Logger.logInfo("No Minecraft Process currently running to thread dump");
        }
    }
}
