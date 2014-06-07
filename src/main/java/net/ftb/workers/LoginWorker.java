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
package net.ftb.workers;

import javax.swing.SwingWorker;

import lombok.Getter;
import net.ftb.data.LoginResponse;
import net.ftb.gui.LaunchFrame;
import net.ftb.log.Logger;
import net.ftb.util.Benchmark;
import net.ftb.util.ErrorUtils;

/**
 * SwingWorker that logs into minecraft.net. Returns a string containing the response received from the server.
 */
public class LoginWorker extends SwingWorker<String, Void> {
    private String username, password, mojangData;
    @Getter
    LoginResponse resp;

    public LoginWorker(String username, String password, String mojangData) {
        super();
        this.username = username;
        this.password = password;
        this.mojangData = mojangData;
    }

    @Override
    protected String doInBackground () {
        Benchmark.start("LoginWorker");
        try {
            if (LaunchFrame.canUseAuthlib) {
                try {
                    LoginResponse resp = AuthlibHelper.authenticateWithAuthlib(username, password, mojangData);
                    this.resp = resp;
                    Benchmark.logBenchAs("LoginWorker", "Login Worker Run");
                    if (resp != null && resp.getUsername() != null && !resp.getUsername().isEmpty())
                        return "good";
                    if (resp == null)
                        return "nullResponse";
                    if (resp.getUsername() == null)
                        return "NullUsername";
                    if (resp.getSessionID() == null)
                        return "offline";
                    return "bad";
                } catch (Exception e) {
                    Logger.logError("Error using authlib", e);
                }
            } else {
                ErrorUtils.tossError("Authlib Unavaible");
            }
        } catch (Exception e) {
            ErrorUtils.tossError("Exception occurred, minecraft servers might be down. Check @ help.mojang.com");
        }
        return "";

    }

}
