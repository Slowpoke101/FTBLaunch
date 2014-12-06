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
package net.ftb.data;

import com.mojang.authlib.UserAuthentication;
import lombok.Getter;

public class LoginResponse {

    @Getter
    private UserAuthentication auth;

    /**
     * Constructor for LoginResponse class
     * @param version - the version from authlib
     * @param dlTicket - the ticket from authlib
     * @param username - the username from authlib
     * @param session - the session ID from authlib
     * @param uniqueID - the user's uuid from authlib
     */
    public LoginResponse (String version, String dlTicket, String username, String session, String uniqueID, UserAuthentication userAuth) {
        this.latestVersion = version;
        this.downloadTicket = dlTicket;
        this.username = username;
        this.sessionID = session;
        this.uuid = uniqueID;
        this.auth = userAuth;
    }

    /**
     * Used to grab the latest version of minecraft from response string
     * @return - the latest version of minecraft
     */
    @Getter
    private String latestVersion;

    /**
     * Used to grab the download ticket from response string
     * @return - the download ticket for minecraft
     */
    @Getter
    private String downloadTicket;

    /**
     * Used to grab the username from response string
     * @return - the username of the user
     */
    @Getter
    private String username;

    /**
     * Used to grab the session ID from response string
     * @return - the session ID of the minecraft instance
     */
    @Getter
    private String sessionID;

    /**
     * Used to grab the user's uuid from response string
     * @return - the uuid of the user
     */
    @Getter
    private String uuid;

}
