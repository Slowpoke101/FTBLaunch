package net.ftb.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
import javax.swing.JList;
import java.awt.Component;
import javax.swing.Box;

public class OptionsDialog extends JDialog
{
	private static final long serialVersionUID = 1L;
	
	private final JPanel contentPanel = new JPanel();
	public static JTextField installFolderTextField;
	private JToggleButton tglbtnForceUpdate;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	private JTextField textField_3;
	
	/**
	 * Create the dialog.
	 */
	public OptionsDialog()
	{
		setModal(true);
		setBounds(100, 100, 600, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{76, 78, 98, 96, 117, 49, 38};
		gbl_contentPanel.rowHeights = new int[] {0, 0, 20, 36, 33, 27, 46, 0};
		gbl_contentPanel.columnWeights = new double[]{1.0, 0.0, 0.0, 1.0, 1.0, 1.0, 0.0};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblInstallFolder = new JLabel("Install folder:");
			GridBagConstraints gbc_lblInstallFolder = new GridBagConstraints();
			gbc_lblInstallFolder.anchor = GridBagConstraints.EAST;
			gbc_lblInstallFolder.insets = new Insets(8, 8, 5, 5);
			gbc_lblInstallFolder.gridx = 0;
			gbc_lblInstallFolder.gridy = 0;
			contentPanel.add(lblInstallFolder, gbc_lblInstallFolder);
		}
		{
			installFolderTextField = new JTextField();
			GridBagConstraints gbc_installFolderTextField = new GridBagConstraints();
			gbc_installFolderTextField.gridwidth = 5;
			gbc_installFolderTextField.insets = new Insets(8, 8, 5, 8);
			gbc_installFolderTextField.fill = GridBagConstraints.BOTH;
			gbc_installFolderTextField.gridx = 1;
			gbc_installFolderTextField.gridy = 0;
			contentPanel.add(installFolderTextField, gbc_installFolderTextField);
			installFolderTextField.setColumns(10);
		}
		{
			JButton installBrowseBtn = new JButton("...");
			installBrowseBtn.addActionListener(new ChooseDir());
			
			GridBagConstraints gbc_installBrowseBtn = new GridBagConstraints();
			gbc_installBrowseBtn.insets = new Insets(8, 0, 5, 8);
			gbc_installBrowseBtn.gridx = 6;
			gbc_installBrowseBtn.gridy = 0;
			contentPanel.add(installBrowseBtn, gbc_installBrowseBtn);
		}
		{
			tglbtnForceUpdate = new JToggleButton("Force update?");
			GridBagConstraints gbc_tglbtnForceUpdate = new GridBagConstraints();
			gbc_tglbtnForceUpdate.insets = new Insets(4, 8, 8, 8);
			gbc_tglbtnForceUpdate.gridwidth = 5;
			gbc_tglbtnForceUpdate.fill = GridBagConstraints.HORIZONTAL;
			gbc_tglbtnForceUpdate.gridx = 1;
			gbc_tglbtnForceUpdate.gridy = 1;
			contentPanel.add(tglbtnForceUpdate, gbc_tglbtnForceUpdate);
		}
		{
			JLabel lblSavedProfiles = new JLabel("Saved Profiles");
			GridBagConstraints gbc_lblSavedProfiles = new GridBagConstraints();
			gbc_lblSavedProfiles.gridwidth = 2;
			gbc_lblSavedProfiles.insets = new Insets(0, 0, 5, 5);
			gbc_lblSavedProfiles.gridx = 1;
			gbc_lblSavedProfiles.gridy = 2;
			contentPanel.add(lblSavedProfiles, gbc_lblSavedProfiles);
		}
		{
			JList list = new JList();
			GridBagConstraints gbc_list = new GridBagConstraints();
			gbc_list.gridwidth = 2;
			gbc_list.gridheight = 2;
			gbc_list.insets = new Insets(0, 0, 5, 5);
			gbc_list.fill = GridBagConstraints.BOTH;
			gbc_list.gridx = 1;
			gbc_list.gridy = 3;
			contentPanel.add(list, gbc_list);
		}
		{
			JLabel lblUsername = new JLabel("Username:");
			GridBagConstraints gbc_lblUsername = new GridBagConstraints();
			gbc_lblUsername.insets = new Insets(0, 0, 5, 5);
			gbc_lblUsername.gridx = 3;
			gbc_lblUsername.gridy = 3;
			contentPanel.add(lblUsername, gbc_lblUsername);
		}
		{
			textField = new JTextField();
			GridBagConstraints gbc_textField = new GridBagConstraints();
			gbc_textField.insets = new Insets(0, 0, 5, 5);
			gbc_textField.fill = GridBagConstraints.HORIZONTAL;
			gbc_textField.gridx = 4;
			gbc_textField.gridy = 3;
			contentPanel.add(textField, gbc_textField);
			textField.setColumns(10);
		}
		{
			JLabel lblPassword = new JLabel("Password:");
			GridBagConstraints gbc_lblPassword = new GridBagConstraints();
			gbc_lblPassword.insets = new Insets(0, 0, 5, 5);
			gbc_lblPassword.gridx = 3;
			gbc_lblPassword.gridy = 4;
			contentPanel.add(lblPassword, gbc_lblPassword);
		}
		{
			textField_1 = new JTextField();
			GridBagConstraints gbc_textField_1 = new GridBagConstraints();
			gbc_textField_1.insets = new Insets(0, 0, 5, 5);
			gbc_textField_1.fill = GridBagConstraints.HORIZONTAL;
			gbc_textField_1.gridx = 4;
			gbc_textField_1.gridy = 4;
			contentPanel.add(textField_1, gbc_textField_1);
			textField_1.setColumns(10);
		}
		{
			JButton btnAdd = new JButton("Add");
			GridBagConstraints gbc_btnAdd = new GridBagConstraints();
			gbc_btnAdd.insets = new Insets(0, 0, 5, 5);
			gbc_btnAdd.gridx = 5;
			gbc_btnAdd.gridy = 4;
			contentPanel.add(btnAdd, gbc_btnAdd);
		}
		{
			JLabel lblAllocatedRam = new JLabel("Minimum RAM");
			GridBagConstraints gbc_lblAllocatedRam = new GridBagConstraints();
			gbc_lblAllocatedRam.anchor = GridBagConstraints.WEST;
			gbc_lblAllocatedRam.insets = new Insets(0, 0, 5, 5);
			gbc_lblAllocatedRam.gridx = 1;
			gbc_lblAllocatedRam.gridy = 5;
			contentPanel.add(lblAllocatedRam, gbc_lblAllocatedRam);
		}
		{
			textField_2 = new JTextField();
			GridBagConstraints gbc_textField_2 = new GridBagConstraints();
			gbc_textField_2.insets = new Insets(0, 0, 5, 5);
			gbc_textField_2.fill = GridBagConstraints.HORIZONTAL;
			gbc_textField_2.gridx = 2;
			gbc_textField_2.gridy = 5;
			contentPanel.add(textField_2, gbc_textField_2);
			textField_2.setColumns(10);
		}
		{
			JLabel lblMaximumRam = new JLabel("Maximum RAM");
			GridBagConstraints gbc_lblMaximumRam = new GridBagConstraints();
			gbc_lblMaximumRam.anchor = GridBagConstraints.WEST;
			gbc_lblMaximumRam.insets = new Insets(0, 0, 0, 5);
			gbc_lblMaximumRam.gridx = 1;
			gbc_lblMaximumRam.gridy = 6;
			contentPanel.add(lblMaximumRam, gbc_lblMaximumRam);
		}
		{
			textField_3 = new JTextField();
			GridBagConstraints gbc_textField_3 = new GridBagConstraints();
			gbc_textField_3.insets = new Insets(0, 0, 0, 5);
			gbc_textField_3.fill = GridBagConstraints.HORIZONTAL;
			gbc_textField_3.gridx = 2;
			gbc_textField_3.gridy = 6;
			contentPanel.add(textField_3, gbc_textField_3);
			textField_3.setColumns(10);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						saveSettings();
						setVisible(false);
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						setVisible(false);
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		
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
