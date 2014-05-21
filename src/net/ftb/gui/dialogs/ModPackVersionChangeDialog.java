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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import net.ftb.data.Settings;
import net.ftb.gui.LaunchFrame;
import net.ftb.locale.I18N;
import net.ftb.log.Logger;
import net.ftb.tools.ModManager;

@SuppressWarnings("serial")
public class ModPackVersionChangeDialog extends JDialog {
    private JLabel messageLbl;
    private JLabel updateLbl;
    private JTextArea versionLbl;

    private JButton update;
    private JButton abort;
    private JCheckBox backupCFG;
    private JCheckBox backupSave;

    public ModPackVersionChangeDialog(LaunchFrame instance, boolean modal, final String storedVersion, String onlineVersion) {
        super(instance, modal);

        setupGui(storedVersion, onlineVersion);

        update.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                LaunchFrame.allowVersionChange = true;
                LaunchFrame.doVersionBackup = backupCFG.isSelected();
                ModManager.backupSave = backupSave.isSelected();
                setVisible(false);
            }
        });

        abort.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                LaunchFrame.allowVersionChange = false;
                Settings.getSettings().setPackVer(storedVersion);
                setVisible(false);
            }
        });
    }

    private void setupGui (String storedVersion, String onlineVersion) {
        setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
        setTitle(I18N.getLocaleString("UPDATEMODPACK_TITLE"));
        setResizable(false);

        Container panel = getContentPane();
        SpringLayout layout = new SpringLayout();
        panel.setLayout(layout);

        if (isNewer(onlineVersion, storedVersion)) {
            messageLbl = new JLabel(I18N.getLocaleString("UPDATEMODPACK_ISAVALIBLE"));
            updateLbl = new JLabel(I18N.getLocaleString("UPDATE_WICHUPDATE"));
        } else {
            messageLbl = new JLabel(I18N.getLocaleString("UPDATEMODPACK_DOWNGRADE"));
            updateLbl = new JLabel(I18N.getLocaleString("UPDATE_DOWNGRADE"));
        }

        versionLbl = new JTextArea(I18N.getLocaleString("UPDATEMODPACK_FROM_TO_VERSION").replace("%LOCALVER%", storedVersion).replace("%ONLINEVER%", onlineVersion));
        versionLbl.setEditable(false);
        versionLbl.setHighlighter(null);
        versionLbl.setBorder(BorderFactory.createEmptyBorder());

        update = new JButton(I18N.getLocaleString("MAIN_YES"));
        abort = new JButton(I18N.getLocaleString("MAIN_NO"));
        backupCFG = new JCheckBox(I18N.getLocaleString("UPDATEMODPACK_BACKUPCFG"));
        backupSave = new JCheckBox(I18N.getLocaleString("UPDATEMODPACK_BACKUPSave"));

        messageLbl.setHorizontalAlignment(SwingConstants.CENTER);
        updateLbl.setHorizontalAlignment(SwingConstants.CENTER);
        backupCFG.setHorizontalAlignment(SwingConstants.CENTER);
        backupSave.setHorizontalAlignment(SwingConstants.CENTER);


        panel.add(messageLbl);
        panel.add(versionLbl);
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
        layout.putConstraint(SpringLayout.WEST, versionLbl, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, backupCFG, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, backupSave, hSpring, SpringLayout.WEST, panel);


        columnWidth = Spring.width(messageLbl);
        columnWidth = Spring.max(columnWidth, Spring.width(updateLbl));
        columnWidth = Spring.max(columnWidth, Spring.width(versionLbl));
        columnWidth = Spring.max(columnWidth, Spring.width(backupCFG));
        columnWidth = Spring.max(columnWidth, Spring.width(backupSave));

        hSpring = Spring.sum(hSpring, columnWidth);

        layout.putConstraint(SpringLayout.EAST, messageLbl, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, updateLbl, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, versionLbl, hSpring, SpringLayout.WEST, panel);
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
        vSpring = Spring.sum(vSpring, Spring.height(messageLbl));

        vSpring = Spring.sum(vSpring, Spring.constant(15));

        layout.putConstraint(SpringLayout.NORTH, versionLbl, vSpring, SpringLayout.NORTH, panel);
        vSpring = Spring.sum(vSpring, Spring.height(versionLbl));

        vSpring = Spring.sum(vSpring, Spring.constant(15));

        layout.putConstraint(SpringLayout.NORTH, updateLbl, vSpring, SpringLayout.NORTH, panel);
        vSpring = Spring.sum(vSpring, Spring.height(updateLbl));

        vSpring = Spring.sum(vSpring, Spring.constant(10));

        layout.putConstraint(SpringLayout.NORTH, backupCFG, vSpring, SpringLayout.NORTH, panel);
        vSpring = Spring.sum(vSpring, Spring.height(backupCFG));

        vSpring = Spring.sum(vSpring, Spring.constant(10));

        layout.putConstraint(SpringLayout.NORTH, backupSave, vSpring, SpringLayout.NORTH, panel);
        vSpring = Spring.sum(vSpring, Spring.height(backupSave));

        vSpring = Spring.sum(vSpring, Spring.constant(10));

        layout.putConstraint(SpringLayout.NORTH, update, vSpring, SpringLayout.NORTH, panel);
        layout.putConstraint(SpringLayout.NORTH, abort, vSpring, SpringLayout.NORTH, panel);

        rowHeight = Spring.height(update);
        rowHeight = Spring.max(rowHeight, Spring.height(abort));

        vSpring = Spring.sum(vSpring, rowHeight);
        vSpring = Spring.sum(vSpring, Spring.constant(10));

        layout.putConstraint(SpringLayout.SOUTH, panel, vSpring, SpringLayout.NORTH, panel);

        pack();
        setLocationRelativeTo(getOwner());
    }

    public boolean isNewer (String onlineVersion, String storedVersion) {
        if (storedVersion == null || storedVersion.isEmpty())
            return true;
        String[] oV = onlineVersion.split("[._-]");
        String[] sV = storedVersion.split("[._-]");
        Logger.logInfo(onlineVersion + " " + storedVersion);
        for (int i = 0; i < oV.length; i++) {
            if (sV.length > i) {
                if (Integer.parseInt(oV[i]) > Integer.parseInt(sV[i])) {
                    Logger.logInfo(oV[i] + ">" + sV[i]);
                    Logger.logInfo("ret true");
                    return true;
                } else if (Integer.parseInt(oV[i]) < Integer.parseInt(sV[i])) {
                    return false;
                    //stored would be older in this case
                }
            }
        }
        Logger.logInfo("ret False");
        return false;
    }
}
