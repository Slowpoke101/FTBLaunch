package net.ftb.gui.dialogs;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import net.ftb.data.Settings;
import net.ftb.gui.ChooseDir;
import net.ftb.gui.LaunchFrame;
import net.ftb.locale.I18N;
import net.ftb.util.OSUtils;

public class InstallDirectoryDialog extends JDialog {
	private JLabel lblInstallFolder;
	private JTextField installFolderTextField;
	private JLabel text = new JLabel("<html><body><center><font size=\"3\"><strong>Since this is your first time using the launcher, we suggest setting the install directory.</strong></font></center></body></html>");
	private JButton applyButton = new JButton("Apply");

	public InstallDirectoryDialog() {
		super(LaunchFrame.getInstance(), true);
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
		setTitle("Choose Install Directory");
		setBounds(560, 150, 560, 150);
		setResizable(false);
		getContentPane().setLayout(null);

		text.setBounds(10, 10, 530, 30);
		text.setHorizontalAlignment(SwingConstants.CENTER);
		add(text);

		JButton installBrowseBtn = new JButton("...");
		installBrowseBtn.setBounds(495, 50, 50, 23);
		installBrowseBtn.addActionListener(new ChooseDir(this));
		add(installBrowseBtn);

		lblInstallFolder = new JLabel(I18N.getLocaleString("INSTALL_FOLDER"));
		lblInstallFolder.setBounds(10, 50, 127, 23);
		add(lblInstallFolder);

		installFolderTextField = new JTextField();
		installFolderTextField.setBounds(90, 50, 400, 23);
		installFolderTextField.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				Settings.getSettings().setInstallPath(installFolderTextField.getText());
				Settings.getSettings().save();
			}
			@Override public void focusGained(FocusEvent e) { }
		});
		installFolderTextField.setColumns(10);
		installFolderTextField.setText(OSUtils.getDefInstallPath());
		add(installFolderTextField);

		applyButton.setBounds(240, 85, 80, 23);
		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
			}
		});
		add(applyButton);

		getRootPane().setDefaultButton(applyButton);
	}

	public void setInstallFolderText(String text) {
		installFolderTextField.setText(text);
		Settings.getSettings().setInstallPath(installFolderTextField.getText());
		Settings.getSettings().save();
	}
}
