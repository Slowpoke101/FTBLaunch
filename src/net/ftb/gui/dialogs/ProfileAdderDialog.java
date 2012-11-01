package net.ftb.gui.dialogs;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.ftb.data.UserManager;
import net.ftb.gui.LaunchFrame;

public class ProfileAdderDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	JPanel panel = new JPanel();

	JTextField username = new JTextField(1);
	JPasswordField password = new JPasswordField(1);
	JTextField name = new JTextField(1);

	JLabel userLabel = new JLabel("Username:");
	JLabel passLabel = new JLabel("Password:");
	JLabel nameLabel = new JLabel("Profile Name:");

	JButton addButton = new JButton("Add");

	public ProfileAdderDialog() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
		setTitle("FTB Launcher Profile Adder");
		setBounds(300, 300, 300, 200);
		setResizable(false);

		getRootPane().setDefaultButton(addButton);

		panel.setBounds(0, 0, 300, 200);
		setContentPane(panel);
		panel.setLayout(null);

		userLabel.setBounds(10, 10, 80, 30);
		userLabel.setVisible(true);
		panel.add(userLabel);

		username.setBounds(100, 10, 170, 30);
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
		panel.add(password);

		nameLabel.setBounds(10, 90, 80, 30);
		nameLabel.setVisible(true);
		panel.add(nameLabel);

		name.setBounds(100, 90, 170, 30);
		name.setVisible(true);
		panel.add(name);

		addButton.setBounds(125, 130, 50, 25);
		addButton.setVisible(true);
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(validate(name.getText(), username.getText(), password.getPassword())) {
					UserManager.addUser(username.getText(), new String(password.getPassword()), name.getText());
					LaunchFrame.writeUsers(name.getText());
					setVisible(false);
				}
			}
		});
		panel.add(addButton);
	}

	private boolean validate(String name, String user, char[] pass) {
		if(name != null && !name.equals("") && user != null && !user.equals("") && pass.length > 1) {
			if(!UserManager.getNames().contains(name) && !UserManager.getUsernames().contains(user)) {
				return true;
			}
		}
		return false;
	}
}
