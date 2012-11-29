package net.ftb.gui.panes;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
	private JLabel minecraftSize;
	private JTextField minecraftX;
	private JLabel lblX;
	private JTextField minecraftY;

	private FocusListener settingsChangeListener = new FocusListener() {
		@Override
		public void focusLost(FocusEvent e) {
			saveSettingsInto(Settings.getSettings());
		}
		@Override public void focusGained(FocusEvent e) { }
	};
	private JTextField xPosField;
	private JTextField yPosField;


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
		} catch (NoSuchMethodException e1) {
		} catch (IllegalArgumentException e1) {
		} catch (IllegalAccessException e1) {
		} catch (InvocationTargetException e1) { }

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
		minecraftX.setText(Settings.getSettings().getMinecraftX());
		minecraftX.addFocusListener(settingsChangeListener);
		add(minecraftX);
		
		lblX = new JLabel("x");
		lblX.setBounds(322, 182, 6, 20);
		add(lblX);
		
		minecraftY = new JTextField();
		minecraftY.setBounds(352, 182, 85, 23);
		minecraftY.setText(Settings.getSettings().getMinecraftY());
		minecraftY.addFocusListener(settingsChangeListener);
		add(minecraftY);
		minecraftY.setColumns(5);
		
		JLabel minecraftPos = new JLabel("Position of Minecraft Window");
		minecraftPos.setBounds(10, 216, 195, 23);
		add(minecraftPos);
		
		xPosField = new JTextField();
		xPosField.setBounds(214, 217, 85, 22);
		xPosField.setText(Settings.getSettings().getMinecraftXPos());
		xPosField.addFocusListener(settingsChangeListener);
		add(xPosField);
		xPosField.setColumns(10);
		
		JLabel lblX_1 = new JLabel("x");
		lblX_1.setBounds(322, 216, 6, 23);
		add(lblX_1);
		
		yPosField = new JTextField();
		yPosField.setBounds(352, 216, 85, 23);
		yPosField.setText(Settings.getSettings().getMinecraftYPos());
		yPosField.addFocusListener(settingsChangeListener);
		add(yPosField);
		yPosField.setColumns(10);
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
		settings.setForceUpdate(!tglbtnForceUpdate.isEnabled());
		settings.setRamMax(String.valueOf(ramMaximum.getValue()));
		settings.setLocale(I18N.localeIndices.get(locale.getSelectedIndex()));
		settings.setMinecraftX(minecraftX.getText());
		settings.setMinecraftY(minecraftY.getText());
		settings.setMinecraftXPos(xPosField.getText());
		settings.setMinecraftYPos(yPosField.getText());
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
