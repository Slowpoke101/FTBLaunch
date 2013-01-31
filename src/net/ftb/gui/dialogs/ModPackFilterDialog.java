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
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.ftb.data.ModPack;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.panes.ModpacksPane;

public class ModPackFilterDialog extends JDialog {
	private JPanel panel = new JPanel();
	private JLabel originLbl = new JLabel("Mod Pack Origin:"), packLbl = new JLabel("Compatible Pack:"), lblModPackAval = new JLabel("Mod Pack Avaliability:");
	private JComboBox originBox = new JComboBox(new String[] {"All", "FTB", "3rd Party"}), compatibleBox, mcVersionBox, 
			avalBox = new JComboBox(new String[]{"All", "Public", "Private"});
	private JButton applyButton = new JButton("Apply Filter"), cancelButton = new JButton("Cancel"), btnSearch = new JButton("Search");
	private final JLabel lblMinecraftVersion = new JLabel("Minecraft Version:");

	private ModpacksPane pane;

	public ModPackFilterDialog(ModpacksPane instance) {
		super(LaunchFrame.getInstance(), true);
		setupGui();
		this.pane = instance;
	}

	private void setupGui() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
		setTitle("Filter");
		setBounds(300, 300, 300, 209);
		setResizable(false);
		panel.setBounds(0, 0, 230, 140);
		panel.setLayout(null);
		setContentPane(panel);
		originLbl.setBounds(10, 11, 150, 30);
		originBox.setBounds(184, 11, 100, 30);
		applyButton.setBounds(10, 143, 274, 25);
		getRootPane().setDefaultButton(applyButton);
		cancelButton.setBounds(184, 107, 100, 25);
		btnSearch.setBounds(10, 107, 150, 25);
		panel.add(btnSearch);
		panel.add(originLbl);
		panel.add(originBox);
		panel.add(applyButton);
		panel.add(cancelButton);

		ArrayList<String> mcVersions = new ArrayList<String>();

		mcVersions.add("All");

		for(ModPack pack : ModPack.getPackArray()) {
			if(!mcVersions.contains(pack.getMcVersion())) {
				mcVersions.add(pack.getMcVersion());
			}
		}

		mcVersionBox = new JComboBox(mcVersions.toArray());
		originBox.setSelectedItem(pane.origin);
		avalBox.setSelectedItem(pane.avaliability);
		mcVersionBox.setSelectedItem(pane.mcVersion);
		lblMinecraftVersion.setBounds(10, 41, 150, 30);

		panel.add(lblMinecraftVersion);
		mcVersionBox.setBounds(184, 41, 100, 30);

		panel.add(mcVersionBox);

		avalBox.setBounds(184, 71, 100, 30);
		panel.add(avalBox);

		lblModPackAval.setBounds(10, 71, 150, 25);
		panel.add(lblModPackAval);

		btnSearch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SearchDialog sd = new SearchDialog(pane);
				sd.setVisible(true);
			}
		});

		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				pane.origin = (String)originBox.getSelectedItem();
				pane.mcVersion = (String)mcVersionBox.getSelectedItem();
				pane.avaliability = (String)avalBox.getSelectedItem();
				pane.updateFilter();
				setVisible(false);
			}
		});
		
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
	}
}
