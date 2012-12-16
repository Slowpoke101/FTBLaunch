package net.ftb.gui.panes;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
import net.ftb.util.OSUtils;
import net.ftb.util.OSUtils.OS;

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
	private JTextField javaInstallDir;

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

		ArrayList<String> locales = new ArrayList<String>();
		for (Map.Entry<Integer, String> entry : I18N.localeIndices.entrySet()) {
			Logger.logInfo("[i18n] Added " + entry.getKey().toString() + " " + entry.getValue() + " to options pane");
			locales.add(entry.getKey(), I18N.localeFiles.get(entry.getValue()));
		}

		locale = new JComboBox(locales.toArray());
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

		minecraftSize = new JLabel("Size for Minecraft Window");
		minecraftSize.setBounds(10, 182, 195, 23);
		add(minecraftSize);

		minecraftX = new JTextField();
		minecraftX.setBounds(190, 182, 85, 22);
		minecraftX.setDocument(new documentFilter("\\D++"));
		minecraftX.setText(Settings.getSettings().getMinecraftX());
		minecraftX.addFocusListener(settingsChangeListener);
		add(minecraftX);

		lblX = new JLabel("x");
		lblX.setBounds(297, 182, 6, 20);
		add(lblX);

		minecraftY = new JTextField();
		minecraftY.setBounds(327, 182, 85, 23);
		minecraftY.setDocument(new documentFilter("\\D++"));
		minecraftY.setText(Settings.getSettings().getMinecraftY());
		minecraftY.addFocusListener(settingsChangeListener);
		add(minecraftY);
		minecraftY.setColumns(5);

		JLabel minecraftPos = new JLabel("Position of Minecraft Window");
		minecraftPos.setBounds(10, 216, 195, 23);
		add(minecraftPos);

		xPosField = new JTextField();
		xPosField.setBounds(190, 217, 85, 22);
		xPosField.setDocument(new documentFilter("\\D++"));
		xPosField.setText(Settings.getSettings().getMinecraftXPos());
		xPosField.addFocusListener(settingsChangeListener);
		add(xPosField);
		xPosField.setColumns(10);

		JLabel lblX_1 = new JLabel("x");
		lblX_1.setBounds(297, 216, 6, 23);
		add(lblX_1);

		yPosField = new JTextField();
		yPosField.setBounds(327, 216, 85, 23);
		yPosField.setDocument(new documentFilter("\\D++"));
		yPosField.setText(Settings.getSettings().getMinecraftYPos());
		yPosField.addFocusListener(settingsChangeListener);
		add(yPosField);
		yPosField.setColumns(10);

		tglbtnCenterScreen = new JToggleButton("Center Window on Screen");
		tglbtnCenterScreen.setBounds(190, 252, 222, 23);
		tglbtnCenterScreen.setSelected(Boolean.parseBoolean(Settings.getSettings().getCenterWindow()));
		tglbtnCenterScreen.addFocusListener(settingsChangeListener);
		add(tglbtnCenterScreen);

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

		autoMaxCheck = new JCheckBox("Automatically Maximize?");
		autoMaxCheck.addFocusListener(settingsChangeListener);
		autoMaxCheck.setSelected(Boolean.parseBoolean(Settings.getSettings().getAutoMaximize()));
		autoMaxCheck.setBounds(613, 184, 183, 23);
		add(autoMaxCheck);
		
		JLabel javaLocLabel = new JLabel("Java Location");
		javaLocLabel.setBounds(490, 220, 110, 14);
		add(javaLocLabel);
		
		javaInstallDir = new JTextField();
		javaInstallDir.setEditable(false);
		String javaInstall = Settings.getSettings().getJavaInstall();
		javaInstallDir.setText(javaInstall);
		javaInstallDir.setToolTipText(javaInstall);
		javaInstallDir.setBounds(613, 220, 230, 23);
		add(javaInstallDir);

		JButton dirChooserButton = new JButton("Change");
		dirChooserButton.setBounds(763, 240, 80, 23);
		dirChooserButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(Settings.getSettings().getJavaInstall());
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int option = chooser.showOpenDialog(OptionsPane.this);
				if (JFileChooser.APPROVE_OPTION == option) {
					File selectedDir = chooser.getSelectedFile();
					String javaExecutableName = "java";
					if (OS.WINDOWS.equals(OSUtils.getCurrentOS())) {
						javaExecutableName += ".exe";
					}
					File javaExecutable = new File(selectedDir + System.getProperty("file.separator") + "bin" + System.getProperty("file.separator") + javaExecutableName);
					if (javaExecutable.exists()) {
						javaInstallDir.setText(selectedDir.getAbsolutePath());
						saveSettingsInto(Settings.getSettings());
					} else {
						JOptionPane.showMessageDialog(OptionsPane.this, "The selected directoy is not a proper Java installation.", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		add(dirChooserButton);
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
		settings.setMinecraftX(minecraftX.getText());
		settings.setMinecraftY(minecraftY.getText());
		settings.setMinecraftXPos(xPosField.getText());
		settings.setMinecraftYPos(yPosField.getText());
		settings.setCenterWindow(String.valueOf(tglbtnCenterScreen.isSelected()));
		settings.setDownlaodServer(String.valueOf(downloadServers.getItemAt(downloadServers.getSelectedIndex())));
		settings.setConsoleActive(String.valueOf(chckbxShowConsole.isSelected()));
		settings.setAutoMaximize(String.valueOf(autoMaxCheck.isSelected()));
		settings.setJavaInstall(javaInstallDir.getText());
		try {
			settings.save();
		} catch (IOException e) { }
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
