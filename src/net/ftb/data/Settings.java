package net.ftb.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import net.ftb.util.OSUtils;
import net.ftb.util.PathUtils;

public class Settings extends Properties
{
	private static final long serialVersionUID = 1L;
	
	public static void initSettings() throws IOException
	{
		File cfgFile;
		
		// Check for a config file in the current working directory.
		cfgFile = new File("ftblaunch.cfg");
		if (cfgFile.exists())
		{
			LoadSettings(cfgFile);
			return;
		}
		
		// Check for a config file in the default installation directory.
		cfgFile = new File(PathUtils.combine(OSUtils.getDefInstallPath(),
				"ftblaunch.cfg"));
		if (cfgFile.exists())
		{
			LoadSettings(cfgFile);
			return;
		}
		
		// If none are found, load a blank config file in the default 
		// installation directory.
		settings = new Settings();
		settings.setConfigFile(cfgFile);
	}
	
	public static void LoadSettings(File file) throws FileNotFoundException,
			IOException
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
	
	public Settings(File file) throws FileNotFoundException, IOException
	{
		configPath = file;
		load(new FileInputStream(file));
	}
	
	public void save() throws FileNotFoundException, IOException
	{
		store(new FileOutputStream(configPath), "FTBLaunch Config File");
	}
	
	public String getLastUser()
	{
		return getProperty("lastUser", null);
	}
	
	public void setLastUser(String user)
	{
		setProperty("lastUser", user);
	}
	
	public String getInstallPath()
    {
        return getProperty("installPath", OSUtils.getDefInstallPath());
    }
	
	public void setInstallPath(String path)
	{
		setProperty("installPath", path);
	}
	
	public String getExtraModsPath()
    {
        return getProperty("ExtraModsPath", OSUtils.getDefInstallPath());
    }
	
	public void setExtraModsPath(String path)
	{
		setProperty("ExtraModsPath", path);
	}
	public Boolean getExtraModsEnabled()
    {
         String tmp = getProperty("ExtraModsEnabled", OSUtils.getDefInstallPath());
         if (tmp == "true" ){  return true; }
         else { return false; }         
    }
	
	public void setExtraModsEnabled(Boolean cfg)
	{
		if (cfg == true) {	setProperty("ExtraModsEnabled", "true"); }
		else { setProperty("ExtraModsEnabled", "false"); }
	}
	
	public boolean getForceUpdate()
	{
		return forceUpdate;
	}
	
	public void setForceUpdate(boolean force)
	{
		forceUpdate = force;
	}
	
	public void setConfigFile(File path)
	{
		configPath = path;
	}
	
	public File getConfigFile()
	{
		return configPath;
	}
	
	
	private File configPath;
	
	// This doesn't get saved to a config.
	private boolean forceUpdate;
}
