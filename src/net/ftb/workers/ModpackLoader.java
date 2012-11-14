package net.ftb.workers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import net.ftb.data.ModPack;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.panes.ModpacksPane;
import net.ftb.log.Logger;
import net.ftb.util.AppUtils;
import net.ftb.util.OSUtils;
import net.ftb.util.PathUtils;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ModpackLoader extends Thread {
	private static String MODPACKSFILE;

	public ModpackLoader() { }

	@Override
	public void run() {
		try {
			Logger.logInfo("loading modpack information...");

			MODPACKSFILE = LaunchFrame.getStaticCreeperhostLink("modpacks.xml");

			Document doc;
			try {
				doc = AppUtils.downloadXML(new URL(MODPACKSFILE));
			} catch (SAXException e) {
				Logger.logError("Exception reading modpackfile", e);
				loadOfflinePacks();
				return;
			} catch (IOException e) {
				Logger.logError("Exception reading modpackfile", e);
				loadOfflinePacks();
				return;
			}

			if (doc == null) {
				Logger.logError("Error: could not load modpackdata!");
				loadOfflinePacks();
				return;
			}

			NodeList modPacks = doc.getElementsByTagName("modpack");

			for (int i = 0; i < modPacks.getLength(); i++) {
				Node modPack = modPacks.item(i);
				NamedNodeMap modPackAttr = modPack.getAttributes();

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
	private void loadOfflinePacks() {
		Logger.logInfo("loading modpack information from offline modpacks.xml...");
		Document doc;
		try {
			doc = AppUtils.readOfflineXML(new File(PathUtils.combine(OSUtils.getDefInstallPath(),"modpacks.xml")));
		} catch (SAXException e) {
			Logger.logError("Exception during reading modpackfile from offline modpacks.xml", e);
			return;
		} catch (IOException e) {
			Logger.logError("Exception during reading modpackfile from offline modpacks.xml", e);
			return;
		}

		if (doc == null) {
			Logger.logError("Error: could not load modpackdata from modpacks.xml!");
			return;
		}

		NodeList modPacks = doc.getElementsByTagName("modpack");

		for (int i = 0; i < modPacks.getLength(); i++) {
			Node modPack = modPacks.item(i);
			NamedNodeMap modPackAttr = modPack.getAttributes();

			try {
				ModPack.addPack(new ModPack(
						modPackAttr.getNamedItem("name").getTextContent(),
						modPackAttr.getNamedItem("author").getTextContent(),
						modPackAttr.getNamedItem("version").getTextContent(),
						modPackAttr.getNamedItem("logo").getTextContent(),
						modPackAttr.getNamedItem("url").getTextContent(),
						modPackAttr.getNamedItem("image").getTextContent(),
						modPackAttr.getNamedItem("dir").getTextContent(),
						modPackAttr.getNamedItem("mcVersion").getTextContent(),
						modPackAttr.getNamedItem("serverPack").getTextContent(),
						modPackAttr.getNamedItem("description")
								.getTextContent(), modPackAttr
								.getNamedItem("mods") != null ? modPackAttr
								.getNamedItem("mods").getTextContent() : "", i));
			} catch (DOMException e) {
			} catch (IOException e) {
			} catch (NoSuchAlgorithmException e) {
			}
		}
		ModpacksPane.loaded = true;

	}
}
