package net.ftb.data;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.ftb.gui.LaunchFrame;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ModPack {
	
	// static stuff
	private static ArrayList<ModPack> packs = new ArrayList<ModPack>();
	
	private static String MODPACKSFILE;
	
	public static void LoadAll() throws NoSuchAlgorithmException {
	
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		Document doc = null;
		
		
		
		/********************* Offline XML loading********************************
		File cpack;
		cpack = new File(PathUtils.combine(OSUtils.getDefInstallPath(),
				"/CPACKS/modpacks.xml"));
		if (cpack.exists())
		{
			
			System.out.println("loading Custom modpack information...");
			
				
			try {
				doc = docFactory.newDocumentBuilder().parse(cpack);
			} catch (SAXException e) { e.printStackTrace(); return;
			} catch (IOException e) {
				e.printStackTrace();
				return;
			} catch (ParserConfigurationException e) { e.printStackTrace(); return; }
			
			
		}else {
		********************* Offline XML loading********************************/
		System.out.println("loading modpack information...");
		
		MODPACKSFILE = LaunchFrame.getCreeperhostLink("modpacks.xml");
		try {
			doc = docFactory.newDocumentBuilder().parse(MODPACKSFILE);
		} catch (SAXException e) { e.printStackTrace(); return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} catch (ParserConfigurationException e) { e.printStackTrace(); return; }
	//	}
		
		
		
		
		if (doc == null) {
			return;
		}
		
		NodeList modPacks = doc.getElementsByTagName("modpack");
		
		for (int i = 0; i < modPacks.getLength(); i++) {
			Node modPack = modPacks.item(i);
			NamedNodeMap modPackAttr = modPack.getAttributes();
			
			try {
				packs.add(new ModPack(
						modPackAttr.getNamedItem("name").getTextContent(),
						modPackAttr.getNamedItem("author").getTextContent(),
						modPackAttr.getNamedItem("version").getTextContent(),
						modPackAttr.getNamedItem("logo").getTextContent(),
						modPackAttr.getNamedItem("url").getTextContent(),
						modPackAttr.getNamedItem("image").getTextContent(),
						modPackAttr.getNamedItem("dir").getTextContent(),
						modPackAttr.getNamedItem("mcVersion").getTextContent()
					));
			} catch (DOMException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static ArrayList<ModPack> getPackArray() {
		return packs;
	}
	
	public static ModPack getPack(int i) {
		return packs.get(i);
	}
	
	public static void main(String[] args) {
		try {
			LoadAll();
			System.out.println(packs.get(1).getName());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	// class stuff
	
	private String name;
	private String author;
	private String version;
	private Image logo;
	private String url;
	private Image image;
	private String dir;
	private String mcVersion;
	private String info = "This is the info until there is an actual info thingy";
	private int size;
	
	public ModPack(String name, String author, String version, String logo, String url, String image, String dir, String mcVersion) throws IOException, NoSuchAlgorithmException {
		this.name = name;
		this.author = author;
		this.version = version;
		URL logoURL = new URL(LaunchFrame.getCreeperhostLink(logo));
		this.logo = Toolkit.getDefaultToolkit().createImage(logoURL);
		this.url = url;
		URL url_ = new URL(LaunchFrame.getCreeperhostLink(url));
		URLConnection c = url_.openConnection();
		this.size = c.getContentLength();
		URL imageUrl = new URL(LaunchFrame.getCreeperhostLink(image));
		this.image = Toolkit.getDefaultToolkit().createImage(imageUrl);
		this.dir = dir;
		this.mcVersion = mcVersion;
	}
	
	public String getName() {
		return name;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public String getVersion() {
		return version;
	}
	
	public Image getLogo() {
		return logo;
	}
	
	public String getUrl() {
		return url;
	}
	
	public Image getImage() {
		return image;
	}
	
	public String getDir() {
		return dir;
	}
	
	public String getMcVersion() {
		return mcVersion;
	}
	
	public String getInfo() {
		return info;
	}
	
	public int getSize() {
		return size;
	}
}
