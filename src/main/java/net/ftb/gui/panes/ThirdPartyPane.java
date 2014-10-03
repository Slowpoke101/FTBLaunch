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
import lombok.Getter;
import net.ftb.data.LauncherStyle;
import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.download.Locations;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.dialogs.EditModPackDialog;
import net.ftb.gui.dialogs.ModPackFilterDialog;
import net.ftb.gui.dialogs.PrivatePackDialog;
import net.ftb.locale.I18N;
import net.ftb.util.DownloadUtils;
import net.ftb.util.ErrorUtils;
import net.ftb.util.OSUtils;
import net.ftb.util.TrackerUtils;

import java.awt.Point;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@SuppressWarnings("serial")
public class ThirdPartyPane extends AbstractModPackPane implements ILauncherPane  {
    @Getter
    private static ThirdPartyPane instance;
    @Getter
    private JScrollPane packsScroll;

    public ThirdPartyPane() {
        super();
        instance = this;
        setBorder(new EmptyBorder(5, 5, 5, 5));
        setLayout(null);

        packPanels = Lists.newArrayList();

        packs = new JPanel();
        packs.setLayout(null);
        packs.setOpaque(false);

        // stub for a real wait message
        final JPanel p = new JPanel();
        p.setBounds(0, 0, 420, 55);
        p.setLayout(null);

        filter = new JButton(I18N.getLocaleString("FILTER_SETTINGS"));
        filter.setBounds(5, 5, 105, 25);
        filter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (loaded) {
                    // TODO: problem here. How to move into abstract?
                    ModPackFilterDialog filterDia = new ModPackFilterDialog(instance);
                    filterDia.setVisible(true);
                }
            }
        });
        add(filter);

        String filterTextColor = LauncherStyle.getColorAsString(LauncherStyle.getCurrentStyle().filterTextColor);
        String filterInnerTextColor = LauncherStyle.getColorAsString(LauncherStyle.getCurrentStyle().filterInnerTextColor);

        String typeLblText = "<html><body>";
        typeLblText += "<strong><font color=rgb\"(" + filterTextColor + ")\">Filter: </strong></font>";
        typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + origin + "</font>";
        typeLblText += "<font color=rgb\"(" + filterTextColor + ")\"> / </font>";
        typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + mcVersion + "</font>";
        typeLblText += "</body></html>";

        typeLbl = new JLabel(typeLblText);
        typeLbl.setBounds(115, 5, 175, 25);
        typeLbl.setHorizontalAlignment(SwingConstants.CENTER);
        add(typeLbl);

        editModPack = new JButton(I18N.getLocaleString("MODS_EDIT_PACK"));
        editModPack.setBounds(300, 5, 110, 25);
        editModPack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (packPanels.size() > 0) {
                    //TODO: fix by rename?
                    if (getSelectedThirdPartyModIndex() >= 0) {
                        EditModPackDialog empd = new EditModPackDialog(LaunchFrame.getInstance(), ModPack.getSelectedPack(false));
                        empd.setVisible(true);
                    }
                }
            }
        });
        add(editModPack);

        JTextArea filler = new JTextArea(I18N.getLocaleString("MODS_WAIT_WHILE_LOADING"));
        filler.setBorder(null);
        filler.setEditable(false);
        filler.setForeground(LauncherStyle.getCurrentStyle().tabPaneForeground);
        filler.setBounds(58, 6, 378, 42);
        filler.setBackground(LauncherStyle.getCurrentStyle().tabPaneBackground);
        //		p.add(loadingImage);
        p.add(filler);
        packs.add(p);

        packsScroll = new JScrollPane();
        packsScroll.setBounds(-3, 30, 420, 283);
        packsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        packsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        packsScroll.setWheelScrollingEnabled(true);
        packsScroll.setOpaque(false);
        packsScroll.setViewportView(packs);
        packsScroll.getVerticalScrollBar().setUnitIncrement(19);
        add(packsScroll);

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
        add(packInfo);

        infoScroll = new JScrollPane();
        infoScroll.setBounds(410, 25, 430, 290);
        infoScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        infoScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        infoScroll.setWheelScrollingEnabled(true);
        infoScroll.setViewportView(packInfo);
        infoScroll.setOpaque(false);
        add(infoScroll);

        server = new JButton(I18N.getLocaleString("DOWNLOAD_SERVER"));
        server.setBounds(420, 5, 130, 25);

        //TODO: check
        server.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                String url;
                ModPack pack = ModPack.getSelectedPack(false);
                if (LaunchFrame.currentPane == LaunchFrame.Panes.THIRDPARTY && !pack.getServerUrl().isEmpty()) {
                    if (packPanels.size() > 0 && getSelectedThirdPartyModIndex() >= 0) {
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
        add(server);

        version = new JComboBox(new String[]{});
        version.setBounds(560, 5, 130, 25);
        version.addActionListener(al);
        version.setToolTipText(I18N.getLocaleString("MODPACK_VERSIONS"));
        add(version);

        privatePack = new JButton(I18N.getLocaleString("PACK_CODES"));
        privatePack.setBounds(700, 5, 120, 25);
        privatePack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PrivatePackDialog ap = new PrivatePackDialog();
                ap.setVisible(true);
            }
        });

        add(privatePack);
    }

    @Override
    public void onVisible() {
        ThirdPartyPane.getInstance().getPacksScroll().getViewport().setViewPosition(new Point(0, 0));
    }

    public int getSelectedThirdPartyModIndex() {
        return modPacksAdded ? getIndex() : -1;
    }

    boolean filterForTab(ModPack pack) {
        return (pack.isThirdPartyTab() && !pack.getParentXml().contains(Locations.MODPACKXML));
    }

    String getLastPack() {
        return Settings.getSettings().getLastThirdPartyPack();
    }

    String getPaneShortName() {return "Third Party";}

    boolean isFTB() {
        return false;
    }
}
