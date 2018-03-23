/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2018, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
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

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.gui.ChooseDir;
import net.ftb.gui.GuiConstants;
import net.ftb.gui.LaunchFrame;
import net.ftb.locale.I18N;
import net.ftb.log.Logger;
import net.ftb.util.ModPackUtil;
import net.ftb.util.OSUtils;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.Set;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class EditModPackDialog extends JDialog {
    private JTabbedPane tabbedPane;

    private JPanel formPnl;
    private JPanel lPnl;
    private JPanel cPnl;
    private JPanel rPnl;

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
    private int mcversion = 0;

    private final File modsFolder = new File(Settings.getSettings().getInstallPath(), ModPack.getSelectedPack().getDir() + File.separator + "minecraft" + File.separator + "mods");
    private final File coreModsFolder = new File(Settings.getSettings().getInstallPath(), ModPack.getSelectedPack().getDir() + File.separator + "minecraft" + File.separator + "coremods");
    private final File jarModsFolder = new File(Settings.getSettings().getInstallPath(), ModPack.getSelectedPack().getDir() + File.separator + "instMods");
    public File folder = modsFolder;

    private Tab currentTab = Tab.MODS;

    public enum Tab {
        MODS, JARMODS, COREMODS, OLD_VERSIONS
    }

    public EditModPackDialog (LaunchFrame instance, ModPack modPack) {
        super(instance, true);
        if (modPack != null && modPack.getMcVersion() != null) {
            mcversion = Integer.parseInt(modPack.getMcVersion().replaceAll("[^\\d]", ""));
        }

        Logger.logInfo("MCVersion: " + mcversion);
        modsFolder.mkdirs();
        coreModsFolder.mkdirs();
        jarModsFolder.mkdirs();

        setupGui();
        this.setSize(700, 600);
        enabledMods = Lists.newArrayList();
        disabledMods = Lists.newArrayList();

        tabbedPane.setSelectedIndex(0);

        enabledModsLst.setListData(getEnabled());
        disabledModsLst.setListData(getDisabled());

        addMod.addActionListener(new ChooseDir(this));

        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged (ChangeEvent arg0) {
                currentTab = Tab.values()[tabbedPane.getSelectedIndex()];
                switch (currentTab) {
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
                ((JPanel) tabbedPane.getSelectedComponent()).add(formPnl);
                updateLists();
            }
        });

        openFolder.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent event) {
                OSUtils.open(folder);
            }
        });

        disableMod.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                if (enabledModsLst.getSelectedIndices().length > 1) {
                    for (int i = 0; i < enabledModsLst.getSelectedIndices().length; i++) {
                        String name = enabledMods.get(enabledModsLst.getSelectedIndices()[i]);
                        new File(folder, name).renameTo(new File(folder, name + ".disabled"));
                    }
                    updateLists();
                } else {
                    if (enabledModsLst.getSelectedIndex() >= 0) {
                        String name = enabledMods.get(enabledModsLst.getSelectedIndex());
                        new File(folder, name).renameTo(new File(folder, name + ".disabled"));
                    }
                    updateLists();
                }
            }
        });

        enableMod.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                if (disabledModsLst.getSelectedIndices().length > 1) {
                    for (int i = 0; i < disabledModsLst.getSelectedIndices().length; i++) {
                        String name = disabledMods.get(disabledModsLst.getSelectedIndices()[i]);
                        new File(folder, name).renameTo(new File(folder, name.replace(".disabled", "")));
                    }
                    updateLists();
                } else {
                    if (disabledModsLst.getSelectedIndex() >= 0) {
                        String name = disabledMods.get(disabledModsLst.getSelectedIndex());
                        new File(folder, name).renameTo(new File(folder, name.replace(".disabled", "")));
                    }
                    updateLists();
                }
            }
        });
    }

    private String[] getEnabled () {
        enabledMods.clear();
        if (folder.exists()) {
            for (String name : folder.list()) {
                if (name.toLowerCase().endsWith(".zip") || name.toLowerCase().endsWith(".jar") || name.toLowerCase().endsWith(".litemod")) {
                    enabledMods.add(name);
                }
            }
        }

        //Look up the default mods contained within the pack
        ModPack modPack = ModPack.getSelectedPack();
        Set<String> defaultMods = ModPackUtil.getDefaultModFiles(modPack);

        String[] enabledList = new String[enabledMods.size()];
        for (int i = 0; i < enabledMods.size(); i++) {
            String display = enabledMods.get(i).replace(".zip", "").replace(".jar", "").replace(".litemod", "");

            //Add additional info to the displayed entry if the mod is part of the default set
            Optional<String> defaultFile = defaultFile(defaultMods, enabledMods.get(i));

            if (defaultFile.isPresent()) {
                display = getModDefaultFormatted(display, modPack, defaultFile.get());
            }
            enabledList[i] = display;
        }
        return enabledList;
    }

    private String[] getDisabled () {
        disabledMods.clear();
        if (folder.exists()) {
            for (String name : folder.list()) {
                if (name.toLowerCase().endsWith(".zip.disabled")) {
                    disabledMods.add(name);
                } else if (name.toLowerCase().endsWith(".jar.disabled")) {
                    disabledMods.add(name);
                } else if (name.toLowerCase().endsWith(".litemod.disabled")) {
                    disabledMods.add(name);
                }
            }
        }

        //Look up the default mods contained within the pack
        ModPack modPack = ModPack.getSelectedPack();
        Set<String> defaultMods = ModPackUtil.getDefaultModFiles(modPack);

        String[] disabledList = new String[disabledMods.size()];
        for (int i = 0; i < disabledMods.size(); i++) {
            String display = disabledMods.get(i).replace(".zip.disabled", "").replace(".jar.disabled", "").replace(".litemod.disabled", "");

            //Add additional info to the displayed entry if the mod is part of the default set
            Optional<String> defaultFile = defaultFile(defaultMods, disabledMods.get(i));

            if (defaultFile.isPresent()) {
                display = getModDefaultFormatted(display, modPack, defaultFile.get());
            }
            disabledList[i] = display;
        }
        return disabledList;
    }

    /**
     * Adds formatted content to the displayed entry, including an indicator that the mod was part of the 
     * default set of shipped mods for the pack, and whether the mod was enabled/disabled by default
     *
     * @param originalDisplayName The original entry for the displayed list
     * @param modPack The mod pack being edited
     * @param defaultFile The matched default file in the mod pack
     * @return A new, formatted entry including the default state information for the entry
     */
    private String getModDefaultFormatted (String originalDisplayName, ModPack modPack, String defaultFile) {
        StringBuilder builder = new StringBuilder();

        //The additional "mod default" data is orange to separate it from the name of the file visually in the UI. 
        //Orange was selected because  the color scheme of the launcher as a whole seemed to be black/gray/orange
        builder.append("<html>").append("<font color=rgb(255,255,255)>").append(originalDisplayName).append("</font>").append(" <font color=rgb(243,119,31)>(");

        builder.append(modPack.getName());

        //Add an indicator if the default mod pack comes with this mod disabled
        if (defaultFile.toLowerCase().endsWith(".disabled")) {
            builder.append(" [").append(I18N.getLocaleString("MODS_EDIT_DISABLED_LABEL")).append("]");
        }

        builder.append(")</font></html>");

        return builder.toString();
    }

    /**
     * @param defaultMods A set representing mod files as they are initially downloaded in a zip archive
     * @param fileName The name of the file being added to an enabled/disabled list 
     * @return The name of the original file, including the original "disabled" state, or an absent optional if no match was made
     */
    private Optional<String> defaultFile (Set<String> defaultMods, String fileName) {
        String alternate = (fileName.endsWith(".disabled") ? fileName.substring(0, fileName.length() - ".disabled".length()) : fileName + ".disabled");

        if (defaultMods.contains(fileName.toLowerCase())) {
            return Optional.of(fileName.toLowerCase());
        } else if (defaultMods.contains(alternate.toLowerCase())) {
            return Optional.of(alternate.toLowerCase());
        }

        return Optional.absent();
    }

    public void updateLists () {
        enabledModsLst.setListData(getEnabled());
        disabledModsLst.setListData(getDisabled());
    }

    private void setupGui () {
        setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
        setTitle(I18N.getLocaleString("MODS_EDIT_TITLE"));
        setResizable(true);

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
        if (mcversion <= 152) {
            tabbedPane.addTab(null, new JPanel(new BorderLayout()));
            tabbedPane.addTab(null, new JPanel(new BorderLayout()));
        }
        JLabel tabLabel;
        tabLabel = new JLabel("Mods");
        tabLabel.setBorder(new EmptyBorder(8, 15, 5, 15));
        tabbedPane.setTabComponentAt(0, tabLabel);
        if (mcversion <= 152) {
            tabLabel = new JLabel("JarMods");
            tabLabel.setBorder(new EmptyBorder(8, 15, 5, 15));
            tabbedPane.setTabComponentAt(1, tabLabel);

            tabLabel = new JLabel("CoreMods");
            tabLabel.setBorder(new EmptyBorder(8, 15, 5, 15));
            tabbedPane.setTabComponentAt(2, tabLabel);
        }
        enabledModsLbl.setHorizontalAlignment(SwingConstants.CENTER);
        disabledModsLbl.setHorizontalAlignment(SwingConstants.CENTER);

        enabledModsLbl.setFont(enabledModsLbl.getFont().deriveFont(Font.BOLD, 22.0f));
        disabledModsLbl.setFont(disabledModsLbl.getFont().deriveFont(Font.BOLD, 22.0f));

        enabledModsLst.setBackground(UIManager.getColor("control").darker().darker());
        disabledModsLst.setBackground(UIManager.getColor("control").darker().darker());

        enabledModsScl.setViewportView(enabledModsLst);
        disabledModsScl.setViewportView(disabledModsLst);

        lPnl = new JPanel();
        cPnl = new JPanel();
        rPnl = new JPanel();
        lPnl.setLayout(new MigLayout(new LC().fillY()));
        lPnl.add(enabledModsLbl, GuiConstants.WRAP);
        lPnl.add(enabledModsScl, "pushy, " + GuiConstants.GROW + GuiConstants.SEP + GuiConstants.WRAP);
        lPnl.add(openFolder, GuiConstants.FILL_SINGLE_LINE);
        cPnl.setLayout(new MigLayout());
        cPnl.add(enableMod, GuiConstants.WRAP);
        cPnl.add(disableMod);
        rPnl.setLayout(new MigLayout(new LC().fillY()));
        rPnl.add(disabledModsLbl, GuiConstants.WRAP);
        rPnl.add(disabledModsScl, "pushy, " + GuiConstants.GROW + GuiConstants.SEP + GuiConstants.WRAP);
        rPnl.add(addMod, GuiConstants.FILL_SINGLE_LINE);

        formPnl.setLayout(new MigLayout(new LC().fillY()));
        formPnl.add(lPnl, "push, grow, " + GuiConstants.SPLIT_3);
        formPnl.add(cPnl, "push, grow, center");
        formPnl.add(rPnl, "push, grow ");

        ((JPanel) tabbedPane.getComponent(0)).add(formPnl);

        pack();
        setLocationRelativeTo(getOwner());
    }
}
