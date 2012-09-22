package net.ftb.data;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ModPack {
	
	// static stuff
	private static ArrayList<ModPack> _packs = new ArrayList<ModPack>();
	
	private static final String MODPACKSURL = "http://dl.dropbox.com/u/2014606/ftb/modpacks.xml";
	
	public static void LoadAll() {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		
		Document doc = null;
		try {
			doc = docFactory.newDocumentBuilder().parse(MODPACKSURL);
		} catch (SAXException e) { e.printStackTrace(); return;
		} catch (IOException e) {
			// TODO Add Fallback Methods
			e.printStackTrace();
			return;
		} catch (ParserConfigurationException e) { e.printStackTrace(); return; }
		
		if (doc == null) {
			return;
		}
		
		NodeList modPacks = doc.getElementsByTagName("modpack");
		
		for (int i = 0; i < modPacks.getLength(); i++) {
			Node modPack = modPacks.item(i);
			NamedNodeMap modPackAttr = modPack.getAttributes();
			
			try {
				_packs.add(new ModPack(modPackAttr.getNamedItem("name").getTextContent(),
						modPackAttr.getNamedItem("url").getTextContent(),
						modPackAttr.getNamedItem("image").getTextContent(),
						modPackAttr.getNamedItem("dir").getTextContent()));
			} catch (DOMException e) { e.printStackTrace();
			} catch (IOException e) { e.printStackTrace(); }
		}
		
	}
	
	public static void main(String[] args) {
		LoadAll();
		System.out.println(_packs.get(0).getName());
	}
	
	// class stuff
	
	private String name;
	private URL url;
	private URLConnection connection;
	private Image image;
	private int size;
	private String dir;
	
	public ModPack(String name, String url, String image, String dir) throws IOException {
		this.name = name;
		this.url = new URL(url);
		URL imageUrl = new URL(image);
		this.image = Toolkit.getDefaultToolkit().createImage(imageUrl);
		connection = this.url.openConnection();
		this.size = connection.getContentLength();
		this.dir = dir;
	}
	
	public String getName() {
		return name;
	}
	
	public URL getUrl() {
		return url;
	}
	
	public Image getImage() {
		return image;
	}
	
	public int getSize() {
		return size;
	}
	
	public String getDir() {
		return dir;
	}
}
