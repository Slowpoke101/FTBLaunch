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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;

import lombok.Getter;
import net.ftb.data.LauncherStyle;
import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.events.PackChangeEvent;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.dialogs.EditModPackDialog;
import net.ftb.gui.dialogs.ModPackFilterDialog;
import net.ftb.gui.dialogs.PrivatePackDialog;
import net.ftb.gui.dialogs.SearchDialog;
import net.ftb.locale.I18N;
import net.ftb.log.Logger;
import net.ftb.util.DownloadUtils;
import net.ftb.util.ErrorUtils;
import net.ftb.util.OSUtils;
import net.ftb.util.TrackerUtils;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

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

@SuppressWarnings("unchecked")
public abstract class AbstractModPackPane extends JPanel {
    
	static final int packItemPadding = 4; 
	
	// container for packs. Upgraded by appPack()
    JPanel packs;
    // array to store packs. Upgraded by addPack
    public ArrayList<JPanel> packPanels;
    //public JScrollPane packsScroll;

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

    @Getter
    protected JScrollPane packsScroll;
    @Getter
    protected JSplitPane splitPane;
    
    
    //	private JLabel loadingImage;
    public String origin = I18N.getLocaleString("MAIN_ALL"), mcVersion = I18N.getLocaleString("MAIN_ALL"), avaliability = I18N.getLocaleString("MAIN_ALL");
    public  boolean loaded = false;

    public AbstractModPackPane() {
    	super();
        
        //setBorder(new EmptyBorder(5, 5, 5, 5));
        setBorder(null);
        //setLayout(null);
        
        setLayout(new BorderLayout());
        
        
        // Contains buttons/filter info/selection boxes along top of mod pack panes
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(1,6));
        buttonsPanel.setMinimumSize(new Dimension(420,25));
        add(buttonsPanel, BorderLayout.PAGE_START);        
        
        
        
        
        packPanels = Lists.newArrayList();

        packs = new JPanel();
        packs.setLayout(new FlowLayout(FlowLayout.LEFT, 0, packItemPadding));
        //packs.setLayout(null);
        packs.setOpaque(false);

        // stub for a real wait message
        final JPanel p = new JPanel();
        p.setBackground(Color.cyan);;
        //p.setBounds(0, 0, 420, 55);
        p.setMinimumSize(new Dimension(420,55));
        //p.setLayout(null);

        filter = new JButton(I18N.getLocaleString("FILTER_SETTINGS"));
        //filter.setBounds(5, 5, 105, 25);
        filter.setMinimumSize(new Dimension(105,25));
        filter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (loaded) {
                    // TODO: problem here. How to move into abstract?  (Fyber fixed)
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
        typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + origin + "</font>";
        typeLblText += "<font color=rgb\"(" + filterTextColor + ")\"> / </font>";
        typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + mcVersion + "</font>";
        typeLblText += "</body></html>";

        typeLbl = new JLabel(typeLblText);
        //typeLbl.setBounds(115, 5, 175, 25);
        typeLbl.setMinimumSize(new Dimension(175,25));
        typeLbl.setHorizontalAlignment(SwingConstants.CENTER);
        buttonsPanel.add(typeLbl);

        editModPack = new JButton(I18N.getLocaleString("MODS_EDIT_PACK"));
        //editModPack.setBounds(300, 5, 110, 25);
        editModPack.setMinimumSize(new Dimension(110,25));
        editModPack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (packPanels.size() > 0) {
                    //TODO: fix by rename
                    if (getSelectedPackIndex() >= 0) {
                        EditModPackDialog empd = new EditModPackDialog(LaunchFrame.getInstance(), ModPack.getSelectedPack(true));
                        empd.setVisible(true);
                    }
                }
            }
        });
        buttonsPanel.add(editModPack);

        JTextArea filler = new JTextArea(I18N.getLocaleString("MODS_WAIT_WHILE_LOADING"));
        filler.setBorder(null);
        filler.setEditable(false);
        filler.setForeground(LauncherStyle.getCurrentStyle().tabPaneForeground);
        //filler.setBounds(58, 6, 378, 42);
        //filler.setMinimumSize(new Dimension(378, 42));
        filler.setBackground(LauncherStyle.getCurrentStyle().tabPaneBackground);
        //		p.add(loadingImage);
        p.add(filler);
        packs.add(p);

        packsScroll = new JScrollPane();
        //packsScroll.setLayout(new BoxLayout(packsScroll,BoxLayout.X_AXIS));
        //packsScroll.setBounds(-3, 30, 420, 283);
        packsScroll.setBorder(null);
        packsScroll.setMinimumSize(new Dimension(420, 283));
        packsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        packsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        packsScroll.setWheelScrollingEnabled(true);
        packsScroll.setOpaque(false);
        packsScroll.setViewportView(packs);
        packsScroll.getVerticalScrollBar().setUnitIncrement(19);
        add(packsScroll, BorderLayout.LINE_START);

        packInfo = new JEditorPane();
        packInfo.setEditable(false);
        packInfo.setContentType("text/html");
        packInfo.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent event) {
                if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    OSUtils.browse(event.getURL().toString());
                }
            }
        });
        // TODO: Fix darker background for text area? Or is it better blending in?
        packInfo.setBackground(UIManager.getColor("control").darker().darker());
       // add(packInfo, BorderLayout.LINE_END);

        infoScroll = new JScrollPane();
        //infoScroll.setBounds(410, 25, 430, 290);
        infoScroll.setMinimumSize(new Dimension(430,290));
        infoScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        infoScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        infoScroll.setWheelScrollingEnabled(true);
        infoScroll.setViewportView(packInfo);
        infoScroll.setOpaque(false);
        add(infoScroll, BorderLayout.LINE_END);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, packsScroll, infoScroll);
        splitPane.setDividerSize(4);
        add(splitPane, BorderLayout.CENTER);        
        
        server = new JButton(I18N.getLocaleString("DOWNLOAD_SERVER"));
        //server.setBounds(420, 5, 130, 25);
        server.setMinimumSize(new Dimension(130,25));

        //TODO: check
        server.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                String url;
                
                ModPack pack = (LaunchFrame.currentPane == LaunchFrame.Panes.MODPACK?ModPack.getSelectedPack(true):ModPack.getSelectedPack(false));
                
                if ((LaunchFrame.currentPane == LaunchFrame.Panes.MODPACK || LaunchFrame.currentPane == LaunchFrame.Panes.THIRDPARTY) && !pack.getServerUrl().isEmpty()) {
                    if (packPanels.size() > 0 && getSelectedPackIndex() >= 0) {
                        if (!pack.getServerUrl().equals("") && pack.getServerUrl() != null) {
                            String version = (Settings.getSettings().getPackVer().equalsIgnoreCase("recommended version") || Settings.getSettings().getPackVer().equalsIgnoreCase("newest version")) ? pack.getVersion().replace(".", "_")
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

        version = new JComboBox(new String[]{});
        //version.setBounds(560, 5, 130, 25);
        version.setMinimumSize(new Dimension(130,25));
        version.addActionListener(al);
        version.setToolTipText(I18N.getLocaleString("MODPACK_VERSIONS"));
        buttonsPanel.add(version);

        privatePack = new JButton(I18N.getLocaleString("PACK_CODES"));
        //privatePack.setBounds(700, 5, 120, 25);
        privatePack.setMinimumSize(new Dimension(120,25));
        privatePack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PrivatePackDialog ap = new PrivatePackDialog();
                ap.setVisible(true);
            }
        });

        buttonsPanel.add(privatePack);
        
        // Resize scrollbar when center divider is moved
        packsScroll.addComponentListener(new ComponentAdapter() {	
			@Override			
			public void componentResized(ComponentEvent e) {				
				int itemsPerWidth = packs.getWidth() / 420;
				if (itemsPerWidth < 1) itemsPerWidth = 1;
				packs.setMinimumSize(new Dimension(420, (packPanels.size() * (55 + packItemPadding)) / itemsPerWidth));
		        packs.setPreferredSize(new Dimension(420, (packPanels.size() * (55 + packItemPadding)) / itemsPerWidth));		        
			}        	
        });
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
        //p.setBounds(0, (packIndex * 55), 420, 55);
        p.setPreferredSize(new Dimension(420,55));        
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

        packs.setMinimumSize(new Dimension(420, (packPanels.size() * (55 + packItemPadding))));
        packs.setPreferredSize(new Dimension(420, (packPanels.size() * (55 + packItemPadding))));
        
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
        if (I18N.currentLocale == I18N.Locale.deDE) {
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
    abstract AbstractModPackPane getThis();
    
    public int getSelectedPackIndex() {
        return modPacksAdded ? getIndex() : -1;
    }

}
