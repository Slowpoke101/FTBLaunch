package net.ftb.workers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.ftb.data.ModPack;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.panes.ModpacksPane;
import net.ftb.log.Logger;
import net.ftb.util.OSUtils;

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
			new File(OSUtils.getDynamicStorageLocation() + File.separator + "ModPacks" + File.separator).mkdirs();
			downloadUrl(OSUtils.getDynamicStorageLocation() + File.separator + "ModPacks" + File.separator + "modpacks.xml", "https://dl.dropbox.com/u/2405919/modpacks.xml");
		} catch (IOException e2) {
			System.out.println("Failed to load modpacks, loading from backup");
		}

		try {
			Logger.logInfo("loading modpack information...");

			MODPACKSFILE = OSUtils.getDynamicStorageLocation() + File.separator + "ModPacks" + File.separator + "modpacks.xml";

			Document doc = null;
			try {
				DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
				doc = docBuilder.parse(MODPACKSFILE);
			} catch (SAXException e) {
				Logger.logError("Exception reading modpackfile", e);
				return;
			} catch (IOException e) {
				Logger.logError("Exception reading modpackfile", e);
				return;
			} catch (ParserConfigurationException e) {
				Logger.logError("Exception reading modpackfile", e);
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

	public void downloadUrl(String filename, String urlString) throws IOException {
		BufferedInputStream in = null;
		FileOutputStream fout = null;
		try {
			in = new BufferedInputStream(new URL(urlString).openStream());
			fout = new FileOutputStream(filename);

			byte data[] = new byte[1024];
			int count;
			while ((count = in.read(data, 0, 1024)) != -1) {
				fout.write(data, 0, count);
			}
		} finally {
			if (in != null) {
				in.close();
			}
			if (fout != null) {
				fout.flush();
				fout.close();
			}	
		}
	}
}
