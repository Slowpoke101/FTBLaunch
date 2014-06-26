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

import io.github.asyncronous.toast.Toaster;

import javax.swing.JOptionPane;

import net.ftb.gui.LaunchFrame;
import net.ftb.log.Logger;

public class ErrorUtils {
    /**
     * Writes error into log and shows error in message dialog
     * <p>
     * Same error message will be used for log and for message dialog.
     * If using translated messages consider using {@link #tossError(String output, String log)}
     * @param output String to log and show in message dialog
     */
    public static void tossError(String output) {
        Logger.logError(output);
        Toaster.instance().popError(output);
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
    public static void tossError(String log, String output) {
        Logger.logError(log);
        Toaster.instance().popError(output);
    }

    /**
     * Writes error and exception stacktrace into log and shows error in message dialog
     * @param output Strong to log and show in message dialog
     * @param t Exception to to log
     */
    public static void tossError(String output, Throwable t) {
        Logger.logError(output, t);
        Toaster.instance().popError(output);
    }

    public static void tossError(String log, String output , Throwable t) {
        Logger.logError(log, t);
        Toaster.instance().popError(output);
    }

    /**
     * Opens dialogwith OK and ignore buttons.
     * Callee should check return value
     * Does not write error or warning messages into log
     *
     * @param message String to show in dialog
     * @param severity JOptionPane message type
     * @return an integer indicating the option chosen by the user, or CLOSED_OPTION if the user closed the dialog
     *
     */
    public static int tossOKIgnoreDialog(String message, int severity) {
        Object[] options = { "OK", "Ignore" };
        int result = JOptionPane.showOptionDialog(LaunchFrame.getInstance(), 
                message + "\n" + "Click OK to continue or IGNORE to skip this warning in future", null,
                JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
        return result;
    }
}
