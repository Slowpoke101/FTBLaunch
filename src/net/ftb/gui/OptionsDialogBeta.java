package net.ftb.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;

import net.ftb.data.Settings;

public class OptionsDialogBeta extends JDialog
{
	private static final long serialVersionUID = 1L;
	
	private final JPanel contentPanel = new JPanel();
	public static JTextField installFolderTextField;
	private JToggleButton tglbtnForceUpdate;
	private JTextField ramMinimum;
	private JTextField ramMaximum;
	
	public static int ramMin = 512;
	public static int ramMax = 1024;
	
	/**
	 * Create the dialog.
	 */
	public OptionsDialogBeta()
	{
		setIconImage(Toolkit.getDefaultToolkit().getImage("res//logo.png"));
		setModal(true);
		setBounds(100, 100, 400, 200);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{86, 71, 73, 49, 38};
		gbl_contentPanel.rowHeights = new int[] {0, 0, 26, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{1.0, 0.0, 1.0, 1.0, 0.0};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
	
		JLabel lblInstallFolder = new JLabel("Install folder:");
		GridBagConstraints gbc_lblInstallFolder = new GridBagConstraints();
		gbc_lblInstallFolder.anchor = GridBagConstraints.EAST;
		gbc_lblInstallFolder.insets = new Insets(8, 8, 5, 5);
		gbc_lblInstallFolder.gridx = 0;
		gbc_lblInstallFolder.gridy = 0;
		contentPanel.add(lblInstallFolder, gbc_lblInstallFolder);
			
		installFolderTextField = new JTextField();
		GridBagConstraints gbc_installFolderTextField = new GridBagConstraints();
		gbc_installFolderTextField.gridwidth = 3;
		gbc_installFolderTextField.insets = new Insets(8, 8, 5, 8);
		gbc_installFolderTextField.fill = GridBagConstraints.BOTH;
		gbc_installFolderTextField.gridx = 1;
		gbc_installFolderTextField.gridy = 0;
		contentPanel.add(installFolderTextField, gbc_installFolderTextField);
		installFolderTextField.setColumns(10);

		JButton installBrowseBtn = new JButton("...");
		installBrowseBtn.addActionListener(new ChooseDir());
		
		GridBagConstraints gbc_installBrowseBtn = new GridBagConstraints();
		gbc_installBrowseBtn.insets = new Insets(8, 0, 5, 8);
		gbc_installBrowseBtn.gridx = 4;
		gbc_installBrowseBtn.gridy = 0;
		contentPanel.add(installBrowseBtn, gbc_installBrowseBtn);
		
		tglbtnForceUpdate = new JToggleButton("Force update?");
		GridBagConstraints gbc_tglbtnForceUpdate = new GridBagConstraints();
		gbc_tglbtnForceUpdate.insets = new Insets(4, 8, 8, 8);
		gbc_tglbtnForceUpdate.gridwidth = 3;
		gbc_tglbtnForceUpdate.fill = GridBagConstraints.HORIZONTAL;
		gbc_tglbtnForceUpdate.gridx = 1;
		gbc_tglbtnForceUpdate.gridy = 1;
		contentPanel.add(tglbtnForceUpdate, gbc_tglbtnForceUpdate);
		
		JLabel lblRamMinimum = new JLabel("RAM Minimum (Mb):");
		GridBagConstraints gbc_lblRamMinimum = new GridBagConstraints();
		gbc_lblRamMinimum.anchor = GridBagConstraints.EAST;
		gbc_lblRamMinimum.insets = new Insets(0, 0, 5, 5);
		gbc_lblRamMinimum.gridx = 1;
		gbc_lblRamMinimum.gridy = 2;
		contentPanel.add(lblRamMinimum, gbc_lblRamMinimum);
		
		ramMinimum = new JTextField();
		GridBagConstraints gbc_textField_1 = new GridBagConstraints();
		gbc_textField_1.insets = new Insets(0, 0, 5, 5);
		gbc_textField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_1.gridx = 2;
		gbc_textField_1.gridy = 2;
		ramMinimum.setText("256");
		ramMin = Integer.parseInt(ramMinimum.getText());
		contentPanel.add(ramMinimum, gbc_textField_1);
		ramMinimum.setColumns(10);

				JLabel lblRamMaximum = new JLabel("RAM Maximum (Mb):");
				GridBagConstraints gbc_lblRamMaximum = new GridBagConstraints();
				gbc_lblRamMaximum.anchor = GridBagConstraints.EAST;
				gbc_lblRamMaximum.insets = new Insets(0, 0, 0, 5);
				gbc_lblRamMaximum.gridx = 1;
				gbc_lblRamMaximum.gridy = 3;
				contentPanel.add(lblRamMaximum, gbc_lblRamMaximum);

		ramMaximum = new JTextField();
		GridBagConstraints gbc_textField_2 = new GridBagConstraints();
		gbc_textField_2.insets = new Insets(0, 0, 0, 5);
		gbc_textField_2.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_2.gridx = 2;
		gbc_textField_2.gridy = 3;
		ramMaximum.setText("1024");
		ramMax = Integer.parseInt(ramMaximum.getText());
		contentPanel.add(ramMaximum, gbc_textField_2);
		ramMaximum.setColumns(10);

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveSettings();
				setVisible(false);
			}
		});
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);
		
		loadSettings();
	}
	
	/**
	 * "Loads" the settings from the settings class into their respective GUI
	 * controls.
	 */
	private void loadSettings()
	{
		Settings settings = Settings.getSettings();
		
		installFolderTextField.setText(settings.getInstallPath());
		
		tglbtnForceUpdate.getModel().setPressed(settings.getForceUpdate());
	}
	
	/**
	 * "Saves" the settings from the GUI controls into the settings class.
	 */
	private void saveSettings()
	{
		Settings settings = Settings.getSettings();
		
		settings.setInstallPath(installFolderTextField.getText());
		
		settings.setForceUpdate(tglbtnForceUpdate.getModel().isPressed());
		
		try
		{
			settings.save();
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, 
					"Failed to save config file: " + e.getMessage(),
					"Error", JOptionPane.ERROR_MESSAGE);
		} catch (IOException e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, 
					"Failed to save config file: " + e.getMessage(),
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}
