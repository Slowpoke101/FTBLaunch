package net.ftb.mclauncher;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import net.minecraft.Launcher;

public class MinecraftFrame extends JFrame implements WindowListener {
	private static final long serialVersionUID = 1L;
	private Launcher appletWrap = null;
	private Dimension size;
	private int windowState;

	public MinecraftFrame(String title, String imagePath, int x, int y, int xPos, int yPos, boolean autoMax, boolean centerWindow) {
		super(title);
		
		
		Color baseColor = new Color(40, 40, 40);
		UIManager.put("control", baseColor);
		UIManager.put("text", baseColor.brighter().brighter().brighter().brighter().brighter());
		UIManager.put("nimbusBase", new Color(0, 0, 0));
		UIManager.put("nimbusFocus", baseColor);
		UIManager.put("nimbusBorder", baseColor);
		UIManager.put("nimbusLightBackground", baseColor);
		UIManager.put("info", baseColor.brighter().brighter());
		UIManager.put("nimbusSelectionBackground", baseColor.brighter().brighter());
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
			try {
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			} catch (Exception e1) { }
		}
		
		setIconImage(Toolkit.getDefaultToolkit().createImage(imagePath));
		super.setVisible(true);
		windowState = getExtendedState() | ((autoMax) ? JFrame.MAXIMIZED_BOTH : 0);
		size = new Dimension(x, y);
		setSize(size);
		if(centerWindow) {
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			setLocation((screenSize.width - x) / 2, (screenSize.height - y) / 2);
		} else  {
			setLocation(xPos, yPos);
		}
		setResizable(true);
		addWindowListener(this);
	}

	public void start(Applet mcApplet, String user, String session) {
		Thread animation = new Thread();
		animation.start();
		JLabel label = new JLabel(new ImageIcon(getClass().getResource("/image/animation_test.gif")));
		label.setBounds(new Rectangle(size));
		getContentPane().setBackground(Color.black);
		add(label);
		try {
			animation.sleep(3000);
		} catch (InterruptedException e) { }
		animation.stop();
		try {
			appletWrap = new Launcher(mcApplet, new URL("http://www.minecraft.net/game"));
		} catch (MalformedURLException ignored){ }
		appletWrap.setParameter("username", user);
		appletWrap.setParameter("sessionid", session);
		appletWrap.setParameter("stand-alone", "true");
		add(appletWrap);
		remove(label);
		appletWrap.setPreferredSize(size);
		pack();
		setExtendedState(windowState);
		validate();
		appletWrap.init();
		appletWrap.start();
		setVisible(true);
	}

	@Override
	public void windowClosing(WindowEvent e) {
		new Thread() {
			public void run() {
				try {
					Thread.sleep(30000L);
				} catch (InterruptedException localInterruptedException) { }
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

	@Override public void windowActivated(WindowEvent e) { }
	@Override public void windowClosed(WindowEvent e) { }
	@Override public void windowDeactivated(WindowEvent e) { }
	@Override public void windowDeiconified(WindowEvent e) { }
	@Override public void windowIconified(WindowEvent e) { }
	@Override public void windowOpened(WindowEvent e) { }
}
