package net.ftb.gui.panes;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Vector;

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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.gui.ChooseDir;
import net.ftb.gui.LaunchFrame;
import net.ftb.locale.I18N;
import net.ftb.log.Logger;

public class OptionsPane extends JPanel implements ILauncherPane {
	private static final long serialVersionUID = 1L;

	protected static JTextField installFolderTextField;
	private JToggleButton tglbtnForceUpdate;
	private JLabel lblInstallFolder, lblRamMaximum, lblLocale, currentRam;
	private JSlider ramMaximum;
	private JComboBox locale;
//	private JLabel minecraftSize;
//	private JTextField minecraftX;
//	private JLabel lblX;
//	private JTextField minecraftY;

	private FocusListener settingsChangeListener = new FocusListener() {
		@Override
		public void focusLost(FocusEvent e) {
			LaunchFrame.getInstance().saveSettings();
		}
		@Override public void focusGained(FocusEvent e) { }
	};


	public OptionsPane () {
		this.setBorder(new EmptyBorder(5, 5, 5, 5));

		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 87, 78, 117, 73, 97, 81, 38 }; // 87, 78, 117, 73, 86, 32, 14, 25, 37 };
		gbl_contentPanel.rowHeights = new int[] { 0, 0, 20, 26, 0, 29, 31, 0,0, 0, 0 };
		gbl_contentPanel.columnWeights = new double[] { 1.0, 0.0, 1.0, 1.0,1.0, 1.0, 0.0 }; // 1.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0 };
		gbl_contentPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0,0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		setLayout(gbl_contentPanel);

		currentRam = new JLabel();

		long ram = 0;

		OperatingSystemMXBean operatingSystemMXBean = 
				ManagementFactory.getOperatingSystemMXBean();

		Method m;
		try {
			m = operatingSystemMXBean.getClass().
					getDeclaredMethod("getTotalPhysicalMemorySize");

			m.setAccessible(true);

			Object value = m.invoke(operatingSystemMXBean);

			if (value != null) {
				ram = Long.valueOf(value.toString()) / 1024 / 1024;
			} else {
				System.out.println("Could not get RAM Value");
				ram = 8192;
			}
		} catch (SecurityException e1) {
			e1.printStackTrace();
		} catch (NoSuchMethodException e1) {
			e1.printStackTrace();
		} catch (IllegalArgumentException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		} catch (InvocationTargetException e1) {
			e1.printStackTrace();
		}

		ramMaximum = new JSlider();
		ramMaximum.setSnapToTicks(true);
		ramMaximum.setMajorTickSpacing(256);
		ramMaximum.setMinorTickSpacing(256);
		ramMaximum.setMinimum(256);
		String vmType = System.getProperty("sun.arch.data.model");
		if(vmType != null){
			if(vmType.equals("64")) {
				ramMaximum.setMaximum((int)ram);
			} else if(vmType.equals("32")) {
				if(ram < 1536) {
					ramMaximum.setMaximum((int)ram);
				} else {
					ramMaximum.setMaximum(1536);
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

		GridBagConstraints gbc_lblCurrentRam = new GridBagConstraints();
		gbc_lblCurrentRam.anchor = GridBagConstraints.WEST;
		gbc_lblCurrentRam.insets = new Insets(0, 0, 5, 5);
		gbc_lblCurrentRam.gridx = 3;
		gbc_lblCurrentRam.gridy = 6;

		GridBagConstraints gbc_textField_2 = new GridBagConstraints();
		gbc_textField_2.insets = new Insets(0, 0, 5, 5);
		gbc_textField_2.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_2.gridx = 2;
		gbc_textField_2.gridy = 6;
		ramMaximum.addFocusListener(settingsChangeListener);

		JButton installBrowseBtn = new JButton("...");
		installBrowseBtn.addActionListener(new ChooseDir(this));

		lblInstallFolder = new JLabel(I18N.getLocaleString("INSTALL_FOLDER"));
		GridBagConstraints gbc_lblInstallFolder = new GridBagConstraints();
		gbc_lblInstallFolder.anchor = GridBagConstraints.EAST;
		gbc_lblInstallFolder.insets = new Insets(8, 8, 5, 5);
		gbc_lblInstallFolder.gridx = 0;
		gbc_lblInstallFolder.gridy = 3;
		this.add(lblInstallFolder, gbc_lblInstallFolder);

		installFolderTextField = new JTextField();
		GridBagConstraints gbc_installFolderTextField = new GridBagConstraints();
		gbc_installFolderTextField.gridwidth = 5; // 7;
		gbc_installFolderTextField.insets = new Insets(8, 8, 5, 8);
		gbc_installFolderTextField.fill = GridBagConstraints.BOTH;
		gbc_installFolderTextField.gridx = 1;
		gbc_installFolderTextField.gridy = 3;
		installFolderTextField.addFocusListener(settingsChangeListener);
		add(installFolderTextField, gbc_installFolderTextField);
		installFolderTextField.setColumns(10);

		GridBagConstraints gbc_installBrowseBtn = new GridBagConstraints();
		gbc_installBrowseBtn.insets = new Insets(8, 0, 5, 8);
		gbc_installBrowseBtn.gridx = 6; // 8;
		gbc_installBrowseBtn.gridy = 3;
		this.add(installBrowseBtn, gbc_installBrowseBtn);

		tglbtnForceUpdate = new JToggleButton(I18N.getLocaleString("FORCE_UPDATE"));
		tglbtnForceUpdate.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				tglbtnForceUpdate.setEnabled(false);
			}
		});

		GridBagConstraints gbc_tglbtnForceUpdate = new GridBagConstraints();
		gbc_tglbtnForceUpdate.insets = new Insets(4, 8, 8, 8);
		gbc_tglbtnForceUpdate.gridwidth = 5; // 7;
		gbc_tglbtnForceUpdate.fill = GridBagConstraints.HORIZONTAL;
		gbc_tglbtnForceUpdate.gridx = 1;
		gbc_tglbtnForceUpdate.gridy = 4;
		add(tglbtnForceUpdate, gbc_tglbtnForceUpdate);

		lblRamMaximum = new JLabel(I18N.getLocaleString("RAM_MAX"));
		GridBagConstraints gbc_lblRamMaximum = new GridBagConstraints();
		gbc_lblRamMaximum.anchor = GridBagConstraints.EAST;
		gbc_lblRamMaximum.insets = new Insets(0, 0, 5, 5);
		gbc_lblRamMaximum.gridx = 1;
		gbc_lblRamMaximum.gridy = 6;
		add(lblRamMaximum, gbc_lblRamMaximum);
		add(ramMaximum, gbc_textField_2);
		add(currentRam, gbc_lblCurrentRam);

		Vector<String> locales = new Vector<String>();
		for (Map.Entry<Integer, String> entry : I18N.localeIndices.entrySet()) {
			Logger.logInfo("[i18n] Added " + entry.getKey().toString() + " " + entry.getValue() + " to options pane");
			locales.add(entry.getKey(), I18N.localeFiles.get(entry.getValue()));
		}

		locale = new JComboBox(locales);
		GridBagConstraints gbc_locale = new GridBagConstraints();
		gbc_locale.insets = new Insets(0, 0, 5, 5);
		gbc_locale.fill = GridBagConstraints.HORIZONTAL;
		gbc_locale.gridx = 2;
		gbc_locale.gridy = 7;
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
		GridBagConstraints gbc_lblLocale = new GridBagConstraints();
		gbc_lblLocale.anchor = GridBagConstraints.EAST;
		gbc_lblLocale.insets = new Insets(0, 0, 5, 5);
		gbc_lblLocale.gridx = 1;
		gbc_lblLocale.gridy = 7;
		add(lblLocale, gbc_lblLocale);
		add(locale, gbc_locale);
		
//		minecraftSize = new JLabel("Size for Minecraft Window");
//		GridBagConstraints gbc_lblmcsize = new GridBagConstraints();
//		gbc_lblmcsize.anchor = GridBagConstraints.WEST;
//		gbc_lblmcsize.fill = GridBagConstraints.VERTICAL;
//		gbc_lblmcsize.gridwidth = 4;
//		gbc_lblmcsize.insets = new Insets(0, 0, 5, 5);
//		gbc_lblmcsize.gridx = 4;
//		gbc_lblmcsize.weighty = 2;
//		gbc_lblmcsize.gridy = 6;
//		add(minecraftSize, gbc_lblmcsize);
//		
//		minecraftX = new JTextField();
//		GridBagConstraints gbc_mcx = new GridBagConstraints();
//		gbc_mcx.fill = GridBagConstraints.HORIZONTAL;
//		gbc_mcx.insets = new Insets(0, 0, 5, 5);
//		gbc_mcx.gridx = 4;
//		gbc_mcx.gridy = 7;
//		minecraftX.setText(Settings.getSettings().getMinecraftX());
//		minecraftX.addFocusListener(new FocusListener() {
//			
//			@Override
//			public void focusLost(FocusEvent arg0) {
//				Settings.getSettings().setMinecraftX(minecraftX.getText());
//			}
//			
//			@Override
//			public void focusGained(FocusEvent arg0) {}
//		});
//		add(minecraftX, gbc_mcx);
//		
//		lblX = new JLabel("x");
//		GridBagConstraints gbc_lblX = new GridBagConstraints();
//		gbc_lblX.fill = GridBagConstraints.VERTICAL;
//		gbc_lblX.insets = new Insets(0, 0, 5, 5);
//		gbc_lblX.gridx = 5;
//		gbc_lblX.gridy = 7;
//		add(lblX, gbc_lblX);
//		
//		minecraftY = new JTextField();
//		GridBagConstraints gbc_mcy = new GridBagConstraints();
//		gbc_mcy.insets = new Insets(0, 0, 5, 5);
//		gbc_mcy.fill = GridBagConstraints.BOTH;
//		gbc_mcy.gridx = 6;
//		gbc_mcy.gridy = 7;
//		minecraftY.setText(Settings.getSettings().getMinecraftY());
//		minecraftY.addFocusListener(new FocusListener() {
//			
//			@Override
//			public void focusLost(FocusEvent e) {
//				Settings.getSettings().setMinecraftY(minecraftY.getText());
//			}
//			
//			@Override
//			public void focusGained(FocusEvent e) {}
//		});
//		add(minecraftY, gbc_mcy);
//		minecraftY.setColumns(5);
	}

	@Override public void onVisible() { }

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
		settings.setForceUpdate(tglbtnForceUpdate.getModel().isPressed());
		settings.setRamMax(String.valueOf(ramMaximum.getValue()));
		settings.setLocale(I18N.localeIndices.get(locale.getSelectedIndex()));
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
