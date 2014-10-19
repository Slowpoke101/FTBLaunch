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
package net.ftb.gui.dialogs;

import java.awt.Container;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;

import com.google.common.collect.Lists;
import net.ftb.data.Map;
import net.ftb.data.ModPack;
import net.ftb.gui.GuiConstants;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.panes.MapUtils;
import net.ftb.locale.I18N;
import net.miginfocom.swing.MigLayout;

public class MapFilterDialog extends JDialog {
    private JLabel typeLbl;
    private JComboBox type;
    private JLabel originLbl;
    private JComboBox origin;
    private JLabel compatiblePackLbl;
    private JComboBox compatiblePack;
    private JButton apply;
    private JButton cancel;
    private JButton search;

    private MapUtils pane;

    public MapFilterDialog (MapUtils instance) {
        super(LaunchFrame.getInstance(), true);
        this.pane = instance;

        setupGui();

        getRootPane().setDefaultButton(apply);

        this.pane = instance;

        type.setSelectedItem(pane.type);
        origin.setSelectedItem(pane.origin);
        compatiblePack.setSelectedItem(pane.compatible);

        ArrayList<String> packs = Lists.newArrayList();
        compatiblePack.addItem(I18N.getLocaleString("MAIN_ALL"));
        packs.add(I18N.getLocaleString("MAIN_ALL"));
        for (int i = 0; i < Map.getMapArray().size(); i++) {
            String[] compat = Map.getMap(i).getCompatible();
            for (String compatable : compat) {
                if (!compatable.isEmpty() && !packs.contains(ModPack.getPack(compatable.trim()).getName())) {
                    packs.add(ModPack.getPack(compatable.trim()).getName());
                    compatiblePack.addItem(ModPack.getPack(compatable.trim()).getName());
                }
            }
        }

        type.setModel(new DefaultComboBoxModel(new String[] { "Client", "Server" }));
        origin.setModel(new DefaultComboBoxModel(new String[] { I18N.getLocaleString("MAIN_ALL"), "FTB", I18N.getLocaleString("FILTER_3THPARTY") }));
        compatiblePack.setModel(new DefaultComboBoxModel(packs.toArray()));

        apply.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                pane.compatible = (String) compatiblePack.getSelectedItem();
                pane.type = (String) type.getSelectedItem();
                pane.origin = (String) origin.getSelectedItem();
                pane.updateFilter();
                setVisible(false);
            }
        });

        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e) {
                setVisible(false);
            }
        });

        search.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                SearchDialog sd = new SearchDialog(pane);
                sd.setVisible(true);
                setVisible(false);
            }
        });
    }

    private void setupGui () {
        setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
        setTitle(I18N.getLocaleString("FILTER_TITLE"));
        setResizable(true);

        Container panel = getContentPane();
        panel.setLayout(new MigLayout());

        originLbl = new JLabel(I18N.getLocaleString("FILTER_ORIGIN"));
        typeLbl = new JLabel(I18N.getLocaleString("FILTER_PACKTYPE"));
        compatiblePackLbl = new JLabel(I18N.getLocaleString("FILTER_COMPERTIBLEPACK"));
        origin = new JComboBox();
        type = new JComboBox();
        compatiblePack = new JComboBox();
        apply = new JButton(I18N.getLocaleString("FILTER_APPLY"));
        cancel = new JButton(I18N.getLocaleString("MAIN_CANCEL"));
        search = new JButton(I18N.getLocaleString("FILTER_SEARCHMAP"));

        origin.setPrototypeDisplayValue("xxxxxxxxxxxxxxxxxxxxxxxxxx");
        type.setPrototypeDisplayValue("xxxxxxxxxxxxxxxxxxxxxxxxxx");
        compatiblePack.setPrototypeDisplayValue("xxxxxxxxxxxxxxxxxxxxxxxxxx");

        panel.add(typeLbl);
        panel.add(type, GuiConstants.WRAP);
        panel.add(originLbl);
        panel.add(origin, GuiConstants.WRAP);
        panel.add(compatiblePackLbl);
        panel.add(compatiblePack, GuiConstants.WRAP);
        panel.add(search, GuiConstants.FILL_TWO);
        panel.add(cancel, "grow, " + GuiConstants.WRAP);
        panel.add(apply, GuiConstants.FILL_SINGLE_LINE);

        pack();
        setLocationRelativeTo(this.getOwner());
    }
}
