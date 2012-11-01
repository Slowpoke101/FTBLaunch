package net.ftb.workers;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;

import javax.swing.SwingWorker;

import net.ftb.util.AppUtils;

/**
 * SwingWorker that logs into minecraft.net. Returns a string containing the
 * response received from the server.
 */
public class LoginWorker extends SwingWorker<String, Void> {
	private final String username;
	private final String password;

	public LoginWorker(String username, String password) {
		super();
		this.username = username;
		this.password = password;
	}

	@Override
	protected String doInBackground() throws IOException{
		StringBuilder requestBuilder = new StringBuilder();
		requestBuilder.append("https://login.minecraft.net/?user=").append(URLEncoder.encode(username, "UTF-8")).append("&password=")
		.append(URLEncoder.encode(password, "UTF-8")).append("&version=13");

		URL url = new URL(requestBuilder.toString());
		return AppUtils.downloadString(url);
	}
}
