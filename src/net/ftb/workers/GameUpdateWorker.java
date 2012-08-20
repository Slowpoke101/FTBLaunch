package net.ftb.workers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.SwingWorker;

import net.ftb.util.AppUtils;

/**
 * SwingWorker that downloads Minecraft. Returns true if successful, false if it
 * fails.
 * 
 * Right now this is pretty much just a port of MultiMC's GameUpdateTask, but
 * it will likely change in the future.
 */
public class GameUpdateWorker extends SwingWorker<Boolean, Void>
{
	public GameUpdateWorker(String latestVersion,
							String mainGameURL,
							String binDir,
							boolean forceUpdate)
	{
		this.latestVersion = latestVersion;
		this.mainGameURL = mainGameURL;
		this.binDir = new File(binDir);
		this.forceUpdate = forceUpdate;
	}
	
	@Override
	protected Boolean doInBackground() throws Exception
	{
		if (!loadJarURLs())
			return false;
		setProgress(5);
		
		if (!binDir.exists())
			binDir.mkdirs();
		
		if (!latestVersion.isEmpty())
		{
			File versionFile = new File(binDir, "version");
			boolean cacheAvailable = false;
			
			if (!forceUpdate && versionFile.exists() && 
					(latestVersion == "-1" || latestVersion == readVersionFile()))
			{
				cacheAvailable = true;
				setProgress(90);
			}
			
			if (forceUpdate || !cacheAvailable)
			{
				shouldUpdate = true;
				if (!forceUpdate && versionFile.exists())
				{
					// Ask to update.
				}
				
				
				// This check is not actually stupid. 
				// The AskToUpdate method will set shouldUpdate to true or false  
				// depending on whether or not the user wants to update.
				if (shouldUpdate)
				{
					writeVersionFile(latestVersion);
					if (!downloadJars())
						return false;
					if (!extractNatives())
						return false;
				}
			}
			
		}
		
		return true;
	}
	
	protected boolean loadJarURLs()
	{
		String[] jarList =
		{
			mainGameURL, "lwjgl.jar", "lwjgl_util.jar", "jinput.jar"
		};
		
		jarURLs = new URL[jarList.length];
		try
		{
			for (int i = 0; i < jarList.length; i++)
			{
				jarURLs[i] = new URL("http://s3.amazonaws.com/MinecraftDownload/" 
						+ jarList[i]);
			}
		}
		catch (MalformedURLException e)
		{
			return false;
		}
		
		return true;
	}
	
	protected boolean downloadJars()
	{
		return true;
	}
	
	protected boolean extractNatives()
	{
		return true;
	}
	
	protected String readVersionFile()
	{
		try
		{
			FileInputStream inputStream = new FileInputStream(
					new File(binDir, "version"));
			return AppUtils.readString(inputStream);
		} catch (FileNotFoundException e)
		{
			return "";
		} catch (IOException e)
		{
			e.printStackTrace();
			return "";
		}
	}
	
	protected void writeVersionFile(String versionString)
	{
		try
		{
			FileOutputStream outputStream = new FileOutputStream(
					new File(binDir, "version"));
			AppUtils.writeString(outputStream, versionString);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	protected String latestVersion;
	protected String mainGameURL;
	protected File binDir;
	protected boolean forceUpdate;
	protected boolean shouldUpdate;
	
	protected URL[] jarURLs;
}
