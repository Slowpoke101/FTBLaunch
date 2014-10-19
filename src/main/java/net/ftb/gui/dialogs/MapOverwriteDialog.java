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

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

import net.ftb.gui.GuiConstants;
import net.ftb.gui.LaunchFrame;
import net.ftb.locale.I18N;
import net.ftb.tools.MapManager;
import net.miginfocom.swing.MigLayout;

public class MapOverwriteDialog extends JDialog {
    private JLabel messageLbl;
    private JLabel overwriteLbl;
    private JButton overwrite;
    private JButton abort;

    public MapOverwriteDialog () {
        super(LaunchFrame.getInstance(), true);

        setupGui();

        overwrite.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent event) {
                MapManager.overwrite = true;
                setVisible(false);
            }
        });

        abort.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent event) {
                MapManager.overwrite = false;
                setVisible(false);
            }
        });
    }

    private void setupGui () {
        setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
        setTitle("WARNING!");
        setResizable(true);

        Container panel = getContentPane();
        panel.setLayout(new MigLayout());

        messageLbl = new JLabel(I18N.getLocaleString("MAPOVERRIDE_FOUNDERROR"));
        overwriteLbl = new JLabel(I18N.getLocaleString("MAPOVERRIDE_WISHOVERRIDE"));
        overwrite = new JButton(I18N.getLocaleString("MAIN_YES"));
        abort = new JButton(I18N.getLocaleString("MAIN_NO"));

        panel.add(messageLbl, GuiConstants.WRAP);
        panel.add(overwriteLbl, GuiConstants.WRAP);
        panel.add(overwrite, GuiConstants.CENTER_SINGLE_LINE);
        panel.add(abort, GuiConstants.CENTER_SINGLE_LINE);

        pack();
        setLocationRelativeTo(getOwner());
    }
}
