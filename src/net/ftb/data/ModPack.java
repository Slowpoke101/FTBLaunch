package net.ftb.data;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import net.ftb.data.events.ModPackListener;
import net.ftb.gui.LaunchFrame;
import net.ftb.workers.ModpackLoader;

public class ModPack {	
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
			listener.onModPackAdded(pack);
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
	}

	// class stuff
	public ModPack(String name, String author, String version, String logo, String url, String image, String dir, String mcVersion) throws IOException, NoSuchAlgorithmException {
		// Always get this information
		this.name = name;
		this.author = author;
		this.version = version;
		this.dir = dir;
		this.mcVersion = mcVersion;
		this.url = url;
		// Check version files
		File verFile = new File(Settings.getSettings().getInstallPath(), "temp" + File.separator + dir + File.separator + "version");
		if(!upToDate(verFile)){
			URL url_ = new URL(LaunchFrame.getCreeperhostLink(logo));
			this.logo = Toolkit.getDefaultToolkit().createImage(url_);
			BufferedImage tempImg = ImageIO.read(url_);
			ImageIO.write(tempImg, "png", new File(Settings.getSettings().getInstallPath(), "temp" + File.separator + dir + File.separator + logo));
			tempImg.flush();
			url_ = new URL(LaunchFrame.getCreeperhostLink(url));
			URLConnection c = url_.openConnection();
			this.size = c.getContentLength();
			url_ =  new URL(LaunchFrame.getCreeperhostLink(image));
			this.image = Toolkit.getDefaultToolkit().createImage(url_);
			tempImg = ImageIO.read(url_);
			ImageIO.write(tempImg, "png", new File(Settings.getSettings().getInstallPath(), "temp" + File.separator + dir + File.separator + image));
			tempImg.flush();
		} else {
			this.logo = Toolkit.getDefaultToolkit().createImage(Settings.getSettings().getInstallPath() + File.separator + "temp" + File.separator + dir + File.separator + logo);
			URL url_ = new URL(LaunchFrame.getCreeperhostLink(url));
			this.size = url_.openConnection().getContentLength();
			this.image = Toolkit.getDefaultToolkit().createImage(Settings.getSettings().getInstallPath() + File.separator + "temp" + File.separator + dir + File.separator + image);
		}
	}

	private boolean upToDate(File verFile){
		boolean result = false;
		try {
			if(!verFile.exists()){
				verFile.createNewFile();
				result = false;
			}
			BufferedReader in = new BufferedReader(new FileReader(verFile));
			String line;
			if((line = in.readLine()) == null || Integer.parseInt(version) > Integer.parseInt(line)) {
				BufferedWriter out = new BufferedWriter(new FileWriter(verFile));
				out.write(version);
				out.flush();
				out.close();
				result = false;
			} else {
				result = true;
			}
			in.close();
		} catch (IOException e) { e.printStackTrace(); }
		return result;
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
