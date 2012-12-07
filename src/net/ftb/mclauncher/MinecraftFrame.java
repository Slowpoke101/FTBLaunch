package net.ftb.mclauncher;

import java.applet.Applet;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import net.minecraft.Launcher;

public class MinecraftFrame extends JFrame implements WindowListener {
	private static final long serialVersionUID = 1L;

	private Launcher appletWrap = null;

	private Dimension size;
	private int windowState;

	public MinecraftFrame(String title, String imagePath, int x, int y, int xPos, int yPos, boolean autoMax, boolean centerWindow) {
		super(title);
		setIconImage(Toolkit.getDefaultToolkit().createImage(imagePath));
		super.setVisible(true);
		
		windowState = this.getExtendedState() | ((autoMax) ? JFrame.MAXIMIZED_BOTH : 0);
		
		size = new Dimension(x, y);

		this.setSize(size);
		
		if(centerWindow) {
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			this.setLocation((screenSize.width - x) / 2, (screenSize.height - y) / 2);
		} else this.setLocation(xPos, yPos);
		
		this.setResizable(true);
		this.addWindowListener(this);
	}

	public void start(Applet mcApplet, String user, String session) {
		
		Thread animation = new Thread();
		animation.start();
		
		JLabel label = new JLabel(new ImageIcon(this.getClass().getResource("/image/animation_test.gif")));
		label.setBounds(new Rectangle(size));
		this.getContentPane().setBackground(Color.black);
		this.add(label);
		try {
			animation.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		animation.stop();
		
		try {
			appletWrap = new Launcher(mcApplet, new URL("http://www.minecraft.net/game"));
		} catch (MalformedURLException ignored){
			ignored.printStackTrace();
		}
		
		appletWrap.setParameter("username", user);
		appletWrap.setParameter("sessionid", session);
		appletWrap.setParameter("stand-alone", "true");
		mcApplet.setStub(appletWrap);

		this.add(appletWrap);
		this.remove(label);
		appletWrap.setPreferredSize(size);
		this.pack();
		
		this.setExtendedState(windowState);

		animation.stop();
		validate();
		appletWrap.init();
		appletWrap.start();
		setVisible(true);
	}

	@Override public void windowActivated(WindowEvent e) { }

	@Override public void windowClosed(WindowEvent e) { }

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

	@Override public void windowDeactivated(WindowEvent e) { }

	@Override public void windowDeiconified(WindowEvent e) { }

	@Override public void windowIconified(WindowEvent e) { }

	@Override public void windowOpened(WindowEvent e) { }
}
