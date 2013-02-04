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

import java.awt.Container;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.Spring;
import javax.swing.SpringLayout;

import net.ftb.data.Map;
import net.ftb.data.ModPack;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.panes.MapsPane;
import net.ftb.locale.I18N;

public class MapFilterDialog extends JDialog {
	private JLabel typeLbl;
	private JComboBox type;
	private JLabel originLbl;
	private JComboBox origin;
	private JLabel compatiblePackLbl;
	private JComboBox compatiblePack;
	private JButton apply;
	private JButton cancel;
	private JButton search;

	private MapsPane pane;

	public MapFilterDialog(MapsPane instance) {
		super(LaunchFrame.getInstance(), true);
		this.pane = instance;

		setupGui();

		getRootPane().setDefaultButton(apply);

		this.pane = instance;

		type.setSelectedItem(pane.type);
		origin.setSelectedItem(pane.origin);
		compatiblePack.setSelectedItem(pane.compatible);

		ArrayList<String> packs = new ArrayList<String>();
		compatiblePack.addItem("All");
		packs.add("All");
		for(int i = 0; i < Map.getMapArray().size(); i++) {
			String[] compat = Map.getMap(i).getCompatible();
			for(String compatable : compat) {
				if(!compatable.isEmpty() && !packs.contains(ModPack.getPack(compatable.trim()).getName())) {
					packs.add(ModPack.getPack(compatable.trim()).getName());
					compatiblePack.addItem(ModPack.getPack(compatable.trim()).getName());
				}
			}
		}

		type.setModel(new DefaultComboBoxModel(new String[] {"Client", "Server"}));
		origin.setModel(new DefaultComboBoxModel(new String[] {I18N.getLocaleString("MAIN_ALL"), "FTB", I18N.getLocaleString("FILTER_3THPARTY")}));
		compatiblePack.setModel(new DefaultComboBoxModel(packs.toArray()));

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

	private void setupGui() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
		setTitle(I18N.getLocaleString("FILTER_TITLE"));
		setResizable(false);

		Container panel = getContentPane();
		SpringLayout layout = new SpringLayout();
		panel.setLayout(layout);

		originLbl = new JLabel(I18N.getLocaleString("FILTER_ORIGIN"));
		typeLbl = new JLabel(I18N.getLocaleString("FILTER_PACKTYPE"));
		compatiblePackLbl = new JLabel(I18N.getLocaleString("FILTER_COMPERTIBLEPACK"));
		origin = new JComboBox();
		type = new JComboBox();
		compatiblePack = new JComboBox();
		apply = new JButton(I18N.getLocaleString("FILTER_APPLY"));
		cancel = new JButton(I18N.getLocaleString("MAIN_CANCEL"));
		search = new JButton(I18N.getLocaleString("FILTER_SEARCHMAP"));

		origin.setPrototypeDisplayValue("xxxxxxxxxxxxxxxxxxxxxxxxxx");
		type.setPrototypeDisplayValue("xxxxxxxxxxxxxxxxxxxxxxxxxx");
		compatiblePack.setPrototypeDisplayValue("xxxxxxxxxxxxxxxxxxxxxxxxxx");

		panel.add(typeLbl);
		panel.add(type);
		panel.add(originLbl);
		panel.add(origin);
		panel.add(compatiblePackLbl);
		panel.add(compatiblePack);
		panel.add(apply);
		panel.add(cancel);
		panel.add(search);

		Spring hSpring;
		Spring columnWidth;

		hSpring = Spring.constant(10);

		layout.putConstraint(SpringLayout.WEST, typeLbl,           hSpring, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.WEST, originLbl,         hSpring, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.WEST, compatiblePackLbl, hSpring, SpringLayout.WEST, panel);

		columnWidth = Spring.width(typeLbl);
		columnWidth = Spring.max(columnWidth, Spring.width(originLbl));
		columnWidth = Spring.max(columnWidth, Spring.width(compatiblePackLbl));

		hSpring = Spring.sum(hSpring, columnWidth);
		hSpring = Spring.sum(hSpring, Spring.constant(10));

		layout.putConstraint(SpringLayout.WEST, type,           hSpring, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.WEST, origin,         hSpring, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.WEST, compatiblePack, hSpring, SpringLayout.WEST, panel);

		columnWidth = Spring.width(type);
		columnWidth = Spring.max(columnWidth, Spring.width(origin));
		columnWidth = Spring.max(columnWidth, Spring.width(compatiblePack));

		hSpring = Spring.sum(hSpring, columnWidth);

		layout.putConstraint(SpringLayout.EAST, type,           hSpring, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.EAST, origin,         hSpring, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.EAST, compatiblePack, hSpring, SpringLayout.WEST, panel);

		hSpring = Spring.sum(hSpring, Spring.constant(10));

		layout.putConstraint(SpringLayout.EAST, panel, hSpring, SpringLayout.WEST, panel);

		layout.putConstraint(SpringLayout.WEST, search,  10, SpringLayout.WEST,              panel);
		layout.putConstraint(SpringLayout.EAST, search,  -5, SpringLayout.HORIZONTAL_CENTER, panel);
		layout.putConstraint(SpringLayout.WEST, cancel,   5, SpringLayout.HORIZONTAL_CENTER, panel);
		layout.putConstraint(SpringLayout.EAST, cancel, -10, SpringLayout.EAST,              panel);

		layout.putConstraint(SpringLayout.WEST, apply,  10, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.EAST, apply, -10, SpringLayout.EAST, panel);

		Spring vSpring;
		Spring rowHeight;

		vSpring = Spring.constant(10);

		layout.putConstraint(SpringLayout.BASELINE, typeLbl,       0, SpringLayout.BASELINE, type);
		layout.putConstraint(SpringLayout.NORTH,    type,    vSpring, SpringLayout.NORTH,    panel);

		rowHeight = Spring.height(typeLbl);
		rowHeight = Spring.max(rowHeight, Spring.height(type));

		vSpring = Spring.sum(vSpring, rowHeight);
		vSpring = Spring.sum(vSpring, Spring.constant(5));

		layout.putConstraint(SpringLayout.BASELINE, originLbl, 0, SpringLayout.BASELINE, origin);
		layout.putConstraint(SpringLayout.NORTH, origin, vSpring, SpringLayout.NORTH, panel);

		rowHeight = Spring.height(originLbl);
		rowHeight = Spring.max(rowHeight, Spring.height(origin));

		vSpring = Spring.sum(vSpring, rowHeight);
		vSpring = Spring.sum(vSpring, Spring.constant(5));

		layout.putConstraint(SpringLayout.BASELINE, compatiblePackLbl,       0, SpringLayout.BASELINE, compatiblePack);
		layout.putConstraint(SpringLayout.NORTH,    compatiblePack,    vSpring, SpringLayout.NORTH,    panel);

		rowHeight = Spring.height(compatiblePackLbl);
		rowHeight = Spring.max(rowHeight, Spring.height(compatiblePack));

		vSpring = Spring.sum(vSpring, rowHeight);
		vSpring = Spring.sum(vSpring, Spring.constant(10));

		layout.putConstraint(SpringLayout.NORTH, search, vSpring, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.NORTH, cancel, vSpring, SpringLayout.NORTH, panel);

		rowHeight = Spring.height(search);
		rowHeight = Spring.max(rowHeight, Spring.height(cancel));

		vSpring = Spring.sum(vSpring, rowHeight);
		vSpring = Spring.sum(vSpring, Spring.constant(5));

		layout.putConstraint(SpringLayout.NORTH, apply, vSpring, SpringLayout.NORTH, panel);

		vSpring = Spring.sum(vSpring, Spring.height(apply));
		vSpring = Spring.sum(vSpring, Spring.constant(10));

		layout.putConstraint(SpringLayout.SOUTH, panel, vSpring, SpringLayout.NORTH, panel);

		pack();
		setLocationRelativeTo(this.getOwner());
	}
}
