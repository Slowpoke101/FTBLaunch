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
package net.ftb.mclauncher;

import java.applet.Applet;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import net.feed_the_beast.launcher.json.JsonFactory;
import net.feed_the_beast.launcher.json.assets.AssetIndex;
import net.feed_the_beast.launcher.json.assets.AssetIndex.Asset;
import net.feed_the_beast.launcher.json.versions.OS;
import net.ftb.data.Settings;
import net.ftb.log.LogLevel;
import net.ftb.log.Logger;
import net.ftb.util.DownloadUtils;
import net.ftb.util.FileUtils;
import net.ftb.util.OSUtils;
import net.ftb.util.winreg.JavaFinder;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class MinecraftLauncherNew
{
	public static Process launchMinecraft(File gameDir, File assetDir, File nativesDir, 
	        List<File> classpath,
	        String username, String password,
	        String mainClass, String args, String assetIndex,
	        String rmax, String maxPermSize,
	        String version) throws IOException
    {
	    
	    assetDir = syncAssets(assetDir, assetIndex);
	    
		StringBuilder cpb = new StringBuilder("");
		for (File f : classpath)
		{
            cpb.append(OSUtils.getJavaDelimiter());
		    cpb.append(f.getAbsolutePath());
		}

		List<String> arguments = new ArrayList<String>();

		String separator = System.getProperty("file.separator");
		String path = new String();
		if (OS.CURRENT == OS.WINDOWS && JavaFinder.parseWinJavaVersion().path != null)
		    path = JavaFinder.parseWinJavaVersion().path.replace(".exe", "w.exe");
		else
		    path = System.getProperty("java.home") + ("/bin/java" + (OS.CURRENT == OS.WINDOWS ? "w" : "")).replace("/", separator);
		Logger.logInfo("Java Path: " + path);
		arguments.add(path);

		setMemory(arguments, rmax);
		if(OSUtils.getCurrentOS().equals(OS.WINDOWS)) {
            // Detect if OS is 64 or 32 bit
            String arch = System.getenv("PROCESSOR_ARCHITECTURE");
            String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
            if(!(arch.endsWith("64") || (wow64Arch != null && wow64Arch.endsWith("64"))))
                 if(maxPermSize == null || maxPermSize.isEmpty()) maxPermSize = "128m";}
		    if(maxPermSize == null || maxPermSize.isEmpty()) maxPermSize = "256m";
		//arguments.add("-XX:+UseConcMarkSweepGC");
		//arguments.add("-XX:+CMSIncrementalMode");
		//arguments.add("-XX:+AggressiveOpts");
		//arguments.add("-XX:+CMSClassUnloadingEnabled");
		arguments.add("-XX:PermSize=" + maxPermSize);
        arguments.add("-Dorg.lwjgl.librarypath=" + nativesDir.getAbsolutePath());
        arguments.add("-Dnet.java.games.input.librarypath=" + nativesDir.getAbsolutePath());
        arguments.add("-Duser.home=" + gameDir.getParentFile().getAbsolutePath());

		arguments.add("-cp");
		arguments.add(System.getProperty("java.class.path") + cpb.toString());
		
		String additionalOptions = Settings.getSettings().getAdditionalJavaOptions();
		if (!additionalOptions.isEmpty()) {
 			Collections.addAll(arguments, additionalOptions.split("\\s+"));
 		}

		arguments.add(mainClass);
		for (String s : args.split(" "))
		{
	        if (s.equals("${auth_player_name}"   )) arguments.add(username);
	        else if (s.equals("${auth_session}"  )) arguments.add(password);
    		else if (s.equals("${version_name}"  )) arguments.add(version);
    		else if (s.equals("${game_directory}")) arguments.add(gameDir.getAbsolutePath());
    		else if (s.equals("${game_assets}"   )) arguments.add(assetDir.getAbsolutePath());
    		else arguments.add(s);
		}

		ProcessBuilder builder = new ProcessBuilder(arguments);
		//StringBuilder tmp = new StringBuilder();
		//for (String a : builder.command()) tmp.append(a).append(' ');
		//Logger.logInfo("Launching: " + tmp.toString());		
		builder.directory(gameDir);
		builder.redirectErrorStream(true);
		return builder.start();
	}

	private static void setMemory(List<String> arguments, String rmax) {
		boolean memorySet = false;
		try {
			int min = 256;
			if (rmax != null && Integer.parseInt(rmax) > 0) {
				arguments.add("-Xms" + min + "M");
				Logger.logInfo("Setting MinMemory to " + min);
				arguments.add("-Xmx" + rmax + "M");
				Logger.logInfo("Setting MaxMemory to " + rmax);
				memorySet = true;
			}
		} catch (Exception e) {
			Logger.logError("Error parsing memory settings", e);
		}
		if (!memorySet) {
			arguments.add("-Xms" + 256 + "M");
			Logger.logInfo("Defaulting MinMemory to " + 256);
			arguments.add("-Xmx" + 1024 + "M");
			Logger.logInfo("Defaulting MaxMemory to " + 1024);
		}
	}

	public static void main(String[] args) {
		String basepath = args[0], animationname = args[1], forgename = args[2], username = args[3], password = args[4], modPackName = args[5], modPackImageName = args[6];
		Settings.getSettings().save();  //Call so that the settings file is loaded from the correct location.  Would be wrong on OS X and *nix if called after user.home is reset

		try {
			System.out.println("Loading jars...");
			String[] jarFiles = new String[] {"minecraft.jar", "lwjgl.jar", "lwjgl_util.jar", "jinput.jar" };
			ArrayList<File> classPathFiles = new ArrayList<File>();
			File tempDir = new File(new File(basepath).getParentFile(), "instMods/");
			if(tempDir.isDirectory()) {
				for(String name : tempDir.list()) {
					if(!name.equalsIgnoreCase(forgename)) {
						if(name.toLowerCase().endsWith(".zip") || name.toLowerCase().endsWith(".jar")) {
							classPathFiles.add(new File(tempDir, name));
						}
					}
				}
			}

			classPathFiles.add(new File(tempDir, forgename));
			for(String jarFile : jarFiles) {
				classPathFiles.add(new File(new File(basepath, "bin"), jarFile));
			}	

			URL[] urls = new URL[classPathFiles.size()];
			for(int i = 0; i < classPathFiles.size(); i++) {
				try {
					urls[i] = classPathFiles.get(i).toURI().toURL();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
				System.out.println("Added URL to classpath: " + urls[i].toString());
			}

			System.out.println("Loading natives...");
			String nativesDir = new File(new File(basepath, "bin"), "natives").toString();
			System.out.println("Natives loaded...");

			System.setProperty("org.lwjgl.librarypath", nativesDir);
			System.setProperty("net.java.games.input.librarypath", nativesDir);

			System.setProperty("user.home", new File(basepath).getParent());

			URLClassLoader cl = new URLClassLoader(urls, MinecraftLauncherNew.class.getClassLoader());

			System.out.println("Loading minecraft class");
			Class<?> mc = cl.loadClass("net.minecraft.client.Minecraft");
			System.out.println("mc = " + mc);
			Field[] fields = mc.getDeclaredFields();
			System.out.println("field amount: " + fields.length);

			for (Field f : fields) {
				if (f.getType() != File.class) {
					continue;
				}
				if (0 == (f.getModifiers() & (Modifier.PRIVATE | Modifier.STATIC))) {
					continue;
				}
				f.setAccessible(true);
				f.set(null, new File(basepath));
				System.out.println("Fixed Minecraft Path: Field was " + f.toString());
				break;
			}

			String mcDir = mc.getMethod("a", String.class).invoke(null, (Object) "minecraft").toString();

			System.out.println("MCDIR: " + mcDir);

			System.out.println("Launching with applet wrapper...");

			try {
				Class<?> MCAppletClass = cl.loadClass("net.minecraft.client.MinecraftApplet");
				Applet mcappl = (Applet) MCAppletClass.newInstance();
				MinecraftFrame mcWindow = new MinecraftFrame(modPackName, modPackImageName, animationname);
				mcWindow.start(mcappl, username, password);
			} catch (InstantiationException e) {
				Logger.log("Applet wrapper failed! Falling back to compatibility mode.", LogLevel.WARN, e);
				mc.getMethod("main", String[].class).invoke(null, (Object) new String[] {username, password});
			}
		} catch (Throwable t) {
			Logger.logError("Unhandled error launching minecraft", t);
		}
	}

	private static File syncAssets(File assetDir, String indexName) throws JsonSyntaxException, JsonIOException, IOException
	{
	    Logger.logInfo("Syncing Assets:");
	    File objects = new File(assetDir, "objects");
	    AssetIndex index = JsonFactory.loadAssetIndex(new File(assetDir, "indexes/{INDEX}.json".replace("{INDEX}", indexName)));
	    
	    if (!index.virtual)
	        return assetDir;

        File targetDir = new File(assetDir, "virtual/" + indexName);
        
	    Set<File> old = FileUtils.listFiles(targetDir);
	    
	    for (Entry<String, Asset> e : index.objects.entrySet())
	    {
	        Asset asset = e.getValue();
	        File local = new File(targetDir, e.getKey());
	        File object = new File(objects, asset.hash.substring(0, 2) + "/" + asset.hash);

            old.remove(local);
	        
	        if (local.exists() && !DownloadUtils.fileSHA(local).equals(asset.hash))
	        {
	            Logger.logInfo("  Changed: " + e.getKey());
	            FileUtils.copyFile(object, local, true);
	        }
	        else if (!local.exists())
	        {
                Logger.logInfo("  Added: " + e.getKey());
                FileUtils.copyFile(object, local);
	        }
	    }

	    for (File f : old)
	    {
	        String name = f.getAbsolutePath().replace(targetDir.getAbsolutePath(), "");
	        Logger.logInfo("  Removed: " + name.substring(1));
	        f.delete();
	    }

	    return targetDir;
	}
}
