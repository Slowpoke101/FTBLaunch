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
import javax.swing.SwingConstants;

import net.ftb.gui.LaunchFrame;
import net.ftb.locale.I18N;
import net.ftb.updater.UpdateChecker;
import net.ftb.util.OSUtils;

public class LauncherUpdateDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private JLabel messageLbl;
	private JLabel updateLbl;
	private JButton showChangeLog;
	private JButton update;
	private JButton abort;

	public LauncherUpdateDialog(final UpdateChecker updateChecker) {
		super(LaunchFrame.getInstance(), true);

		setupGui();

		showChangeLog.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO: Call new frame containing html page?
				OSUtils.browse("http://launcher.feed-the-beast.com/showChangeLog.html?" + LaunchFrame.buildNumber);
			}
		});

		update.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
				updateChecker.update();
			}
		});

		abort.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
			}
		});
	}

	private void setupGui() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
		setTitle(I18N.getLocaleString("LUNCHERUPDATE_ISAVAILABLETITLE"));
		setBounds(300, 300, 300, 150);
		setResizable(false);

		JPanel panel = new JPanel();
		messageLbl = new JLabel("Version " + UpdateChecker.verString + " " + I18N.getLocaleString("LUNCHERUPDATE_ISAVAILABLE"));
		updateLbl = new JLabel(I18N.getLocaleString("UPDATE_WICHUPDATE"));
		showChangeLog = new JButton(I18N.getLocaleString("LUNCHERUPDATE_CHANGELOG"));
		update = new JButton(I18N.getLocaleString("MAIN_YES"));
		abort = new JButton(I18N.getLocaleString("MAIN_NO"));

		panel.setLayout(null);
		panel.setBounds(0, 0, 300, 150);
		setContentPane(panel);

		messageLbl.setBounds(0, 0, 300, 30);
		messageLbl.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(messageLbl);

		updateLbl.setBounds(0, 20, 300, 30);
		updateLbl.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(updateLbl);

		showChangeLog.setBounds(65, 55, 170, 25);
		panel.add(showChangeLog);

		update.setBounds(65, 90, 80, 25);
		panel.add(update);

		abort.setBounds(155, 90, 80, 25);
		panel.add(abort);
	}
}
