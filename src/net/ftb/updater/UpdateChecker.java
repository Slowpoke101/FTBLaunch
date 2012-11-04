package net.ftb.updater;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import net.ftb.data.Settings;
import net.ftb.gui.LaunchFrame;
import net.ftb.log.Logger;
import net.ftb.util.AppUtils;
import net.ftb.util.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;

public class UpdateChecker {
	private class UpdateInfo {
		public int currentBuild = -1;
		public URL downloadURL = null;
	}

	private Channel channel;
	private int version;
	private UpdateInfo updateInfo;

	public UpdateChecker(Channel channel, int version) {
		this.channel = channel;
		this.version = version;
		this.updateInfo = loadInfo();
		try {
			FileUtils.delete(new File(Settings.getSettings().getInstallPath() + File.separator + "updatetemp"));
		} catch (Exception ignored) {
		}
	}

	private UpdateInfo loadInfo() {
		UpdateInfo updateInfo = new UpdateInfo();
		if (channel.updateURL == null) {
			return updateInfo;
		}
		try {
			Document d = AppUtils.downloadXML(channel.updateURL);
			NamedNodeMap updateInfoAttributes = d.getDocumentElement().getAttributes();
			updateInfo.currentBuild = Integer.parseInt(updateInfoAttributes.getNamedItem("currentBuild").getTextContent());
			String downloadAddress = updateInfoAttributes.getNamedItem("downloadURL").getTextContent();
			if (downloadAddress.indexOf("http") != 0) {
				downloadAddress = LaunchFrame.getCreeperhostLink(downloadAddress);
			}
			updateInfo.downloadURL = new URL(downloadAddress);
		} catch (IOException e) {
			Logger.logError("Failed to load update information", e);
		} catch (SAXException e) {
			Logger.logError("Failed to load update information", e);
		} catch (NoSuchAlgorithmException e) {
			Logger.logError("Failed to build creeperhost link for download", e);
		}
		return updateInfo;
	}

	public boolean shouldUpdate() {
		return version < updateInfo.currentBuild;
	}

	public void update() {
		String path = null;
		try {
			path = new File(LaunchFrame.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getCanonicalPath();
		} catch (IOException e) {
			Logger.logError("Couldn't get path to current launcher jar/exe", e);
		}
		String temporaryUpdatePath = Settings.getSettings().getInstallPath() + File.separator + "updatetemp" + File.separator + path.substring(path.lastIndexOf(File.separator) + 1);
		String extension = path.substring(path.lastIndexOf('.') + 1);
		extension = "exe".equalsIgnoreCase(extension) ? extension : "jar";

		try {
			URL updateURL = new URL(updateInfo.downloadURL.toString() + "." + extension);
			File temporaryUpdate = new File(temporaryUpdatePath);
			temporaryUpdate.getParentFile().mkdir();
			FileUtils.downloadToFile(updateURL, temporaryUpdate);
			SelfUpdate.runUpdate(path, temporaryUpdatePath);
		} catch (MalformedURLException e) {
			Logger.logError("Malformed download URL for launcher update", e);
		} catch (IOException e) {
			Logger.logError("Failed to download launcher update", e);
		}
	}
}
