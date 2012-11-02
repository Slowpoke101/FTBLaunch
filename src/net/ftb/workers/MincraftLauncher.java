package net.ftb.workers;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import net.ftb.gui.LaunchFrame;
import net.ftb.log.Logger;

public class MincraftLauncher {
	public static int launchMinecraft(String workingDir, String username,
			String password, String forgename, String rmin, String rmax) {
		int success = -1;
		try {
			Logger.logInfo("Loading jars...");
			String[] jarFiles = new String[] { forgename, "minecraft.jar",
					"lwjgl.jar", "lwjgl_util.jar", "jinput.jar" };
			URL[] urls = new URL[jarFiles.length];

			StringBuffer cp = new StringBuffer("");

			for (int i = 0; i < urls.length; i++) {
				try {
					File f;
					if (i == 0) {
						f = new File(new File(workingDir).getParentFile(),
								"/instMods/" + jarFiles[i]);
					} else {
						f = new File(new File(workingDir, "bin"), jarFiles[i]);
					}
					urls[i] = f.toURI().toURL();
					if (!cp.toString().equals(""))
						cp.append(";");
					cp.append(f.getAbsolutePath());

					Logger.logInfo("Loading URL: " + urls[i].toString());
				} catch (MalformedURLException e) {
					Logger.logError("Malformed URL Exception occured", e);
					System.exit(5);
				}
			}

			// Holder for the Arguments
			List<String> arguments = new ArrayList<String>();

			// Setting the JAVA vm
			String separator = System.getProperty("file.separator");
			String path = System.getProperty("java.home") + separator + "bin"
					+ separator + "java";
			arguments.add(path);

			// Homedir
			String home = new File(workingDir).getParentFile().getAbsolutePath();
			arguments.add("-Duser.home=\"" + home + "\"");
			Logger.logInfo("Setting homedir to "+home);

			// Setting memory settings
			setMemory(arguments, rmin, rmax);

			// Now the classpath...

			arguments.add("-cp");
			arguments.add(cp.toString());

			// Natives path
			String nativesDir = new File(new File(workingDir, "bin"), "natives")
					.toString();
			arguments.add("-Djava.library.path=\"" + nativesDir + "\"");

			// Misc stuff

			arguments.add("-Dorg.lwjgl.librarypath=\"" + nativesDir + "\"");
			arguments.add("-Dnet.java.games.input.librarypath=\"" + nativesDir
					+ "\"");

			// Adding mainclass
			arguments.add("net.minecraft.client.Minecraft");
			arguments.add(username);
			arguments.add(password);

			String cmdline = "";

			for (String cmd : arguments)
				cmdline += "" + cmd + " ";
			Logger.logInfo(cmdline);

			// Lets go
			ProcessBuilder processBuilder = new ProcessBuilder(arguments);

			try {

				Process p = processBuilder.start();

				System.out.println("Launcher started");
				success = 1;
			} catch (IOException e) {
				Logger.logError("Error during Minecraft launch", e);
			}

		} catch (Exception e) {
			Logger.logError("Exception during launch of Minecraft", e);
		}

		return success;
	}

	private static void setMemory(List<String> arguments, String rmin,
			String rmax) {
		boolean memorySet = false;
		try {
			int min = -1, max = -1;
			if (rmin != null && Integer.parseInt(rmin) > 0)
				min = Integer.parseInt(rmin);
			if (rmax != null && Integer.parseInt(rmax) > 0) {
				max = Integer.parseInt(rmax);
				if (min > 0 && max > 0) {
					arguments.add("-Xms" + rmin + "M");
					Logger.logInfo("Setting MinMemory to " + rmin);
					arguments.add("-Xmx" + rmax + "M");
					Logger.logInfo("Setting MaxMemory to " + rmax);
					memorySet = true;
				}
			}
		} catch (Exception e) {
			Logger.logError("Error parsing memory settings", e);
		}
		// Set Defaults if no Settings available
		if (!memorySet) {
			arguments.add("-Xms" + 256 + "M");
			Logger.logInfo("Defaulting MinMemory to " + 256);
			arguments.add("-Xmx" + 1024 + "M");
			Logger.logInfo("Defaulting MaxMemory to " + 1024);
		}
	}

}
