package net.ftb.updater;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;

import net.ftb.gui.LaunchFrame;
import net.ftb.log.Logger;
import net.ftb.util.AppUtils;
import net.ftb.util.DownloadUtils;
import net.ftb.util.FileUtils;
import net.ftb.util.OSUtils;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;

public class UpdateChecker {
	private int version;
	private int latest;
	public static String verString = "";
	private String downloadAddress = "";

	public UpdateChecker(int version) {
		this.version = version;
		loadInfo();
		try {
			FileUtils.delete(new File(OSUtils.getDynamicStorageLocation(), "updatetemp"));
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
			char[] temp = String.valueOf(latest).toCharArray();
			for(int i = 0; i < (temp.length - 1); i++) {
				verString += temp[i] + ".";
			}
			verString += temp[temp.length - 1];
			downloadAddress = updateAttributes.getNamedItem("downloadURL").getTextContent();
		} catch (MalformedURLException e) { 
		} catch (IOException e) { 
		} catch (SAXException e) { }
	}

	public boolean shouldUpdate() {
		return version < latest;
	}

	public void update() {
		String path = null;
		try {
			path = new File(LaunchFrame.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getCanonicalPath();
			path = URLDecoder.decode(path, "UTF-8");
		} catch (IOException e) { Logger.logError("Couldn't get path to current launcher jar/exe", e); }
		String temporaryUpdatePath = OSUtils.getDynamicStorageLocation() + File.separator + "updatetemp" + File.separator + path.substring(path.lastIndexOf(File.separator) + 1);
		String extension = path.substring(path.lastIndexOf('.') + 1);
		extension = "exe".equalsIgnoreCase(extension) ? extension : "jar";
		try {
			URL updateURL = new URL(DownloadUtils.getCreeperhostLink(downloadAddress + "." + extension));
			File temporaryUpdate = new File(temporaryUpdatePath);
			temporaryUpdate.getParentFile().mkdir();
			FileUtils.downloadToFile(updateURL, temporaryUpdate);
			SelfUpdate.runUpdate(path, temporaryUpdatePath);
		} catch (MalformedURLException e) { Logger.logError("Malformed download URL for launcher update", e);
		} catch (IOException e) { Logger.logError("Failed to download launcher update", e); 
		} catch (NoSuchAlgorithmException e) { }
	}
}
