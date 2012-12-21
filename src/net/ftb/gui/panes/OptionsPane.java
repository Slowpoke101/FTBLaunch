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
import net.ftb.locale.I18N;
import net.ftb.log.Logger;
import net.ftb.util.DownloadUtils;

public class OptionsPane extends JPanel implements ILauncherPane {
	private JToggleButton tglbtnForceUpdate, tglbtnCenterScreen;
	private JLabel lblInstallFolder, lblRamMaximum, lblLocale, currentRam, minecraftSize, lblX;
	private JSlider ramMaximum;
	private JComboBox locale, downloadServers;
	private JTextField minecraftX, minecraftY, installFolderTextField, xPosField, yPosField;
	private JCheckBox chckbxShowConsole, autoMaxCheck;

	private FocusListener settingsChangeListener = new FocusListener() {
		@Override
		public void focusLost(FocusEvent e) {
			saveSettingsInto(Settings.getSettings());
		}
		@Override public void focusGained(FocusEvent e) { }
	};

	public OptionsPane () {
		this.setBorder(new EmptyBorder(5, 5, 5, 5));
		currentRam = new JLabel();
		currentRam.setBounds(427, 114, 85, 23);
		long ram = 0;
		OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
		Method m;
		try {
			m = operatingSystemMXBean.getClass().getDeclaredMethod("getTotalPhysicalMemorySize");
			m.setAccessible(true);
			Object value = m.invoke(operatingSystemMXBean);
			if (value != null) {
				ram = Long.valueOf(value.toString()) / 1024 / 1024;
			} else {
				Logger.logWarn("Could not get RAM Value");
				ram = 8192;
			}
		} catch (Exception e) {
			Logger.logError(e.getMessage(), e);
		}

		ramMaximum = new JSlider();
		ramMaximum.setBounds(190, 114, 222, 23);
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
		int ramMax = (Integer.parseInt(Settings.getSettings().getRamMax()) > ramMaximum.getMaximum()) ? ramMaximum.getMaximum() : Integer.parseInt(Settings.getSettings().getRamMax());
		ramMaximum.setValue(ramMax);
		currentRam.setText(getAmount());
		ramMaximum.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				currentRam.setText(getAmount());
			}
		});
		ramMaximum.addFocusListener(settingsChangeListener);

		JButton installBrowseBtn = new JButton("...");
		installBrowseBtn.setBounds(786, 11, 49, 23);
		installBrowseBtn.addActionListener(new ChooseDir(this));
		setLayout(null);
		add(installBrowseBtn);

		lblInstallFolder = new JLabel(I18N.getLocaleString("INSTALL_FOLDER"));
		lblInstallFolder.setBounds(10, 11, 127, 23);
		add(lblInstallFolder);

		installFolderTextField = new JTextField();
		installFolderTextField.setBounds(147, 11, 629, 23);
		installFolderTextField.addFocusListener(settingsChangeListener);
		installFolderTextField.setColumns(10);
		installFolderTextField.setText(Settings.getSettings().getInstallPath());
		add(installFolderTextField);

		tglbtnForceUpdate = new JToggleButton(I18N.getLocaleString("FORCE_UPDATE"));
		tglbtnForceUpdate.setBounds(147, 45, 629, 29);
		tglbtnForceUpdate.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				tglbtnForceUpdate.setEnabled(false);
				saveSettingsInto(Settings.getSettings());
			}
		});
		tglbtnForceUpdate.getModel().setPressed(Settings.getSettings().getForceUpdate());
		add(tglbtnForceUpdate);

		lblRamMaximum = new JLabel(I18N.getLocaleString("RAM_MAX"));
		lblRamMaximum.setBounds(10, 114, 195, 23);
		add(lblRamMaximum);
		add(ramMaximum);
		add(currentRam);

		String[] locales = new String[I18N.localeIndices.size()];
		for (Map.Entry<Integer, String> entry : I18N.localeIndices.entrySet()) {
			Logger.logInfo("[i18n] Added " + entry.getKey().toString() + " " + entry.getValue() + " to options pane");
			locales[entry.getKey()] = entry.getValue();
		}

		locale = new JComboBox(locales);
		locale.setBounds(190, 148, 222, 23);
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
		locale.setSelectedItem(I18N.localeFiles.get(Settings.getSettings().getLocale()));

		lblLocale = new JLabel(I18N.getLocaleString("LANGUAGE"));
		lblLocale.setBounds(10, 148, 195, 23);
		add(lblLocale);
		add(locale);

		downloadServers = new JComboBox(getDownloadServerNames());
		downloadServers.setBounds(613, 115, 222, 23);
		downloadServers.addFocusListener(settingsChangeListener);
		if(DownloadUtils.serversLoaded) {
			if(DownloadUtils.downloadServers.containsKey(Settings.getSettings().getDownloadServer())) {
				downloadServers.setSelectedItem(Settings.getSettings().getDownloadServer());
			}
		}
		add(downloadServers);

		JLabel downloadLocation = new JLabel("Download Location");
		downloadLocation.setBounds(490, 118, 110, 14);
		add(downloadLocation);

		chckbxShowConsole = new JCheckBox("Show Console?");
		chckbxShowConsole.addFocusListener(settingsChangeListener);
		chckbxShowConsole.setSelected(Boolean.parseBoolean(Settings.getSettings().getConsoleActive()));
		chckbxShowConsole.setBounds(613, 148, 183, 23);
		add(chckbxShowConsole);
	}

	public void setDownloadServers() {
		String downloadserver = Settings.getSettings().getDownloadServer();
		downloadServers.removeAllItems();
		for(String server : DownloadUtils.downloadServers.keySet()) {
			downloadServers.addItem(server);
		}
		if(DownloadUtils.downloadServers.containsKey(downloadserver)) {
			downloadServers.setSelectedItem(downloadserver);
		}
	}

	public String[] getDownloadServerNames() {
		if(!DownloadUtils.serversLoaded) {
			Logger.logWarn("Servers not loaded yet.");
			return new String[] { "Automatic" };
		} else {
			Logger.logInfo("Servers are loaded, inserting into combo box.");
			String[] out = new String[DownloadUtils.downloadServers.size()];
			for(int i = 0; i < out.length; i++) {
				out[i] = String.valueOf(DownloadUtils.downloadServers.keySet().toArray()[i]);
			}
			return out;
		}
	}

	private class documentFilter extends PlainDocument {
		public documentFilter(final String pattern) {
			this.setDocumentFilter(new DocumentFilter() {
				@Override
				public void insertString(FilterBypass fb, int off, String str, AttributeSet attr) throws BadLocationException {
					fb.insertString(off, str.replaceAll(pattern, ""), attr);
				} 
				@Override
				public void replace(FilterBypass fb, int off, int len, String str, AttributeSet attr) throws BadLocationException {
					fb.replace(off, len, str.replaceAll(pattern, ""), attr);
				}
			});
		}
	}

	public void setInstallFolderText(String text) {
		installFolderTextField.setText(text);
		saveSettingsInto(Settings.getSettings());
	}

	public void saveSettingsInto(Settings settings) {
		settings.setInstallPath(installFolderTextField.getText());
		settings.setForceUpdate(!tglbtnForceUpdate.isEnabled());
		settings.setRamMax(String.valueOf(ramMaximum.getValue()));
		settings.setLocale(I18N.localeIndices.get(locale.getSelectedIndex()));
		settings.setDownloadServer(String.valueOf(downloadServers.getItemAt(downloadServers.getSelectedIndex())));
		settings.setConsoleActive(String.valueOf(chckbxShowConsole.isSelected()));
		settings.save();
	}

	public void updateLocale() {
		lblInstallFolder.setText(I18N.getLocaleString("INSTALL_FOLDER"));
		tglbtnForceUpdate.setText(I18N.getLocaleString("FORCE_UPDATE"));;
		lblRamMaximum.setText(I18N.getLocaleString("RAM_MAX"));
		lblLocale.setText(I18N.getLocaleString("LANGUAGE"));
	}

	private String getAmount() {
		int ramMax = ramMaximum.getValue();
		return (ramMax >= 1024) ? Math.round((ramMax / 256) / 4) + "." + (((ramMax / 256) % 4) * 25) + " GB" : ramMax + " MB";
	}

	@Override public void onVisible() { }
}
