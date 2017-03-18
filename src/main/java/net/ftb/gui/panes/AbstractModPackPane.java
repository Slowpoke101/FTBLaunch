/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2017, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import lombok.Getter;
import lombok.Setter;
import net.ftb.data.LauncherStyle;
import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.events.PackChangeEvent;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.dialogs.EditModPackDialog;
import net.ftb.gui.dialogs.ModPackFilterDialog;
import net.ftb.gui.dialogs.PrivatePackDialog;
import net.ftb.locale.I18N;
import net.ftb.log.Logger;
import net.ftb.util.DownloadUtils;
import net.ftb.util.ErrorUtils;
import net.ftb.util.OSUtils;
import net.ftb.util.TrackerUtils;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.*;

@SuppressWarnings("unchecked")
public abstract class AbstractModPackPane extends JPanel {

    // container for packs. Upgraded by appPack()
    JPanel packs;
    // array to store packs. Upgraded by addPack
    public ArrayList<JPanel> packPanels;


    JLabel typeLbl;
    JButton filter, editModPack;

    JButton server;

    JButton privatePack;
    JComboBox version;

    boolean modPacksAdded = false;
    HashMap<ModPack, JPanel> panelByPack = Maps.newHashMap();
    @Setter
    @Getter
    ModPack selectedPack;

    JEditorPane packInfo;

    @Getter
    protected JScrollPane packsScroll;
    @Getter
    protected ObjectInfoSplitPane splitPane;

    public String origin = I18N.getLocaleString("MAIN_ALL"), mcVersion = I18N.getLocaleString("MAIN_ALL"), avaliability = I18N.getLocaleString("MAIN_ALL"), searchString="";
    public boolean loaded = false;

    public AbstractModPackPane () {
        super();
        setBorder(null);
        setLayout(new BorderLayout());

        // Contains buttons/filter info/selection boxes along top of mod pack panes
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(1, 6));
        buttonsPanel.setMinimumSize(new Dimension(420, 25));
        add(buttonsPanel, BorderLayout.PAGE_START);

        packPanels = Lists.newArrayList();

        filter = new JButton(I18N.getLocaleString("FILTER_SETTINGS"));
        filter.setMinimumSize(new Dimension(105, 25));
        filter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e) {
                if (loaded) {
                    ModPackFilterDialog filterDia = new ModPackFilterDialog(getThis());
                    filterDia.setVisible(true);
                }
            }
        });
        buttonsPanel.add(filter);

        String filterTextColor = LauncherStyle.getColorAsString(LauncherStyle.getCurrentStyle().filterTextColor);
        String filterInnerTextColor = LauncherStyle.getColorAsString(LauncherStyle.getCurrentStyle().filterInnerTextColor);

        String typeLblText = "<html><body>";
        typeLblText += "<strong><font color=rgb\"(" + filterTextColor + ")\">Filter: </strong></font>";
        typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + avaliability + "</font>";
        typeLblText += "<font color=rgb\"(" + filterTextColor + ")\"> / </font>";
        typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + mcVersion + "</font>";
        typeLblText += "</body></html>";

        typeLbl = new JLabel(typeLblText);
        typeLbl.setMinimumSize(new Dimension(175, 25));
        typeLbl.setHorizontalAlignment(SwingConstants.CENTER);
        buttonsPanel.add(typeLbl);

        editModPack = new JButton(I18N.getLocaleString("MODS_EDIT_PACK"));
        editModPack.setMinimumSize(new Dimension(110, 25));
        editModPack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e) {
                if (!packPanels.isEmpty()) {
                    //TODO: fix by rename
                    EditModPackDialog empd = new EditModPackDialog(LaunchFrame.getInstance(), selectedPack);
                    empd.setVisible(true);
                }
            }
        });
        buttonsPanel.add(editModPack);

        // stub for a real wait message
        final JPanel p = new JPanel();
        p.setBackground(Color.cyan);
        ;
        p.setMinimumSize(new Dimension(420, 55));

        JTextArea filler = new JTextArea(I18N.getLocaleString("MODS_WAIT_WHILE_LOADING"));
        filler.setBorder(null);
        filler.setEditable(false);
        filler.setForeground(LauncherStyle.getCurrentStyle().tabPaneForeground);
        filler.setBackground(LauncherStyle.getCurrentStyle().tabPaneBackground);
        //p.add(loadingImage);
        p.add(filler);

        splitPane = new ObjectInfoSplitPane();
        packs = splitPane.getPacks();
        packInfo = splitPane.getPackInfo();
        infoScroll = splitPane.getInfoScroll();
        packsScroll = splitPane.getPacksScroll();
        add(splitPane, BorderLayout.CENTER);

        packs.add(p);

        server = new JButton(I18N.getLocaleString("DOWNLOAD_SERVER"));
        server.setMinimumSize(new Dimension(130, 25));

        //TODO: check
        server.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent event) {
                String url;

                ModPack pack = selectedPack;

                if ((LaunchFrame.currentPane == LaunchFrame.Panes.MODPACK || LaunchFrame.currentPane == LaunchFrame.Panes.THIRDPARTY) && !pack.getServerUrl().isEmpty()) {
                    if (!packPanels.isEmpty()) {
                        if (!pack.getServerUrl().equals("") && pack.getServerUrl() != null) {
                            String version = (Settings.getSettings().getPackVer().equalsIgnoreCase("recommended version") || Settings.getSettings().getPackVer().equalsIgnoreCase("newest version"))
                                    ? pack.getVersion().replace(".", "_")
                                    : Settings.getSettings().getPackVer().replace(".", "_");
                            if (pack.isPrivatePack()) {
                                url = DownloadUtils.getCreeperhostLink("privatepacks/" + pack.getDir() + "/" + version + "/" + pack.getServerUrl());
                            } else {
                                url = DownloadUtils.getCreeperhostLink("modpacks/" + pack.getDir() + "/" + version + "/" + pack.getServerUrl());
                            }

                            if (DownloadUtils.fileExistsURL(url)) {
                                OSUtils.browse(url);
                            } else {
                                ErrorUtils.tossError("Server file for selected version was not found on the server");
                            }
                            TrackerUtils.sendPageView(pack.getName() + " Server Download", "Server Download / " + pack.getName() + " / " + version);
                        }
                    }
                }
            }
        });
        buttonsPanel.add(server);

        version = new JComboBox(new String[] { });
        version.setMinimumSize(new Dimension(130, 25));
        version.addActionListener(al);
        version.setToolTipText(I18N.getLocaleString("MODPACK_VERSIONS"));
        buttonsPanel.add(version);

        privatePack = new JButton(I18N.getLocaleString("PACK_CODES"));
        privatePack.setMinimumSize(new Dimension(120, 25));
        privatePack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e) {
                PrivatePackDialog ap = new PrivatePackDialog();
                ap.setVisible(true);
            }
        });

        buttonsPanel.add(privatePack);

        // Resize scrollbar when center divider is moved
        packsScroll.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized (ComponentEvent e) {
                int itemsPerWidth = packs.getWidth() / 420;
                if (itemsPerWidth < 1) {
                    itemsPerWidth = 1;
                }
                packs.setMinimumSize(new Dimension(420, (packPanels.size() * (55 + ObjectInfoSplitPane.verticalItemPadding)) / itemsPerWidth));
                packs.setPreferredSize(new Dimension(420, (packPanels.size() * (55 + ObjectInfoSplitPane.verticalItemPadding)) / itemsPerWidth));
            }
        });
    }

    JScrollPane infoScroll;
    final ActionListener al = new ActionListener() {
        @Override
        public void actionPerformed (ActionEvent arg0) {
            if (version.getItemCount() > 0) {
                Settings.getSettings().setPackVer((String.valueOf(version.getSelectedItem()).equalsIgnoreCase("recommended") ? "Recommended Version" : String.valueOf(version.getSelectedItem())));
                Settings.getSettings().save();
            }
        }
    };

    /*
     * GUI Code to add a modpack to the selection
     */
    public JPanel addPack (final ModPack pack) {
        if (!modPacksAdded) {
            modPacksAdded = true;
            packs.removeAll();
            packs.repaint();
        }
        final int packIndex = packPanels.size();
        final JPanel p = new JPanel();
        p.setPreferredSize(new Dimension(420, 55));
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
            public void mouseClicked (MouseEvent e) {
                if (e.getClickCount() == 2) {
                    LaunchFrame.getInstance().doLaunch();
                }
            }

            @Override
            public void mousePressed (MouseEvent e) {
                ModPack.setSelectedPack(pack);
                selectedPack = pack;
                updateInfoScreen();
            }
        };

        p.addMouseListener(lin);
        filler.addMouseListener(lin);
        logo.addMouseListener(lin);
        p.add(filler);
        p.add(logo);
        packPanels.add(p);
        packs.add(p);

        packs.setMinimumSize(new Dimension(420, (packPanels.size() * (55 + ObjectInfoSplitPane.verticalItemPadding))));
        packs.setPreferredSize(new Dimension(420, (packPanels.size() * (55 + ObjectInfoSplitPane.verticalItemPadding))));

        //packsScroll.revalidate();
        if (pack.getDir().equalsIgnoreCase(getLastPack())) {
            selectedPack = pack;
        }
        return p;
    }

    //TODO handle changes & removals here as well!!!
    @Subscribe
    public void packChange (PackChangeEvent evt) {
        final PackChangeEvent event = evt;
        SwingUtilities.invokeLater(new Runnable() {
            public void run () {
                if (event.getType() == PackChangeEvent.TYPE.ADD) {
                    filterPacks();
                } else if (event.getType() == PackChangeEvent.TYPE.FILTER) {
                    filterPacks();
                } else if (event.getType() == PackChangeEvent.TYPE.REMOVE) {
                    filterPacks();
                }
            }
        });
    }

    public void filterPacks () {
        // TODO add sorting. How, where?
        packPanels.clear();
        packs.removeAll();
        selectedPack = null;
        packInfo.setText("");
        // all removed, repaint
        packs.repaint();
        // not really needed
        //modPacksAdded = false;
        for (ModPack pack : ModPack.getPackArray()) {
            if (filterForTab(pack) && mcVersionCheck(pack) && avaliabilityCheck(pack) && textSearch(pack)) {
                JPanel p = addPack(pack);
                panelByPack.put(pack, p);
            }
        }
        updateInfoScreen();
    }

    // WTF: this does not update packs!!
    // only updating info for selected pack. pulldown menus and info area!
    public void updateInfoScreen () {
        // make all colors and cursors normal in left side list
        for (JPanel p: panelByPack.values()) {
            p.setBackground(UIManager.getColor("control"));
            p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
                ModPack pack = selectedPack;
                if (pack != null && panelByPack.get(selectedPack)!= null) {
                    String mods = "";
                    if (pack.getMods() != null) {
                        mods += "<p>This pack contains the following mods by default:</p><ul>";
                        for (String name : pack.getMods()) {
                            mods += "<li>" + name + "</li>";
                        }
                        mods += "</ul>";
                    }

                    panelByPack.get(selectedPack).setBackground(UIManager.getColor("control").darker().darker());
                    panelByPack.get(selectedPack).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    File tempDir = new File(OSUtils.getCacheStorageLocation(), "ModPacks" + File.separator + pack.getDir());
                    packInfo.setText("<html><img src='file:///" + tempDir.getPath() + File.separator + pack.getImageName() + "' width=400 height=200></img> <br>"
                            + pack.getInfo() + mods);
                    packInfo.setCaretPosition(0);

                    if (selectedPack.getServerUrl().equals("") || selectedPack.getServerUrl() == null) {
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
        //}
    }

    public void updateFilterText () {
        String filterTextColor = LauncherStyle.getColorAsString(LauncherStyle.getCurrentStyle().filterTextColor);
        String filterInnerTextColor = LauncherStyle.getColorAsString(LauncherStyle.getCurrentStyle().filterInnerTextColor);
        String typeLblText = "<html><body>";
        typeLblText += "<strong><font color=rgb\"(" + filterTextColor + ")\">Filter: </strong></font>";
        typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + avaliability + "</font>";
        typeLblText += "<font color=rgb\"(" + filterTextColor + ")\"> / </font>";
        typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + mcVersion + "</font>";
        typeLblText += "</body></html>";

        typeLbl.setText(typeLblText);
    }

    public void updateFilter () {
        updateFilterText();
        filterPacks();
        LaunchFrame.getInstance().updateFooter();
    }

    public void updateLocale () {
        origin = I18N.getLocaleString("MAIN_ALL");
        mcVersion = I18N.getLocaleString("MAIN_ALL");
        avaliability = I18N.getLocaleString("MAIN_ALL");
        filter.setText(I18N.getLocaleString("FILTER_SETTINGS"));
        updateFilterText();
        editModPack.setText(I18N.getLocaleString("MODS_EDIT_PACK"));
        if (I18N.currentLocale == I18N.Locale.deDE) {
            editModPack.setBounds(290, 5, 120, 25);
            typeLbl.setBounds(115, 5, 165, 25);
        } else {
            editModPack.setBounds(300, 5, 110, 25);
            typeLbl.setBounds(115, 5, 175, 25);
        }
    }

    boolean avaliabilityCheck (ModPack pack) {
        return (avaliability.equalsIgnoreCase(I18N.getLocaleString("MAIN_ALL"))) || (avaliability.equalsIgnoreCase(I18N.getLocaleString("FILTER_PUBLIC")) && !pack.isPrivatePack())
                || (avaliability.equalsIgnoreCase(I18N.getLocaleString("FILTER_PRIVATE")) && pack.isPrivatePack());
    }

    boolean mcVersionCheck (ModPack pack) {
        return (mcVersion.equalsIgnoreCase(I18N.getLocaleString("MAIN_ALL"))) || (mcVersion.equalsIgnoreCase(pack.getMcVersion()));
    }

    boolean textSearch (ModPack pack) {
        String s  = searchString.toLowerCase();
        return ((s.isEmpty()) || pack.getName().toLowerCase().contains(s) || pack.getAuthor().toLowerCase().contains(s));
    }

    abstract boolean filterForTab (ModPack pack);

    abstract String getLastPack ();

    abstract String getPaneShortName ();

    abstract boolean isFTB ();

    abstract AbstractModPackPane getThis ();
}
