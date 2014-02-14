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
import javax.swing.Spring;
import javax.swing.SpringLayout;

import net.ftb.data.ModPack;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.panes.ModpacksPane;
import net.ftb.locale.I18N;

public class ModPackFilterDialog extends JDialog {
    private JLabel originLbl;
    private JComboBox origin;
    private JLabel availabilityLbl;
    private JComboBox availability;
    private JLabel mcVersionLbl;
    private JComboBox mcVersion;
    private JButton apply;
    private JButton cancel;
    private JButton search;

    private ModpacksPane pane;

    public ModPackFilterDialog(ModpacksPane instance) {
        super(LaunchFrame.getInstance(), true);
        this.pane = instance;

        setupGui();

        getRootPane().setDefaultButton(apply);

        this.pane = instance;

        ArrayList<String> mcVersions = new ArrayList<String>();
        mcVersion.addItem("All");
        mcVersions.add("All");
        for (ModPack pack : ModPack.getPackArray()) {
            if (!mcVersions.contains(pack.getMcVersion())) {
                mcVersions.add(pack.getMcVersion());
                mcVersion.addItem(pack.getMcVersion());
            }
        }

        mcVersion.setModel(new DefaultComboBoxModel(mcVersions.toArray()));
        origin.setModel(new DefaultComboBoxModel(new String[] { I18N.getLocaleString("MAIN_ALL"), "FTB", I18N.getLocaleString("FILTER_3THPARTY") }));
        availability.setModel(new DefaultComboBoxModel(new String[] { I18N.getLocaleString("MAIN_ALL"), I18N.getLocaleString("FILTER_PUBLIC"), I18N.getLocaleString("FILTER_PRIVATE") }));

        origin.setSelectedItem(pane.origin);
        mcVersion.setSelectedItem(pane.mcVersion);
        availability.setSelectedItem(pane.avaliability);

        pack();

        apply.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                pane.origin = (String) origin.getSelectedItem();
                pane.mcVersion = (String) mcVersion.getSelectedItem();
                pane.avaliability = (String) availability.getSelectedItem();
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
        setResizable(false);

        Container panel = getContentPane();
        SpringLayout layout = new SpringLayout();
        panel.setLayout(layout);

        originLbl = new JLabel(I18N.getLocaleString("FILTER_ORIGIN"));
        availabilityLbl = new JLabel(I18N.getLocaleString("FILTER_MODPACKAVALIABILITY"));
        mcVersionLbl = new JLabel(I18N.getLocaleString("FILTER_MCVERSION"));
        origin = new JComboBox();
        mcVersion = new JComboBox();
        availability = new JComboBox();
        apply = new JButton(I18N.getLocaleString("FILTER_APPLY"));
        cancel = new JButton(I18N.getLocaleString("MAIN_CANCEL"));
        search = new JButton(I18N.getLocaleString("FILTER_SEARCHPACK"));

        origin.setPrototypeDisplayValue("xxxxxxxxxxxx");
        mcVersion.setPrototypeDisplayValue("xxxxxxxxxxxx");
        availability.setPrototypeDisplayValue("xxxxxxxxxxxx");

        panel.add(mcVersionLbl);
        panel.add(mcVersion);
        panel.add(originLbl);
        panel.add(origin);
        panel.add(availability);
        panel.add(availabilityLbl);
        panel.add(apply);
        panel.add(cancel);
        panel.add(search);

        Spring hSpring;
        Spring columnWidth;

        hSpring = Spring.constant(10);

        layout.putConstraint(SpringLayout.WEST, mcVersionLbl, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, originLbl, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, availabilityLbl, hSpring, SpringLayout.WEST, panel);

        columnWidth = Spring.width(mcVersionLbl);
        columnWidth = Spring.max(columnWidth, Spring.width(originLbl));
        columnWidth = Spring.max(columnWidth, Spring.width(availabilityLbl));

        hSpring = Spring.sum(hSpring, columnWidth);
        hSpring = Spring.sum(hSpring, Spring.constant(10));

        layout.putConstraint(SpringLayout.WEST, mcVersion, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, origin, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, availability, hSpring, SpringLayout.WEST, panel);

        columnWidth = Spring.width(mcVersion);
        columnWidth = Spring.max(columnWidth, Spring.width(origin));
        columnWidth = Spring.max(columnWidth, Spring.width(availability));

        hSpring = Spring.sum(hSpring, columnWidth);

        layout.putConstraint(SpringLayout.EAST, mcVersion, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, origin, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, availability, hSpring, SpringLayout.WEST, panel);

        hSpring = Spring.sum(hSpring, Spring.constant(10));

        layout.putConstraint(SpringLayout.EAST, panel, hSpring, SpringLayout.WEST, panel);

        layout.putConstraint(SpringLayout.WEST, search, 10, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, search, -5, SpringLayout.HORIZONTAL_CENTER, panel);
        layout.putConstraint(SpringLayout.WEST, cancel, 5, SpringLayout.HORIZONTAL_CENTER, panel);
        layout.putConstraint(SpringLayout.EAST, cancel, -10, SpringLayout.EAST, panel);

        layout.putConstraint(SpringLayout.WEST, apply, 10, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, apply, -10, SpringLayout.EAST, panel);

        Spring vSpring;
        Spring rowHeight;

        vSpring = Spring.constant(10);

        layout.putConstraint(SpringLayout.BASELINE, mcVersionLbl, 0, SpringLayout.BASELINE, mcVersion);
        layout.putConstraint(SpringLayout.NORTH, mcVersion, vSpring, SpringLayout.NORTH, panel);

        rowHeight = Spring.height(mcVersionLbl);
        rowHeight = Spring.max(rowHeight, Spring.height(mcVersion));

        vSpring = Spring.sum(vSpring, rowHeight);
        vSpring = Spring.sum(vSpring, Spring.constant(5));

        layout.putConstraint(SpringLayout.BASELINE, originLbl, 0, SpringLayout.BASELINE, origin);
        layout.putConstraint(SpringLayout.NORTH, origin, vSpring, SpringLayout.NORTH, panel);

        rowHeight = Spring.height(originLbl);
        rowHeight = Spring.max(rowHeight, Spring.height(origin));

        vSpring = Spring.sum(vSpring, rowHeight);
        vSpring = Spring.sum(vSpring, Spring.constant(5));

        layout.putConstraint(SpringLayout.BASELINE, availabilityLbl, 0, SpringLayout.BASELINE, availability);
        layout.putConstraint(SpringLayout.NORTH, availability, vSpring, SpringLayout.NORTH, panel);

        rowHeight = Spring.height(availabilityLbl);
        rowHeight = Spring.max(rowHeight, Spring.height(availability));

        vSpring = Spring.sum(vSpring, rowHeight);
        vSpring = Spring.sum(vSpring, Spring.constant(10));

        layout.putConstraint(SpringLayout.NORTH, search, vSpring, SpringLayout.NORTH, panel);
        layout.putConstraint(SpringLayout.NORTH, cancel, vSpring, SpringLayout.NORTH, panel);

        rowHeight = Spring.height(search);
        rowHeight = Spring.max(rowHeight, Spring.height(cancel));

        vSpring = Spring.sum(vSpring, rowHeight);
        vSpring = Spring.sum(vSpring, Spring.constant(5));

        layout.putConstraint(SpringLayout.NORTH, apply, vSpring, SpringLayout.NORTH, panel);

        vSpring = Spring.sum(vSpring, Spring.height(apply));
        vSpring = Spring.sum(vSpring, Spring.constant(10));

        layout.putConstraint(SpringLayout.SOUTH, panel, vSpring, SpringLayout.NORTH, panel);

        pack();
        setLocationRelativeTo(this.getOwner());
    }
}
