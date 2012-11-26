package net.ftb.workers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import net.ftb.data.TexturePack;
import net.ftb.gui.panes.TexturepackPane;
import net.ftb.log.Logger;
import net.ftb.util.AppUtils;
import net.ftb.util.DownloadUtils;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class TexturePackLoader extends Thread {
	private static String TEXTUREPACKFILE;

	public TexturePackLoader() { }

	@Override
	public void run() {
		try {
			Logger.logInfo("loading texture pack information...");

			TEXTUREPACKFILE = DownloadUtils.getStaticCreeperhostLink("texturepack.xml");

			Document doc = AppUtils.downloadXML(new URL(TEXTUREPACKFILE));
			if(doc == null) {
				Logger.logError("Error: Could not load texture pack data!");
			}
			NodeList texturePacks = doc.getElementsByTagName("texturepack");
			for(int i = 0; i < texturePacks.getLength(); i++) {
				Node texturePack = texturePacks.item(i);
				NamedNodeMap textureAttr = texturePack.getAttributes();
				TexturePack.addTexturePack(new TexturePack(textureAttr.getNamedItem("name").getTextContent(), textureAttr.getNamedItem("author").getTextContent(),
						textureAttr.getNamedItem("version").getTextContent(), textureAttr.getNamedItem("url").getTextContent(),
						textureAttr.getNamedItem("logo").getTextContent(), textureAttr.getNamedItem("image").getTextContent(),
						textureAttr.getNamedItem("mcversion").getTextContent(), textureAttr.getNamedItem("compatible").getTextContent(), 
						textureAttr.getNamedItem("description").getTextContent(), i));
			}
			TexturepackPane.loaded = true;
		} catch (MalformedURLException e) { 
		} catch (IOException e) { 
		} catch (SAXException e) { 
		} catch (NoSuchAlgorithmException e) { 
		} catch (DOMException e) { }
	}
}
