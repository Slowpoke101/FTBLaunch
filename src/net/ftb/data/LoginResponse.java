package net.ftb.data;


public class LoginResponse {
	private String latestVersion;
	private String downloadTicket;
	private String username;
	private String sessionID;

	public LoginResponse() { }

	public LoginResponse(String latestVersion, String downloadTicket, String username, String sessionID) {
		this.latestVersion = latestVersion;
		this.downloadTicket = downloadTicket;
		this.username = username;
		this.sessionID = sessionID;
	}

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

	public String getLatestVersion() {
		return latestVersion;
	}

	public void setLatestVersion(String latestVersion) {
		this.latestVersion = latestVersion;
	}

	public String getDownloadTicket() {
		return downloadTicket;
	}

	public void setDownloadTicket(String downloadTicket) {
		this.downloadTicket = downloadTicket;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getSessionID() {
		return sessionID;
	}

	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}
}
