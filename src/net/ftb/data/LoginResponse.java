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
package net.ftb.data;

public class LoginResponse {
	private String latestVersion, downloadTicket, username, sessionID;

	/**
	 * Constructor for LoginResponse class
	 * @param responseString - the response from the minecraft server
	 */
	public LoginResponse(String responseString) {
		String[] responseValues = responseString.split(":");
		if (responseValues.length < 4) {
			throw new IllegalArgumentException("Invalid response string.");
		} else {
			this.latestVersion = responseValues[0];
			this.downloadTicket = responseValues[1];
			this.username = responseValues[2];
			this.sessionID = responseValues[3];
		}
	}

	/**
	 * Used to grab the latest version of minecraft from response string
	 * @return - the latest version of minecraft
	 */
	public String getLatestVersion() {
		return latestVersion;
	}

	/**
	 * Used to grab the download ticket from response string
	 * @return - the download ticket for minecraft
	 */
	public String getDownloadTicket() {
		return downloadTicket;
	}

	/**
	 * Used to grab the username from response string
	 * @return - the username of the user
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Used to grab the session ID from response string
	 * @return - the session ID of the minecraft instance
	 */
	public String getSessionID() {
		return sessionID;
	}
}
