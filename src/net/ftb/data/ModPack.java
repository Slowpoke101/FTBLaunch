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
	private String dir;
	
	@SuppressWarnings("static-access")
	public ModPack(String name, String url, String image, String dir) throws IOException {
		this.name = name;
		this.url = new URL(url);
		URL imageUrl = new URL(image);
		this.image = java.awt.Toolkit.getDefaultToolkit().getDefaultToolkit().createImage(imageUrl);
		connection = this.url.openConnection();
		this.size = connection.getContentLength();
		this.dir = dir;
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
	
	public String getDir() {
		return dir;
	}
}
