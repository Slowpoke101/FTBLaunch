package net.ftb.gui.dialogs;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
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

	JPanel panel = new JPanel();

	JTextField username = new JTextField(1);
	JPasswordField password = new JPasswordField(1);
	JTextField name = new JTextField(1);

	JLabel userLabel = new JLabel("Username:");
	JLabel passLabel = new JLabel("Password:");
	JLabel nameLabel = new JLabel("Profile Name:");

	JButton updateButton = new JButton("Update");
	JButton removeButton = new JButton("Remove");

	public ProfileEditorDialog(LaunchFrame instance, final String editingName, boolean modal) {
		super(instance, modal);
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
		setTitle("FTB Launcher Profile Editor");
		setBounds(300, 300, 300, 200);
		setResizable(false);

		panel.setBounds(0, 0, 300, 200);
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

		password.setBounds(100, 50, 170, 30);
		password.setVisible(true);
		password.setText(UserManager.getPassword(editingName));
		panel.add(password);

		nameLabel.setBounds(10, 90, 80, 30);
		nameLabel.setVisible(true);
		panel.add(nameLabel);

		name.setBounds(100, 90, 170, 30);
		name.setVisible(true);
		name.setText(editingName);
		panel.add(name);

		updateButton.setBounds(57, 130, 80, 25);
		updateButton.setVisible(true);
		updateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(editingName.equals(name.getText()) || (!UserManager.getUsernames().contains(username.getText()) && !UserManager.getNames().contains(name.getText()))){
					UserManager.updateUser(editingName, username.getText(), new String(password.getPassword()), name.getText());
					LaunchFrame.writeUsers(name.getText());
					setVisible(false);
				}
			}
		});
		panel.add(updateButton);

		removeButton.setBounds(163, 130, 80, 25);
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
