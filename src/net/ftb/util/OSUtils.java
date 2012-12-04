package net.ftb.util;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.CodeSource;

import net.ftb.gui.LaunchFrame;
import net.ftb.log.Logger;

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
		} catch (URISyntaxException e) {
			Logger.logError(e.getMessage(), e);
		}
		Logger.logWarn("Failed to get path for current directory - falling back to user's home directory.");
		return System.getProperty("user.dir") + "//FTB Pack Install";
	}

	public static String getDynamicStorageLocation() {
		switch(getCurrentOS()) {
		case WINDOWS:
			return System.getenv("APPDATA") + "/ftblauncher/";
		case MACOSX:
			return System.getProperty("user.home") + "/Library/Application Support/ftblauncher/";
		case UNIX:
			return System.getProperty("user.home") + "/.ftblauncher/";
		default:
			return getDefInstallPath() + "/temp/";
		}
	}

	public static String getJavaDelimiter() {
		switch(getCurrentOS()) {
		case WINDOWS:
			return ";";
		case UNIX:
			return ":";
		case MACOSX:
			return ":";
		default:
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

	/**
	 * Opens the given URL in the default browser
	 * @param url The URL
	 */
	public static void browse(String url) {
		try {
			if (Desktop.isDesktopSupported()) {
				Desktop.getDesktop().browse(new URI(url));
			} else if (getCurrentOS() == OS.UNIX) {
				// Work-around to support non-GNOME Linux desktop environments with xdg-open installed
				if (new File("/usr/bin/xdg-open").exists() || new File("/usr/local/bin/xdg-open").exists()) {
					new ProcessBuilder("xdg-open", url).start();
				}
			}
		} catch (Exception e) {
			Logger.logError("Could not open link", e);
		}
	}

	/**
	 * Opens the given path with the default application
	 * @param path The path
	 */
	public static void open(File path) {
		if (!path.exists()) {
			return;
		}
		try {
			if (Desktop.isDesktopSupported()) {
				Desktop.getDesktop().open(path);
			} else if (getCurrentOS() == OS.UNIX) {
				// Work-around to support non-GNOME Linux desktop environments with xdg-open installed
				if (new File("/usr/bin/xdg-open").exists() || new File("/usr/local/bin/xdg-open").exists()) {
					new ProcessBuilder("xdg-open", path.toString()).start();
				}
			}
		} catch (Exception e) {
			Logger.logError("Could not open file", e);
		}
	}

	public static enum OS {
		WINDOWS,
		UNIX,
		MACOSX,
		OTHER,
	}
}
