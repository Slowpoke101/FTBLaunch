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
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import net.ftb.data.events.ModPackListener;
import net.ftb.gui.LaunchFrame;
import net.ftb.util.OSUtils;
import net.ftb.workers.ModpackLoader;

public class ModPack {	
	private String name, author, version, url, dir, mcVersion, serverUrl, logoName, imageName, info;
	private Image logo, image;
	private int size, index;

	private final static ArrayList<ModPack> packs = new ArrayList<ModPack>();

	/*
	 * List of Listeners that will be informed if a modpack was added
	 */
	private static List<ModPackListener> listeners = new ArrayList<ModPackListener>();

	/*
	 * Invoking async Load of Modpacks
	 */
	public static void loadAll() {
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

	public ModPack(String name, String author, String version, String logo, String url, String image, String dir, String mcVersion, String serverUrl, String info, int idx) throws IOException, NoSuchAlgorithmException {
		index = idx;
		this.name = name;
		this.author = author;
		this.version = version;
		this.dir = dir;
		this.mcVersion = mcVersion;
		this.url = url;
		this.serverUrl = serverUrl;
		logoName = logo;
		imageName = image;
		this.info = info;
		String installPath = OSUtils.getDynamicStorageLocation();
		File verFile = new File(installPath, "temp" + File.separator + dir + File.separator + "version");
		URL url_;
		if(!upToDate(verFile)) {
			url_ = new URL(LaunchFrame.getCreeperhostLink(logo));
			this.logo = Toolkit.getDefaultToolkit().createImage(url_);
			BufferedImage tempImg = ImageIO.read(url_);
			ImageIO.write(tempImg, "png", new File(installPath, "temp" + File.separator + dir + File.separator + logo));
			tempImg.flush();
			url_ =  new URL(LaunchFrame.getCreeperhostLink(image));
			this.image = Toolkit.getDefaultToolkit().createImage(url_);
			tempImg = ImageIO.read(url_);
			ImageIO.write(tempImg, "png", new File(installPath, "temp" + File.separator + dir + File.separator + image));
			tempImg.flush();
		} else {
			this.logo = Toolkit.getDefaultToolkit().createImage(installPath + File.separator + "temp" + File.separator + dir + File.separator + logo);
			this.image = Toolkit.getDefaultToolkit().createImage(installPath + File.separator + "temp" + File.separator + dir + File.separator + image);
		}
		url_ = new URL(LaunchFrame.getCreeperhostLink(url));
		size = url_.openConnection().getContentLength();
	}

	private boolean upToDate(File verFile) {
		boolean result = false;
		try {
			if(!verFile.exists()) {
				new File(OSUtils.getDynamicStorageLocation(), "temp" + File.separator + dir).mkdirs();
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
			}
			in.close();
		} catch (IOException e) { e.printStackTrace(); }
		return result;
	}

	public int getIndex() {
		return index;
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

	public String getServerUrl() {
		return serverUrl;
	}

	public String getLogoName() {
		return logoName;
	}

	public String getImageName() {
		return imageName;
	}
}
