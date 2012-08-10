package net.ftb.data;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import net.ftb.util.OSUtils;

public class Settings extends Properties
{
	private static final long serialVersionUID = 1L;
	
	public static void LoadSettings(String file) 
			throws FileNotFoundException, IOException
	{
		settings = new Settings(file);
	}
	
	public static Settings getSettings()
	{
		return settings;
	}
	
	private static Settings settings;
	
	public Settings()
	{
		
	}
	
	public Settings(String file) throws FileNotFoundException, IOException
	{
		load(new FileInputStream(file));
	}
	
	public String getInstallPath()
	{
		return getProperty("installPath", OSUtils.getDefInstallPath());
	}
	
	public void setInstallPath(String path)
	{
		setProperty("installPath", path);
	}
}
