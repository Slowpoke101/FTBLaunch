package net.ftb.workers;

import java.net.URL;

import javax.swing.SwingWorker;

import net.ftb.util.AppUtils;

/**
 * SwingWorker that logs into minecraft.net. Returns a string containing the
 * response received from the server.
 */
public class LoginWorker extends SwingWorker<String, Void>
{
	public LoginWorker(String username, String password)
	{
		super();
		this.username = username;
		this.password = password;
	}
	
	@Override
	protected String doInBackground() throws Exception
	{
		StringBuilder requestBuilder = new StringBuilder();
		requestBuilder.append("https://login.minecraft.net/?user=");
		requestBuilder.append(username);
		requestBuilder.append("&password=");
		requestBuilder.append(password);
		requestBuilder.append("&version=13");
		
		URL url = new URL(requestBuilder.toString());
		return AppUtils.downloadString(url);
	}
	
	String username;
	String password;
}
