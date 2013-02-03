/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2013, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
 * FTB Launcher is licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import net.ftb.gui.panes.MapsPane;
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
	 * Adds a listener to the listeners array
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
	 * Used to get the List of maps
	 * @return - the array containing all the maps
	 */
	public static ArrayList<Map> getMapArray() {
		return maps;
	}

	/**
	 * Gets the map form the array and the given index
	 * @param i - the value in the array
	 * @return - the Map based on the i value
	 */
	public static Map getMap(int i) {
		return maps.get(i);
	}

	public static int size() {
		return maps.size();
	}

	/**
	 * Used to grab the currently selected Map based off the selected index from MapsPane
	 * @return Map - the currently selected Map
	 */
	public static Map getSelectedMap() {
		return getMap(MapsPane.getSelectedMapIndex());
	}

	/**
	 * Constructor for Map class
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
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
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
	 * Used to check if the cached items are up to date
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
	 * Used to get index of map
	 * @return - the index of the map in the GUI
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Used to get name of map
	 * @return - the name of the map
	 */
	public String getName() {
		return name;
	}

	/**
	 * Used to get Author of map
	 * @return - the map's author
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * Used to get the version of the map
	 * @return - the maps version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Used to get the URL or File name of the map
	 * @return - the maps URL
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Used to get an Image variable of the map's logo
	 * @return - the maps logo
	 */
	public Image getLogo() {
		return logo;
	}

	/**
	 * Used to get an Image variable of the map's splash image
	 * @return - the maps splash image
	 */
	public Image getImage() {
		return image;
	}

	/**
	 * Used to get the array of compatible mod packs
	 * @return - the compatible packs
	 */
	public String[] getCompatible() {
		return compatible;
	}

	/**
	 * Used to get the selected mod pack
	 * @return - the compatible pack based on the selected map
	 */
	public String getSelectedCompatible() {
		return compatible[LaunchFrame.getSelectedMapInstallIndex()].trim();
	}

	/**
	 * Used to get the minecraft version required for the map
	 * @return - the minecraft version
	 */
	public String getMcVersion() {
		return mcversion;
	}

	/**
	 * Used to get the name of the map
	 * @return - the mapname
	 */
	public String getMapName() {
		return mapname;
	}

	/**
	 * Used to get the info or description of the map
	 * @return - the info for the map
	 */
	public String getInfo() {
		return info;
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
	 * Checks if the map is compatible with the passed modpack name
	 * @param packName the name of the pack
	 * @return true if the pack is compatible with a map
	 */
	public boolean isCompatible(String packName) {
		for (String aCompatible : compatible) {
			if (ModPack.getPack(aCompatible).getName().equals(packName)) {
				return true;
			}
		}
		return false;
	}
}
