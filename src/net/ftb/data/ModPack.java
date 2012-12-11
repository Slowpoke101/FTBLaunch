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
import net.ftb.gui.panes.ModpacksPane;
import net.ftb.log.Logger;
import net.ftb.util.DownloadUtils;
import net.ftb.util.OSUtils;
import net.ftb.workers.ModpackLoader;

public class ModPack {	
	private String name, author, version, url, dir, mcVersion, serverUrl, logoName, imageName, info, animation, sep = File.separator;
	private String[] mods, oldVersions;
	private Image logo, image;
	private int index;
	private boolean updated = false;
	private final static ArrayList<ModPack> packs = new ArrayList<ModPack>();
	private static List<ModPackListener> listeners = new ArrayList<ModPackListener>();

	/**
	 * Loads the modpack.xml and adds it to the modpack array in this class
	 */
	public static void loadAll() {
		ModpackLoader loader = new ModpackLoader();
		loader.start();
	}

	/**
	 * Adds a listener to the listeners array
	 * @param listener - the ModPackListener to add
	 */
	public static void addListener(ModPackListener listener) {
		listeners.add(listener);
	}

	/**
	 * Adds modpack to the modpacks array
	 * @param pack - a ModPack instance
	 */
	public static void addPack(ModPack pack) {
		synchronized (packs) {
			packs.add(pack);
		}
		for (ModPackListener listener : listeners) {
			listener.onModPackAdded(pack);
		}
	}

	/**
	 * Used to get the List of modpacks
	 * @return - the array containing all the modpacks
	 */
	public static ArrayList<ModPack> getPackArray() {
		return packs;
	}

	/**
	 * Gets the ModPack form the array and the given index
	 * @param i - the value in the array
	 * @return - the ModPack based on the i value
	 */
	public static ModPack getPack(int i) {
		return packs.get(i);
	}

	/**
	 * Used to grab the currently selected ModPack based off the selected index from ModPacksPane
	 * @return ModPack - the currently selected ModPack
	 */
	public static ModPack getSelectedPack() {
		return getPack(ModpacksPane.getIndex());
	}

	/**
	 * Constructor for ModPack class
	 * @param name - the name of the ModPack
	 * @param author - the author of the ModPack
	 * @param version - the version of the ModPack
	 * @param logo - the logo file name for the ModPack
	 * @param url - the ModPack file name
	 * @param image - the splash image file name for the ModPack
	 * @param dir - the directory for the ModPack
	 * @param mcVersion - the minecraft version required for the ModPack
	 * @param serverUrl - the server file name of the ModPack
	 * @param info - the description for the ModPack
	 * @param mods - string containing a list of mods included in the ModPack by default
	 * @param oldVersions - string containing all available old versions of the ModPack
	 * @param animation - the animation to display before minecraft launches
	 * @param idx - the actual position of the modpack in the index
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public ModPack(String name, String author, String version, String logo, String url, String image, String dir, String mcVersion, String serverUrl, String info, String mods, String oldVersions, String animation, int idx) throws IOException, NoSuchAlgorithmException {
		index = idx;
		this.name = name;
		this.author = author;
		this.version = version;
		this.dir = dir;
		this.mcVersion = mcVersion;
		this.url = url;
		this.serverUrl = serverUrl;
		this.animation = animation;
		logoName = logo;
		imageName = image;
		this.info = info;
		if(mods.isEmpty()) {
			this.mods = null;
		} else {
			this.mods = mods.split("; ");
		}
		if(oldVersions.isEmpty()) {
			this.oldVersions = null;
		} else {
			this.oldVersions = oldVersions.split(";");
		}
		String installPath = OSUtils.getDynamicStorageLocation();
		File tempDir = new File(installPath, "ModPacks" + sep + dir);
		File verFile = new File(tempDir, "version");
		URL url_;
		if(!upToDate(verFile)) {
			url_ = new URL(DownloadUtils.getStaticCreeperhostLink(logo));
			this.logo = Toolkit.getDefaultToolkit().createImage(url_);
			BufferedImage tempImg = ImageIO.read(url_);
			ImageIO.write(tempImg, "png", new File(tempDir, logo));
			tempImg.flush();
			url_ =  new URL(DownloadUtils.getStaticCreeperhostLink(image));
			this.image = Toolkit.getDefaultToolkit().createImage(url_);
			tempImg = ImageIO.read(url_);
			ImageIO.write(tempImg, "png", new File(tempDir, image));
			tempImg.flush();
		} else {
			if(new File(tempDir, logo).exists()) {
				this.logo = Toolkit.getDefaultToolkit().createImage(tempDir.getPath() + sep + logo);
			} else {
				url_ = new URL(DownloadUtils.getStaticCreeperhostLink(logo));
				this.logo = Toolkit.getDefaultToolkit().createImage(url_);
				BufferedImage tempImg = ImageIO.read(url_);
				ImageIO.write(tempImg, "png", new File(tempDir, logo));
				tempImg.flush();
			}
			if(new File(tempDir, image).exists()) {
				this.image = Toolkit.getDefaultToolkit().createImage(tempDir.getPath() + sep + image);
			} else {
				url_ = new URL(DownloadUtils.getStaticCreeperhostLink(image));
				this.image = Toolkit.getDefaultToolkit().createImage(url_);
				BufferedImage tempImg = ImageIO.read(url_);
				ImageIO.write(tempImg, "png", new File(tempDir, image));
				tempImg.flush();
			}
		}
	}

	/**
	 * Used to check if the cached items are up to date
	 * @param verFile - the version file to check
	 * @return checks the version file against the current modpack version
	 */
	private boolean upToDate(File verFile) {
		boolean result = true;
		try {
			if(!verFile.exists()) {
				verFile.getParentFile().mkdirs();
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
		} catch (IOException e) {
			Logger.logError(e.getMessage(), e);
		}
		return result;
	}

	/**
	 * Used to get index of modpack
	 * @return - the index of the modpack in the GUI
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Used to get name of modpack
	 * @return - the name of the modpack
	 */
	public String getName() {
		return name;
	}

	/**
	 * Used to get Author of modpack
	 * @return - the modpack's author
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * Used to get the version of the modpack
	 * @return - the modpacks version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Used to get an Image variable of the modpack's logo
	 * @return - the modpacks logo
	 */
	public Image getLogo() {
		return logo;
	}

	/**
	 * Used to get the URL or File name of the modpack
	 * @return - the modpacks URL
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Used to get an Image variable of the modpack's splash image
	 * @return - the modpacks splash image
	 */
	public Image getImage() {
		return image;
	}

	/**
	 * Used to get the directory of the modpack
	 * @return - the directory for the modpack
	 */
	public String getDir() {
		return dir;
	}

	/**
	 * Used to get the minecraft version required for the modpack
	 * @return - the minecraft version
	 */
	public String getMcVersion() {
		return mcVersion;
	}

	/**
	 * Used to get the info or description of the modpack
	 * @return - the info for the modpack
	 */
	public String getInfo() {
		return info;
	}

	/**
	 * Used to get an array of mods inside the modpack
	 * @return - string array of all mods contained
	 */
	public String[] getMods() {
		return mods;
	}

	/**
	 * Used to get the name of the server file for the modpack
	 * @return - string representing server file name
	 */
	public String getServerUrl() {
		return serverUrl;
	}

	/**
	 * Used to get the logo file name
	 * @return - the logo name as saved on the repo
	 */
	public String getLogoName() {
		return logoName;
	}

	/**
	 * Used to get the splash file name
	 * @return - the splash image name as saved on the repo
	 */
	public String getImageName() {
		return imageName;
	}

	/**
	 * Used to set whether the modpack has been updated
	 * @param result - the status of whether the modpack has been updated or not
	 */
	public void setUpdated(boolean result) {
		updated = result;
	}

	/**
	 * Used to check if the modpack has been updated
	 * @return - the boolean representing whether the modpack has been updated
	 */
	public boolean isUpdated() {
		return updated;
	}

	/**
	 * Used to get all available old versions of the modpack
	 * @return - string array containing all available old version of the modpack
	 */
	public String[] getOldVersions() {
		return oldVersions;
	}

	/**
	 * Used to set the minecraft version required of the pack to a custom version
	 * @param version - the version of minecraft for the pack
	 */
	public void setMcVersion(String version) {
		mcVersion = version;
	}
	
	/**
	 * @return the filename of the gif animation to display before minecraft loads
	 */
	public String getAnimation() {
		return animation;
	}
}
