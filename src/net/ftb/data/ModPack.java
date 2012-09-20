package net.ftb.data;

import java.awt.Image;
import java.net.URL;

public class ModPack {
	
	private String name;
	private URL url;
	private Image image;
	
	public ModPack(String name, URL url, Image image) {
		this.name = name;
		this.url = url;
		this.image = image;
	}
	
	public String getName() {
		return name;
	}
	
	public URL getUrl() {
		return url;
	}
	
	public Image getImage() {
		return image;
	}
}
