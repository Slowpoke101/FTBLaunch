/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2016, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
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

import net.ftb.gui.GuiConstants;
import net.ftb.gui.LaunchFrame;
import net.ftb.locale.I18N;
import net.ftb.tools.ModManager;
import net.miginfocom.swing.MigLayout;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

@SuppressWarnings("serial")
public class ModpackUpdateDialog extends JDialog {
    private JLabel messageLbl;
    private JLabel updateLbl;
    private JButton update;
    private JButton abort;
    private JCheckBox backupCFG;
    private JCheckBox backupSave;

    public ModpackUpdateDialog (LaunchFrame instance, boolean modal) {
        super(instance, modal);

        setupGui();

        update.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                ModManager.update = true;
                ModManager.backupCFG = backupCFG.isSelected();
                ModManager.backupSave = backupSave.isSelected();
                setVisible(false);
            }
        });

        abort.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                ModManager.update = false;
                setVisible(false);
            }
        });
    }

    private void setupGui () {
        setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
        setTitle(I18N.getLocaleString("UPDATEMODPACK_TITLE"));
        setResizable(true);

        Container panel = getContentPane();
        panel.setLayout(new MigLayout());

        messageLbl = new JLabel(I18N.getLocaleString("UPDATEMODPACK_ISAVALIBLE"));
        updateLbl = new JLabel(I18N.getLocaleString("UPDATE_WICHUPDATE"));
        backupCFG = new JCheckBox(I18N.getLocaleString("UPDATEMODPACK_BACKUPCFG"));
        backupSave = new JCheckBox(I18N.getLocaleString("UPDATEMODPACK_BACKUPSAVE"));
        update = new JButton(I18N.getLocaleString("MAIN_YES"));
        abort = new JButton(I18N.getLocaleString("MAIN_NO"));

        panel.add(messageLbl, GuiConstants.WRAP);
        panel.add(updateLbl, GuiConstants.WRAP);
        panel.add(backupCFG, GuiConstants.CENTER_SINGLE_LINE);
        panel.add(backupSave, GuiConstants.CENTER_SINGLE_LINE);
        panel.add(update, GuiConstants.CENTER_TWO);
        panel.add(abort);

        pack();
        setLocationRelativeTo(getOwner());
    }
}
