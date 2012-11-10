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

import net.ftb.data.events.TexturePackListener;
import net.ftb.gui.LaunchFrame;
import net.ftb.workers.TexturePackLoader;

public class TexturePack {
	private String name, author, version, url, mcversion, logoName, imageName;
	private String info = "This is the info until there is an actual info thingy";
	private Image logo, image;
	private int size, index;
	private String sep = File.separator;

	private final static ArrayList<TexturePack> texturePacks = new ArrayList<TexturePack>();

	private static List<TexturePackListener> listeners = new ArrayList<TexturePackListener>();

	public static void addListener(TexturePackListener listener) {
		listeners.add(listener);
	}

	public static void loadAll() {
		TexturePackLoader loader = new TexturePackLoader();
		loader.start();
	}

	public static void addTexturePack(TexturePack texturePack) {
		synchronized (texturePacks) {
			texturePacks.add(texturePack);
		}
		for (TexturePackListener listener : listeners) {
			listener.onTexturePackAdded(texturePack);
		}
	}

	public static ArrayList<TexturePack> getTexturePackArray() {
		return texturePacks;
	}

	public static TexturePack getTexturePack(int i) {
		return texturePacks.get(i);
	}

	public TexturePack(String name, String author, String version, String url, String logo, String image, String mcversion, int idx) throws NoSuchAlgorithmException, IOException {
		index = idx;
		this.name = name;
		this.author = author;
		this.version = version;
		this.url = url;
		this.version = version;
		String installPath = Settings.getSettings().getInstallPath();
		logoName = logo;
		imageName = image;
		File verFile = new File(installPath, "temp" + sep + "TexturePacks" + sep + name + sep + "version");
		File dir = new File(installPath, "temp" + sep + "TexturePacks" + sep + name);
		URL url_;
		if(!upToDate(verFile)) {
			url_ = new URL(LaunchFrame.getCreeperhostLink(logo));
			this.logo = Toolkit.getDefaultToolkit().createImage(url_);
			BufferedImage tempImg = ImageIO.read(url_);
			ImageIO.write(tempImg, "png", new File(dir, logo));
			tempImg.flush();
			url_ = new URL(LaunchFrame.getCreeperhostLink(image));
			this.image = Toolkit.getDefaultToolkit().createImage(url_);
			tempImg = ImageIO.read(url_);
			ImageIO.write(tempImg, "png", new File(dir, image));
			tempImg.flush();
		} else {
			this.logo = Toolkit.getDefaultToolkit().createImage(dir.getPath() + sep + logo);
			this.image = Toolkit.getDefaultToolkit().createImage(dir.getPath() + sep + image);
		}
		url_ = new URL(LaunchFrame.getCreeperhostLink(url));
		size = url_.openConnection().getContentLength();
	}

	private boolean upToDate(File verFile) {
		boolean result = false;
		try {
			if(!verFile.exists()) {
				verFile.mkdirs();
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

	public String getMcVersion() {
		return mcversion;
	}

	public String getInfo() {
		return info;
	}

	public int getSize() {
		return size;
	}

	public String getLogoName() {
		return logoName;
	}

	public String getImageName() {
		return imageName;
	}
}
