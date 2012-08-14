package net.ftb.workers;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.SwingWorker;

import net.ftb.util.AppUtils;

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
		
		URL url;
		try
		{
			url = new URL(requestBuilder.toString());
		} catch (MalformedURLException e)
		{
			e.printStackTrace();
			return "Malformed URL";
		}
		
		return AppUtils.downloadString(url);
	}
	
	String username;
	String password;
}
