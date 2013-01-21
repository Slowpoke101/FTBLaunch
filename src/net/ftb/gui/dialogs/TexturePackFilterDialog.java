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

import javax.management.InstanceAlreadyExistsException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.ftb.data.ModPack;
import net.ftb.data.TexturePack;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.panes.TexturepackPane;
import net.ftb.locale.I18N;

@SuppressWarnings("serial")
public class TexturePackFilterDialog extends JDialog {
	private JLabel compatiblePackLbl;
	private JComboBox compatiblePack;
	private JLabel resolutionLbl;
	private JComboBox resolution;
	private JButton apply;
	private JButton cancel;
	private JButton search;

	private TexturepackPane instance;
	
	public TexturePackFilterDialog(final TexturepackPane instance) {
		super(LaunchFrame.getInstance(), true);
		
		this.instance = instance;
		
		setupGui();
		
		int textures = TexturePack.getTexturePackArray().size();
		
		ArrayList<String> comp = new ArrayList<String>();
		comp.add("All");
		for(int i = 0; i < textures; i++) {
			String[] s = TexturePack.getTexturePack(i).getCompatible();
			for(int j = 0; j < s.length; j++) {
				if(!comp.contains(ModPack.getPack(s[j].trim()).getName())) {
					comp.add(ModPack.getPack(s[j].trim()).getName());
				}
			}
		}

		ArrayList<String> res = new ArrayList<String>();
		res.add("All");
		for(int i = 0; i < textures; i++) {
			if(!res.contains(TexturePack.getTexturePack(i).getResolution())) {
				res.add(TexturePack.getTexturePack(i).getResolution());
			}
		}

		compatiblePack.setModel(new DefaultComboBoxModel(res.toArray(new String[]{})));
		resolution.setModel(new DefaultComboBoxModel(res.toArray(new String[]{})));

		compatiblePack.setSelectedItem(instance.compatible);
		resolution.setSelectedItem(instance.resolution);

		apply.addActionListener(new ActionListener() {
			@SuppressWarnings("static-access")
			@Override
			public void actionPerformed(ActionEvent arg0) {
				instance.compatible = (String)compatiblePack.getSelectedItem();
				instance.resolution = (String)resolution.getSelectedItem();
				instance.updateFilter();
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
				SearchDialog sd = new SearchDialog(instance);
				sd.setVisible(true);
			}
		});
	}

	@SuppressWarnings("static-access")
	private void setupGui() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
		setTitle(I18N.getLocaleString("FILTER_TITLE"));
		
		JPanel panel = new JPanel();
		compatiblePackLbl = new JLabel(I18N.getLocaleString("FILTER_COMPERTIBLEPACK"));
		resolutionLbl = new JLabel(I18N.getLocaleString("FILTER_RESULUTION"));
		compatiblePack = new JComboBox();
		resolution = new JComboBox();
		apply = new JButton(I18N.getLocaleString("FILTER_APPLY"));
		cancel = new JButton(I18N.getLocaleString("MAIN_CANCEL"));
		search = new JButton(I18N.getLocaleString("FILTER_TEXSEARCH"));
		
		setBounds(350, 300, 280, 181);
		setResizable(false);
		panel.setBounds(0, 0, 230, 140);
		panel.setLayout(null);
		setContentPane(panel);
		compatiblePackLbl.setBounds(10, 10, 144, 30);
		compatiblePack.setBounds(146, 10, 118, 30);
		resolution.setBounds(146, 40, 118, 30);
		resolutionLbl.setBounds(10, 40, 144, 30);
		apply.setBounds(10, 117, 254, 25);
		search.setBounds(10, 81, 118, 25);
		getRootPane().setDefaultButton(apply);
		cancel.setBounds(146, 81, 118, 25);
		panel.add(compatiblePackLbl);
		panel.add(resolutionLbl);
		panel.add(compatiblePack);
		panel.add(resolution);
		panel.add(apply);
		panel.add(cancel);
		panel.add(search);
	}
}
