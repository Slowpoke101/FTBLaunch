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

import net.ftb.data.Constants;
import net.ftb.gui.GuiConstants;
import net.ftb.gui.LaunchFrame;
import net.ftb.locale.I18N;
import net.ftb.updater.UpdateChecker;
import net.ftb.util.OSUtils;
import net.miginfocom.swing.MigLayout;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class LauncherUpdateDialog extends JDialog {
    private JLabel messageLbl;
    private JLabel extraText;
    private JLabel updateLbl;
    private JButton showChangeLog;
    private JButton update;
    private JButton abort;
    private boolean usable;

    public LauncherUpdateDialog (final UpdateChecker updateChecker, int deadVersion) {
        super(LaunchFrame.getInstance(), true);
        this.usable = Constants.buildNumber > deadVersion;
        setupGui();

        showChangeLog.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                // TODO: Call new frame containing html page?
                // TODO: beta changelogs???
                OSUtils.browse("http://feed-the-beast.com/launcher/change-log/" + UpdateChecker.UCString);//TODO this should use the new version #!!!
            }
        });

        update.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                setVisible(false);
                updateChecker.update();
            }
        });

        abort.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                setVisible(false);
            }
        });
    }

    private void setupGui () {
        setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
        setTitle(I18N.getLocaleString("LUNCHERUPDATE_ISAVAILABLETITLE"));
        setResizable(true);

        Container panel = getContentPane();
        panel.setLayout(new MigLayout());

        messageLbl = new JLabel(UpdateChecker.UCString + " " + I18N.getLocaleString("LUNCHERUPDATE_ISAVAILABLE"));
        extraText = new JLabel(!usable ? I18N.getLocaleString("LUNCHERUPDATE_CURRENTDEAD") : "");//TODO use this for beta channel??
        extraText.setForeground(Color.red);
        updateLbl = new JLabel(I18N.getLocaleString("UPDATE_WICHUPDATE"));
        showChangeLog = new JButton(I18N.getLocaleString("LUNCHERUPDATE_CHANGELOG"));
        update = new JButton(I18N.getLocaleString("MAIN_YES"));
        abort = new JButton(I18N.getLocaleString("MAIN_NO"));

        panel.add(messageLbl, GuiConstants.CENTER_SINGLE_LINE);
        panel.add(extraText, GuiConstants.CENTER_SINGLE_LINE);
        panel.add(showChangeLog, GuiConstants.WRAP);
        panel.add(updateLbl, GuiConstants.WRAP);
        panel.add(update, GuiConstants.CENTER_TWO);
        panel.add(abort);

        pack();
        setLocationRelativeTo(getOwner());
    }
}
