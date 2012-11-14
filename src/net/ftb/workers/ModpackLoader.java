package net.ftb.workers;

import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import net.ftb.data.ModPack;
import net.ftb.gui.panes.ModpacksPane;
import net.ftb.log.Logger;
import net.ftb.util.AppUtils;

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

			MODPACKSFILE = "https://dl.dropbox.com/u/2405919/modpack.xml"; // LaunchFrame.getCreeperhostLink("modpacks.xml");

			Document doc;
			try {
				doc = AppUtils.downloadXML(new URL(MODPACKSFILE));
			} catch (SAXException e) {
				Logger.logError("Exception during reading modpackfile",e);
				return;
			} catch (IOException e) {
				Logger.logError("Exception during reading modpackfile",e);
				return;
			}

			if (doc == null) {
				Logger.logError("Error: could not load modpackdata!");
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
}
