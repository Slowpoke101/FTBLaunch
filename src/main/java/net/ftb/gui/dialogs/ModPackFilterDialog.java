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
import javax.swing.Spring;
import javax.swing.SpringLayout;

import net.ftb.data.ModPack;
import net.ftb.gui.GuiConstants;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.panes.AbstractModPackPane;
import net.ftb.locale.I18N;
import net.ftb.util.SwingUtils;
import net.miginfocom.swing.MigLayout;

public class ModPackFilterDialog extends JDialog {
    private JLabel availabilityLbl;
    private JComboBox availability;
    private JLabel mcVersionLbl;
    private JComboBox mcVersion;
    private JButton apply;
    private JButton cancel;
    private JButton search;

    private AbstractModPackPane pane;

    public ModPackFilterDialog(AbstractModPackPane instance) {
        super(LaunchFrame.getInstance(), true);
        this.pane = instance;

        setupGui();

        getRootPane().setDefaultButton(apply);

        this.pane = instance;

        ArrayList<String> mcVersions = new ArrayList<String>();
        mcVersion.addItem(I18N.getLocaleString("MAIN_ALL"));
        mcVersions.add(I18N.getLocaleString("MAIN_ALL"));
        for (ModPack pack : ModPack.getPackArray()) {
            if (!mcVersions.contains(pack.getMcVersion())) {
                mcVersions.add(pack.getMcVersion());
                mcVersion.addItem(pack.getMcVersion());
            }
        }

        mcVersion.setModel(new DefaultComboBoxModel(mcVersions.toArray()));
        availability.setModel(new DefaultComboBoxModel(new String[] { I18N.getLocaleString("MAIN_ALL"), I18N.getLocaleString("FILTER_PUBLIC"), I18N.getLocaleString("FILTER_PRIVATE") }));

        mcVersion.setSelectedItem(pane.mcVersion);
        availability.setSelectedItem(pane.avaliability);

        pack();

        apply.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
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
        panel.setLayout(new MigLayout());

        availabilityLbl = new JLabel(I18N.getLocaleString("FILTER_MODPACKAVALIABILITY"));
        mcVersionLbl = new JLabel(I18N.getLocaleString("FILTER_MCVERSION"));
        mcVersion = new JComboBox();
        availability = new JComboBox();
        apply = new JButton(I18N.getLocaleString("FILTER_APPLY"));
        cancel = new JButton(I18N.getLocaleString("MAIN_CANCEL"));
        search = new JButton(I18N.getLocaleString("FILTER_SEARCHPACK"));

        mcVersion.setPrototypeDisplayValue("xxxxxxxxxxxx");
        availability.setPrototypeDisplayValue("xxxxxxxxxxxx");

        panel.add(mcVersionLbl);
        panel.add(mcVersion, GuiConstants.WRAP);
        panel.add(availabilityLbl);
        panel.add(availability, GuiConstants.WRAP);
        panel.add(search, GuiConstants.FILL_TWO);
        panel.add(cancel, "grow, " + GuiConstants.WRAP);
        panel.add(apply, GuiConstants.FILL_SINGLE_LINE);
        pack();
        setLocationRelativeTo(this.getOwner());
    }
}
