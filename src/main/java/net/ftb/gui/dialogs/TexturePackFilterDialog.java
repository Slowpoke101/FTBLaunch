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
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;

import com.google.common.collect.Lists;
import net.ftb.data.ModPack;
import net.ftb.data.TexturePack;
import net.ftb.gui.GuiConstants;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.panes.TexturepackPane;
import net.ftb.locale.I18N;
import net.miginfocom.swing.MigLayout;

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

    public TexturePackFilterDialog (final TexturepackPane instance) {
        super(LaunchFrame.getInstance(), true);
        this.instance = instance;
        setupGui();

        getRootPane().setDefaultButton(apply);

        int textures = TexturePack.getTexturePackArray().size();

        ArrayList<String> res = Lists.newArrayList();
        res.add(I18N.getLocaleString("MAIN_ALL"));
        for (int i = 0; i < textures; i++) {
            if (!res.contains(TexturePack.getTexturePack(i).getResolution())) {
                res.add(TexturePack.getTexturePack(i).getResolution());
            }
        }

        ArrayList<String> comp = Lists.newArrayList();
        comp.add(I18N.getLocaleString("MAIN_ALL"));
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

        compatiblePack.setSelectedItem(TexturepackPane.compatible);
        resolution.setSelectedItem(TexturepackPane.resolution);

        apply.addActionListener(new ActionListener() {
            @SuppressWarnings("static-access")
            @Override
            public void actionPerformed (ActionEvent arg0) {
                TexturepackPane.compatible = (String) compatiblePack.getSelectedItem();
                TexturepackPane.resolution = (String) resolution.getSelectedItem();
                TexturepackPane.updateFilter();
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
        setResizable(true);

        Container panel = getContentPane();
        panel.setLayout(new MigLayout());

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
        panel.add(compatiblePack, GuiConstants.WRAP);
        panel.add(resolutionLbl);
        panel.add(resolution, GuiConstants.WRAP);
        panel.add(search, GuiConstants.FILL_TWO);
        panel.add(cancel, "grow, " + GuiConstants.WRAP);
        panel.add(apply, GuiConstants.FILL_SINGLE_LINE);

        pack();
        setLocationRelativeTo(this.getOwner());
    }
}
