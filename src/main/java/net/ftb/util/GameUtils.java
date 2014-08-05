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

import net.ftb.gui.LauncherFrame;
import net.ftb.gui.dialogs.YNDialog;
import net.ftb.log.Logger;

public class GameUtils {
	
    /**
     * Used to destroy the currently running instance of Minecraft
     */
	public static void killMC() {
		//if Mc is running
        if (LauncherFrame.MCRunning) {
            //open confirm dialog for closing MC
        	YNDialog yn = new YNDialog("KILL_MC_MESSAGE", "KILL_MC_CONFIRM", "KILL_MC_TITLE");
            yn.setVisible(true);
            yn.toFront();
            
			if (yn.ready && yn.ret && LauncherFrame.MCRunning && LauncherFrame.getProcMonitor() != null) {
                Logger.logWarn("MC Killed by the user!");
				LauncherFrame.getProcMonitor().stop();
            }
			
            yn.setVisible(false);
        } else {
            Logger.logInfo("No Minecraft Process currently running to kill");
        }
	}

}
