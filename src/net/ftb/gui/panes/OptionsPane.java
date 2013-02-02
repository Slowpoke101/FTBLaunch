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
package net.ftb.gui.panes;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

import net.ftb.data.Settings;
import net.ftb.gui.ChooseDir;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.dialogs.AdvancedOptionsDialog;
import net.ftb.locale.I18N;
import net.ftb.log.Logger;

public class OptionsPane extends JPanel implements ILauncherPane {
	private JButton advancedOptionsBtn;
	private JLabel lblRamMaximum, lblLocale, currentRam;
	private JSlider ramMaximum;
	private JComboBox locale;
	private JCheckBox chckbxShowConsole, keepLauncherOpen;
	private final Settings settings;

	private FocusListener settingsChangeListener = new FocusListener() {
		@Override
		public void focusLost(FocusEvent e) {
			saveSettingsInto(settings);
		}
		@Override public void focusGained(FocusEvent e) { }
	};

	public OptionsPane (Settings settings) {
		this.settings = settings;
		setBorder(new EmptyBorder(5, 5, 5, 5));

		setLayout(null);

		currentRam = new JLabel();
		currentRam.setBounds(427, 95, 85, 25);
		long ram = 0;
		OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
		Method m;
		try {
			m = operatingSystemMXBean.getClass().getDeclaredMethod("getTotalPhysicalMemorySize");
			m.setAccessible(true);
			Object value = m.invoke(operatingSystemMXBean);
			if(value != null) {
				ram = Long.valueOf(value.toString()) / 1024 / 1024;
			} else {
				Logger.logWarn("Could not get RAM Value");
				ram = 8192;
			}
		} catch (Exception e) {
			Logger.logError(e.getMessage(), e);
		}

		ramMaximum = new JSlider();
		ramMaximum.setBounds(190, 95, 222, 25);
		ramMaximum.setSnapToTicks(true);
		ramMaximum.setMajorTickSpacing(256);
		ramMaximum.setMinorTickSpacing(256);
		ramMaximum.setMinimum(256);
		String vmType = System.getProperty("sun.arch.data.model");
		if(vmType != null){
			if(vmType.equals("64")) {
				ramMaximum.setMaximum((int)ram);
			} else if(vmType.equals("32")) {
				if(ram < 1024) {
					ramMaximum.setMaximum((int)ram);
				} else {
					ramMaximum.setMaximum(1024);
				}
			}
		}
		int ramMax = (Integer.parseInt(settings.getRamMax()) > ramMaximum.getMaximum()) ? ramMaximum.getMaximum() : Integer.parseInt(settings.getRamMax());
		ramMaximum.setValue(ramMax);
		currentRam.setText(getAmount());
		ramMaximum.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				currentRam.setText(getAmount());
			}
		});
		ramMaximum.addFocusListener(settingsChangeListener);

		lblRamMaximum = new JLabel(I18N.getLocaleString("RAM_MAX"));
		lblRamMaximum.setBounds(10, 95, 195, 25);
		add(lblRamMaximum);
		add(ramMaximum);
		add(currentRam);

		String[] locales = new String[I18N.localeIndices.size()];
		for(Map.Entry<Integer, String> entry : I18N.localeIndices.entrySet()) {
			Logger.logInfo("[i18n] Added " + entry.getKey().toString() + " " + entry.getValue() + " to options pane");
			locales[entry.getKey()] = I18N.localeFiles.get(entry.getValue());
		}
		locale = new JComboBox(locales);
		locale.setBounds(190, 130, 222, 25);
		locale.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				I18N.setLocale(I18N.localeIndices.get(locale.getSelectedIndex()));
				if(LaunchFrame.getInstance() != null) {
					LaunchFrame.getInstance().updateLocale();
				}
			}
		});
		locale.addFocusListener(settingsChangeListener);
		locale.setSelectedItem(I18N.localeFiles.get(settings.getLocale()));

		lblLocale = new JLabel(I18N.getLocaleString("LANGUAGE"));
		lblLocale.setBounds(10, 130, 195, 25);
		add(lblLocale);
		add(locale);

		chckbxShowConsole = new JCheckBox("Show Console?");
		chckbxShowConsole.addFocusListener(settingsChangeListener);
		chckbxShowConsole.setSelected(settings.getConsoleActive());
		chckbxShowConsole.setBounds(550, 95, 183, 25);
		add(chckbxShowConsole);

		keepLauncherOpen = new JCheckBox("Reopen launcher after exiting minecraft?");
		keepLauncherOpen.setBounds(550, 130, 300, 25);
		keepLauncherOpen.setSelected(settings.getKeepLauncherOpen());
		keepLauncherOpen.addFocusListener(settingsChangeListener);
		add(keepLauncherOpen);
		
		advancedOptionsBtn = new JButton(I18N.getLocaleString("ADVANCED_OPTIONS"));
		advancedOptionsBtn.setBounds(147, 275, 629, 29);
		advancedOptionsBtn.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				AdvancedOptionsDialog aod = new AdvancedOptionsDialog();
				aod.setVisible(true);
			}
		});
		advancedOptionsBtn.getModel().setPressed(settings.getForceUpdate());
		add(advancedOptionsBtn);
	}

	public void setInstallFolderText(String text) {
		saveSettingsInto(settings);
	}

	public void saveSettingsInto(Settings settings) {
		settings.setRamMax(String.valueOf(ramMaximum.getValue()));
		settings.setLocale(I18N.localeIndices.get(locale.getSelectedIndex()));
		settings.setConsoleActive(chckbxShowConsole.isSelected());
		settings.setKeepLauncherOpen(keepLauncherOpen.isSelected());
		settings.save();
	}

	public void updateLocale() {
		lblRamMaximum.setText(I18N.getLocaleString("RAM_MAX"));
		lblLocale.setText(I18N.getLocaleString("LANGUAGE"));
	}

	private String getAmount() {
		int ramMax = ramMaximum.getValue();
		return (ramMax >= 1024) ? Math.round((ramMax / 256) / 4) + "." + (((ramMax / 256) % 4) * 25) + " GB" : ramMax + " MB";
	}

	@Override public void onVisible() { }
}
