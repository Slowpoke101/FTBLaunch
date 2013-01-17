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
package net.ftb.mclauncher;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import net.ftb.data.Settings;
import net.ftb.util.OSUtils;
import net.ftb.util.OSUtils.OS;
import net.ftb.util.StyleUtil;
import net.minecraft.Launcher;

public class MinecraftFrame extends JFrame implements WindowListener {
	private static final long serialVersionUID = 1L;
	private Launcher appletWrap = null;
	private String animationname;

	public MinecraftFrame(String title, String imagePath, String animationname) {
		super(title);
		this.animationname = animationname;
		StyleUtil.loadUiStyles();
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

		// Fullscreen support in OS X.
		if (OSUtils.getCurrentOS() == OS.MACOSX) {
			try
			{
				Class<?> fullScreenUtilityClass = Class.forName("com.apple.eawt.FullScreenUtilities");
				java.lang.reflect.Method setWindowCanFullScreenMethod = fullScreenUtilityClass.getDeclaredMethod("setWindowCanFullScreen", new Class[] { Window.class, Boolean.TYPE });
				setWindowCanFullScreenMethod.invoke(null, new Object[] { this, Boolean.valueOf(true) });
			} catch (Exception e) {
				// This is not a fatal exception, so just log it for brevity.
				e.printStackTrace();
			}
		}

		setIconImage(Toolkit.getDefaultToolkit().createImage(imagePath));
		super.setVisible(true);
		setResizable(true);
		fixSize(Settings.getSettings().getLastDimension());
		addWindowListener(this);
		final MinecraftFrame thisFrame = this;
		addComponentListener(new ComponentListener() {
			@Override
			public void componentResized(ComponentEvent e) {
				Settings.getSettings().setLastDimension(thisFrame.getSize());
				Settings.getSettings().save();
			}
			@Override
			public void componentMoved(ComponentEvent e) {
				Settings.getSettings().setLastPosition(thisFrame.getLocation());
			}
			@Override public void componentShown(ComponentEvent e) { }
			@Override public void componentHidden(ComponentEvent e) { }
		});
	}

	public void start(Applet mcApplet, String user, String session) {
		JLabel label = new JLabel();
		Thread animation = new Thread();
		Dimension size = Settings.getSettings().getLastDimension();
		if(!animationname.equalsIgnoreCase("empty")) {
			try {
				animation.start();
				label = new JLabel(new ImageIcon(animationname));
				label.setBounds(0, 0, size.width, size.height);
				fixSize(size);
				getContentPane().setBackground(Color.black);
				add(label);
				animation.sleep(3000);
				animation.stop();
			} catch (Exception e) {
				label.add(label);
			} finally {
				remove(label);
			}
		}

		try {
			appletWrap = new Launcher(mcApplet, new URL("http://www.minecraft.net/game"));
		} catch (MalformedURLException ignored) { }
		appletWrap.setParameter("username", user);
		appletWrap.setParameter("sessionid", session);
		appletWrap.setParameter("stand-alone", "true");
		mcApplet.setStub(appletWrap);
		add(appletWrap);

		appletWrap.setPreferredSize(size);

		pack();
		validate();
		appletWrap.init();
		appletWrap.start();
		fixSize(size);
		setVisible(true);
	}

	private void fixSize(Dimension size) {
		setSize(size);
		setLocation(Settings.getSettings().getLastPosition());
		setExtendedState(Settings.getSettings().getLastExtendedState());
	}

	@Override
	public void windowClosing(WindowEvent e) {
		Settings.getSettings().setLastExtendedState(this.getExtendedState());
		Settings.getSettings().save();
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
