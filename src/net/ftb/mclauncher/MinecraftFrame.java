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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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

@SuppressWarnings("serial")
public class MinecraftFrame extends JFrame {
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
		// TODO: TEST THIS, also implement into using settings.
		if(OSUtils.getCurrentOS() == OS.MACOSX) {
			try {
				Class<?> fullScreenUtilityClass = Class.forName("com.apple.eawt.FullScreenUtilities");
				java.lang.reflect.Method setWindowCanFullScreenMethod = fullScreenUtilityClass.getDeclaredMethod("setWindowCanFullScreen", new Class[] { Window.class, Boolean.TYPE });
				setWindowCanFullScreenMethod.invoke(null, new Object[] { this, Boolean.valueOf(true) });
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// END TEST

		setIconImage(Toolkit.getDefaultToolkit().createImage(imagePath));
		super.setVisible(true);
		setResizable(true);
		fixSize(Settings.getSettings().getLastDimension());
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				Settings.getSettings().setLastExtendedState(getExtendedState());
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
		});
		final MinecraftFrame thisFrame = this;
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				Settings.getSettings().setLastDimension(thisFrame.getSize());
				Settings.getSettings().save();
			}
			@Override
			public void componentMoved(ComponentEvent e) {
				Settings.getSettings().setLastPosition(thisFrame.getLocation());
			}
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
				Thread.sleep(3000);
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
}
