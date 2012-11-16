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
import net.ftb.util.OSUtils;
import net.ftb.workers.MapLoader;

public class Map {
	private String name, author, version, url, mapname, mcversion, logoName, imageName, info, sep = File.separator;
	private String[] compatible;
	private Image logo, image;
	private int index;

	private final static ArrayList<Map> maps = new ArrayList<Map>();
	private static List<MapListener> listeners = new ArrayList<MapListener>();

	public static void addListener(MapListener listener) {
		listeners.add(listener);
	}

	public static void loadAll() {
		MapLoader loader = new MapLoader();
		loader.start();
	}

	public static void addMap(Map map) {
		synchronized (maps) {
			maps.add(map);
		}
		for (MapListener listener : listeners) {
			listener.onMapAdded(map);
		}
	}

	public static ArrayList<Map> getMapArray() {
		return maps;
	}

	public static Map getMap(int i) {
		return maps.get(i);
	}

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
			url_ = new URL(LaunchFrame.getStaticCreeperhostLink(logo));
			this.logo = Toolkit.getDefaultToolkit().createImage(url_);
			BufferedImage tempImg = ImageIO.read(url_);
			ImageIO.write(tempImg, "png", new File(tempDir, logo));
			tempImg.flush();
			url_ = new URL(LaunchFrame.getStaticCreeperhostLink(image));
			this.image = Toolkit.getDefaultToolkit().createImage(url_);
			tempImg = ImageIO.read(url_);
			ImageIO.write(tempImg, "png", new File(tempDir, image));
			tempImg.flush();
		} else {
			if(new File(tempDir, logo).exists()) {
				this.logo = Toolkit.getDefaultToolkit().createImage(tempDir.getPath() + sep + logo);
			} else {
				url_ = new URL(LaunchFrame.getStaticCreeperhostLink(logo));
				this.logo = Toolkit.getDefaultToolkit().createImage(url_);
				BufferedImage tempImg = ImageIO.read(url_);
				ImageIO.write(tempImg, "png", new File(tempDir, logo));
				tempImg.flush();
			}
			if(new File(tempDir, image).exists()) {
				this.image = Toolkit.getDefaultToolkit().createImage(tempDir.getPath() + sep + image);
			} else {
				url_ = new URL(LaunchFrame.getStaticCreeperhostLink(image));
				this.image = Toolkit.getDefaultToolkit().createImage(url_);
				BufferedImage tempImg = ImageIO.read(url_);
				ImageIO.write(tempImg, "png", new File(tempDir, image));
				tempImg.flush();
			}
		}
	}

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
		} catch (IOException e) { }
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

	public String getUrl() {
		return url;
	}

	public Image getLogo() {
		return logo;
	}

	public Image getImage() {
		return image;
	}

	public String[] getCompatible() {
		return compatible;
	}

	public String getSelectedCompatible() {
		return compatible[LaunchFrame.getSelectedMapInstallIndex()];
	}

	public String getMcVersion() {
		return mcversion;
	}

	public String getMapName() {
		return mapname;
	}

	public String getInfo() {
		return info;
	}

	public String getLogoName() {
		return logoName;
	}

	public String getImageName() {
		return imageName;
	}

	public boolean isCompatible(String dir) {
		for(int i = 0; i < compatible.length; i++) {
			if(compatible[i].equalsIgnoreCase(dir)) {
				return true;
			}
		}
		return false;
	}
}
