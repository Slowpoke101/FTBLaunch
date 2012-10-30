package net.ftb.gui.panes;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JButton;
import javax.swing.JLabel;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;

import net.ftb.data.Settings;
import net.ftb.gui.ChooseDir;
import net.ftb.gui.LaunchFrame;

public class OptionsPane extends JPanel implements ILauncherPane {
	
	private static final long serialVersionUID = 1L;
	
	public static JTextField installFolderTextField;
	private JToggleButton tglbtnForceUpdate;
	private JTextField ramMinimum;
	private JTextField ramMaximum;
	
	
	public OptionsPane () {
		this.setBorder(new EmptyBorder(5, 5, 5, 5));
		//optionsPane.add(backgroundImage2);
		//optionsPane.setBackground(back);

		
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 87, 78, 117, 73, 97, 81, 38 };
		gbl_contentPanel.rowHeights = new int[] { 0, 0, 20, 26, 0, 29, 31, 0,0, 0, 0 };
		gbl_contentPanel.columnWeights = new double[] { 1.0, 0.0, 1.0, 1.0,1.0, 1.0, 0.0 };
		gbl_contentPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0,0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		this.setLayout(gbl_contentPanel);

		
		ramMaximum = new JTextField();
		GridBagConstraints gbc_textField_2 = new GridBagConstraints();
		gbc_textField_2.insets = new Insets(0, 0, 5, 5);
		gbc_textField_2.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_2.gridx = 2;
		gbc_textField_2.gridy = 7;
		ramMaximum.setText(Settings.getSettings().getRamMax());
		ramMaximum.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				LaunchFrame.ramMax = Integer.parseInt(ramMaximum.getText());
				LaunchFrame.getInstance().saveSettings();
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				
			}
		});
				
		ramMinimum = new JTextField();
		GridBagConstraints gbc_textField_1 = new GridBagConstraints();
		gbc_textField_1.insets = new Insets(0, 0, 5, 5);
		gbc_textField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_1.gridx = 2;
		gbc_textField_1.gridy = 6;
		ramMinimum.setText(Settings.getSettings().getRamMin());
		ramMinimum.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				LaunchFrame.ramMin = Integer.parseInt(ramMinimum.getText());
				LaunchFrame.getInstance().saveSettings();
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				
			}
		});
		
		JButton installBrowseBtn = new JButton("...");
		installBrowseBtn.addActionListener(new ChooseDir(this));
		
		JLabel lblInstallFolder = new JLabel("Install folder:");
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
		installFolderTextField.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent arg0) {
				LaunchFrame.getInstance().saveSettings();
			}
			
			@Override
			public void focusGained(FocusEvent arg0) {
				
			}
		});
		this.add(installFolderTextField, gbc_installFolderTextField);
		installFolderTextField.setColumns(10);
		
		GridBagConstraints gbc_installBrowseBtn = new GridBagConstraints();
		gbc_installBrowseBtn.insets = new Insets(8, 0, 5, 8);
		gbc_installBrowseBtn.gridx = 6;
		gbc_installBrowseBtn.gridy = 3;
		this.add(installBrowseBtn, gbc_installBrowseBtn);
		
		tglbtnForceUpdate = new JToggleButton("Force update?");
		GridBagConstraints gbc_tglbtnForceUpdate = new GridBagConstraints();
		gbc_tglbtnForceUpdate.insets = new Insets(4, 8, 8, 8);
		gbc_tglbtnForceUpdate.gridwidth = 5;
		gbc_tglbtnForceUpdate.fill = GridBagConstraints.HORIZONTAL;
		gbc_tglbtnForceUpdate.gridx = 1;
		gbc_tglbtnForceUpdate.gridy = 4;
		this.add(tglbtnForceUpdate, gbc_tglbtnForceUpdate);
		
		JLabel lblRamMinimum = new JLabel("RAM Minimum (M):");
		GridBagConstraints gbc_lblRamMinimum = new GridBagConstraints();
		gbc_lblRamMinimum.anchor = GridBagConstraints.EAST;
		gbc_lblRamMinimum.insets = new Insets(0, 0, 5, 5);
		gbc_lblRamMinimum.gridx = 1;
		gbc_lblRamMinimum.gridy = 6;
		this.add(lblRamMinimum, gbc_lblRamMinimum);
		this.add(ramMinimum, gbc_textField_1);
		ramMinimum.setColumns(10);
		
		JLabel lblRamMaximum = new JLabel("RAM Maximum (M):");
		GridBagConstraints gbc_lblRamMaximum = new GridBagConstraints();
		gbc_lblRamMaximum.anchor = GridBagConstraints.EAST;
		gbc_lblRamMaximum.insets = new Insets(0, 0, 5, 5);
		gbc_lblRamMaximum.gridx = 1;
		gbc_lblRamMaximum.gridy = 7;
		this.add(lblRamMaximum, gbc_lblRamMaximum);
		this.add(ramMaximum, gbc_textField_2);
		ramMaximum.setColumns(10);
		
	}
	
	
	@Override
	public void onVisible() {
		// TODO Auto-generated method stub
		
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
		settings.setForceUpdate(tglbtnForceUpdate.getModel().isPressed());
		settings.setRamMax(ramMaximum.getText());
		settings.setRamMin(ramMinimum.getText());
	}

}
