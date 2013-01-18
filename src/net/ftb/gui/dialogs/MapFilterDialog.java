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

import net.ftb.data.Map;
import net.ftb.data.ModPack;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.panes.MapsPane;

public class MapFilterDialog extends JDialog {
	private JPanel panel = new JPanel();
	private JLabel typeLbl = new JLabel(I18N.getLocaleString("FILTER_PACKTYPE")), originLbl = new JLabel(I18N.getLocaleString("FILTER_ORIGIN")), packLbl = new JLabel(I18N.getLocaleString("FILTER_COMPERTIBLEPACK"));
	private JComboBox typeBox = new JComboBox(new String[] {"Client", "Server"}), originBox = new JComboBox(new String[] {I18N.getLocaleString("MAIN_ALL"), "FTB", I18N.getLocaleString("FILTER_3THPARTY")}), compatibleBox;
	private JButton applyButton = new JButton(I18N.getLocaleString("FILTER_APPLY")), cancelButton = new JButton(I18N.getLocaleString("MAIN_CANCEL")), searchButton = new JButton(I18N.getLocaleString("FILTER_SEARCHMAP"));

	private MapsPane pane;

	public MapFilterDialog(MapsPane instance) {
		super(LaunchFrame.getInstance(), true);
		setupGui();
		this.pane = instance;
	}

	private void setupGui() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
		setTitle(I18N.getLocaleString("FILTER_TITLE"));
		setBounds(300, 300, 230, 205);
		setResizable(false);
		panel.setBounds(0, 0, 230, 140);
		panel.setLayout(null);
		setContentPane(panel);
		typeLbl.setBounds(10, 10, 100, 30);
		typeBox.setBounds(120, 10, 100, 30);
		originLbl.setBounds(10, 40, 100, 30);
		originBox.setBounds(120, 40, 100, 30);
		applyButton.setBounds(10, 141, 210, 25);
		searchButton.setBounds(10, 110, 100, 25);
		getRootPane().setDefaultButton(applyButton);
		cancelButton.setBounds(120, 110, 100, 25);
		panel.add(typeLbl);
		panel.add(typeBox);
		panel.add(originLbl);
		panel.add(originBox);
		panel.add(applyButton);
		panel.add(cancelButton);
		panel.add(searchButton);
		panel.setBounds(0, 0, 230, 250);
		cancelButton.setBounds(120, 110, 100, 25);

		typeBox.setSelectedItem(pane.type);
		originBox.setSelectedItem(pane.origin);

		packLbl.setBounds(10, 70, 100, 30);
		panel.add(packLbl);

		ArrayList<String> packs = new ArrayList<String>();
		packs.add("All");
		for(int i = 0; i < Map.getMapArray().size(); i++) {
			String[] compat = Map.getMap(i).getCompatible();
			for(String compatable : compat) {
				if(!compatable.isEmpty() && !packs.contains(ModPack.getPack(compatable.trim()).getName())) {
					packs.add(ModPack.getPack(compatable.trim()).getName());
				}
			}
		}

		compatibleBox = new JComboBox(packs.toArray());
		compatibleBox.setBounds(120, 70, 100, 30);
		compatibleBox.setSelectedItem(pane.compatible);
		panel.add(compatibleBox);

		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				pane.compatible = (String)compatibleBox.getSelectedItem();
				pane.type = (String)typeBox.getSelectedItem();
				pane.origin = (String)originBox.getSelectedItem();
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
		searchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SearchDialog sd = new SearchDialog(pane);
				sd.setVisible(true);
				setVisible(false);
			}
		});
	}
}
