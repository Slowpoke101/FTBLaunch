package net.ftb.workers;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import net.ftb.data.ModPack;
import net.ftb.gui.LaunchFrame;
import net.ftb.log.Logger;


/*
 * Async ModPack loader
 * 
 * 
 */
public class ModpackLoader extends Thread {
	private static String MODPACKSFILE;

	public ModpackLoader() { }

	public void run() {
		try {
			Logger.logInfo("starting modpack information loading");

			MODPACKSFILE = LaunchFrame.getCreeperhostLink("modpacks.xml");

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();

			Document doc = null;
			try {
				doc = docFactory.newDocumentBuilder().parse(MODPACKSFILE);
			} catch (SAXException e) {
				Logger.logError("could not read modpackinfo: "+e.getMessage(),e);
				return;
			} catch (IOException e) {
				Logger.logError("could not read modpackinfo: "+e.getMessage(),e);
				return;
			} catch (ParserConfigurationException e) {
				Logger.logError("could not read modpackinfo: "+e.getMessage(),e);
				return;
			}

			if (doc == null) {
				Logger.logError("could not read modpackinfo: doc==null");
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
							modPackAttr.getNamedItem("dir").getTextContent(), modPackAttr.getNamedItem("mcVersion").getTextContent(), modPackAttr.getNamedItem("serverPack").getTextContent()));
				} catch (DOMException e) {
					Logger.logError("could not read modpackinfo: "+e.getMessage(),e);
				} catch (IOException e) {
					Logger.logError("could not read modpackinfo: "+e.getMessage(),e);
				}
			}
		} catch (NoSuchAlgorithmException e1) {
			Logger.logError("could not load modpackinfo: "+e1.getMessage(),e1);
		}
	}
}
