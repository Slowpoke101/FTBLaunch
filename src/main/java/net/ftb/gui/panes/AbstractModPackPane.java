/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2014, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
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
package net.ftb.gui.panes;

import net.ftb.data.LauncherStyle;
import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.events.PackChangeEvent;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.dialogs.SearchDialog;
import net.ftb.locale.I18N;
import net.ftb.locale.Locale;
import net.ftb.log.Logger;
import net.ftb.util.OSUtils;

import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

@SuppressWarnings("unchecked")
public abstract class AbstractModPackPane extends JPanel {
    // container for packs. Upgraded by appPack()
    JPanel packs;
    // array to store packs. Upgraded by addPack
    public ArrayList<JPanel> packPanels;
    public JScrollPane packsScroll;

    int numberOfPacks;

    JLabel typeLbl;
    JButton filter, editModPack;

    JButton server;

    JButton privatePack;
    JComboBox version;
    int selectedPack = 0;
    boolean modPacksAdded = false;
    // pack hashes. Updated by updateDatas()
    HashMap<Integer, ModPack> currentPacks = Maps.newHashMap();
    HashMap<Integer, Integer> packMapping = Maps.newHashMap();

    JEditorPane packInfo;

    //	private JLabel loadingImage;
    public String origin = I18N.getLocaleString("MAIN_ALL"), mcVersion = I18N.getLocaleString("MAIN_ALL"), avaliability = I18N.getLocaleString("MAIN_ALL");
    public  boolean loaded = false;

    public AbstractModPackPane() {

    }

    JScrollPane infoScroll;
    final ActionListener al = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (version.getItemCount() > 0) {
                Settings.getSettings().setPackVer((String.valueOf(version.getSelectedItem()).equalsIgnoreCase("recommended") ? "Recommended Version" : String.valueOf(version.getSelectedItem())));
                Settings.getSettings().save();
            }
        }
    };

    /*
     * GUI Code to add a modpack to the selection
     */
    public void addPack(final ModPack pack) {
        if (!modPacksAdded) {
            modPacksAdded = true;
            packs.removeAll();
            packs.repaint();
        }
        final int packIndex = packPanels.size();
        final JPanel p = new JPanel();
        p.setBounds(0, (packIndex * 55), 420, 55);
        p.setLayout(null);
        JLabel logo = new JLabel(new ImageIcon(pack.getLogo()));
        logo.setBounds(6, 6, 42, 42);
        logo.setVisible(true);

        JTextArea filler = new JTextArea(pack.getName() + " (v" + pack.getVersion() + ") Minecraft Version " + pack.getMcVersion() + "\n" + "By " + pack.getAuthor());
        filler.setBorder(null);
        filler.setEditable(false);
        filler.setForeground(LauncherStyle.getCurrentStyle().tabPaneForeground);
        filler.setBounds(58, 6, 362, 42);
        filler.setBackground(LauncherStyle.getCurrentStyle().tabPaneBackground);

        MouseAdapter lin = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    LaunchFrame.getInstance().doLaunch();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                selectedPack = packIndex;
                updatePacks();
            }
        };
        p.addMouseListener(lin);
        filler.addMouseListener(lin);
        logo.addMouseListener(lin);
        p.add(filler);
        p.add(logo);
        packPanels.add(p);
        packs.add(p);

        packs.setMinimumSize(new Dimension(420, (packPanels.size() * 55)));
        packs.setPreferredSize(new Dimension(420, (packPanels.size() * 55)));

        //
        //packsScroll.revalidate();
        if (pack.getDir().equalsIgnoreCase(getLastPack())) {
            selectedPack = packIndex;
        }
    }

    //TODO handle changes & removals here as well!!!
    @Subscribe
    public void packChange(PackChangeEvent evt) {
        final PackChangeEvent event = evt;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (event.getType() == PackChangeEvent.TYPE.ADD) {
                    boolean doneWork = false;
                    if (event.getPacks() != null) {
                        for (ModPack p : event.getPacks()) {
                            if (filterForTab(p)) {
                                addPack(p);
                                //TODO: fix
                                Logger.logInfo("Adding " + getPaneShortName() + " Pack: " + packPanels.size() + " (" + p.getName() + ")");
                                doneWork = true;
                                numberOfPacks++;
                            }
                        }
                        if (doneWork) {
                            if (!currentPacks.isEmpty()) {
                                updateDatas();
                                updatePacks();
                            } else {
                                updateDatas();
                                updatePacks();
                            }
                            loaded = true;
                        }
                    }
                } else if(event.getType() == PackChangeEvent.TYPE.REMOVE) {
                    filterPacks();
                }
            }
        });
    }

    public void updateDatas() {
        currentPacks.clear();
        packMapping.clear();
        int counter = 0;
        // Are we going to save list of modpack there or here?!
        for (ModPack pack : ModPack.getPackArray()) {
            if (filterForTab(pack) && mcVersionCheck(pack) && avaliabilityCheck(pack) && textSearch(pack)) {
                currentPacks.put(counter, pack);
                packMapping.put(counter, pack.getIndex());
                counter++;
            }
        }
    }

    public void filterPacks() {
        packPanels.clear();
        packs.removeAll();
        currentPacks.clear();
        packMapping.clear();
        int counter = 0;
        selectedPack = 0;
        packInfo.setText("");
        // all removed, repaint
        packs.repaint();
        // not really needed
        //modPacksAdded = false;
        for (ModPack pack : ModPack.getPackArray()) {
            if (filterForTab(pack) && mcVersionCheck(pack) && avaliabilityCheck(pack) && textSearch(pack)) {
                currentPacks.put(counter, pack);
                packMapping.put(counter, pack.getIndex());
                addPack(pack);
                counter++;
            }
        }
        updateDatas();
        updatePacks();
    }

    // WTF: this does not update packs!!
    // only updating info for selected pack. pulldown menus and info area!
    void updatePacks() {
        for (int i = 0; i < packPanels.size(); i++) {
            if (selectedPack == i && getIndex() >= 0) {
                ModPack pack = ModPack.getPackArray().get(getIndex());
                if (pack != null) {
                    String mods = "";
                    if (pack.getMods() != null) {
                        mods += "<p>This pack contains the following mods by default:</p><ul>";
                        for (String name : pack.getMods()) {
                            mods += "<li>" + name + "</li>";
                        }
                        mods += "</ul>";
                    }
                    packPanels.get(i).setBackground(UIManager.getColor("control").darker().darker());
                    packPanels.get(i).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    File tempDir = new File(OSUtils.getCacheStorageLocation(), "ModPacks" + File.separator + pack.getDir());
                    packInfo.setText("<html><img src='file:///" + tempDir.getPath() + File.separator + pack.getImageName() + "' width=400 height=200></img> <br>"
                            + pack.getInfo() + mods);
                    packInfo.setCaretPosition(0);

                    if (ModPack.getSelectedPack(isFTB()).getServerUrl().equals("") || ModPack.getSelectedPack(isFTB()).getServerUrl() == null) {
                        server.setEnabled(false);
                    } else {
                        server.setEnabled(true);
                    }
                    String tempVer = Settings.getSettings().getPackVer(pack.getDir());
                    version.removeActionListener(al);
                    version.removeAllItems();
                    version.addItem("Recommended");
                    if (pack.getOldVersions() != null) {
                        for (String s : pack.getOldVersions()) {
                            version.addItem(s);
                        }
                        version.setSelectedItem(tempVer);
                    }
                    version.addActionListener(al);
                }
            } else {
                packPanels.get(i).setBackground(UIManager.getColor("control"));
                packPanels.get(i).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        }
    }

    public void updateFilterText() {
        String filterTextColor = LauncherStyle.getColorAsString(LauncherStyle.getCurrentStyle().filterTextColor);
        String filterInnerTextColor = LauncherStyle.getColorAsString(LauncherStyle.getCurrentStyle().filterInnerTextColor);
        String typeLblText = "<html><body>";
        typeLblText += "<strong><font color=rgb\"(" + filterTextColor + ")\">Filter: </strong></font>";
        typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + origin + "</font>";
        typeLblText += "<font color=rgb\"(" + filterTextColor + ")\"> / </font>";
        typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + mcVersion + "</font>";
        typeLblText += "</body></html>";

        typeLbl.setText(typeLblText);
    }

    public void updateFilter() {
        updateFilterText();
        filterPacks();
        LaunchFrame.getInstance().updateFooter();
    }

    int getIndex() {
        if (packMapping.get(selectedPack) == null) {
            return -1;
        } else
            return packMapping.get(selectedPack);
    }

    public void updateLocale() {
        origin = I18N.getLocaleString("MAIN_ALL"); mcVersion = I18N.getLocaleString("MAIN_ALL"); avaliability = I18N.getLocaleString("MAIN_ALL");
        filter.setText(I18N.getLocaleString("FILTER_SETTINGS"));
        updateFilterText();
        editModPack.setText(I18N.getLocaleString("MODS_EDIT_PACK"));
        if (I18N.current == Locale.deDE) {
            editModPack.setBounds(290, 5, 120, 25);
            typeLbl.setBounds(115, 5, 165, 25);
        } else {
            editModPack.setBounds(300, 5, 110, 25);
            typeLbl.setBounds(115, 5, 175, 25);
        }
    }

    boolean avaliabilityCheck(ModPack pack) {
        return (avaliability.equalsIgnoreCase(I18N.getLocaleString("MAIN_ALL"))) || (avaliability.equalsIgnoreCase(I18N.getLocaleString("FILTER_PUBLIC")) && !pack.isPrivatePack())
                || (avaliability.equalsIgnoreCase(I18N.getLocaleString("FILTER_PRIVATE")) && pack.isPrivatePack());
    }

    boolean mcVersionCheck(ModPack pack) {
        return (mcVersion.equalsIgnoreCase(I18N.getLocaleString("MAIN_ALL"))) || (mcVersion.equalsIgnoreCase(pack.getMcVersion()));
    }

    boolean textSearch(ModPack pack) {
        String searchString = SearchDialog.lastPackSearch.toLowerCase();
        return ((searchString.isEmpty()) || pack.getName().toLowerCase().contains(searchString) || pack.getAuthor().toLowerCase().contains(searchString));
    }

    abstract boolean filterForTab(ModPack pack);
    abstract String getLastPack();
    abstract String getPaneShortName();
    abstract boolean isFTB();
}
