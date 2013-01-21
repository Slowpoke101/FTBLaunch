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
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
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

	private JPanel modsFolderPnl;
	private JPanel coreModsFolderPnl;
	private JPanel jarModsFolderPnl;

	private JLabel enabledModsLbl;
	private JLabel disabledModsLbl;

	private JButton openFolder;
	private JButton addMod;
	private JButton disableMod;
	private JButton enableMod;

	private JList enabledModsLst;
	private JList disabledModsLst;

	private List<String> enabledMods;
	private List<String> disabledMods;

	private JScrollPane enabledModsScl;
	private JScrollPane disabledModsScl;

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

		tabbedPane.setSelectedIndex(0);

		tabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				currentTab = Tab.values()[tabbedPane.getSelectedIndex()];
				JPanel temp;
				switch(currentTab) {
				case MODS:
					temp = modsFolderPnl;
					folder = modsFolder;
					break;
				case COREMODS:
					temp = coreModsFolderPnl;
					folder = coreModsFolder;
					break;
				case JARMODS:
					temp = jarModsFolderPnl;
					folder = jarModsFolder;
					break;
				default: 
					return;
				}
				temp.add(enabledModsScl);
				temp.add(disabledModsScl);
				temp.add(enabledModsLbl);
				temp.add(disabledModsLbl);
				temp.add(openFolder);
				temp.add(addMod);
				temp.add(enableMod);
				temp.add(disableMod);
				updateLists();
			}
		});

		addMod.addActionListener(new ChooseDir(this));

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

	public void setupGui() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
		setTitle(I18N.getLocaleString("MODS_EDIT_TITLE"));
		setBounds(300, 300, 635, 525);
		setResizable(false);
		getContentPane().setLayout(null);

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);

		modsFolderPnl = new JPanel();
		coreModsFolderPnl = new JPanel();
		jarModsFolderPnl = new JPanel();

		enabledModsLbl = new JLabel("<html><body><h1>" + I18N.getLocaleString("MODS_EDIT_ENABLED_LABEL") + "</h1></html></body>");
		disabledModsLbl = new JLabel("<html><body><h1>" + I18N.getLocaleString("MODS_EDIT_DISABLED_LABEL") + "</h1></html></body>");

		openFolder = new JButton(I18N.getLocaleString("MODS_EDIT_OPEN_FOLDER"));
		addMod = new JButton(I18N.getLocaleString("MODS_EDIT_ADD_MOD"));
		disableMod = new JButton(I18N.getLocaleString("MODS_EDIT_DISABLE_MOD"));
		enableMod = new JButton(I18N.getLocaleString("MODS_EDIT_ENABLE_MOD"));

		enabledModsLst = new JList();
		disabledModsLst = new JList();

		enabledMods = new ArrayList<String>();
		disabledMods = new ArrayList<String>();

		enabledModsScl = new JScrollPane(enabledModsLst);
		disabledModsScl = new JScrollPane(disabledModsLst);

		tabbedPane.setLocation(0, 0);
		tabbedPane.setSize(getSize());

		modsFolderPnl.setLayout(null);
		coreModsFolderPnl.setLayout(null);
		jarModsFolderPnl.setLayout(null);

		getContentPane().add(tabbedPane);
		tabbedPane.addTab("<html><body leftMargin=15 topmargin=8 marginwidth=15 marginheight=5>Mods</body></html>", modsFolderPnl);
		tabbedPane.addTab("<html><body leftMargin=15 topmargin=8 marginwidth=15 marginheight=5>JarMods</body></html>", jarModsFolderPnl);
		tabbedPane.addTab("<html><body leftMargin=15 topmargin=8 marginwidth=15 marginheight=5>CoreMods</body></html>", coreModsFolderPnl);

		addMod.setBounds(380, 410, 240, 35);
		modsFolderPnl.add(addMod);

		openFolder.setBounds(10, 410, 240, 35);
		modsFolderPnl.add(openFolder);

		enabledModsLbl.setBounds(10, 10, 240, 30);
		enabledModsLbl.setHorizontalAlignment(SwingConstants.CENTER);
		modsFolderPnl.add(enabledModsLbl);

		disabledModsLbl.setBounds(380, 10, 240, 30);
		disabledModsLbl.setHorizontalAlignment(SwingConstants.CENTER);
		modsFolderPnl.add(disabledModsLbl);

		enabledModsLst.setListData(getEnabled());
		enabledModsLst.setBackground(UIManager.getColor("control").darker().darker());
		enabledModsScl.setViewportView(enabledModsLst);
		enabledModsScl.setBounds(10, 40, 240, 360);
		modsFolderPnl.add(enabledModsScl);

		disabledModsLst.setListData(getDisabled());
		disabledModsLst.setBackground(UIManager.getColor("control").darker().darker());
		disabledModsScl.setViewportView(disabledModsLst);
		disabledModsScl.setBounds(380, 40, 240, 360);
		modsFolderPnl.add(disabledModsScl);

		disableMod.setBounds(255, 80, 115, 30);
		disableMod.setVisible(true);
		modsFolderPnl.add(disableMod);

		enableMod.setBounds(255, 120, 115, 30);
		enableMod.setVisible(true);
		modsFolderPnl.add(enableMod);
	}
}
