package net.ftb.workers;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.ftb.data.ModPack;
import net.ftb.gui.LaunchFrame;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
			System.out.println("loading modpack information...");

			MODPACKSFILE = LaunchFrame.getCreeperhostLink("modpacks.xml");

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();

			Document doc;
			try {
				doc = docFactory.newDocumentBuilder().parse(MODPACKSFILE);
			} catch (SAXException e) {
				e.printStackTrace();
				return;
			} catch (IOException e) {
				e.printStackTrace();
				return;
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
				return;
			}

			if (doc == null) {
				System.out.println("Error: could not load modpackdata!");
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
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}
	}
}
