package net.ftb.workers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import net.ftb.data.Map;
import net.ftb.gui.panes.MapsPane;
import net.ftb.log.Logger;
import net.ftb.util.AppUtils;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MapLoader extends Thread {
	private static String MAPFILE;

	public MapLoader() { }

	@Override
	public void run() {
		try {
			Logger.logInfo("loading map information...");

//			MAPFILE = LaunchFrame.getCreeperhostLink("maps.xml");
			MAPFILE = "https://dl.dropbox.com/u/2405919/maps.xml";

			Document doc = AppUtils.downloadXML(new URL(MAPFILE));
			if(doc == null) {
				Logger.logError("Error: Could not load map data!");
			}
			NodeList maps = doc.getElementsByTagName("map");
			for(int i = 0; i < maps.getLength(); i++) {
				Node map = maps.item(i);
				NamedNodeMap mapAttr = map.getAttributes();
				Map.addMap(new Map(mapAttr.getNamedItem("name").getTextContent(), mapAttr.getNamedItem("author").getTextContent(),
						mapAttr.getNamedItem("version").getTextContent(), mapAttr.getNamedItem("url").getTextContent(),
						mapAttr.getNamedItem("logo").getTextContent(), mapAttr.getNamedItem("image").getTextContent(),
						mapAttr.getNamedItem("compatible").getTextContent(), mapAttr.getNamedItem("mcversion").getTextContent(), 
						mapAttr.getNamedItem("mapname").getTextContent(), i));
			}
			MapsPane.loaded = true;
		} catch (MalformedURLException e) { 
		} catch (IOException e) { 
		} catch (SAXException e) { 
		} catch (NoSuchAlgorithmException e) { 
		} catch (DOMException e) { }
	}
}
