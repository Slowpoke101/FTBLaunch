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
	private JLabel messageLbl;
	private JLabel installPathLbl;
	private JTextField installPath;
	private JButton installPathBrowse;
	private JButton apply;

	public InstallDirectoryDialog() {
		super(LaunchFrame.getInstance(), true);

		setupGui();

		getRootPane().setDefaultButton(apply);

		installPathBrowse.addActionListener(new ChooseDir(this));

		installPath.setText(OSUtils.getDefInstallPath());

		installPath.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				Settings.getSettings().setInstallPath(installPath.getText());
				Settings.getSettings().save();
			}
			@Override public void focusGained(FocusEvent e) { }
		});

		apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
			}
		});
	}

	public void setInstallFolderText(String messageLbl) {
		installPath.setText(messageLbl);
		Settings.getSettings().setInstallPath(installPath.getText());
		Settings.getSettings().save();
	}

	private void setupGui() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
		setTitle("Choose Install Directory");
		setBounds(560, 150, 560, 150);
		setResizable(false);
		getContentPane().setLayout(null);

		messageLbl = new JLabel("<html><body><center><font size=\"3\"><strong>" + I18N.getLocaleString("INSTALL_FIRSTUSE") + "</strong></font></center></body></html>");
		installPathLbl = new JLabel(I18N.getLocaleString("INSTALL_FOLDER"));
		installPath = new JTextField();
		installPathBrowse = new JButton("...");
		apply = new JButton(I18N.getLocaleString("MAIN_APPLY"));

		messageLbl.setBounds(10, 10, 530, 30);
		messageLbl.setHorizontalAlignment(SwingConstants.CENTER);
		add(messageLbl);

		installPathBrowse.setBounds(495, 50, 50, 23);
		add(installPathBrowse);
		installPathLbl.setBounds(10, 50, 127, 23);
		add(installPathLbl);
		installPath.setBounds(90, 50, 400, 23);

		installPath.setColumns(10);
		add(installPath);

		apply.setBounds(240, 85, 80, 23);

		add(apply);
	}
}
