package net.ftb.data;

public class LoginResponse {
	private String latestVersion, downloadTicket, username, sessionID;

	/**
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
	 * @return - the latest version of minecraft
	 */
	public String getLatestVersion() {
		return latestVersion;
	}

	/**
	 * @return - the download ticket for minecraft
	 */
	public String getDownloadTicket() {
		return downloadTicket;
	}

	/**
	 * @return - the username of the user
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @return - the session ID of the minecraft instance
	 */
	public String getSessionID() {
		return sessionID;
	}
}
