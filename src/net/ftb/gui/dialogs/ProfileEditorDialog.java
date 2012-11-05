package net.ftb.gui.dialogs;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.ftb.data.UserManager;
import net.ftb.gui.LaunchFrame;

public class ProfileEditorDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private JPanel panel = new JPanel();

	private JTextField username = new JTextField(1);
	private JPasswordField password = new JPasswordField(1);
	private JTextField name = new JTextField(1);
	private JLabel userLabel = new JLabel("Username:");
	private JLabel passLabel = new JLabel("Password:");
	private JLabel nameLabel = new JLabel("Profile Name:");
	private JCheckBox savePassword = new JCheckBox("Remember Password");
	private JButton updateButton = new JButton("Update");
	private JButton removeButton = new JButton("Remove");

	public ProfileEditorDialog(LaunchFrame instance, final String editingName, boolean modal) {
		super(instance, modal);
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
		setTitle("FTB Launcher Profile Editor");
		setBounds(300, 300, 300, 240);
		setResizable(false);

		getRootPane().setDefaultButton(updateButton);

		panel.setBounds(0, 0, 300, 240);
		setContentPane(panel);
		panel.setLayout(null);

		userLabel.setBounds(10, 10, 80, 30);
		userLabel.setVisible(true);
		panel.add(userLabel);

		username.setBounds(100, 10, 170, 30);
		username.setText(UserManager.getUsername(editingName));
		username.setVisible(true);
		username.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				name.setText(username.getText());
			}
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				name.setText(username.getText());
			}
			@Override public void changedUpdate(DocumentEvent e) { }
		});
		panel.add(username);

		passLabel.setBounds(10, 50, 80, 30);
		passLabel.setVisible(true);
		panel.add(passLabel);

		savePassword.setBounds(100, 130, 170, 30);
		savePassword.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				password.setEnabled(savePassword.isSelected());
			}
		});

		password.setBounds(100, 50, 170, 30);
		password.setVisible(true);
		if(UserManager.getPassword(editingName).isEmpty()){
			password.setEnabled(false);
			savePassword.setSelected(false);
		} else {
			password.setText(UserManager.getPassword(editingName));
			savePassword.setSelected(true);
		}
		panel.add(password);
		panel.add(savePassword);

		nameLabel.setBounds(10, 90, 80, 30);
		nameLabel.setVisible(true);
		panel.add(nameLabel);

		name.setBounds(100, 90, 170, 30);
		name.setVisible(true);
		name.setText(editingName);
		panel.add(name);

		updateButton.setBounds(57, 170, 80, 25);
		updateButton.setVisible(true);
		updateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(editingName.equals(name.getText()) || (!UserManager.getUsernames().contains(username.getText()) && !UserManager.getNames().contains(name.getText()))) {
					if(savePassword.isSelected()) {
						UserManager.updateUser(editingName, username.getText(), new String(password.getPassword()), name.getText());
					} else {
						UserManager.updateUser(editingName, username.getText(), "", name.getText());
					}
					LaunchFrame.writeUsers(name.getText());
					setVisible(false);
				}
			}
		});
		panel.add(updateButton);

		removeButton.setBounds(163, 170, 80, 25);
		removeButton.setVisible(true);
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				UserManager.removeUser(editingName);
				LaunchFrame.writeUsers(null);
				setVisible(false);
			}
		});
		panel.add(removeButton);
	}
}
