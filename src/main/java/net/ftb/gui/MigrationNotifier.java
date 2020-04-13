/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2018, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
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
package net.ftb.gui;

import net.ftb.data.Settings;
import net.ftb.locale.I18N;
import net.ftb.log.Logger;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class MigrationNotifier extends JDialog {

    private static final long serialVersionUID = 1L;

    private final JPanel contentPanel = new JPanel();

    private JLabel messageLbl;
    private JButton launchlink;
    private JButton play;


    public MigrationNotifier () {
        super(LaunchFrame.getInstance(), true);

        setupGUI();

        getRootPane().setDefaultButton(launchlink);

        launchlink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                Logger.logDebug("user launching page");
                //TODO launch link here

                Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                    try {
                        desktop.browse(new URL("https://www.feed-the-beast.com/").toURI());
                        setVisible(false);
                    } catch (Exception e) {
                        Logger.logError("error with launching url", e);
                    }
                }

            }
        });

        play.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                Logger.logDebug("user not launching page");
                setVisible(false);
            }
        });
    }

    private void setupGUI () {
        setTitle(I18N.getLocaleString("MIGRATION_TITLE"));
        setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(750, 160);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(null);

        messageLbl = new JLabel(I18N.getLocaleString("MIGRATION_MESSAGE"));
        messageLbl.setBounds(5, 5, 729, 60);
        messageLbl.setHorizontalAlignment(SwingConstants.CENTER);
        messageLbl.setFont(messageLbl.getFont().deriveFont(Font.BOLD, 16.0f));
        contentPanel.add(messageLbl);

        launchlink = new JButton(I18N.getLocaleString("MIGRATION_LINK"));
        launchlink.setBounds(400, 97, 200, 23);
        contentPanel.add(launchlink);


        play = new JButton(I18N.getLocaleString("MIGRATION_PLAY"));
        play.setBounds(150, 97, 200, 23);
        contentPanel.add(play);

    }

}