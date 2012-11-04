package net.ftb.mclauncher;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.ftb.log.Logger;
import net.ftb.util.OSUtils;


/**
 * 
 *  Support Class for starting Minecraft with custom Memory options
 *
 */
public class MinecraftLauncher {
	public static int launchMinecraft(String workingDir, String username, String password, String forgename, String rmin, String rmax) {
		int success = -1;
		try {
			String[] jarFiles = new String[] {"minecraft.jar", "lwjgl.jar", "lwjgl_util.jar", "jinput.jar" };
			StringBuffer cpb = new StringBuffer("");
			File tempDir = new File(new File(workingDir).getParentFile(), "/instMods/");
			// Rename old forge version with the new name
			File oldForge = new File(tempDir, "minecraftforge-universal-6.0.1.353.zip");
			if(oldForge.exists()) {
				oldForge.renameTo(new File(tempDir, "MinecraftForge.zip"));
			}
			// Go through all remaining items in instmods folder
			if(tempDir.isDirectory()) {
				for(String name : tempDir.list()) {
					if(!name.equalsIgnoreCase(forgename)) {
						if(name.toLowerCase().endsWith(".zip") || name.toLowerCase().endsWith(".jar")) {
							cpb.append(OSUtils.getJavaDelimiter());
							cpb.append(new File(tempDir, name).getAbsolutePath());
						}
					}
				}
			} else {
				Logger.logInfo("Not a directory.");
			}
			// Load forge LAASSSSSST
			cpb.append(OSUtils.getJavaDelimiter());
			cpb.append(new File(tempDir, forgename).getAbsolutePath());
			
			for(int i = 0; i < jarFiles.length; i++) {
				cpb.append(OSUtils.getJavaDelimiter());
				cpb.append(new File(new File(workingDir, "bin"), jarFiles[i]).getAbsolutePath());
			}

			// Holder for the Arguments
			List<String> arguments = new ArrayList<String>();

			// Setting the JAVA vm
			String separator = System.getProperty("file.separator");
			String path = System.getProperty("java.home") + separator + "bin"
					+ separator + "java";
			arguments.add(path);

			

			// Setting memory settings
			setMemory(arguments, rmin, rmax);

			// Now the classpath...

			arguments.add("-cp");
			arguments.add(System.getProperty("java.class.path")
					+ cpb.toString());

			arguments.add(MinecraftLauncher.class.getCanonicalName());
			arguments.add(workingDir);
			arguments.add(forgename);
			arguments.add(username);
			arguments.add(password);
			
			// Lets go
			ProcessBuilder processBuilder = new ProcessBuilder(arguments);

			try {
				processBuilder.start();
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
					if(min < max) {
						arguments.add("-Xms" + rmin + "M");
						Logger.logInfo("Setting MinMemory to " + rmin);
						arguments.add("-Xmx" + rmax + "M");
						Logger.logInfo("Setting MaxMemory to " + rmax);
						memorySet = true;
					} else {
						Logger.logInfo("Min memory bigger than Max, swapping values.");
						arguments.add("-Xms" + rmax + "M");
						Logger.logInfo("Setting MinMemory to " + rmin);
						arguments.add("-Xmx" + rmin + "M");
						Logger.logInfo("Setting MaxMemory to " + rmax);
						memorySet = true;
					}
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

	public static void main(String[] args) {
		String basepath = args[0];
		String forgename = args[1];
		String username = args[2];
		String password = args[3];

		try {
			System.out.println("Loading jars...");
			String[] jarFiles = new String[] {"minecraft.jar", "lwjgl.jar", "lwjgl_util.jar", "jinput.jar" };
			HashMap<Integer, File> map = new HashMap<Integer, File>();
			int counter = 0;
			File tempDir = new File(new File(basepath).getParentFile(), "/instMods/");
			if(tempDir.isDirectory()) {
				for(String name : tempDir.list()) {
					if(!name.equalsIgnoreCase(forgename)) {
						if(name.toLowerCase().endsWith(".zip") || name.toLowerCase().endsWith(".jar")) {
							map.put(counter, new File(tempDir, name));
							counter++;
						}
					}
				}
			}
			// Load forge LAST
			map.put(counter, new File(tempDir, forgename));
			counter++;
			for(String jarFile : jarFiles) {
				map.put(counter, new File(new File(basepath, "bin"), jarFile));
				counter++;
			}	
			
			URL[] urls = new URL[map.size()];
			for(int i = 0; i < counter; i++) {
				try {
					urls[i] = map.get(i).toURI().toURL();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
				System.out.println("Loading URL: " + urls[i].toString());
			}

			System.out.println("Loading natives...");
			String nativesDir = new File(new File(basepath, "bin"), "natives")
					.toString();

			System.setProperty("org.lwjgl.librarypath", nativesDir);
			System.setProperty("net.java.games.input.librarypath", nativesDir);

			System.setProperty("user.home", new File(basepath).getParent());

			URLClassLoader cl = new URLClassLoader(urls,
					MinecraftLauncher.class.getClassLoader());

			// Get the Minecraft Class.
			Class<?> mc = cl.loadClass("net.minecraft.client.Minecraft");

//			try {
//				cl.close();
//			} catch (IOException e) {
//				System.err.println("Exception during closing classloader");
//				e.printStackTrace();
//			}

			Field[] fields = mc.getDeclaredFields();

			for (Field f : fields) {
				if (f.getType() != File.class) {
					// Has to be File
					continue;
				}
				if (0 == (f.getModifiers() & (Modifier.PRIVATE | Modifier.STATIC))) {
					// And Private Static.
					continue;
				}
				f.setAccessible(true);
				f.set(null, new File(basepath));
				// And set it.
				System.out.println("Fixed Minecraft Path: Field was "
						+ f.toString());
				break;
			}

			String[] mcArgs = new String[2];
			mcArgs[0] = username;
			mcArgs[1] = password;

			String mcDir = mc.getMethod("a", String.class)
					.invoke(null, (Object) "minecraft").toString();

			System.out.println("MCDIR: " + mcDir);

			mc.getMethod("main", String[].class).invoke(null, (Object) mcArgs);

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}

	}
}