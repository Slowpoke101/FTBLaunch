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

import com.google.common.collect.Maps;
import lombok.Getter;
import net.ftb.data.LauncherStyle;
import net.ftb.data.Map;
import net.ftb.data.ModPack;
import net.ftb.data.events.MapListener;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.dialogs.MapFilterDialog;
import net.ftb.gui.dialogs.SearchDialog;
import net.ftb.locale.I18N;
import net.ftb.log.Logger;
import net.ftb.util.OSUtils;

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
import java.util.List;

import javax.swing.*;

public class MapUtils extends JPanel implements ILauncherPane, MapListener {

    protected static JPanel maps;
    public static ArrayList<JPanel> mapPanels;

    @Getter
    private static JScrollPane mapsScroll;

    @Getter
    ObjectInfoSplitPane splitPane;

    private static JLabel typeLbl;
    private static JButton filter;
    private static int selectedMap = 0;
    protected static boolean mapsAdded = false;
    public static String type = I18N.getLocaleString("MAIN_CLIENT"), origin = I18N.getLocaleString("MAIN_ALL"), compatible = I18N.getLocaleString("MAIN_ALL");

    //stuff for swapping between maps/texture packs
    private JButton mapButton;
    private JButton textureButton;

    private static JEditorPane mapInfo;

    public static boolean loaded = false;

    @Getter
    private static MapUtils instance;

    private static HashMap<Integer, Map> currentMaps = Maps.newHashMap();

    public MapUtils () {
        super();
        instance = this;
        this.setBorder(null);
        setLayout(new BorderLayout());
        setup();
    }

    @Override
    public void onVisible () {
        MapUtils.sortMaps();
        MapUtils.updateFilter();
        MapUtils.getInstance().getMapsScroll().getViewport().setViewPosition(new Point(0, 0));
    }

    public void setup () {

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(1, 4));
        buttonsPanel.setMinimumSize(new Dimension(420, 25));
        add(buttonsPanel, BorderLayout.PAGE_START);

        mapPanels = new ArrayList<JPanel>();

        // TODO: Set loading animation while we wait
        maps = new JPanel();
        maps.setLayout(null);
        maps.setOpaque(false);

        final JPanel p = new JPanel();
        p.setBounds(0, 0, 420, 55);
        p.setLayout(null);

        filter = new JButton(I18N.getLocaleString("FILTER_SETTINGS"));
        filter.setBounds(5, 5, 105, 25);
        filter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e) {
                if (loaded) {
                    MapFilterDialog filterDia = new MapFilterDialog(getInstance());
                    filterDia.setVisible(true);
                }
            }
        });
        buttonsPanel.add(filter);

        String filterTextColor = LauncherStyle.getColorAsString(LauncherStyle.getCurrentStyle().filterTextColor);
        String filterInnerTextColor = LauncherStyle.getColorAsString(LauncherStyle.getCurrentStyle().filterInnerTextColor);

        String typeLblText = "<html><body>";
        typeLblText += "<strong><font color=rgb\"(" + filterTextColor + ")\">Filter: </strong></font>";
        typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + type + "</font>";
        typeLblText += "<font color=rgb\"(" + filterTextColor + ")\"> / </font>";
        typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + origin + "</font>";
        typeLblText += "<font color=rgb\"(" + filterTextColor + ")\"> / </font>";
        typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + compatible + "</font>";
        typeLblText += "</body></html>";

        typeLbl = new JLabel(typeLblText);
        typeLbl.setBounds(115, 5, 295, 25);
        typeLbl.setHorizontalAlignment(SwingConstants.CENTER);
        buttonsPanel.add(typeLbl);

        mapButton = new JButton(I18N.getLocaleString("SWAP_MAP"));
        mapButton.setBounds(400, 5, 105, 25);
        mapButton.setBackground(UIManager.getColor("control").darker().darker());
        mapButton.setForeground(UIManager.getColor("text").darker());
        mapButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                LaunchFrame.getInstance().swapTabs(true);
            }
        });
        buttonsPanel.add(mapButton);

        textureButton = new JButton(I18N.getLocaleString("SWAP_TEXTURE"));
        textureButton.setBounds(510, 5, 105, 25);
        textureButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                LaunchFrame.getInstance().swapTabs(false);
            }
        });
        buttonsPanel.add(textureButton);

        JTextArea filler = new JTextArea(I18N.getLocaleString("MAPS_WAIT_WHILE_LOADING"));
        filler.setBorder(null);
        filler.setEditable(false);
        filler.setForeground(Color.white);
        filler.setBounds(58, 6, 378, 42);
        filler.setBackground(new Color(255, 255, 255, 0));
        p.add(filler);

        splitPane = new ObjectInfoSplitPane();
        maps = splitPane.getPacks();
        mapInfo = splitPane.getPackInfo();
        //infoScroll = splitPane.getInfoScroll();
        mapsScroll = splitPane.getPacksScroll();
        add(splitPane); //, BorderLayout.CENTER);

        maps.add(p);

        // Resize scrollbar when center divider is moved
        mapsScroll.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized (ComponentEvent e) {
                int itemsPerWidth = maps.getWidth() / 420;
                if (itemsPerWidth < 1) {
                    itemsPerWidth = 1;
                }
                maps.setMinimumSize(new Dimension(420, (mapPanels.size() * (55 + ObjectInfoSplitPane.verticalItemPadding)) / itemsPerWidth));
                maps.setPreferredSize(new Dimension(420, (mapPanels.size() * (55 + ObjectInfoSplitPane.verticalItemPadding)) / itemsPerWidth));
            }
        });

    }

    /*
     * GUI Code to add a map to the selection
     */
    public static void addMap (Map map) {
        if (!mapsAdded) {
            mapsAdded = true;
            maps.removeAll();
        }

        final int mapIndex = mapPanels.size();
        final JPanel p = new JPanel();
        p.setPreferredSize(new Dimension(420, 55));
        p.setLayout(null);

        JLabel logo = new JLabel(new ImageIcon(map.getLogo()));
        logo.setBounds(6, 6, 42, 42);
        logo.setVisible(true);

        ModPack pack = ModPack.getPack(map.getCompatible()[0]);
        JTextArea filler = new JTextArea(map.getName() + " (v." + map.getVersion() + ")\n" + "By " + map.getAuthor() + " for MC v" + (pack != null ? pack.getMcVersion() : "unknown"));
        filler.setBorder(null);
        filler.setEditable(false);
        filler.setForeground(Color.white);
        filler.setBounds(58, 6, 378, 42);
        filler.setBackground(LauncherStyle.getCurrentStyle().tabPaneBackground);
        MouseAdapter lin = new MouseAdapter() {
            @Override
            public void mouseClicked (MouseEvent e) {
                selectedMap = mapIndex;
                updateMaps();
            }

            @Override
            public void mousePressed (MouseEvent e) {
                selectedMap = mapIndex;
                updateMaps();
            }
        };
        p.addMouseListener(lin);
        filler.addMouseListener(lin);
        logo.addMouseListener(lin);
        p.add(filler);
        p.add(logo);
        mapPanels.add(p);
        maps.add(p);

        maps.setMinimumSize(new Dimension(420, (mapPanels.size() * (55 + ObjectInfoSplitPane.verticalItemPadding))));
        maps.setPreferredSize(new Dimension(420, (mapPanels.size() * (55 + ObjectInfoSplitPane.verticalItemPadding))));

    }

    @Override
    public void onMapAdded (Map map) {
        final Map map_ = map;
        SwingUtilities.invokeLater(new Runnable() {
            public void run () {
                addMap(map_);
                Logger.logInfo("Adding map " + getMapNum() + " (" + map_.getName() + ")");
                updateMaps();
            }
        });
    }

    public static void sortMaps () {
        mapPanels.clear();
        maps.removeAll();
        currentMaps.clear();
        int counter = 0;
        selectedMap = 0;
        maps.repaint();
        LaunchFrame.updateMapInstallLocs(new String[] { "" });
        mapInfo.setText("");
        ModPack FTBPack = FTBPacksPane.getInstance().getSelectedPack();
        ModPack ThirdpartyPack = ThirdPartyPane.getInstance().getSelectedPack();
        HashMap<Integer, List<Map>> sorted = Maps.newHashMap();
        sorted.put(0, new ArrayList<Map>());
        sorted.put(1, new ArrayList<Map>());
        for (Map map : Map.getMapArray()) {
            if (originCheck(map) && compatibilityCheck(map) && textSearch(map)) {
                if (FTBPack != null && map.isCompatible(FTBPack.getName())) {
                    sorted.get(1).add(map);
                } else if (ThirdpartyPack != null && map.isCompatible(ThirdpartyPack.getName())) {
                    sorted.get(1).add(map);
                } else {
                    sorted.get(0).add(map);
                }
            }
        }
        for (Map map : sorted.get(1)) {
            addMap(map);
            currentMaps.put(counter, map);
            counter++;
        }
        for (Map map : sorted.get(0)) {
            addMap(map);
            currentMaps.put(counter, map);
            counter++;
        }
        updateMaps();
    }

    private static void updateMaps () {
        for (int i = 0; i < mapPanels.size(); i++) {
            if (selectedMap == i) {
                String packs = "";
                if (Map.getMap(getIndex()).getCompatible() != null) {
                    packs += "<p>This map works with the following packs:</p><ul>";
                    for (String name : Map.getMap(getIndex()).getCompatible()) {
                        packs += "<li>" + (ModPack.getPack(name) != null ? ModPack.getPack(name).getNameWithVersion() : name) + "</li>";
                    }
                    packs += "</ul>";
                }
                mapPanels.get(i).setBackground(UIManager.getColor("control").darker().darker());
                mapPanels.get(i).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                LaunchFrame.updateMapInstallLocs(Map.getMap(getIndex()).getCompatible());
                File tempDir = new File(OSUtils.getCacheStorageLocation(), "Maps" + File.separator + Map.getMap(getIndex()).getMapName());
                mapInfo.setText("<html><img src='file:///" + tempDir.getPath() + File.separator + Map.getMap(getIndex()).getImageName() + "' width=400 height=200></img> <br>"
                        + Map.getMap(getIndex()).getInfo() + packs);
                mapInfo.setCaretPosition(0);
            } else {
                mapPanels.get(i).setBackground(UIManager.getColor("control"));
                mapPanels.get(i).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        }
    }

    public static int getSelectedMapIndex () {
        return mapsAdded ? getIndex() : -1;
    }

    public static void updateFilter () {
        // TODO: Show Modpack specific filtering
        String filterTextColor = LauncherStyle.getColorAsString(LauncherStyle.getCurrentStyle().filterTextColor);
        String filterInnerTextColor = LauncherStyle.getColorAsString(LauncherStyle.getCurrentStyle().filterInnerTextColor);

        String typeLblText = "<html><body>";
        typeLblText += "<strong><font color=rgb\"(" + filterTextColor + ")\">Filter: </strong></font>";
        typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + type + "</font>";
        typeLblText += "<font color=rgb\"(" + filterTextColor + ")\"> / </font>";
        typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + origin + "</font>";
        typeLblText += "<font color=rgb\"(" + filterTextColor + ")\"> / </font>";
        typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + compatible + "</font>";
        typeLblText += "</body></html>";

        typeLbl.setText(typeLblText);
        sortMaps();
        LaunchFrame.getInstance().updateFooter();
    }

    private static int getIndex () {
        return (currentMaps.size() > 0) ? currentMaps.get(selectedMap).getIndex() : selectedMap;
    }

    private static int getMapNum () {
        if (currentMaps.size() > 0) {
            if (!origin.equalsIgnoreCase(I18N.getLocaleString("MAIN_ALL"))) {
                return currentMaps.get((mapPanels.size() - 1)).getIndex();
            }
        }
        return mapPanels.size();
    }

    public void updateLocale () {
        filter.setText(I18N.getLocaleString("FILTER_SETTINGS"));
    }

    private static boolean originCheck (Map map) {
        return (origin.equalsIgnoreCase(I18N.getLocaleString("MAIN_ALL"))) || (origin.equalsIgnoreCase(I18N.getLocaleString("FILTER_FTB")) && map.getAuthor().equalsIgnoreCase("the ftb team"))
                || (origin.equalsIgnoreCase(I18N.getLocaleString("FILTER_3THPARTY")) && !map.getAuthor().equalsIgnoreCase("the ftb team"));
    }

    private static boolean compatibilityCheck (Map map) {
        return (compatible.equalsIgnoreCase(I18N.getLocaleString("MAIN_ALL")) || map.isCompatible(compatible));
    }

    private static boolean textSearch (Map map) {
        String searchString = SearchDialog.lastMapSearch.toLowerCase();
        return ((searchString.isEmpty()) || map.getName().toLowerCase().contains(searchString) || map.getAuthor().toLowerCase().contains(searchString));
    }
}
