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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import com.google.common.collect.Maps;
import lombok.Getter;
import net.ftb.data.LauncherStyle;
import net.ftb.data.ModPack;
import net.ftb.data.TexturePack;
import net.ftb.data.events.TexturePackListener;
import net.ftb.gui.LauncherFrame;
import net.ftb.gui.dialogs.SearchDialog;
import net.ftb.gui.dialogs.TexturePackFilterDialog;
import net.ftb.locale.I18N;
import net.ftb.log.Logger;
import net.ftb.util.OSUtils;

@SuppressWarnings("serial")
public class TexturepackPane extends JPanel implements ILauncherPane, TexturePackListener {
    private static JPanel texturePacks;
    public static ArrayList<JPanel> texturePackPanels;
    @Getter
    private static JScrollPane texturePacksScroll;

    //stuff for swapping between maps/texture packs
    private JButton mapButton;
    private JButton textureButton;

    private static JLabel typeLbl;
    public static String compatible = "All", resolution = "All";
    private JButton filter;
    private static boolean texturePacksAdded = false;
    private static int selectedTexturePack = 0;
    private static JEditorPane textureInfo;

    @Getter
    private static TexturepackPane instance;

    private static HashMap<Integer, TexturePack> currentTexturePacks = Maps.newHashMap();

    public static boolean loaded = false;

    public TexturepackPane() {
        super();
        instance = this;
        this.setBorder(new EmptyBorder(5, 5, 5, 5));
        this.setLayout(null);

        texturePackPanels = new ArrayList<JPanel>();

        // TODO: Set loading animation while we wait
        texturePacks = new JPanel();
        texturePacks.setLayout(null);
        texturePacks.setOpaque(false);

        final JPanel p = new JPanel();
        p.setBounds(0, 0, 420, 55);
        p.setLayout(null);

        filter = new JButton(I18N.getLocaleString("FILTER_SETTINGS"));
        filter.setBounds(5, 5, 105, 25);
        filter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                TexturePackFilterDialog filter = new TexturePackFilterDialog(instance);
                filter.setVisible(true);
            }
        });
        add(filter);

        String filterTextColor = LauncherStyle.getColorAsString(LauncherStyle.getCurrentStyle().filterTextColor);
        String filterInnerTextColor = LauncherStyle.getColorAsString(LauncherStyle.getCurrentStyle().filterInnerTextColor);

        String typeLblText = "<html><body>";
        typeLblText += "<strong><font color=rgb\"(" + filterTextColor + ")\">Filter: </strong></font>";
        typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + compatible + "</font>";
        typeLblText += "<strong><font color=rgb\"(" + filterTextColor + ")\"> / </strong></font>";
        typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + resolution + "</font>";
        typeLblText += "</body></html>";


        mapButton = new JButton(I18N.getLocaleString("SWAP_MAP"));
        mapButton.setBounds(400, 5, 105, 25);
        mapButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                LauncherFrame.getInstance().swapTabs(true);
            }
        });
        add(mapButton);

        textureButton = new JButton(I18N.getLocaleString("SWAP_TEXTURE"));
        textureButton.setBounds(510, 5, 105, 25);
        textureButton.setBackground(UIManager.getColor("control").darker().darker());
        textureButton.setForeground(UIManager.getColor("text").darker());
        textureButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                LauncherFrame.getInstance().swapTabs(false);
            }
        });
        add(textureButton);


        typeLbl = new JLabel(typeLblText);
        typeLbl.setBounds(115, 5, 295, 25);
        typeLbl.setHorizontalAlignment(SwingConstants.CENTER);
        add(typeLbl);

        JTextArea filler = new JTextArea(I18N.getLocaleString("TEXTURE_WAIT_WHILE_LOADING"));
        filler.setBorder(null);
        filler.setEditable(false);
        filler.setForeground(LauncherStyle.getCurrentStyle().tabPaneForeground);
        filler.setBounds(58, 6, 378, 42);
        filler.setBackground(LauncherStyle.getCurrentStyle().tabPaneBackground);
        p.add(filler);
        texturePacks.add(p);

        texturePacksScroll = new JScrollPane();
        texturePacksScroll.setBounds(-3, 30, 420, 283);
        texturePacksScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        texturePacksScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        texturePacksScroll.setWheelScrollingEnabled(true);
        texturePacksScroll.setOpaque(false);
        texturePacksScroll.setViewportView(texturePacks);
        texturePacksScroll.getVerticalScrollBar().setUnitIncrement(19);
        add(texturePacksScroll);

        textureInfo = new JEditorPane();
        textureInfo.setEditable(false);
        textureInfo.setContentType("text/html");
        textureInfo.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate (HyperlinkEvent event) {
                if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    OSUtils.browse(event.getURL().toString());
                }
            }
        });
        textureInfo.setBounds(420, 210, 410, 90);
        textureInfo.setBackground(UIManager.getColor("control").darker().darker());
        add(textureInfo);

        JScrollPane infoScroll = new JScrollPane();
        infoScroll.setBounds(410, 25, 430, 290);
        infoScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        infoScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        infoScroll.setWheelScrollingEnabled(true);
        infoScroll.setViewportView(textureInfo);
        infoScroll.setOpaque(false);
        add(infoScroll);
    }

    @Override
    public void onVisible () {
        updateFilter();
        TexturepackPane.getInstance().getTexturePacksScroll().getViewport().setViewPosition(new Point(0, 0));
    }

    /*
     * GUI Code to add a texture pack to the selection
     */
    public static void addTexturePack (TexturePack texturePack) {
        if (!texturePacksAdded) {
            texturePacksAdded = true;
            texturePacks.removeAll();
        }

        final int texturePackIndex = texturePackPanels.size();

        final JPanel p = new JPanel();
        p.setBounds(0, (texturePackIndex * 55), 420, 55);
        p.setLayout(null);
        JLabel logo = new JLabel(new ImageIcon(texturePack.getLogo()));
        logo.setBounds(6, 6, 42, 42);
        logo.setVisible(true);
        String info;
        if (texturePack.getInfo().length() > 60) {
            info = texturePack.getInfo().substring(0, 59) + "...";
        } else {
            info = texturePack.getInfo();
        }
        JTextArea filler = new JTextArea(texturePack.getName() + " : " + texturePack.getAuthor() + "\n" + info);
        filler.setBorder(null);
        filler.setEditable(false);
        filler.setForeground(LauncherStyle.getCurrentStyle().tabPaneForeground);
        filler.setBounds(58, 6, 378, 42);
        filler.setBackground(LauncherStyle.getCurrentStyle().tabPaneBackground);
        MouseAdapter lin = new MouseAdapter() {
            @Override
            public void mouseClicked (MouseEvent e) {
                selectedTexturePack = texturePackIndex;
                updateTexturePacks();
            }
        };
        p.addMouseListener(lin);
        filler.addMouseListener(lin);
        logo.addMouseListener(lin);
        p.add(filler);
        p.add(logo);
        texturePackPanels.add(p);
        texturePacks.add(p);
        if (compatible.equalsIgnoreCase("all") && resolution.equalsIgnoreCase("all")) {
            texturePacks.setMinimumSize(new Dimension(420, (TexturePack.getTexturePackArray().size()) * 55));
            texturePacks.setPreferredSize(new Dimension(420, (TexturePack.getTexturePackArray().size()) * 55));
        } else {
            texturePacks.setMinimumSize(new Dimension(420, (currentTexturePacks.size()) * 55));
            texturePacks.setPreferredSize(new Dimension(420, (currentTexturePacks.size()) * 55));
        }
        texturePacksScroll.revalidate();
    }

    @Override
    public void onTexturePackAdded (TexturePack texturePack) {
        final TexturePack texturePack_ = texturePack;
        SwingUtilities.invokeLater(new Runnable() {
            public void run () {
                addTexturePack(texturePack_);
                Logger.logInfo("Adding texture pack " + getTexturePackNum() + " (" + texturePack_.getName() + ")");
                updateTexturePacks();
            }
        });
    }

    public static void sortTexturePacks () {
        texturePackPanels.clear();
        texturePacks.removeAll();
        currentTexturePacks.clear();
        int counter = 0;
        selectedTexturePack = 0;
        texturePacks.repaint();
        HashMap<Integer, List<TexturePack>> sorted = Maps.newHashMap();
        sorted.put(0, new ArrayList<TexturePack>());
        sorted.put(1, new ArrayList<TexturePack>());
        for (TexturePack texturePack : TexturePack.getTexturePackArray()) {
            if (compatibilityCheck(texturePack) && resolutionCheck(texturePack) && textSearch(texturePack)) {
                if (texturePack.isCompatible(ModPack.getSelectedPack(true).getName())) {
                    sorted.get(1).add(texturePack);
                } else if (texturePack.isCompatible(ModPack.getSelectedPack(false).getName())) {
                    sorted.get(1).add(texturePack);
                } else {
                    sorted.get(0).add(texturePack);
                }
            }
        }
        for (TexturePack tp : sorted.get(1)) {
            addTexturePack(tp);
            currentTexturePacks.put(counter, tp);
            counter++;
        }
        for (TexturePack tp : sorted.get(0)) {
            addTexturePack(tp);
            currentTexturePacks.put(counter, tp);
            counter++;
        }
        updateTexturePacks();
    }

    private static void updateTexturePacks () {
        for (int i = 0; i < texturePackPanels.size(); i++) {
            if (selectedTexturePack == i) {
                String packs = "";
                if (TexturePack.getTexturePack(getIndex()).getCompatible() != null) {
                    packs += "<p>This texture pack works with the following packs:</p><ul>";
                    for (String name : TexturePack.getTexturePack(getIndex()).getCompatible()) {
                        packs += "<li>" + (ModPack.getPack(name) != null ? ModPack.getPack(name).getName() : name) + "</li>";
                    }
                    packs += "</ul>";
                }
                texturePackPanels.get(i).setBackground(UIManager.getColor("control").darker().darker());
                texturePackPanels.get(i).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                LauncherFrame.updateTpInstallLocs(TexturePack.getTexturePack(getIndex()).getCompatible());
                File tempDir = new File(OSUtils.getCacheStorageLocation(), "TexturePacks" + File.separator + TexturePack.getTexturePack(getIndex()).getName());
                textureInfo.setText("<html><img src='file:///" + tempDir.getPath() + File.separator + TexturePack.getTexturePack(getIndex()).getImageName() + "' width=400 height=200></img> <br>"
                        + TexturePack.getTexturePack(getIndex()).getInfo() + packs);
                textureInfo.setCaretPosition(0);
            } else {
                texturePackPanels.get(i).setBackground(UIManager.getColor("control"));
                texturePackPanels.get(i).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        }
    }

    public static int getSelectedTexturePackIndex () {
        return texturePacksAdded ? getIndex() : -1;
    }

    public static void updateFilter () {
        String filterTextColor = LauncherStyle.getColorAsString(LauncherStyle.getCurrentStyle().filterTextColor);
        String filterInnerTextColor = LauncherStyle.getColorAsString(LauncherStyle.getCurrentStyle().filterInnerTextColor);

        String typeLblText = "<html><body>";
        typeLblText += "<strong><font color=rgb\"(" + filterTextColor + ")\">Filter: </strong></font>";
        typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + compatible + "</font>";
        typeLblText += "<strong><font color=rgb\"(" + filterTextColor + ")\"> / </strong></font>";
        typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + resolution + "</font>";
        typeLblText += "</body></html>";

        typeLbl.setText(typeLblText);
        sortTexturePacks();
        LauncherFrame.getInstance().updateFooter();
    }

    private static int getIndex () {
        return (currentTexturePacks.size() > 0) ? currentTexturePacks.get(selectedTexturePack).getIndex() : selectedTexturePack;
    }

    private static int getTexturePackNum () {
        if (currentTexturePacks.size() > 0) {
            if (!compatible.equalsIgnoreCase(I18N.getLocaleString("MAIN_ALL")) || !resolution.equalsIgnoreCase(I18N.getLocaleString("MAIN_ALL"))) {
                return currentTexturePacks.get((texturePackPanels.size() - 1)).getIndex();
            }
        }
        return texturePackPanels.size();
    }

    public void updateLocale () {
        filter.setText(I18N.getLocaleString("FILTER_SETTINGS"));
    }

    private static boolean compatibilityCheck (TexturePack tp) {
        return (compatible.equalsIgnoreCase(I18N.getLocaleString("MAIN_ALL")) || tp.isCompatible(compatible));
    }

    private static boolean resolutionCheck (TexturePack tp) {
        return (resolution.equalsIgnoreCase(I18N.getLocaleString("MAIN_ALL")) || tp.getResolution().equalsIgnoreCase(resolution));
    }

    private static boolean textSearch (TexturePack tp) {
        String searchString = SearchDialog.lastTextureSearch.toLowerCase();
        return ((searchString.isEmpty()) || tp.getName().toLowerCase().contains(searchString) || tp.getAuthor().toLowerCase().contains(searchString));
    }

}
