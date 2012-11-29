package net.ftb.updater;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import net.ftb.data.Settings;
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
	private int latest, latestDev;
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
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new URL("http://nallar.me/buildservice/job/FTB%20Launcher/lastStableBuild/buildNumber").openStream()));
			latestDev = Integer.parseInt(in.readLine());
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
		if(!Settings.getSettings().getUseDevBuild())
			return version < latest;
		else{
			verString = "Dev-" + latestDev;
			try {
				Scanner scanner;
				if(new File(Settings.getSettings().getInstallPath(), "devVersion").exists()){
					scanner = new Scanner(new File(Settings.getSettings().getInstallPath(), "devVersion"));
					if(scanner.hasNextInt())
						return scanner.nextInt() < latestDev;
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			return true;
		}
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
			File temporaryUpdate;
			URL updateURL;
			if(!Settings.getSettings().getUseDevBuild()){
				updateURL = new URL(DownloadUtils.getCreeperhostLink(downloadAddress + "." + extension));
				temporaryUpdate = new File(temporaryUpdatePath);
			}else{
				updateURL = new URL( "http://nallar.me/buildservice/job/FTB%20Launcher/lastSuccessfulBuild/artifact/target/FTB_Launcher." + extension);
				File devVersion = new File(Settings.getSettings().getInstallPath(), "devVersion");
				BufferedWriter out = new BufferedWriter(new FileWriter(devVersion));
				out.write(Integer.toString(latestDev));
				out.flush();
				out.close();
				temporaryUpdate = new File(temporaryUpdatePath);
			}
			temporaryUpdate.getParentFile().mkdir();
			FileUtils.downloadToFile(updateURL, temporaryUpdate);
			SelfUpdate.runUpdate(path, temporaryUpdatePath);
		} catch (MalformedURLException e) { Logger.logError("Malformed download URL for launcher update", e);
		} catch (IOException e) { Logger.logError("Failed to download launcher update", e); 
		} catch (NoSuchAlgorithmException e) { }

	}
}
