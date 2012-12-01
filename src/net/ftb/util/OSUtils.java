package net.ftb.util;

import java.io.File;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.Enumeration;

import net.ftb.gui.LaunchFrame;
import net.ftb.log.Logger;

public class OSUtils {
	private static byte[] cachedMacAddress;

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

	public static enum OS {
		WINDOWS,
		UNIX,
		MACOSX,
		OTHER,
	}

	public static byte[] getMacAddress() {
		if(cachedMacAddress != null && cachedMacAddress.length >= 10) {
			return cachedMacAddress;
		}
		try {
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			while(networkInterfaces.hasMoreElements()) {
				NetworkInterface network = networkInterfaces.nextElement();
				byte[] mac = network.getHardwareAddress();
				if(mac != null && mac.length > 0) {
					cachedMacAddress = new byte[mac.length * 10];
					for(int i = 0; i < cachedMacAddress.length; i++) {
						cachedMacAddress[i] = mac[i - (Math.round(i / mac.length) * mac.length)];
					}
					return cachedMacAddress;
				}
			}
		} catch (SocketException e) {
			Logger.logWarn("Failed to get MAC address, using default logindata key", e);
		}
		return new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
	}
}
