package net.ftb.data;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.ftb.data.events.ModPackListener;
import net.ftb.gui.LaunchFrame;
import net.ftb.workers.ModpackLoader;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ModPack {
	
	// static stuff
	
	
	private static ArrayList<ModPack> packs = new ArrayList<ModPack>();
	
	/*
	 * List of Listeners that will be informed if a modpack was added
	 */
	private static List<ModPackListener> listeners = new ArrayList<ModPackListener>();
	
	
	/*
	 * Invoking async Load of Modpacks
	 */
	
	public static void LoadAll() {
		ModpackLoader loader = new ModpackLoader();
		loader.start();
	
	}
	
	/*
	 * Add a Listener that will be informed if a pack has been added
	 */
	public static void addListener(ModPackListener listener) {
		listeners.add(listener);
	}
	
	/*
	 * Function to add a Modpack to the Model (used by the ModPackLoader)
	 * this will also inform listeners.
	 */
	public static void addPack(ModPack pack) {
		synchronized (packs) {
			packs.add(pack);
		}
		for (ModPackListener listener : listeners) {
			listener.onMobPackAdded(pack);
		}
	}
	
	public static ArrayList<ModPack> getPackArray() {
		return packs;
	}
	
	public static ModPack getPack(int i) {
		return packs.get(i);
	}
	
	/*
	 * Test Function, no use in production
	 */
	public static void main(String[] args) {
	
			LoadAll();
		//	System.out.println(packs.get(1).getName());
		
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
