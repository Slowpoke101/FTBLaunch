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

public class OptionsDialog extends JDialog
{
	private static final long serialVersionUID = 1L;
	
	private final JPanel contentPanel = new JPanel();
	private JTextField installFolderTextField;
	private JToggleButton tglbtnForceUpdate;
	
	/**
	 * Create the dialog.
	 */
	public OptionsDialog()
	{
		setModal(true);
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{0, 0, 0};
		gbl_contentPanel.rowHeights = new int[]{0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 1.0, 0.0};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
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
			gbc_installFolderTextField.insets = new Insets(8, 8, 5, 8);
			gbc_installFolderTextField.fill = GridBagConstraints.BOTH;
			gbc_installFolderTextField.gridx = 1;
			gbc_installFolderTextField.gridy = 0;
			contentPanel.add(installFolderTextField, gbc_installFolderTextField);
			installFolderTextField.setColumns(10);
		}
		{
			JButton installBrowseBtn = new JButton("...");
			GridBagConstraints gbc_installBrowseBtn = new GridBagConstraints();
			gbc_installBrowseBtn.insets = new Insets(8, 0, 5, 8);
			gbc_installBrowseBtn.gridx = 2;
			gbc_installBrowseBtn.gridy = 0;
			contentPanel.add(installBrowseBtn, gbc_installBrowseBtn);
		}
		{
			tglbtnForceUpdate = new JToggleButton("Force update?");
			GridBagConstraints gbc_tglbtnForceUpdate = new GridBagConstraints();
			gbc_tglbtnForceUpdate.insets = new Insets(4, 8, 8, 8);
			gbc_tglbtnForceUpdate.gridwidth = 3;
			gbc_tglbtnForceUpdate.fill = GridBagConstraints.HORIZONTAL;
			gbc_tglbtnForceUpdate.gridx = 0;
			gbc_tglbtnForceUpdate.gridy = 1;
			contentPanel.add(tglbtnForceUpdate, gbc_tglbtnForceUpdate);
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
