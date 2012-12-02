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

import net.ftb.data.events.MapListener;
import net.ftb.gui.LaunchFrame;
import net.ftb.log.Logger;
import net.ftb.util.DownloadUtils;
import net.ftb.util.OSUtils;
import net.ftb.workers.MapLoader;

public class Map {
	private String name, author, version, url, mapname, mcversion, logoName, imageName, info, sep = File.separator;
	private String[] compatible;
	private Image logo, image;
	private int index;

	private final static ArrayList<Map> maps = new ArrayList<Map>();
	private static List<MapListener> listeners = new ArrayList<MapListener>();

	/**
	 * @param listener - the MapListener to add
	 */
	public static void addListener(MapListener listener) {
		listeners.add(listener);
	}

	/**
	 * loads the map.xml and adds it to the maps array in this class
	 */
	public static void loadAll() {
		MapLoader loader = new MapLoader();
		loader.start();
	}

	/**
	 * adds maps to the maps array
	 * @param map - a Map instance
	 */
	public static void addMap(Map map) {
		synchronized (maps) {
			maps.add(map);
		}
		for (MapListener listener : listeners) {
			listener.onMapAdded(map);
		}
	}

	/**
	 * @return - the array containing all the maps
	 */
	public static ArrayList<Map> getMapArray() {
		return maps;
	}

	/**
	 * @param i - the value in the array
	 * @return - the Map based on the i value
	 */
	public static Map getMap(int i) {
		return maps.get(i);
	}

	/**
	 * @param name - the name of the map
	 * @param author - the map name
	 * @param version - the version of the map
	 * @param url - the map's url
	 * @param logo - the url of the maps logo
	 * @param image - the url of the splash image
	 * @param compatible - the pack(s) compatible with the map
	 * @param mcversion - the minecraft version of the map
	 * @param mapname - the map name, as put in the saves folder
	 * @param info - info about the map
	 * @param idx - the id with which it is displayed on the GUI
	 */
	public Map(String name, String author, String version, String url, String logo, String image, String compatible, String mcversion, String mapname, String info, int idx) throws NoSuchAlgorithmException, IOException {
		index = idx;
		this.name = name;
		this.author = author;
		this.version = version;
		this.url = url;
		this.compatible = compatible.split(",");
		this.mcversion = mcversion;
		this.mapname = mapname;
		String installPath = OSUtils.getDynamicStorageLocation();
		this.info = info;
		logoName = logo;
		imageName = image;
		File tempDir = new File(installPath, "Maps" + sep + mapname);
		File verFile = new File(tempDir, "version");
		URL url_;
		if(!upToDate(verFile)) {
			url_ = new URL(DownloadUtils.getStaticCreeperhostLink(logo));
			this.logo = Toolkit.getDefaultToolkit().createImage(url_);
			BufferedImage tempImg = ImageIO.read(url_);
			ImageIO.write(tempImg, "png", new File(tempDir, logo));
			tempImg.flush();
			url_ = new URL(DownloadUtils.getStaticCreeperhostLink(image));
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
	 * @param verFile - the version file to check
	 * @return checks the version file against the current map version
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
	 * @return - the index of the map in the GUI
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @return - the name of the map
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return - the map's author
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * @return - the maps version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @return - the maps URL
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @return - the maps logo
	 */
	public Image getLogo() {
		return logo;
	}

	/**
	 * @return - the maps splash image
	 */
	public Image getImage() {
		return image;
	}

	/**
	 * @return - the compatible packs
	 */
	public String[] getCompatible() {
		return compatible;
	}

	/**
	 * @return - the compatible pack based on the selected map
	 */
	public String getSelectedCompatible() {
		return compatible[LaunchFrame.getSelectedMapInstallIndex()];
	}

	/**
	 * @return - the minecraft version
	 */
	public String getMcVersion() {
		return mcversion;
	}

	/**
	 * @return - the mapname
	 */
	public String getMapName() {
		return mapname;
	}

	/**
	 * @return - the info for the map
	 */
	public String getInfo() {
		return info;
	}

	/**
	 * @return - the logo name as saved on the repo
	 */
	public String getLogoName() {
		return logoName;
	}

	/**
	 * @return - the splash image name as saved on the repo
	 */
	public String getImageName() {
		return imageName;
	}

	/**
	 * @param dir the directory of the pack
	 * @return true if the pack is compatible with a map
	 */
	public boolean isCompatible(String dir) {
		for(int i = 0; i < compatible.length; i++) {
			if(compatible[i].equalsIgnoreCase(dir)) {
				return true;
			}
		}
		return false;
	}
}
