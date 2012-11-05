package net.ftb.updater;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import net.ftb.gui.LaunchFrame;
import net.ftb.log.Logger;

public enum Channel {
	RELEASE("release.xml", "Standard Testing Channel"),
	DEVELOPMENT("http://nallar.me/ftb/b/updateinfo.xml", "Unstable Development Channel"),
	NONE(null, "Disable automatic updates");

	private static Channel defaultChannel = NONE;
	private final String title;
	public final URL updateURL;

	/*
	 * Unless this is being used for people to test dev builds (which I don't think is needed)
	 * then I don't think we'll require a Channel.java
	 */
	private Channel(String updateAddress) {
		this(updateAddress, null);
	}

	private Channel(String updateAddress, String title) {
		this.title = title;
		URL url = null;
		if (updateAddress != null && !updateAddress.isEmpty()) {
			try {
				if (updateAddress.indexOf("http") != 0) {
					updateAddress = LaunchFrame.getCreeperhostLink(updateAddress);
				}
				url = new URL(updateAddress);
			} catch (MalformedURLException e) {
				Logger.logError("Invalid versionURL for " + name() + ": " + e.getMessage());
			} catch (NoSuchAlgorithmException e) {
				Logger.logError("Failed to build creeperhost link for channel " + name(), e);
			}
		}
		this.updateURL = url;
	}

	@Override
	public String toString() {
		if (title == null || title.isEmpty()) {
			return name();
		}
		return title;
	}

	public static Channel fromName(String name) {
		for (Channel c : values()) {
			if (c.name().equals(name)) {
				return c;
			}
		}
		return defaultChannel;
	}
}
