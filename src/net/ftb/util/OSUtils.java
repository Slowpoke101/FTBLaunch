package net.ftb.util;

import java.io.File;
import java.net.URISyntaxException;
import java.security.CodeSource;

import net.ftb.gui.LaunchFrame;

public class OSUtils {
	/**
	 * Gets the default installation path for the current OS.
	 * @return a string containing the default install path for the current OS.
	 */
	public static String getDefInstallPath() {
		try {
			CodeSource codeSource = LaunchFrame.class.getProtectionDomain().getCodeSource();
			File jarFile;
			jarFile = new File(codeSource.getLocation().toURI().getPath());
			return jarFile.getParentFile().getPath();
		} catch (URISyntaxException e) { e.printStackTrace(); }
		System.out.println("Failed to get path for current directory - falling back to user's home directory.");
		return System.getProperty("user.dir") + "//FTB Pack Install";
	}
	
	public static String getDynamicStorageLocation() {
		String output = "";
		switch(getCurrentOS()) {
		case WINDOWS:
			output = System.getenv("APPDATA") + "/ftblauncher/";
			break;
		case MACOSX:
			output = System.getProperty("user.home") + "/Library/Application Support/ftblauncher/";
			break;
		case UNIX:
			output = System.getProperty("user.home") + "/.ftblauncher/";
			break;
		default:
			output = System.getProperty("user.dir");
			break;
		}
		return output;
	}

	public static String getJavaDelimiter() {
		if(getCurrentOS() == OS.WINDOWS) {
			return ";";
		} else if(getCurrentOS() == OS.UNIX) {
			return ":";
		} else if(getCurrentOS() == OS.MACOSX) {
			return ":";
		} else {
			return ";";
		}
	}

	public static OS getCurrentOS() {
		String osString = System.getProperty("os.name").toLowerCase();
		if (osString.contains("win")) {
			return OS.WINDOWS;
		} else if (osString.contains("nix") || osString.contains("nux")) {
			return OS.UNIX;
		} else if (osString.contains("mac")) {
			return OS.MACOSX;
		} else {
			return OS.OTHER;
		}
	}

	public static enum OS {
		WINDOWS,
		UNIX,
		MACOSX,
		OTHER,
	}
}
