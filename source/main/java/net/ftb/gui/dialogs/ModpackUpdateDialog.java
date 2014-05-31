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

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import net.ftb.gui.LaunchFrame;
import net.ftb.locale.I18N;
import net.ftb.tools.ModManager;
import net.ftb.util.SwingUtils;

@SuppressWarnings("serial")
public class ModpackUpdateDialog extends JDialog {
    private JLabel messageLbl;
    private JLabel updateLbl;
    private JButton update;
    private JButton abort;
    private JCheckBox backupCFG;
    private JCheckBox backupSave;

    public ModpackUpdateDialog(LaunchFrame instance, boolean modal) {
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
        setResizable(false);

        Container panel = getContentPane();
        SpringLayout layout = new SpringLayout();
        panel.setLayout(layout);

        messageLbl = new JLabel(I18N.getLocaleString("UPDATEMODPACK_ISAVALIBLE"));
        updateLbl = new JLabel(I18N.getLocaleString("UPDATE_WICHUPDATE"));
        backupCFG = new JCheckBox(I18N.getLocaleString("UPDATEMODPACK_BACKUPCFG"));
        backupSave = new JCheckBox(I18N.getLocaleString("UPDATEMODPACK_BACKUPSAVE"));
        update = new JButton(I18N.getLocaleString("MAIN_YES"));
        abort = new JButton(I18N.getLocaleString("MAIN_NO"));

        messageLbl.setHorizontalAlignment(SwingConstants.CENTER);
        updateLbl.setHorizontalAlignment(SwingConstants.CENTER);
        backupCFG.setHorizontalAlignment(SwingConstants.CENTER);
        backupSave.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(messageLbl);
        panel.add(updateLbl);
        panel.add(backupCFG);
        panel.add(backupSave);
        panel.add(update);
        panel.add(abort);

        Spring hSpring;
        Spring columnWidth;

        hSpring = Spring.constant(10);

        layout.putConstraint(SpringLayout.WEST, messageLbl, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, updateLbl, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, backupCFG, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, backupSave, hSpring, SpringLayout.WEST, panel);

        columnWidth = SwingUtils.springMax(Spring.width(messageLbl), Spring.width(updateLbl), Spring.width(backupCFG), Spring.width(backupSave));

        hSpring = Spring.sum(hSpring, columnWidth);

        layout.putConstraint(SpringLayout.EAST, messageLbl, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, updateLbl, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, backupCFG, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, backupSave, hSpring, SpringLayout.WEST, panel);

        hSpring = Spring.sum(hSpring, Spring.constant(10));

        layout.putConstraint(SpringLayout.EAST, panel, hSpring, SpringLayout.WEST, panel);

        layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, backupCFG, 0, SpringLayout.HORIZONTAL_CENTER, panel);
        layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, backupSave, 0, SpringLayout.HORIZONTAL_CENTER, panel);
        layout.putConstraint(SpringLayout.EAST, update, -5, SpringLayout.HORIZONTAL_CENTER, panel);
        layout.putConstraint(SpringLayout.WEST, abort, 5, SpringLayout.HORIZONTAL_CENTER, panel);

        Spring vSpring;
        Spring rowHeight;

        vSpring = Spring.constant(10);

        layout.putConstraint(SpringLayout.NORTH, messageLbl, vSpring, SpringLayout.NORTH, panel);

        vSpring = SwingUtils.springSum(vSpring, Spring.height(messageLbl), Spring.constant(5));

        layout.putConstraint(SpringLayout.NORTH, updateLbl, vSpring, SpringLayout.NORTH, panel);

        vSpring = SwingUtils.springSum(vSpring, Spring.height(updateLbl), Spring.constant(10));

        layout.putConstraint(SpringLayout.NORTH, backupCFG, vSpring, SpringLayout.NORTH, panel);

        vSpring = SwingUtils.springSum(vSpring, Spring.height(backupCFG), Spring.constant(10));

        layout.putConstraint(SpringLayout.NORTH, backupSave, vSpring, SpringLayout.NORTH, panel);

        vSpring = SwingUtils.springSum(vSpring, Spring.height(backupSave), Spring.constant(10));

        layout.putConstraint(SpringLayout.NORTH, update, vSpring, SpringLayout.NORTH, panel);
        layout.putConstraint(SpringLayout.NORTH, abort, vSpring, SpringLayout.NORTH, panel);

        rowHeight = Spring.max(Spring.height(update), Spring.height(abort));

        vSpring = SwingUtils.springSum(vSpring, rowHeight, Spring.constant(10));

        layout.putConstraint(SpringLayout.SOUTH, panel, vSpring, SpringLayout.NORTH, panel);

        pack();
        setLocationRelativeTo(getOwner());
    }
}
