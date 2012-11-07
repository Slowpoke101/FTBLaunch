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
	private Channel channel;
	private int version;
	private int latest;
	private URL downloadUrl;

	public UpdateChecker(int version) {
		this.version = version;
		loadInfo();
		try {
			FileUtils.delete(new File(Settings.getSettings().getInstallPath() + File.separator + "updatetemp"));
		} catch (Exception ignored) { }
	}

	public UpdateChecker(Channel channel, int version) {
		this.channel = channel;
		this.version = version;
		loadInfo();
		try {
			FileUtils.delete(new File(Settings.getSettings().getInstallPath() + File.separator + "updatetemp"));
		} catch (Exception ignored) { }
	}

	private void loadInfo() {
		try {
			Document doc;
			// TODO: Maybe swap this over to being hosted on creeper host
			doc = AppUtils.downloadXML(new URL("http://launcher.feed-the-beast.com/version.xml"));
			if(doc == null) {
				return;
			}
			NamedNodeMap updateAttributes = doc.getDocumentElement().getAttributes();
			latest = Integer.parseInt(updateAttributes.getNamedItem("currentBuild").getTextContent());
			String downloadAddress = updateAttributes.getNamedItem("downloadURL").getTextContent();
			if (downloadAddress.indexOf("http") != 0) {
				// TODO: Make this link work, aka upload the newest launcher onto creeperhost. 
				// Will be named FTB_Launcher.exe or FTB_Launcher.jar
				downloadAddress = LaunchFrame.getCreeperhostLink(downloadAddress);
			}
			downloadUrl = new URL(downloadAddress);
		} catch (MalformedURLException e) { e.printStackTrace();
		} catch (IOException e) { e.printStackTrace();
		} catch (SAXException e) { e.printStackTrace();
		} catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
	}

	public boolean shouldUpdate() {
		return version < latest;
	}

	public void update() {
		String path = null;
		try {
			path = new File(LaunchFrame.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getCanonicalPath();
		} catch (IOException e) { Logger.logError("Couldn't get path to current launcher jar/exe", e); }
		String temporaryUpdatePath = Settings.getSettings().getInstallPath() + File.separator + "updatetemp" + File.separator + path.substring(path.lastIndexOf(File.separator) + 1);
		String extension = path.substring(path.lastIndexOf('.') + 1);
		extension = "exe".equalsIgnoreCase(extension) ? extension : "jar";

		try {
			URL updateURL = new URL(downloadUrl.toString() + "." + extension);
			File temporaryUpdate = new File(temporaryUpdatePath);
			temporaryUpdate.getParentFile().mkdir();
			FileUtils.downloadToFile(updateURL, temporaryUpdate);
			SelfUpdate.runUpdate(path, temporaryUpdatePath);
		} catch (MalformedURLException e) { Logger.logError("Malformed download URL for launcher update", e);
		} catch (IOException e) { Logger.logError("Failed to download launcher update", e); }
	}
}
