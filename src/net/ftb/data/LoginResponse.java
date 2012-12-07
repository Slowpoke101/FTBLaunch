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
