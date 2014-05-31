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
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.Spring;
import javax.swing.SpringLayout;

import net.ftb.data.ModPack;
import net.ftb.data.TexturePack;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.panes.TexturepackPane;
import net.ftb.locale.I18N;
import net.ftb.util.SwingUtils;

@SuppressWarnings("serial")
public class TexturePackFilterDialog extends JDialog {
    private JLabel compatiblePackLbl;
    private JComboBox compatiblePack;
    private JLabel resolutionLbl;
    private JComboBox resolution;
    private JButton apply;
    private JButton cancel;
    private JButton search;

    private TexturepackPane instance;

    public TexturePackFilterDialog(final TexturepackPane instance) {
        super(LaunchFrame.getInstance(), true);
        this.instance = instance;
        setupGui();

        getRootPane().setDefaultButton(apply);

        int textures = TexturePack.getTexturePackArray().size();

        ArrayList<String> res = new ArrayList<String>();
        res.add("All");
        for (int i = 0; i < textures; i++) {
            if (!res.contains(TexturePack.getTexturePack(i).getResolution())) {
                res.add(TexturePack.getTexturePack(i).getResolution());
            }
        }

        ArrayList<String> comp = new ArrayList<String>();
        comp.add("All");
        for (int i = 0; i < textures; i++) {
            List<String> s = TexturePack.getTexturePack(i).getCompatible();
            for (String value : s) {
                if (!comp.contains(ModPack.getPack(value.trim()).getName())) {
                    comp.add(ModPack.getPack(value.trim()).getName());
                }
            }
        }

        compatiblePack.setModel(new DefaultComboBoxModel(comp.toArray(new String[comp.size()])));
        resolution.setModel(new DefaultComboBoxModel(res.toArray(new String[res.size()])));

        compatiblePack.setSelectedItem(instance.compatible);
        resolution.setSelectedItem(instance.resolution);

        apply.addActionListener(new ActionListener() {
            @SuppressWarnings("static-access")
            @Override
            public void actionPerformed (ActionEvent arg0) {
                instance.compatible = (String) compatiblePack.getSelectedItem();
                instance.resolution = (String) resolution.getSelectedItem();
                instance.updateFilter();
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
                SearchDialog sd = new SearchDialog(instance);
                sd.setVisible(true);
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

        compatiblePackLbl = new JLabel(I18N.getLocaleString("FILTER_COMPERTIBLEPACK"));
        resolutionLbl = new JLabel(I18N.getLocaleString("FILTER_RESULUTION"));
        resolution = new JComboBox();
        compatiblePack = new JComboBox();
        apply = new JButton(I18N.getLocaleString("FILTER_APPLY"));
        cancel = new JButton(I18N.getLocaleString("MAIN_CANCEL"));
        search = new JButton(I18N.getLocaleString("FILTER_TEXSEARCH"));

        resolution.setPrototypeDisplayValue("xxxxxxxxxxxxxxxxxxxxxxxxxx");
        compatiblePack.setPrototypeDisplayValue("xxxxxxxxxxxxxxxxxxxxxxxxxx");

        panel.add(compatiblePackLbl);
        panel.add(resolutionLbl);
        panel.add(compatiblePack);
        panel.add(resolution);
        panel.add(apply);
        panel.add(cancel);
        panel.add(search);

        Spring hSpring;
        Spring columnWidth;

        hSpring = Spring.constant(10);

        layout.putConstraint(SpringLayout.WEST, compatiblePackLbl, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, resolutionLbl, hSpring, SpringLayout.WEST, panel);

        columnWidth = Spring.max(Spring.width(compatiblePackLbl), Spring.width(resolutionLbl));

        hSpring = SwingUtils.springSum(hSpring, columnWidth, Spring.constant(10));

        layout.putConstraint(SpringLayout.WEST, compatiblePack, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, resolution, hSpring, SpringLayout.WEST, panel);

        columnWidth = Spring.max(Spring.width(compatiblePack), Spring.width(resolution));

        hSpring = Spring.sum(hSpring, columnWidth);

        layout.putConstraint(SpringLayout.EAST, compatiblePack, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, resolution, hSpring, SpringLayout.WEST, panel);

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

        layout.putConstraint(SpringLayout.BASELINE, compatiblePackLbl, 0, SpringLayout.BASELINE, compatiblePack);
        layout.putConstraint(SpringLayout.NORTH, compatiblePack, vSpring, SpringLayout.NORTH, panel);

        rowHeight = Spring.max(Spring.height(compatiblePackLbl), Spring.height(compatiblePack));

        vSpring = SwingUtils.springSum(vSpring, rowHeight, Spring.constant(5));

        layout.putConstraint(SpringLayout.BASELINE, resolutionLbl, 0, SpringLayout.BASELINE, resolution);
        layout.putConstraint(SpringLayout.NORTH, resolution, vSpring, SpringLayout.NORTH, panel);

        rowHeight = Spring.max(Spring.height(resolutionLbl), Spring.height(resolution));

        vSpring = SwingUtils.springSum(vSpring, rowHeight, Spring.constant(10));

        layout.putConstraint(SpringLayout.NORTH, search, vSpring, SpringLayout.NORTH, panel);
        layout.putConstraint(SpringLayout.NORTH, cancel, vSpring, SpringLayout.NORTH, panel);

        rowHeight = Spring.max(Spring.height(search), Spring.height(cancel));

        vSpring = SwingUtils.springSum(vSpring, rowHeight, Spring.constant(5));

        layout.putConstraint(SpringLayout.NORTH, apply, vSpring, SpringLayout.NORTH, panel);

        vSpring = SwingUtils.springSum(vSpring, Spring.height(apply), Spring.constant(10));

        layout.putConstraint(SpringLayout.SOUTH, panel, vSpring, SpringLayout.NORTH, panel);

        pack();
        setLocationRelativeTo(this.getOwner());
    }
}
