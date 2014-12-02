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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextArea;

import net.ftb.data.Settings;
import net.ftb.gui.GuiConstants;
import net.ftb.gui.LaunchFrame;
import net.ftb.locale.I18N;
import net.ftb.log.Logger;
import net.ftb.tools.ModManager;
import net.miginfocom.swing.MigLayout;

@SuppressWarnings("serial")
public class ModPackVersionChangeDialog extends JDialog {
    private JLabel messageLbl;
    private JLabel updateLbl;
    private JTextArea versionLbl;

    private JButton update;
    private JButton abort;
    private JCheckBox backupCFG;
    private JCheckBox backupSave;

    public ModPackVersionChangeDialog (LaunchFrame instance, boolean modal, final String storedVersion, final String onlineVersion) {
        super(instance, modal);

        setupGui(storedVersion, onlineVersion);

        update.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                LaunchFrame.allowVersionChange = true;
                LaunchFrame.doVersionBackup = backupCFG.isSelected();
                Settings.getSettings().setPackVer(onlineVersion);
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
        setResizable(true);

        Container panel = getContentPane();
        panel.setLayout(new MigLayout());

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

        panel.add(messageLbl, GuiConstants.CENTER_SINGLE_LINE);
        panel.add(versionLbl, GuiConstants.WRAP);
        panel.add(updateLbl, GuiConstants.CENTER_SINGLE_LINE);
        panel.add(backupCFG, GuiConstants.CENTER_SINGLE_LINE);
        panel.add(backupSave, GuiConstants.CENTER_SINGLE_LINE);
        panel.add(update);
        panel.add(abort);

        pack();
        setLocationRelativeTo(getOwner());
    }

    public boolean isNewer (String onlineVersion, String storedVersion) {
        if (storedVersion == null || storedVersion.isEmpty()) {
            return true;
        }
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
