package net.ftb.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.net.URL;
import javax.swing.Icon;

public class ImageAndTextIcon implements Icon {

	/*
	 * Keep references to the filename and location so that alternate
	 * persistence schemes have the option to archive images symbolically rather
	 * than including the image data in the archive.
	 */

	transient Image image;
	transient int loadStatus = 0;
	ImageObserver imageObserver;
	String description = null;

	protected final static Component component = new Component() {private static final long serialVersionUID = 1L;};
		
	protected final static MediaTracker tracker = new MediaTracker(component);

	/**
	 * Id used in loading images from MediaTracker.
	 */
	private static int mediaTrackerID;

	int width = -1;
	int height = -1;

	/**
	 * Creates an ImageIcon from the specified file. The image will be preloaded
	 * by using MediaTracker to monitor the loading state of the image.
	 * 
	 * @param filename
	 *            the name of the file containing the image
	 * @param description
	 *            a brief textual description of the image
	 * @see #ImageIcon(String)
	 */
	public ImageAndTextIcon(URL filename, String desc) {
		image = Toolkit.getDefaultToolkit().getImage(filename);
		if (image == null) {
			return;
		}
		this.description = desc;
		loadImage(image);
	}

	/**
	 * Loads the image, returning only when the image is loaded.
	 * 
	 * @param image
	 *            the image
	 */
	protected void loadImage(Image image) {
		synchronized (tracker) {
			int id = getNextID();

			tracker.addImage(image, id);
			try {
				tracker.waitForID(id, 0);
			} catch (InterruptedException e) {
				System.out.println("INTERRUPTED while loading Image");
			}
			loadStatus = tracker.statusID(id, false);
			tracker.removeImage(image, id);

			width = image.getWidth(imageObserver);
			height = image.getHeight(imageObserver);
		}
	}

	/**
	 * Returns an ID to use with the MediaTracker in loading an image.
	 */
	private int getNextID() {
		synchronized (tracker) {
			return ++mediaTrackerID;
		}
	}

	/**
	 * Paints the icon. The top-left corner of the icon is drawn at the point (
	 * <code>x</code>, <code>y</code>) in the coordinate space of the graphics
	 * context <code>g</code>. If this icon has no image observer, this method
	 * uses the <code>c</code> component as the observer.
	 * 
	 * @param c
	 *            the component to be used as the observer if this icon has no
	 *            image observer
	 * @param g
	 *            the graphics context
	 * @param x
	 *            the X coordinate of the icon's top-left corner
	 * @param y
	 *            the Y coordinate of the icon's top-left corner
	 */
	public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
		g.drawImage(image, x, y, c);
		g.setColor(new Color(40, 40, 40));
		g.fillOval(x + image.getWidth(imageObserver) - 23, y - 5, 23, 20);
		g.setColor(new Color(235, 115, 30));
		g.setFont(new Font("SansSerif", 12, 12));
		g.drawString(description, x + image.getWidth(imageObserver) - 16, y + 10);
	}

	@Override
	public int getIconHeight() {
		return width;
	}

	@Override
	public int getIconWidth() {
		return height;
	}
}
