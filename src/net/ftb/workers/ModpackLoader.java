package net.ftb.workers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import net.ftb.data.ModPack;
import net.ftb.gui.panes.ModpacksPane;
import net.ftb.log.Logger;
import net.ftb.util.AppUtils;
import net.ftb.util.DownloadUtils;
import net.ftb.util.OSUtils;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ModpackLoader extends Thread {
	private ArrayList<String> xmlFiles = new ArrayList<String>();

	public ModpackLoader(ArrayList<String> xmlFiles) {
		this.xmlFiles = xmlFiles;
	}

	@Override
	public void run() {
		int counter = 0;
		for(String xmlFile : xmlFiles) {
			boolean privatePack = !xmlFile.equalsIgnoreCase("modpacks.xml");
			File modPackFile = new File(OSUtils.getDynamicStorageLocation(), "ModPacks" + File.separator + xmlFile);
			try {
				modPackFile.getParentFile().mkdirs();
				DownloadUtils.downloadToFile(new URL(DownloadUtils.getStaticCreeperhostLink(xmlFile)), modPackFile);
			} catch (IOException e) {
				Logger.logWarn("Failed to load modpacks, loading from backup", e);
			}
			Logger.logInfo("Loading modpack information for " + xmlFile + "...");
			InputStream modPackStream = null;
			try {
				modPackStream = new FileInputStream(modPackFile);
			} catch (IOException e) {
				Logger.logWarn("Failed to read modpack file - falling back to direct download", e);
			}
			if(modPackStream == null) {
				try {
					modPackStream = new URL(DownloadUtils.getStaticCreeperhostLink(xmlFile)).openStream();
				} catch (IOException e) {
					Logger.logError("Completely unable to download the modpack file - check your connection", e);
				}
			}
			if(modPackStream != null) {
				Document doc;
				try {
					doc = AppUtils.getXML(modPackStream);
				} catch (Exception e) {
					Logger.logError("Exception reading modpack file", e);
					return;
				}
				if(doc == null) {
					Logger.logError("Error: could not load modpack data!");
					return;
				}
				NodeList modPacks = doc.getElementsByTagName("modpack");
				for(int i = 0; i < modPacks.getLength(); i++) {
					Node modPackNode = modPacks.item(i);
					NamedNodeMap modPackAttr = modPackNode.getAttributes();
					try {
						ModPack.addPack(new ModPack(modPackAttr.getNamedItem("name").getTextContent(), modPackAttr.getNamedItem("author").getTextContent(),
								modPackAttr.getNamedItem("version").getTextContent(), modPackAttr.getNamedItem("logo").getTextContent(),
								modPackAttr.getNamedItem("url").getTextContent(), modPackAttr.getNamedItem("image").getTextContent(),
								modPackAttr.getNamedItem("dir").getTextContent(), modPackAttr.getNamedItem("mcVersion").getTextContent(), 
								modPackAttr.getNamedItem("serverPack").getTextContent(), modPackAttr.getNamedItem("description").getTextContent(),
								modPackAttr.getNamedItem("mods") != null ? modPackAttr.getNamedItem("mods").getTextContent() : "", 
								modPackAttr.getNamedItem("oldVersions") != null ? modPackAttr.getNamedItem("oldVersions").getTextContent() : "",
								modPackAttr.getNamedItem("animation") != null ? modPackAttr.getNamedItem("animation").getTextContent() : "", counter, privatePack, xmlFile));
						counter++;
					} catch (Exception e) {
						Logger.logError(e.getMessage(), e);
					}
				}
				try {
					modPackStream.close();
				} catch (IOException e) { }
			}
		}
		ModpacksPane.loaded = true;
	}
}
