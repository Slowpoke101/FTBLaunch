package net.ftb.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class ProfileAdder extends JDialog {
	private static final long serialVersionUID = 1L;
	
	JPanel panel = new JPanel();
	
	JTextField username = new JTextField(1);
	JPasswordField password = new JPasswordField(1);
	JTextField name = new JTextField(1);
	
	JLabel userLabel = new JLabel("Username:");
	JLabel passLabel = new JLabel("Password:");
	JLabel nameLabel = new JLabel("Profile Name:");

	JButton addButton = new JButton("Add");
	
	public ProfileAdder() {
		setBounds(300, 300, 300, 200);
		setResizable(false);
		
		panel.setBounds(0, 0, 300, 200);
		setContentPane(panel);
		panel.setLayout(null);
		
		userLabel.setBounds(10, 10, 80, 30);
		userLabel.setVisible(true);
		panel.add(userLabel);
		
		username.setBounds(100, 10, 170, 30);
		username.setVisible(true);
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
				/*
				 * TODO save Username and passwords to file to be picked up in the JComboBox
				 */
			}
		});
		panel.add(addButton);
	}
}
