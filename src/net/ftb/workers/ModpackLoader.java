package net.ftb.workers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import net.ftb.data.ModPack;
import net.ftb.gui.panes.ModpacksPane;
import net.ftb.log.Logger;
import net.ftb.util.DownloadUtils;
import net.ftb.util.AppUtils;
import net.ftb.util.OSUtils;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ModpackLoader extends Thread {
	@Override
	public void run() {

		File modPackFile = new File(OSUtils.getDynamicStorageLocation() + File.separator + "Modpacks" + File.separator + "modpacks.xml");

		try {
			modPackFile.getParentFile().mkdirs();
			DownloadUtils.downloadToFile(new URL("https://dl.dropbox.com/u/40374207/modpacks.xml"), modPackFile);
		} catch (IOException e) {
			Logger.logWarn("Failed to load modpacks, loading from backup", e);
		}
		
		try {
			Logger.logInfo("loading modpack information...");
			Document doc;
			InputStream modPackStream = null;

			try {
				modPackStream = new FileInputStream(modPackFile);
			} catch(IOException e) {
				Logger.logWarn("Failed to read modpackfile - falling back to direct download", e);
			}

			if (modPackStream == null) {
				try {
					modPackStream = new URL(DownloadUtils.getStaticCreeperhostLink("modpacks.xml")).openStream();
				} catch(IOException e) {
					Logger.logError("Completely unable to download the modpackfile - check your connection", e);
					return;
				}
			}
			
			try {
				doc = AppUtils.getXML(modPackStream);
			} catch (SAXException e) {
				Logger.logError("Exception reading modpackfile", e);
				return;
			} catch (IOException e) {
				Logger.logError("Exception reading modpackfile", e);
				return;
			}
			if (doc == null) {
				Logger.logError("Error: could not load modpackdata!");
				return;
			}

			NodeList modPacks = doc.getElementsByTagName("modpack");

			for (int i = 0; i < modPacks.getLength(); i++) {
				Node modPackNode = modPacks.item(i);
				NamedNodeMap modPackAttr = modPackNode.getAttributes();
				try {
					ModPack.addPack(new ModPack(modPackAttr.getNamedItem("name").getTextContent(), modPackAttr.getNamedItem("author").getTextContent(),
							modPackAttr.getNamedItem("version").getTextContent(), modPackAttr.getNamedItem("logo").getTextContent(),
							modPackAttr.getNamedItem("url").getTextContent(), modPackAttr.getNamedItem("image").getTextContent(),
							modPackAttr.getNamedItem("dir").getTextContent(), modPackAttr.getNamedItem("mcVersion").getTextContent(), 
							modPackAttr.getNamedItem("serverPack").getTextContent(), modPackAttr.getNamedItem("description").getTextContent(),
							modPackAttr.getNamedItem("mods") != null ? modPackAttr.getNamedItem("mods").getTextContent() : "", i));
				} catch (DOMException e) { 
				} catch (IOException e) { }
			}
			ModpacksPane.loaded = true;
		} catch (NoSuchAlgorithmException e1) { }
	}
}
