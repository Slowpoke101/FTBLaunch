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
import net.ftb.locale.I18N;

public class MapFilterDialog extends JDialog {
	private JPanel panel = new JPanel();
	private JLabel typeLbl = new JLabel(I18N.getLocaleString("FILTER_PACKTYPE"));
	private JLabel originLbl = new JLabel(I18N.getLocaleString("FILTER_ORIGIN"));
	private JLabel compatiblePackLbl = new JLabel(I18N.getLocaleString("FILTER_COMPERTIBLEPACK"));
	private JComboBox type = new JComboBox(new String[] {"Client", "Server"});
	private JComboBox origin = new JComboBox(new String[] {I18N.getLocaleString("MAIN_ALL"), "FTB", I18N.getLocaleString("FILTER_3THPARTY")});
	private JComboBox compatiblePack;
	private JButton apply = new JButton(I18N.getLocaleString("FILTER_APPLY"));
	private JButton cancel = new JButton(I18N.getLocaleString("MAIN_CANCEL"));
	private JButton search = new JButton(I18N.getLocaleString("FILTER_SEARCHMAP"));

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
		type.setBounds(120, 10, 100, 30);
		originLbl.setBounds(10, 40, 100, 30);
		origin.setBounds(120, 40, 100, 30);
		apply.setBounds(10, 141, 210, 25);
		search.setBounds(10, 110, 100, 25);
		getRootPane().setDefaultButton(apply);
		cancel.setBounds(120, 110, 100, 25);
		panel.add(typeLbl);
		panel.add(type);
		panel.add(originLbl);
		panel.add(origin);
		panel.add(apply);
		panel.add(cancel);
		panel.add(search);
		panel.setBounds(0, 0, 230, 250);
		cancel.setBounds(120, 110, 100, 25);

		type.setSelectedItem(pane.type);
		origin.setSelectedItem(pane.origin);

		compatiblePackLbl.setBounds(10, 70, 100, 30);
		panel.add(compatiblePackLbl);

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

		compatiblePack = new JComboBox(packs.toArray());
		compatiblePack.setBounds(120, 70, 100, 30);
		compatiblePack.setSelectedItem(pane.compatible);
		panel.add(compatiblePack);

		apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				pane.compatible = (String)compatiblePack.getSelectedItem();
				pane.type = (String)type.getSelectedItem();
				pane.origin = (String)origin.getSelectedItem();
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
		search.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SearchDialog sd = new SearchDialog(pane);
				sd.setVisible(true);
				setVisible(false);
			}
		});
	}
}
