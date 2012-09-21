package net.ftb.data;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class ModPack {
	
	private String name;
	private URL url;
	private URLConnection connection;
	private Image image;
	private int size;
	
	
	public ModPack(String name, URL url, Image image) throws IOException {
		this.name = name;
		this.url = url;
		this.image = image;
		connection = url.openConnection();
		this.size = connection.getContentLength();
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
	
	public int getSize() {
		return size;
	}
}
