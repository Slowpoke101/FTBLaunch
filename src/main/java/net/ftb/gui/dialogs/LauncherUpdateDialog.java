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

import java.awt.Color;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import net.ftb.gui.LaunchFrame;
import net.ftb.locale.I18N;
import net.ftb.updater.UpdateChecker;
import net.ftb.util.OSUtils;
import net.ftb.util.SwingUtils;

public class LauncherUpdateDialog extends JDialog {
    private JLabel messageLbl;
    private JLabel extraText;
    private JLabel updateLbl;
    private JButton showChangeLog;
    private JButton update;
    private JButton abort;
    private boolean usable;

    public LauncherUpdateDialog(final UpdateChecker updateChecker, int deadVersion) {
        super(LaunchFrame.getInstance(), true);
        this.usable = LaunchFrame.getInstance().buildNumber > deadVersion;
        setupGui();

        showChangeLog.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                // TODO: Call new frame containing html page?
                // TODO: beta changelogs???
                OSUtils.browse("http://feed-the-beast.com/launcher/change-log/" + LaunchFrame.buildNumber);//TODO update this!!!
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
        setResizable(false);

        Container panel = getContentPane();
        SpringLayout layout = new SpringLayout();
        panel.setLayout(layout);

        messageLbl = new JLabel("Version " + UpdateChecker.verString + " " + I18N.getLocaleString("LUNCHERUPDATE_ISAVAILABLE"));
        extraText = new JLabel(!usable ? I18N.getLocaleString("LUNCHERUPDATE_CURRENTDEAD") : "");//TODO use this for beta channel??
        extraText.setForeground(Color.red);
        JLabel updateLbl = new JLabel(I18N.getLocaleString("UPDATE_WICHUPDATE"));
        showChangeLog = new JButton(I18N.getLocaleString("LUNCHERUPDATE_CHANGELOG"));
        update = new JButton(I18N.getLocaleString("MAIN_YES"));
        abort = new JButton(I18N.getLocaleString("MAIN_NO"));

        messageLbl.setHorizontalAlignment(SwingConstants.CENTER);
        updateLbl.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(messageLbl);
        panel.add(extraText);
        panel.add(showChangeLog);
        panel.add(updateLbl);
        panel.add(update);
        panel.add(abort);

        Spring hSpring;
        Spring columnWidth;

        hSpring = Spring.constant(10);

        layout.putConstraint(SpringLayout.WEST, messageLbl, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, extraText, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, updateLbl, hSpring, SpringLayout.WEST, panel);
        columnWidth = Spring.width(messageLbl);
        columnWidth = Spring.max(columnWidth, Spring.width(extraText));
        columnWidth = Spring.max(columnWidth, Spring.width(showChangeLog));
        columnWidth = Spring.max(columnWidth, Spring.width(updateLbl));

        hSpring = Spring.sum(hSpring, columnWidth);

        layout.putConstraint(SpringLayout.EAST, messageLbl, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, extraText, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, updateLbl, hSpring, SpringLayout.WEST, panel);

        hSpring = Spring.sum(hSpring, Spring.constant(10));

        layout.putConstraint(SpringLayout.EAST, panel, hSpring, SpringLayout.WEST, panel);

        layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, showChangeLog, 0, SpringLayout.HORIZONTAL_CENTER, panel);
        layout.putConstraint(SpringLayout.EAST, update, -5, SpringLayout.HORIZONTAL_CENTER, panel);
        layout.putConstraint(SpringLayout.WEST, abort, 5, SpringLayout.HORIZONTAL_CENTER, panel);

        Spring vSpring;
        Spring rowHeight;

        vSpring = Spring.constant(10);

        layout.putConstraint(SpringLayout.NORTH, messageLbl, vSpring, SpringLayout.NORTH, panel);

        vSpring = Spring.sum(vSpring, Spring.height(messageLbl));
        vSpring = Spring.sum(vSpring, Spring.constant(10));

        layout.putConstraint(SpringLayout.NORTH, extraText, vSpring, SpringLayout.NORTH, panel);

        vSpring = Spring.sum(vSpring, Spring.height(extraText));
        vSpring = Spring.sum(vSpring, Spring.constant(10));

        layout.putConstraint(SpringLayout.NORTH, showChangeLog, vSpring, SpringLayout.NORTH, panel);

        vSpring = Spring.sum(vSpring, Spring.height(showChangeLog));
        vSpring = Spring.sum(vSpring, Spring.constant(10));

        layout.putConstraint(SpringLayout.NORTH, updateLbl, vSpring, SpringLayout.NORTH, panel);

        vSpring = SwingUtils.springSum(vSpring, Spring.height(updateLbl), Spring.constant(10));

        layout.putConstraint(SpringLayout.NORTH, update, vSpring, SpringLayout.NORTH, panel);
        layout.putConstraint(SpringLayout.NORTH, abort, vSpring, SpringLayout.NORTH, panel);

        rowHeight = Spring.max(Spring.height(update), Spring.height(abort));

        vSpring = SwingUtils.springSum(vSpring, rowHeight, Spring.constant(10));

        layout.putConstraint(SpringLayout.SOUTH, panel, vSpring, SpringLayout.NORTH, panel);

        pack();
        setLocationRelativeTo(getOwner());
    }
}
