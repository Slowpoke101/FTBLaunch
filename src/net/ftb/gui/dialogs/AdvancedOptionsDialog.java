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
package net.ftb.gui.dialogs;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import net.ftb.data.Settings;
import net.ftb.gui.LaunchFrame;
import net.ftb.locale.I18N;
import net.ftb.log.Logger;
import net.ftb.util.DownloadUtils;

public class AdvancedOptionsDialog extends JDialog {
	private JButton exit;
	private JLabel downloadLocationLbl;
	private static JComboBox downloadLocation;
	private JLabel additionalJavaOptionsLbl;
	private JTextField additionalJavaOptions;
	private JLabel mcWindowSizeLbl;
	private JTextField mcWindowSizeWidth;
	private JLabel mcWindowSizeSepLbl;
	private JTextField mcWindowSizeHeight;
	private JLabel mcWindowPosLbl;
	private JTextField mcWindowPosX;
	private JLabel mcWindowPosSepLbl;
	private JTextField mcWindowPosY;
	private JCheckBox autoMaxCheck;
	private JCheckBox snooper;

	private final Settings settings = Settings.getSettings();

	public AdvancedOptionsDialog() {
		super(LaunchFrame.getInstance(), true);
		setupGui();

		if(DownloadUtils.serversLoaded) {
			if(DownloadUtils.downloadServers.containsKey(settings.getDownloadServer())) {
				downloadLocation.setSelectedItem(settings.getDownloadServer());
			}
		}

		mcWindowSizeWidth.setText(Integer.toString(settings.getLastDimension().width));
		mcWindowSizeHeight.setText(Integer.toString(settings.getLastDimension().height));
		mcWindowPosX.setText(Integer.toString(settings.getLastPosition().x));
		mcWindowPosY.setText(Integer.toString(settings.getLastPosition().y));
		autoMaxCheck.setSelected((settings.getLastExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH);
		snooper.setSelected(settings.getSnooper());

		FocusListener settingsChangeListener = new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				saveSettingsInto(settings);
			}
			@Override public void focusGained(FocusEvent e) { }
		};

		downloadLocation.addFocusListener(settingsChangeListener);
		additionalJavaOptions.addFocusListener(settingsChangeListener);
		mcWindowSizeWidth.addFocusListener(settingsChangeListener);
		mcWindowSizeHeight.addFocusListener(settingsChangeListener);
		mcWindowPosX.addFocusListener(settingsChangeListener);
		mcWindowPosY.addFocusListener(settingsChangeListener);
		autoMaxCheck.addFocusListener(settingsChangeListener);
		snooper.addFocusListener(settingsChangeListener);

		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
	}

	public static void setDownloadServers() {
		String downloadserver = Settings.getSettings().getDownloadServer();
		downloadLocation.removeAllItems();
		for(String server : DownloadUtils.downloadServers.keySet()) {
			downloadLocation.addItem(server);
		}
		if(DownloadUtils.downloadServers.containsKey(downloadserver)) {
			downloadLocation.setSelectedItem(downloadserver);
		}
	}

	public String[] getDownloadServerNames() {
		if(!DownloadUtils.serversLoaded) {
			Logger.logWarn("Servers not loaded yet.");
			return new String[] { "Automatic" };
		} else {
			String[] out = new String[DownloadUtils.downloadServers.size()];
			for(int i = 0; i < out.length; i++) {
				out[i] = String.valueOf(DownloadUtils.downloadServers.keySet().toArray()[i]);
			}
			return out;
		}
	}

	public void saveSettingsInto(Settings settings) {
		settings.setDownloadServer(String.valueOf(downloadLocation.getItemAt(downloadLocation.getSelectedIndex())));
		settings.setLastDimension(new Dimension(Integer.parseInt(mcWindowSizeWidth.getText()), Integer.parseInt(mcWindowSizeHeight.getText())));
		int lastExtendedState = settings.getLastExtendedState();
		settings.setLastExtendedState(autoMaxCheck.isSelected() ? (lastExtendedState | JFrame.MAXIMIZED_BOTH) : (lastExtendedState & ~JFrame.MAXIMIZED_BOTH));
		settings.setLastPosition(new Point(Integer.parseInt(mcWindowPosX.getText()), Integer.parseInt(mcWindowPosY.getText())));
		settings.setAdditionalJavaOptions(additionalJavaOptions.getText());
		settings.setSnooper(snooper.isSelected());
		settings.save();
	}

	private void setupGui() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
		setTitle(I18N.getLocaleString("ADVANCED_OPTIONS_TITLE"));
		setResizable(false);
		getContentPane().setLayout(null);
		setBounds(440, 260, 440, 260);

		downloadLocationLbl = new JLabel(I18N.getLocaleString("ADVANCED_OPTIONS_DLLOCATION"));
		downloadLocationLbl.setBounds(10, 10, 110, 25);
		add(downloadLocationLbl);
		downloadLocation = new JComboBox(getDownloadServerNames());
		downloadLocation.setBounds(190, 10, 222, 25);
		add(downloadLocation);

		additionalJavaOptionsLbl = new JLabel(I18N.getLocaleString("ADVANCED_OPTIONS_ADDJAVAOPTIONS"));
		additionalJavaOptionsLbl.setBounds(10, 45, 320, 25);
		add(additionalJavaOptionsLbl);

		additionalJavaOptions = new JTextField(settings.getAdditionalJavaOptions());
		additionalJavaOptions.setBounds(190, 45, 222, 28);
		add(additionalJavaOptions);

		mcWindowSizeLbl = new JLabel(I18N.getLocaleString("ADVANCED_OPTIONS_MCWINDOW_SIZE"));
		mcWindowSizeLbl.setBounds(10, 80, 170, 25);
		add(mcWindowSizeLbl);

		mcWindowSizeWidth = new JTextField();
		mcWindowSizeWidth.setBounds(190, 80, 95, 25);
		add(mcWindowSizeWidth);
		mcWindowSizeWidth.setColumns(10);

		mcWindowSizeSepLbl = new JLabel("x");
		mcWindowSizeSepLbl.setBounds(297, 80, 15, 25);
		add(mcWindowSizeSepLbl);

		mcWindowSizeHeight = new JTextField();
		mcWindowSizeHeight.setBounds(317, 80, 95, 25);
		add(mcWindowSizeHeight);
		mcWindowSizeHeight.setColumns(10);

		mcWindowPosLbl = new JLabel(I18N.getLocaleString("ADVANCED_OPTIONS_MCWINDOW_POS"));
		mcWindowPosLbl.setBounds(10, 115, 170, 25);
		add(mcWindowPosLbl);

		mcWindowPosX = new JTextField();
		mcWindowPosX.setBounds(190, 115, 95, 25);
		add(mcWindowPosX);
		mcWindowPosX.setColumns(10);

		mcWindowPosSepLbl = new JLabel("x");
		mcWindowPosSepLbl.setBounds(297, 115, 15, 25);
		add(mcWindowPosSepLbl);

		mcWindowPosY = new JTextField();
		mcWindowPosY.setBounds(317, 115, 95, 25);
		mcWindowPosY.setColumns(10);
		add(mcWindowPosY);

		autoMaxCheck = new JCheckBox(I18N.getLocaleString("ADVANCED_OPTIONS_MCWINDOW_AUTOMAXCHECK"));
		autoMaxCheck.setBounds(10, 150, 170, 25);
		add(autoMaxCheck);

		snooper = new JCheckBox(I18N.getLocaleString("ADVANCED_OPTIONS_DISABLEGOOGLEANALYTICS"));
		snooper.setBounds(190, 150, 300, 25);
		add(snooper);

		exit = new JButton(I18N.getLocaleString("MAIN_EXIT"));
		exit.setBounds(150, 190, 140, 28);
		add(exit);
	}
}
