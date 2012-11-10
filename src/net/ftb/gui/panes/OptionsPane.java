package net.ftb.gui.panes;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.util.Map;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;

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
	private JLabel lblInstallFolder, lblRamMinimum, lblRamMaximum, lblLocale;
	private JTextField ramMinimum, ramMaximum;
	private JComboBox locale;

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
		gbl_contentPanel.columnWidths = new int[] { 87, 78, 117, 73, 97, 81, 38 };
		gbl_contentPanel.rowHeights = new int[] { 0, 0, 20, 26, 0, 29, 31, 0,0, 0, 0 };
		gbl_contentPanel.columnWeights = new double[] { 1.0, 0.0, 1.0, 1.0,1.0, 1.0, 0.0 };
		gbl_contentPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0,0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		this.setLayout(gbl_contentPanel);

		ramMaximum = new JTextField(Settings.getSettings().getRamMax());
		GridBagConstraints gbc_textField_2 = new GridBagConstraints();
		gbc_textField_2.insets = new Insets(0, 0, 5, 5);
		gbc_textField_2.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_2.gridx = 2;
		gbc_textField_2.gridy = 7;
		ramMaximum.addFocusListener(settingsChangeListener);

		ramMinimum = new JTextField(Settings.getSettings().getRamMin());
		GridBagConstraints gbc_textField_1 = new GridBagConstraints();
		gbc_textField_1.insets = new Insets(0, 0, 5, 5);
		gbc_textField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_1.gridx = 2;
		gbc_textField_1.gridy = 6;
		ramMinimum.addFocusListener(settingsChangeListener);

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
		gbc_installFolderTextField.gridwidth = 5;
		gbc_installFolderTextField.insets = new Insets(8, 8, 5, 8);
		gbc_installFolderTextField.fill = GridBagConstraints.BOTH;
		gbc_installFolderTextField.gridx = 1;
		gbc_installFolderTextField.gridy = 3;
		installFolderTextField.addFocusListener(settingsChangeListener);
		this.add(installFolderTextField, gbc_installFolderTextField);
		installFolderTextField.setColumns(10);

		GridBagConstraints gbc_installBrowseBtn = new GridBagConstraints();
		gbc_installBrowseBtn.insets = new Insets(8, 0, 5, 8);
		gbc_installBrowseBtn.gridx = 6;
		gbc_installBrowseBtn.gridy = 3;
		this.add(installBrowseBtn, gbc_installBrowseBtn);

		tglbtnForceUpdate = new JToggleButton(I18N.getLocaleString("FORCE_UPDATE"));
		tglbtnForceUpdate.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				tglbtnForceUpdate.setEnabled(false);
				removeVersionFiles();
			}
		});

		GridBagConstraints gbc_tglbtnForceUpdate = new GridBagConstraints();
		gbc_tglbtnForceUpdate.insets = new Insets(4, 8, 8, 8);
		gbc_tglbtnForceUpdate.gridwidth = 5;
		gbc_tglbtnForceUpdate.fill = GridBagConstraints.HORIZONTAL;
		gbc_tglbtnForceUpdate.gridx = 1;
		gbc_tglbtnForceUpdate.gridy = 4;
		this.add(tglbtnForceUpdate, gbc_tglbtnForceUpdate);

		lblRamMinimum = new JLabel(I18N.getLocaleString("RAM_MIN"));
		GridBagConstraints gbc_lblRamMinimum = new GridBagConstraints();
		gbc_lblRamMinimum.anchor = GridBagConstraints.EAST;
		gbc_lblRamMinimum.insets = new Insets(0, 0, 5, 5);
		gbc_lblRamMinimum.gridx = 1;
		gbc_lblRamMinimum.gridy = 6;
		this.add(lblRamMinimum, gbc_lblRamMinimum);
		this.add(ramMinimum, gbc_textField_1);
		ramMinimum.setColumns(10);

		lblRamMaximum = new JLabel(I18N.getLocaleString("RAM_MAX"));
		GridBagConstraints gbc_lblRamMaximum = new GridBagConstraints();
		gbc_lblRamMaximum.anchor = GridBagConstraints.EAST;
		gbc_lblRamMaximum.insets = new Insets(0, 0, 5, 5);
		gbc_lblRamMaximum.gridx = 1;
		gbc_lblRamMaximum.gridy = 7;
		this.add(lblRamMaximum, gbc_lblRamMaximum);
		this.add(ramMaximum, gbc_textField_2);
		ramMaximum.setColumns(10);

		Vector locales = new Vector();
		for (Map.Entry<Integer, String> entry : I18N.localeIndices.entrySet()) {
			Logger.logInfo("[i18n] Added " + entry.getKey().toString() + " " + entry.getValue() + " to options pane");
			locales.add(entry.getKey(), I18N.localeFiles.get(entry.getValue()));
		}

		locale = new JComboBox(locales);
		GridBagConstraints gbc_locale = new GridBagConstraints();
		gbc_locale.insets = new Insets(0, 0, 5, 5);
		gbc_locale.fill = GridBagConstraints.HORIZONTAL;
		gbc_locale.gridx = 2;
		gbc_locale.gridy = 8;
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
		gbc_lblLocale.gridy = 8;
		this.add(lblLocale, gbc_lblLocale);
		this.add(locale, gbc_locale);
	}

	@Override public void onVisible() { }

	public void loadSettings(Settings settings) {
		installFolderTextField.setText(settings.getInstallPath());
		tglbtnForceUpdate.getModel().setPressed(settings.getForceUpdate());
		Logger.logInfo(settings.getChannel().name());
	}

	public String getInstallFolderText() {
		return installFolderTextField.getText();
	}

	public void setInstallFolderText(String text) {
		installFolderTextField.setText(text);
		LaunchFrame.getInstance().saveSettings();
	}

	private void removeVersionFiles() {
		for(ModPack pack : ModPack.getPackArray()) {
			File temp = new File(new File(Settings.getSettings().getInstallPath(), pack.getDir()), "version");
			if(temp.exists()) {
				temp.delete();
			}
		}
	}

	public void saveSettingsInto(Settings settings) {
		settings.setInstallPath(installFolderTextField.getText());
		settings.setForceUpdate(tglbtnForceUpdate.getModel().isPressed());
		settings.setRamMax(ramMaximum.getText());
		settings.setRamMin(ramMinimum.getText());
		settings.setLocale(I18N.localeIndices.get(locale.getSelectedIndex()));
	}

	public void updateLocale() {
		lblInstallFolder.setText(I18N.getLocaleString("INSTALL_FOLDER"));
		tglbtnForceUpdate.setText(I18N.getLocaleString("FORCE_UPDATE"));
		lblRamMinimum.setText(I18N.getLocaleString("RAM_MIN"));
		lblRamMaximum.setText(I18N.getLocaleString("RAM_MAX"));
		lblLocale.setText(I18N.getLocaleString("LANGUAGE"));
	}
}
