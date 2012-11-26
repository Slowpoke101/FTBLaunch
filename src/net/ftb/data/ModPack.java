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
import net.ftb.util.DownloadUtils;
import net.ftb.util.OSUtils;
import net.ftb.workers.ModpackLoader;

public class ModPack {	
	private String name, author, version, url, dir, mcVersion, serverUrl, logoName, imageName, info, sep = File.separator;
	private String[] mods;
	private Image logo, image;
	private int index;
	private boolean uptodate = true;

	private final static ArrayList<ModPack> packs = new ArrayList<ModPack>();
	private static List<ModPackListener> listeners = new ArrayList<ModPackListener>();

	public static void loadAll() {
		ModpackLoader loader = new ModpackLoader();
		loader.start();
	}

	public static void addListener(ModPackListener listener) {
		listeners.add(listener);
	}

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

	public ModPack(String name, String author, String version, String logo, String url, String image, String dir, String mcVersion, String serverUrl, String info, String mods, int idx) throws IOException, NoSuchAlgorithmException {
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
		if(mods.isEmpty()) {
			this.mods = null;
		} else {
			this.mods = mods.split("; ");
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

	private boolean upToDate(File verFile) {
		boolean result = true;
		try {
			if(!verFile.exists()) {
				verFile.getParentFile().mkdirs();
				verFile.createNewFile();
				result = false;
				uptodate = false;
			}
			BufferedReader in = new BufferedReader(new FileReader(verFile));
			String line;
			if((line = in.readLine()) == null || Integer.parseInt(version) > Integer.parseInt(line)) {
				BufferedWriter out = new BufferedWriter(new FileWriter(verFile));
				out.write(version);
				out.flush();
				out.close();
				result = false;
				uptodate = false;
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

	public String[] getMods() {
		return mods;
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

	public void setUpToDate(boolean result) {
		uptodate = result;
	}

	public boolean isUpToDate() {
		return uptodate;
	}
}
