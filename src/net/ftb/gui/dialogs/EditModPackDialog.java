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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.gui.ChooseDir;
import net.ftb.gui.LaunchFrame;
import net.ftb.locale.I18N;
import net.ftb.util.OSUtils;

public class EditModPackDialog extends JDialog {
	private JTabbedPane tabbedPane;

	private JPanel formPnl;

	private JButton openFolder;
	private JButton addMod;
	private JButton disableMod;
	private JButton enableMod;

	private JLabel enabledModsLbl;
	private JLabel disabledModsLbl;

	private JScrollPane enabledModsScl;
	private JScrollPane disabledModsScl;

	private JList enabledModsLst;
	private JList disabledModsLst;

	private List<String> enabledMods;
	private List<String> disabledMods;

	private final File modsFolder = new File(Settings.getSettings().getInstallPath(), ModPack.getSelectedPack().getDir() + File.separator + "minecraft" + File.separator + "mods");
	private final File coreModsFolder = new File(Settings.getSettings().getInstallPath(), ModPack.getSelectedPack().getDir() + File.separator + "minecraft" + File.separator + "coremods");
	private final File jarModsFolder = new File(Settings.getSettings().getInstallPath(), ModPack.getSelectedPack().getDir() + File.separator + "instMods");
	public File folder = modsFolder;

	private Tab currentTab = Tab.MODS;

	public enum Tab {
		MODS,
		JARMODS,
		COREMODS,
		OLD_VERSIONS
	}

	public EditModPackDialog(LaunchFrame instance) {
		super(instance, true);

		modsFolder.mkdirs();
		coreModsFolder.mkdirs();
		jarModsFolder.mkdirs();

		setupGui();

		enabledMods = new ArrayList<String>();
		disabledMods = new ArrayList<String>();

		tabbedPane.setSelectedIndex(0);

		enabledModsLst.setListData(getEnabled());
		disabledModsLst.setListData(getDisabled());

		addMod.addActionListener(new ChooseDir(this));

		tabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				currentTab = Tab.values()[tabbedPane.getSelectedIndex()];
				switch(currentTab) {
				case MODS:
					folder = modsFolder;
					break;
				case COREMODS:
					folder = coreModsFolder;
					break;
				case JARMODS:
					folder = jarModsFolder;
					break;
				default: 
					return;
				}
				((JPanel)tabbedPane.getSelectedComponent()).add(formPnl);
				updateLists();
			}
		});

		openFolder.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				OSUtils.open(folder);
			}
		});

		disableMod.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(enabledModsLst.getSelectedIndices().length > 1) {
					for(int i = 0; i < enabledModsLst.getSelectedIndices().length; i++) {
						String name = enabledMods.get(enabledModsLst.getSelectedIndices()[i]);
						new File(folder, name).renameTo(new File(folder, name + ".disabled"));
					}
					updateLists();
				} else {
					if(enabledModsLst.getSelectedIndex() >= 0) {
						String name = enabledMods.get(enabledModsLst.getSelectedIndex());
						new File(folder, name).renameTo(new File(folder, name + ".disabled"));
					}
					updateLists();
				}
			}
		});

		enableMod.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(disabledModsLst.getSelectedIndices().length > 1) {
					for(int i = 0; i < disabledModsLst.getSelectedIndices().length; i++) {
						String name = disabledMods.get(disabledModsLst.getSelectedIndices()[i]);
						new File(folder, name).renameTo(new File(folder, name.replace(".disabled", "")));
					}
					updateLists();
				} else {
					if(disabledModsLst.getSelectedIndex() >= 0) {
						String name = disabledMods.get(disabledModsLst.getSelectedIndex());
						new File(folder, name).renameTo(new File(folder, name.replace(".disabled", "")));
					}
					updateLists();
				}
			}
		});
	}

	private String[] getEnabled() {
		enabledMods.clear();
		if(folder.exists()) {
			for(String name : folder.list()) {
				if(name.toLowerCase().endsWith(".zip")) {
					enabledMods.add(name);
				} else if(name.toLowerCase().endsWith(".jar")) {
					enabledMods.add(name);
				} else if(name.toLowerCase().endsWith(".litemod")) {
					enabledMods.add(name);
				}
			}
		}
		String[] enabledList = new String[enabledMods.size()];
		for(int i = 0; i < enabledMods.size(); i++) {
			enabledList[i] = enabledMods.get(i).replace(".zip", "").replace(".jar", "").replace(".litemod", "");
		}
		return enabledList;
	}

	private String[] getDisabled() {
		disabledMods.clear();
		if(folder.exists()) {
			for(String name : folder.list()) {
				if(name.toLowerCase().endsWith(".zip.disabled")) {
					disabledMods.add(name);
				} else if(name.toLowerCase().endsWith(".jar.disabled")) {
					disabledMods.add(name);
				} else if(name.toLowerCase().endsWith(".litemod.disabled")) {
					disabledMods.add(name);
				}
			}
		}
		String[] enabledList = new String[disabledMods.size()];
		for(int i = 0; i < disabledMods.size(); i++) {
			enabledList[i] = disabledMods.get(i).replace(".zip.disabled", "").replace(".jar.disabled", "").replace(".litemod.disabled", "");
		}
		return enabledList;
	}

	public void updateLists() {
		enabledModsLst.setListData(getEnabled());
		disabledModsLst.setListData(getDisabled());
	}

	private void setupGui() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
		setTitle(I18N.getLocaleString("MODS_EDIT_TITLE"));
		setResizable(false);

		Container panel;
		panel = getContentPane();
		panel.setLayout(new BorderLayout());

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);

		formPnl = new JPanel();

		enabledModsLbl = new JLabel(I18N.getLocaleString("MODS_EDIT_ENABLED_LABEL"));
		disabledModsLbl = new JLabel(I18N.getLocaleString("MODS_EDIT_DISABLED_LABEL"));

		openFolder = new JButton(I18N.getLocaleString("MODS_EDIT_OPEN_FOLDER"));
		addMod = new JButton(I18N.getLocaleString("MODS_EDIT_ADD_MOD"));
		disableMod = new JButton(I18N.getLocaleString("MODS_EDIT_DISABLE_MOD"));
		enableMod = new JButton(I18N.getLocaleString("MODS_EDIT_ENABLE_MOD"));

		enabledModsLst = new JList();
		disabledModsLst = new JList();

		enabledModsScl = new JScrollPane(enabledModsLst);
		disabledModsScl = new JScrollPane(disabledModsLst);

		panel.add(tabbedPane);

		tabbedPane.addTab(null, new JPanel(new BorderLayout()));
		tabbedPane.addTab(null, new JPanel(new BorderLayout()));
		tabbedPane.addTab(null, new JPanel(new BorderLayout()));

		JLabel tabLabel;
		tabLabel = new JLabel("Mods");
		tabLabel.setBorder(new EmptyBorder(8, 15, 5, 15));
		tabbedPane.setTabComponentAt(0, tabLabel);

		tabLabel = new JLabel("JarMods");
		tabLabel.setBorder(new EmptyBorder(8, 15, 5, 15));
		tabbedPane.setTabComponentAt(1, tabLabel);

		tabLabel = new JLabel("CoreMods");
		tabLabel.setBorder(new EmptyBorder(8, 15, 5, 15));
		tabbedPane.setTabComponentAt(2, tabLabel);

		enabledModsLbl.setHorizontalAlignment(SwingConstants.CENTER);
		disabledModsLbl.setHorizontalAlignment(SwingConstants.CENTER);

		enabledModsLbl.setFont(enabledModsLbl.getFont().deriveFont(Font.BOLD, 22.0f));
		disabledModsLbl.setFont(disabledModsLbl.getFont().deriveFont(Font.BOLD, 22.0f));

		enabledModsLst.setBackground(UIManager.getColor("control").darker().darker());
		disabledModsLst.setBackground(UIManager.getColor("control").darker().darker());

		enabledModsScl.setViewportView(enabledModsLst);
		disabledModsScl.setViewportView(disabledModsLst);

		SpringLayout layout = new SpringLayout();
		formPnl.setLayout(layout);

		formPnl.add(enabledModsLbl);
		formPnl.add(disabledModsLbl);
		formPnl.add(enabledModsScl);
		formPnl.add(disabledModsScl);
		formPnl.add(disableMod);
		formPnl.add(enableMod);
		formPnl.add(addMod);
		formPnl.add(openFolder);

		Spring vSpring;
		Spring rowHeight;
		Spring buttonRowHeight;

		vSpring = Spring.constant(10);

		layout.putConstraint(SpringLayout.NORTH, enabledModsLbl,  vSpring, SpringLayout.NORTH, formPnl);
		layout.putConstraint(SpringLayout.NORTH, disabledModsLbl, vSpring, SpringLayout.NORTH, formPnl);

		rowHeight = Spring.height(enabledModsLbl);
		rowHeight = Spring.max(rowHeight, Spring.height(disabledModsLbl));

		vSpring = Spring.sum(vSpring, rowHeight);
		vSpring = Spring.sum(vSpring, Spring.constant(10));

		layout.putConstraint(SpringLayout.NORTH, enabledModsScl,  vSpring, SpringLayout.NORTH, formPnl);
		layout.putConstraint(SpringLayout.NORTH, disabledModsScl, vSpring, SpringLayout.NORTH, formPnl);

		rowHeight = Spring.constant(320);

		buttonRowHeight = Spring.scale(rowHeight, .5f);
		buttonRowHeight = Spring.sum(buttonRowHeight, Spring.minus(Spring.height(enableMod)));
		buttonRowHeight = Spring.sum(buttonRowHeight, Spring.minus(Spring.constant(5)));

		layout.putConstraint(SpringLayout.SOUTH, enableMod, Spring.sum(vSpring, buttonRowHeight), SpringLayout.NORTH, formPnl);

		buttonRowHeight = Spring.sum(buttonRowHeight, Spring.constant(10));

		layout.putConstraint(SpringLayout.NORTH, disableMod, Spring.sum(vSpring, buttonRowHeight), SpringLayout.NORTH, formPnl);

		vSpring = Spring.sum(vSpring, rowHeight);

		layout.putConstraint(SpringLayout.SOUTH, enabledModsScl,  vSpring, SpringLayout.NORTH, formPnl);
		layout.putConstraint(SpringLayout.SOUTH, disabledModsScl, vSpring, SpringLayout.NORTH, formPnl);

		vSpring = Spring.sum(vSpring, Spring.constant(10));

		layout.putConstraint(SpringLayout.NORTH, addMod,     vSpring, SpringLayout.NORTH, formPnl);
		layout.putConstraint(SpringLayout.NORTH, openFolder, vSpring, SpringLayout.NORTH, formPnl);

		rowHeight = Spring.height(addMod);
		rowHeight = Spring.max(rowHeight, Spring.height(openFolder));

		vSpring = Spring.sum(vSpring, rowHeight);
		vSpring = Spring.sum(vSpring, Spring.constant(10));

		layout.putConstraint(SpringLayout.SOUTH, formPnl, vSpring, SpringLayout.NORTH, formPnl);

		Spring hSpring;
		Spring columnWidth;
		Spring buttonColumnWidth;

		hSpring = Spring.constant(10);

		layout.putConstraint(SpringLayout.WEST, enabledModsLbl,     hSpring, SpringLayout.WEST, formPnl);
		layout.putConstraint(SpringLayout.WEST, enabledModsScl,    hSpring, SpringLayout.WEST, formPnl);
		layout.putConstraint(SpringLayout.WEST, openFolder, hSpring, SpringLayout.WEST, formPnl);

		columnWidth = Spring.width(enabledModsLbl);
		columnWidth = Spring.max(columnWidth, Spring.width(disabledModsLbl));
		columnWidth = Spring.max(columnWidth, Spring.constant(260));

		hSpring = Spring.sum(hSpring, columnWidth);

		layout.putConstraint(SpringLayout.EAST, enabledModsLbl,     hSpring, SpringLayout.WEST, formPnl);
		layout.putConstraint(SpringLayout.EAST, enabledModsScl,    hSpring, SpringLayout.WEST, formPnl);
		layout.putConstraint(SpringLayout.EAST, openFolder, hSpring, SpringLayout.WEST, formPnl);

		hSpring = Spring.sum(hSpring, Spring.constant(10));

		layout.putConstraint(SpringLayout.WEST, enableMod,  hSpring, SpringLayout.WEST, formPnl);
		layout.putConstraint(SpringLayout.WEST, disableMod, hSpring, SpringLayout.WEST, formPnl);

		buttonColumnWidth = Spring.width(enableMod);
		buttonColumnWidth = Spring.max(buttonColumnWidth, Spring.width(disableMod));

		hSpring = Spring.sum(hSpring, buttonColumnWidth);

		layout.putConstraint(SpringLayout.EAST, enableMod,  hSpring, SpringLayout.WEST, formPnl);
		layout.putConstraint(SpringLayout.EAST, disableMod, hSpring, SpringLayout.WEST, formPnl);

		hSpring = Spring.sum(hSpring, Spring.constant(10));

		layout.putConstraint(SpringLayout.WEST, disabledModsLbl,  hSpring, SpringLayout.WEST, formPnl);
		layout.putConstraint(SpringLayout.WEST, disabledModsScl, hSpring, SpringLayout.WEST, formPnl);
		layout.putConstraint(SpringLayout.WEST, addMod,   hSpring, SpringLayout.WEST, formPnl);

		hSpring = Spring.sum(hSpring, columnWidth);

		layout.putConstraint(SpringLayout.EAST, disabledModsLbl,  hSpring, SpringLayout.WEST, formPnl);
		layout.putConstraint(SpringLayout.EAST, disabledModsScl, hSpring, SpringLayout.WEST, formPnl);
		layout.putConstraint(SpringLayout.EAST, addMod,   hSpring, SpringLayout.WEST, formPnl);

		hSpring = Spring.sum(hSpring, Spring.constant(10));

		layout.putConstraint(SpringLayout.EAST, formPnl, hSpring, SpringLayout.WEST, formPnl);

		((JPanel)tabbedPane.getComponent(0)).add(formPnl);

		pack();
		setLocationRelativeTo(getOwner());
	}
}
