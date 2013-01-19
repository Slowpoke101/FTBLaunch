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
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import net.ftb.gui.LaunchFrame;

public class PasswordDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	JPanel panel = new JPanel();
	JPasswordField password = new JPasswordField(1);
	JLabel passLabel = new JLabel(I18N.getLocaleString("PASSWORD_PASSLABEL"));
	JButton submitButton = new JButton(I18N.getLocaleString("MAIN_SUBMIT"));

	public PasswordDialog(LaunchFrame instance, boolean modal) {
		super(instance, modal);

		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
		setTitle(I18N.getLocaleString("PASSWORD_TITLE"));
		setBounds(300, 300, 300, 120);
		setResizable(false);

		getRootPane().setDefaultButton(submitButton);

		panel.setBounds(0, 0, 300, 100);
		setContentPane(panel);
		panel.setLayout(null);

		passLabel.setBounds(10, 10, 80, 30);
		passLabel.setVisible(true);
		panel.add(passLabel);

		password.setBounds(100, 10, 170, 30);
		password.setVisible(true);
		panel.add(password);

		submitButton.setBounds(105, 50, 90, 25);
		submitButton.setVisible(true);
		submitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if(!new String(password.getPassword()).isEmpty()){
					LaunchFrame.tempPass = new String(password.getPassword());
					setVisible(false);
				}
			}
		});
		panel.add(submitButton);
	}
}
