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

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.ftb.data.ModPack;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.panes.ModpacksPane;
import net.ftb.locale.I18N;

public class ModPackFilterDialog extends JDialog {
	private JLabel originLbl;
	private JComboBox origin;
	private JLabel availabilityLbl;
	private JComboBox availability;
	private JLabel mcVersionLbl;
	private JComboBox mcVersion; 
	private JButton apply;
	private JButton cancel;
	private JButton search;

	private ModpacksPane pane;

	public ModPackFilterDialog(ModpacksPane instance) {
		super(LaunchFrame.getInstance(), true);

		setupGui();

		getRootPane().setDefaultButton(apply);

		this.pane = instance;

		ArrayList<String> mcVersions = new ArrayList<String>();

		mcVersions.add("All");

		for(ModPack pack : ModPack.getPackArray()) {
			if(!mcVersions.contains(pack.getMcVersion())) {
				mcVersions.add(pack.getMcVersion());
			}
		}

		mcVersion.setModel(new DefaultComboBoxModel(mcVersions.toArray()));
		origin.setModel(new DefaultComboBoxModel(new String[] {I18N.getLocaleString("MAIN_ALL"), "FTB", I18N.getLocaleString("FILTER_3THPARTY")}));
		availability.setModel(new DefaultComboBoxModel(new String[]{ I18N.getLocaleString("MAIN_ALL"),  I18N.getLocaleString("FILTER_PUBLIC"),  I18N.getLocaleString("FILTER_PRIVATE")}));

		origin.setSelectedItem(pane.origin);
		mcVersion.setSelectedItem(pane.mcVersion);
		availability.setSelectedItem(pane.avaliability);

		search.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SearchDialog sd = new SearchDialog(pane);
				sd.setVisible(true);
			}
		});

		apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				pane.origin = (String)origin.getSelectedItem();
				pane.mcVersion = (String)mcVersion.getSelectedItem();
				pane.avaliability = (String)availability.getSelectedItem();
				pane.updateFilter();
				setVisible(false);
			}
		});
		
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
	}

	private void setupGui() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
		setTitle(I18N.getLocaleString("FILTER_TITLE"));
		setBounds(300, 300, 300, 209);
		setResizable(false);

		JPanel panel = new JPanel();
		originLbl = new JLabel(I18N.getLocaleString("FILTER_ORIGIN"));
		origin = new JComboBox();
		availabilityLbl = new JLabel(I18N.getLocaleString("FILTER_MODPACKAVALIABILITY"));
		availability = new JComboBox();
		mcVersionLbl = new JLabel(I18N.getLocaleString("FILTER_MCVERSION"));
		mcVersion = new JComboBox();
		apply = new JButton(I18N.getLocaleString("FILTER_APPLY"));
		cancel = new JButton(I18N.getLocaleString("MAIN_CANCEL"));
		search = new JButton(I18N.getLocaleString("FILTER_SEARCHPACK"));

		panel.setBounds(0, 0, 230, 140);
		panel.setLayout(null);
		setContentPane(panel);
		originLbl.setBounds(10, 11, 150, 30);
		origin.setBounds(184, 11, 100, 30);
		apply.setBounds(10, 143, 274, 25);
		cancel.setBounds(184, 107, 100, 25);
		search.setBounds(10, 107, 150, 25);
		panel.add(search);
		panel.add(originLbl);
		panel.add(origin);
		panel.add(apply);
		panel.add(cancel);

		mcVersionLbl.setBounds(10, 41, 150, 30);

		panel.add(mcVersionLbl);
		mcVersion.setBounds(184, 41, 100, 30);

		panel.add(mcVersion);

		availability.setBounds(184, 71, 100, 30);
		panel.add(availability);

		availabilityLbl.setBounds(10, 71, 150, 25);
		panel.add(availabilityLbl);
	}
}
