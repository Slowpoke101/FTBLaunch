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
package net.ftb.util;

import javax.swing.JOptionPane;

import net.ftb.gui.LaunchFrame;
import net.ftb.log.Logger;

public class ErrorUtils {
    /**
     * Writes error into log and shows error in message dialog
     * <p>
     * Same error message will be used for log and for message dialog.
     * If using translated messages consider using {@link #tossError(Sting output, String log)}
     * @param output String to log and show in message dialog
     */
    public static void tossError(String output) {
        Logger.logError(output);
        JOptionPane.showMessageDialog(LaunchFrame.getInstance(), output, "ERROR!", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Writes error into log and shows error in message dialog
     * <p>
     * Parameter for logging system and for message dialog are given separately and
     * this method is optimal to show localized message in message dialog but
     * to log error message in english.
     * @param output String to show in message dialog
     * @param log String to log
     */
    public static void tossError(String output, String log) {
        Logger.logError(log);
        JOptionPane.showMessageDialog(LaunchFrame.getInstance(), output, "ERROR!", JOptionPane.ERROR_MESSAGE);
    }
}
