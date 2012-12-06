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
import java.util.HashMap;
import java.util.List;

import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.panes.ModpacksPane;
import net.ftb.log.LogLevel;
import net.ftb.log.Logger;
import net.ftb.util.OSUtils;

/**
 * 
 *  Support Class for starting Minecraft with custom Memory options
 *
 */
public class MinecraftLauncher {

	public static Process launchMinecraft(String workingDir, String username, String password, String forgename, String rmax) throws IOException {
		String[] jarFiles = new String[] {"minecraft.jar", "lwjgl.jar", "lwjgl_util.jar", "jinput.jar" };
		StringBuilder cpb = new StringBuilder("");
		File tempDir = new File(new File(workingDir).getParentFile(), "/instMods/");

		if(tempDir.isDirectory()) {
			for(String name : tempDir.list()) {
				if(name.toLowerCase().contains("forge") && name.toLowerCase().endsWith(".zip")) {
					if(!name.toLowerCase().equalsIgnoreCase(forgename)) {
						if(new File(tempDir, forgename).exists()) {
							new File(tempDir, name).delete();
						} else {
							new File(tempDir, name).renameTo(new File(tempDir, forgename));
						}
					}
				}
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

		cpb.append(OSUtils.getJavaDelimiter());
		cpb.append(new File(tempDir, forgename).getAbsolutePath());

		for(String jarFile : jarFiles) {
			cpb.append(OSUtils.getJavaDelimiter());
			cpb.append(new File(new File(workingDir, "bin"), jarFile).getAbsolutePath());
		}

		List<String> arguments = new ArrayList<String>();

		String separator = System.getProperty("file.separator");
		String path = System.getProperty("java.home") + separator + "bin" + separator + "java";
		arguments.add(path);

		setMemory(arguments, rmax);

		arguments.add("-cp");
		arguments.add(System.getProperty("java.class.path") + cpb.toString());

		arguments.add(MinecraftLauncher.class.getCanonicalName());
		arguments.add(workingDir);
		arguments.add(forgename);
		arguments.add(username);
		arguments.add(password);
		arguments.add(ModPack.getPack(ModpacksPane.getIndex()).getName());
		arguments.add(OSUtils.getDynamicStorageLocation() + "ModPacks" + separator + ModPack.getPack(ModpacksPane.getIndex()).getDir() + separator + ModPack.getPack(ModpacksPane.getIndex()).getLogoName());
		arguments.add(Settings.getSettings().getMinecraftX());
		arguments.add(Settings.getSettings().getMinecraftY());
		arguments.add(Settings.getSettings().getMinecraftXPos());
		arguments.add(Settings.getSettings().getMinecraftYPos());
		arguments.add(Settings.getSettings().getAutoMaximize());
		arguments.add(Settings.getSettings().getCenterWindow());

		ProcessBuilder processBuilder = new ProcessBuilder(arguments);
		processBuilder.redirectErrorStream(true);
		return processBuilder.start();
	}

	private static void setMemory(List<String> arguments, String rmax) {
		boolean memorySet = false;
		try {
			int min = 256, max = -1;
			if (rmax != null && Integer.parseInt(rmax) > 0) {
				max = Integer.parseInt(rmax);
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
		String basepath = args[0];
		String forgename = args[1];
		String username = args[2];
		String password = args[3];
		String modPackName = args[4];
		String modPackImageName = args[5];
		String minecraftX = args[6];
		String minecraftY = args[7];
		String minecraftXPos = args[8];
		String minecraftYPos = args[9];
		String minecraftMax = args[10];
		String centerWindow = args[11];

		try {
			System.out.println("Loading jars...");
			String[] jarFiles = new String[] {"minecraft.jar", "lwjgl.jar", "lwjgl_util.jar", "jinput.jar" };
			HashMap<Integer, File> map = new HashMap<Integer, File>();
			int counter = 0;
			File tempDir = new File(new File(basepath).getParentFile(), "instMods/");
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
			String nativesDir = new File(new File(basepath, "bin"), "natives").toString();
			System.out.println("Natives loaded...");

			System.setProperty("org.lwjgl.librarypath", nativesDir);
			System.setProperty("net.java.games.input.librarypath", nativesDir);

			System.setProperty("user.home", new File(basepath).getParent());

			URLClassLoader cl = new URLClassLoader(urls, MinecraftLauncher.class.getClassLoader());

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

			String[] mcArgs = new String[2];
			mcArgs[0] = username;
			mcArgs[1] = password;

			String mcDir = mc.getMethod("a", String.class).invoke(null, (Object) "minecraft").toString();

			System.out.println("MCDIR: " + mcDir);

			System.out.println("Launching with applet wrapper...");
			
			try {
				Class<?> MCAppletClass = cl.loadClass("net.minecraft.client.MinecraftApplet");
				Applet mcappl = (Applet) MCAppletClass.newInstance();
				MinecraftFrame mcWindow = new MinecraftFrame(modPackName, modPackImageName, Integer.parseInt(minecraftX), Integer.parseInt(minecraftY), 
						Integer.parseInt(minecraftXPos), Integer.parseInt(minecraftYPos), Boolean.parseBoolean(minecraftMax), Boolean.parseBoolean(centerWindow));
				mcWindow.start(mcappl, mcArgs[0], mcArgs[1]);
			} catch (InstantiationException e) {
				Logger.log("Applet wrapper failed! Falling back to compatibility mode.", LogLevel.WARN, e);
				mc.getMethod("main", String[].class).invoke(null, (Object) mcArgs);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
