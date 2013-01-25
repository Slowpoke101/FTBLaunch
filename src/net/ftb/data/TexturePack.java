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

import net.ftb.data.events.TexturePackListener;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.panes.TexturepackPane;
import net.ftb.log.Logger;
import net.ftb.util.DownloadUtils;
import net.ftb.util.OSUtils;
import net.ftb.workers.TexturePackLoader;

public class TexturePack {
	private String name, author, version, url, mcversion, logoName, imageName, info, resolution, sep = File.separator;
	private Image logo, image;
	private String[] compatible;
	private int index;
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

	public static int size() {
		return texturePacks.size();
	}

	/**
	 * Used to grab the currently selected TexturePack based off the selected index from TexturepackPane
	 * @return TexturePack - the currently selected TexturePack
	 */
	public static TexturePack getSelectedTexturePack() {
		return getTexturePack(TexturepackPane.getSelectedTexturePackIndex());
	}

	public TexturePack(String name, String author, String version, String url, String logo, String image, String mcversion, String compatible, String info, String resolution, int idx) throws NoSuchAlgorithmException, IOException {
		index = idx;
		this.name = name;
		this.author = author;
		this.version = version;
		this.url = url;
		this.version = version;
		String installPath = OSUtils.getDynamicStorageLocation();
		logoName = logo;
		imageName = image;
		this.compatible = compatible.split(",");
		this.info = info;
		this.resolution = resolution;
		File tempDir = new File(installPath, "TexturePacks" + sep + name);
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
			if((line = in.readLine()) == null || Integer.parseInt(version.replace(".", "")) > Integer.parseInt(line.replace(".", ""))) {
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

	public String getLogoName() {
		return logoName;
	}

	public String getImageName() {
		return imageName;
	}

	public String[] getCompatible() {
		return compatible;
	}
	
	public String getResolution() {
		return resolution;
	}

	/**
	 * Used to get the selected mod pack
	 * @return - the compatible pack based on the selected texture pack
	 */
	public String getSelectedCompatible() {
		return compatible[LaunchFrame.getSelectedTPInstallIndex()].trim();
	}

	public boolean isCompatible(String packName) {
		for (String aCompatible : compatible) {
			if (ModPack.getPack(aCompatible).getName().equals(packName)) {
				return true;
			}
		}
		return false;
	}
}
