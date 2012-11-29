package net.ftb.mclauncher;

import java.applet.Applet;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JFrame;

import net.minecraft.Launcher;

public class MinecraftFrame extends JFrame implements WindowListener {
	private static final long serialVersionUID = 1L;
	
	private Launcher appletWrap = null;
	
	private Dimension size;

	public MinecraftFrame(String title, String imagePath, int x, int y, int xPos, int yPos) {
		super(title);
		super.setVisible(true);
		setIconImage(Toolkit.getDefaultToolkit().createImage(imagePath));

		size = new Dimension(x, y);
		
		this.setSize(size);
		this.setLocation(xPos, yPos);

		this.setResizable(true);
		this.addWindowListener(this);
	}

	public void start(Applet mcApplet, String user, String session) {
		try {
			appletWrap = new Launcher(mcApplet, new URL("http://www.minecraft.net/game"));
		}
		catch (MalformedURLException ignored){}

		appletWrap.setParameter("username", user);
		appletWrap.setParameter("sessionid", session);
		appletWrap.setParameter("stand-alone", "true"); // Show the quit button.
		mcApplet.setStub(appletWrap);

		this.add(appletWrap);
		appletWrap.setPreferredSize(size);
		this.pack();
		
		validate();
		appletWrap.init();
		appletWrap.start();
		setVisible(true);
	}

	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowClosed(WindowEvent e) {}

	@Override
	public void windowClosing(WindowEvent e) {
		new Thread() {
			public void run() {
				try {
					Thread.sleep(30000L);
				} catch (InterruptedException localInterruptedException) {
					localInterruptedException.printStackTrace();
				}
				System.out.println("FORCING EXIT!");
				System.exit(0);
			}
		}.start();

		if (appletWrap != null) {
			appletWrap.stop();
			appletWrap.destroy();
		}
		System.exit(0);
	}

	@Override
	public void windowDeactivated(WindowEvent e) {}

	@Override
	public void windowDeiconified(WindowEvent e) {}

	@Override
	public void windowIconified(WindowEvent e) {}

	@Override
	public void windowOpened(WindowEvent e) {}
}
