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
import net.ftb.tools.MapManager;

public class MapOverwriteDialog extends JDialog {
	private JPanel panel = new JPanel();
	private JLabel textOne = new JLabel("Warning! There is already a map by that name!");
	private JLabel textTwo = new JLabel("Do you want to overwrite?");
	private JButton yesButton = new JButton("Yes");
	private JButton noButton = new JButton("No");

	public MapOverwriteDialog() {
		super(LaunchFrame.getInstance(), true);

		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
		setTitle("WARNING!");
		setBounds(300, 300, 300, 90);
		setResizable(false);

		panel.setBounds(0, 0, 300, 90);
		setContentPane(panel);

		textOne.setLocation(10, 50);
		textOne.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(textOne);
		textTwo.setLocation(10, 80);
		textTwo.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(textTwo);

		yesButton.setVisible(true);
		yesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				MapManager.overwrite = true;
				setVisible(false);
			}
		});
		panel.add(yesButton);

		noButton.setVisible(true);
		noButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				MapManager.overwrite = false;
				setVisible(false);
			}
		});
		panel.add(noButton);
	}
}
