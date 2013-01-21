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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.gui.LaunchFrame;
import net.ftb.locale.I18N;

public class PlayOfflineDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private JTextArea text;
	private JButton play;
	private JButton abort;

	public PlayOfflineDialog(String cause, final String username) {
		setupGui();

		play.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				LaunchFrame.getInstance().launchMinecraft(Settings.getSettings().getInstallPath() + File.separator + ModPack.getSelectedPack().getDir()
						+ File.separator + "minecraft", username, "offlinemods");
			}
		});

		abort.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
	}

	public void setupGui() {
		setBounds(300, 300, 225, 150);
		text = new JTextArea(I18N.getLocaleString("PLAYOFFLINE_WANNA"));
		play = new JButton(I18N.getLocaleString("MAIN_YES"));
		abort = new JButton(I18N.getLocaleString("MAIN_NO"));
		JScrollPane pane = new JScrollPane(text);
		pane.setBounds(10, 10, 190, 60);
		abort.setBounds(110, 80, 90, 25);
		play.setBounds(10, 80, 90, 25);
		getContentPane().setLayout(null);
		getContentPane().add(pane);
		getContentPane().add(abort);
		getContentPane().add(play);
	}
}
