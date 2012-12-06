package net.ftb.gui.panes;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JButton;
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
import javax.swing.JCheckBox;

public class OptionsPane extends JPanel implements ILauncherPane {
	private static final long serialVersionUID = 1L;

	protected static JTextField installFolderTextField;
	private JToggleButton tglbtnForceUpdate;
	private JLabel lblInstallFolder, lblRamMaximum, lblLocale, currentRam;
	private JSlider ramMaximum;
	@SuppressWarnings("rawtypes")
	private JComboBox locale;
	private JLabel minecraftSize;
	private JTextField minecraftX;
	private JLabel lblX;
	private JTextField minecraftY;
	@SuppressWarnings("rawtypes")
	private JComboBox downloadServers;
	private JCheckBox chckbxShowConsole;
	private JToggleButton tglbtnCenterScreen;

	private FocusListener settingsChangeListener = new FocusListener() {
		@Override
		public void focusLost(FocusEvent e) {
			saveSettingsInto(Settings.getSettings());
		}
		@Override public void focusGained(FocusEvent e) { }
	};
	private JTextField xPosField;
	private JTextField yPosField;
	private JCheckBox autoMaxCheck;


	@SuppressWarnings({ "rawtypes", "unchecked" })
	public OptionsPane () {
		this.setBorder(new EmptyBorder(5, 5, 5, 5));

		currentRam = new JLabel();
		currentRam.setBounds(447, 114, 85, 23);
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
				System.out.println("Could not get RAM Value");
				ram = 8192;
			}
		} catch (SecurityException e1) {
			Logger.logError(e1.getMessage(), e1);
		} catch (NoSuchMethodException e1) {
			Logger.logError(e1.getMessage(), e1);
		} catch (IllegalArgumentException e1) {
			Logger.logError(e1.getMessage(), e1);
		} catch (IllegalAccessException e1) {
			Logger.logError(e1.getMessage(), e1);
		} catch (InvocationTargetException e1) {
			Logger.logError(e1.getMessage(), e1);
		}

		ramMaximum = new JSlider();
		ramMaximum.setBounds(215, 114, 222, 23);
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
		int ramMax = Integer.parseInt(Settings.getSettings().getRamMax());
		if(ramMax > ramMaximum.getMaximum()) {
			ramMaximum.setValue(ramMaximum.getMaximum());
		} else {
			ramMaximum.setValue(ramMax);
		}
		currentRam.setText(getAmount());
		ramMaximum.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				currentRam.setText(getAmount());
			}
		});
		ramMaximum.addFocusListener(settingsChangeListener);
		ramMaximum.addFocusListener(settingsChangeListener);

		JButton installBrowseBtn = new JButton("...");
		installBrowseBtn.setBounds(786, 11, 49, 23);
		installBrowseBtn.addActionListener(new ChooseDir(this));
		setLayout(null);

		lblInstallFolder = new JLabel(I18N.getLocaleString("INSTALL_FOLDER"));
		lblInstallFolder.setBounds(10, 11, 127, 23);
		this.add(lblInstallFolder);

		installFolderTextField = new JTextField();
		installFolderTextField.setBounds(147, 11, 629, 23);
		installFolderTextField.addFocusListener(settingsChangeListener);
		add(installFolderTextField);
		installFolderTextField.setColumns(10);
		this.add(installBrowseBtn);

		tglbtnForceUpdate = new JToggleButton(I18N.getLocaleString("FORCE_UPDATE"));
		tglbtnForceUpdate.setBounds(147, 45, 629, 29);
		tglbtnForceUpdate.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				tglbtnForceUpdate.setEnabled(false);
				saveSettingsInto(Settings.getSettings());
			}
		});
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
		locale.setBounds(215, 148, 222, 23);
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
		minecraftX.setBounds(214, 182, 85, 22);
		minecraftX.setDocument(new documentFilter("\\D++"));
		minecraftX.setText(Settings.getSettings().getMinecraftX());
		minecraftX.addFocusListener(settingsChangeListener);
		add(minecraftX);

		lblX = new JLabel("x");
		lblX.setBounds(322, 182, 6, 20);
		add(lblX);

		minecraftY = new JTextField();
		minecraftY.setBounds(352, 182, 85, 23);
		minecraftY.setDocument(new documentFilter("\\D++"));
		minecraftY.setText(Settings.getSettings().getMinecraftY());
		minecraftY.addFocusListener(settingsChangeListener);
		add(minecraftY);
		minecraftY.setColumns(5);

		JLabel minecraftPos = new JLabel("Position of Minecraft Window");
		minecraftPos.setBounds(10, 216, 195, 23);
		add(minecraftPos);

		xPosField = new JTextField();
		xPosField.setBounds(214, 217, 85, 22);
		xPosField.setDocument(new documentFilter("\\D++"));
		xPosField.setText(Settings.getSettings().getMinecraftXPos());
		xPosField.addFocusListener(settingsChangeListener);
		add(xPosField);
		xPosField.setColumns(10);

		JLabel lblX_1 = new JLabel("x");
		lblX_1.setBounds(322, 216, 6, 23);
		add(lblX_1);

		yPosField = new JTextField();
		yPosField.setBounds(352, 216, 85, 23);
		yPosField.setDocument(new documentFilter("\\D++"));
		yPosField.setText(Settings.getSettings().getMinecraftYPos());
		yPosField.addFocusListener(settingsChangeListener);
		add(yPosField);
		yPosField.setColumns(10);
		
		tglbtnCenterScreen = new JToggleButton("Center Window on Screen");
		tglbtnCenterScreen.setBounds(215, 252, 222, 23);
		tglbtnCenterScreen.setSelected(Boolean.parseBoolean(Settings.getSettings().getCenterWindow()));
		tglbtnCenterScreen.addFocusListener(settingsChangeListener);
		add(tglbtnCenterScreen);

		downloadServers = new JComboBox(getDownloadServerNames());
		downloadServers.setBounds(652, 115, 183, 20);
		downloadServers.addFocusListener(settingsChangeListener);
		add(downloadServers);

		JLabel downloadLocation = new JLabel("Download Location");
		downloadLocation.setBounds(550, 118, 100, 14);
		add(downloadLocation);
		
		chckbxShowConsole = new JCheckBox("Show Console?");
		chckbxShowConsole.addFocusListener(settingsChangeListener);
		chckbxShowConsole.setSelected(Boolean.parseBoolean(Settings.getSettings().getConsoleActive()));
		chckbxShowConsole.setBounds(550, 148, 183, 23);
		add(chckbxShowConsole);
		
		//autoMaxCheck
		autoMaxCheck = new JCheckBox("Automaticly Maximize?");
		autoMaxCheck.addFocusListener(settingsChangeListener);
		autoMaxCheck.setSelected(Boolean.parseBoolean(Settings.getSettings().getAutoMaximize()));
		autoMaxCheck.setBounds(550, 184, 183, 23);
		add(autoMaxCheck);
	}

	public String[] getDownloadServerNames() {

		String[] servers = {  };
		return servers;
	}

	@Override public void onVisible() { }

	private class documentFilter extends PlainDocument {
		private static final long serialVersionUID = 1L;

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

	public void loadSettings(Settings settings) {
		installFolderTextField.setText(settings.getInstallPath());
		tglbtnForceUpdate.getModel().setPressed(settings.getForceUpdate());
	}

	public String getInstallFolderText() {
		return installFolderTextField.getText();
	}

	public void setInstallFolderText(String text) {
		installFolderTextField.setText(text);
		LaunchFrame.getInstance().saveSettings();
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
		settings.setCenterWindow(tglbtnCenterScreen.isSelected() ? "true" : "false");
		settings.setDownlaodServer(Integer.toString(downloadServers.getSelectedIndex()));
		settings.setConsoleActive(chckbxShowConsole.isSelected() ? "true" : "false");
		settings.setAutoMaximize(autoMaxCheck.isSelected() ? "true" : "false");
	}

	public void updateLocale() {
		lblInstallFolder.setText(I18N.getLocaleString("INSTALL_FOLDER"));
		tglbtnForceUpdate.setText(I18N.getLocaleString("FORCE_UPDATE"));;
		lblRamMaximum.setText(I18N.getLocaleString("RAM_MAX"));
		lblLocale.setText(I18N.getLocaleString("LANGUAGE"));
	}

	private String getAmount() {
		String result = "";
		if(ramMaximum.getValue() >= 1024) {
			int quaters = ramMaximum.getValue() / 256;
			result = Math.round(quaters / 4) + "." + ((quaters % 4) * 25) + " GB";
		} else {
			result = ramMaximum.getValue() + " MB";
		}
		return result;
	}
}
