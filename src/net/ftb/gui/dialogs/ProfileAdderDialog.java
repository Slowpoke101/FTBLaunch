/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2013, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
 * FTB Launcher is licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import net.ftb.locale.I18N;
import net.ftb.util.ErrorUtils;

public class ProfileAdderDialog extends JDialog {
	private JPanel panel = new JPanel();
	private JTextField username = new JTextField(1);
	private JPasswordField password = new JPasswordField(1);
	private JTextField name = new JTextField(1);
	private JLabel userLabel = new JLabel(I18N.getLocaleString("PROFILEADDER_USERNAME"));
	private JLabel passLabel = new JLabel(I18N.getLocaleString("PROFILEADDER_PASSWORD"));
	private JLabel nameLabel = new JLabel(I18N.getLocaleString("PROFILEADDER_NAME"));
	private JCheckBox savePassword = new JCheckBox(I18N.getLocaleString("PROFILEADDER_SAVEPASSWORD"));
	private JButton addButton = new JButton(I18N.getLocaleString("MAIN_ADD"));

	public ProfileAdderDialog(LaunchFrame instance, boolean modal) {
		super(instance, modal);
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
		setTitle(I18N.getLocaleString("PROFILEADDER_TITLE"));
		setBounds(300, 300, 300, 240);
		setResizable(false);

		getRootPane().setDefaultButton(addButton);

		panel.setBounds(0, 0, 300, 240);
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

		savePassword.setBounds(100, 130, 170, 30);
		savePassword.setSelected(true);
		savePassword.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				password.setEnabled(savePassword.isSelected());
			}
		});
		panel.add(savePassword);

		addButton.setBounds(125, 170, 50, 25);
		addButton.setVisible(true);
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if(savePassword.isSelected()) {
					if(validate(name.getText(), username.getText(), password.getPassword())) {
						UserManager.addUser(username.getText(), new String(password.getPassword()), name.getText());
						LaunchFrame.writeUsers(name.getText());
						setVisible(false);
					} else {
						ErrorUtils.tossError(I18N.getLocaleString("PROFILADDER_ERROR"));
					}
				} else {
					if(validate(name.getText(), username.getText())) {
						UserManager.addUser(username.getText(), "", name.getText());
						LaunchFrame.writeUsers(name.getText());
						setVisible(false);
					} else {
						ErrorUtils.tossError(I18N.getLocaleString("PROFILADDER_ERROR"));
					}
				}
			}
		});
		panel.add(addButton);
	}

	private boolean validate(String name, String user, char[] pass) {
		if(!name.isEmpty() && !user.isEmpty() && pass.length > 1) {
			if(!UserManager.getNames().contains(name) && !UserManager.getUsernames().contains(user)) {
				return true;
			}
		}
		return false;
	}

	private boolean validate(String name, String user) {
		if(!name.isEmpty() && !user.isEmpty()) {
			if(!UserManager.getNames().contains(name) && !UserManager.getUsernames().contains(user)) {
				return true;
			}
		}
		return false;
	}
}
