package net.ftb.prelauncher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.ftb.data.Settings;
import net.ftb.gui.LaunchFrame;


/**
 * 
 * PreLauncher for FTB Launcher
 * 
 * for setting up a new JVM Instance with custom memory Settings and maybe more later
 * 
 */
public class PreLauncher {
	public static void main(String[] args) {
		Class<LaunchFrame> clazz = LaunchFrame.class;

		String separator = System.getProperty("file.separator");
		String classpath = System.getProperty("java.class.path");
		String path = System.getProperty("java.home") + separator + "bin"
				+ separator + "java";

		List<String> arguments = new ArrayList<String>();
		arguments.add(path);
		
		// Lets try to load memory settings....
		setMemory(arguments);
		
		// Lets add the normal things to start
		arguments.add("-cp");
		arguments.add(classpath);
		arguments.add(clazz.getCanonicalName());

		System.out.print("cmdline: ");
		for (String arg : arguments)
			System.out.print(arg + " ");
		System.out.println("");

		ProcessBuilder processBuilder = new ProcessBuilder(arguments);

		try {
			processBuilder.start();
			System.out.println("Launcher started");
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.err.println("Something gone wrong, starting directly...");
		LaunchFrame.main(null);
	}
	
	private static void setMemory(List<String> arguments) {
		boolean memorySet = false;
		try {
			Settings.initSettings();
			String rmax = Settings.getSettings().getRamMax();
			String rmin = Settings.getSettings().getRamMin();
			int min = -1, max = -1;
			if (rmin != null && Integer.parseInt(rmin) > 0)
				min = Integer.parseInt(rmin);
			if (rmax != null && Integer.parseInt(rmax) > 0) {
				max = Integer.parseInt(rmax);
				if (min > 0 && max > 0) {
					arguments.add("-Xms" + rmin + "M");
					System.out.println("Setting MinMemory to " + rmin);
					arguments.add("-Xmx" + rmax + "M");
					System.out.println("Setting MaxMemory to " + rmax);
					memorySet = true;
				}
			}
		} catch (IOException e1) {
			System.err.println("Could not load settings, normal on first start!");
		}
		// Set Defaults if no Settings available
		if (!memorySet) {
			arguments.add("-Xms" + 256 + "M");
			System.out.println("Defaulting MinMemory to " + 256);
			arguments.add("-Xmx" + 1024 + "M");
			System.out.println("Defaulting MaxMemory to " + 1024);
		}
	}
}
