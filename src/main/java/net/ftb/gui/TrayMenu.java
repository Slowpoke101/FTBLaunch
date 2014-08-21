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
package net.ftb.gui;

import net.ftb.download.Locations;
import net.ftb.locale.I18N;
import net.ftb.log.Logger;
import net.ftb.util.GameUtils;
import net.ftb.util.OSUtils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class TrayMenu
extends JPopupMenu{
    private final JMenuItem killMCButton = new JMenuItem();
    private final JMenuItem quitButton = new JMenuItem();
    private final JMenuItem ftbWebsiteButton = new JMenuItem();
    private final JCheckBoxMenuItem showConsoleBox = new JCheckBoxMenuItem();

    public TrayMenu(){
        super();
        this.killMCButton.setText(I18N.getLocaleString("KILL_MC"));
        this.quitButton.setText(I18N.getLocaleString("TRAY_QUIT"));
        this.ftbWebsiteButton.setText(I18N.getLocaleString("TRAY_FTB_WEBSITE"));
        this.showConsoleBox.setText(I18N.getLocaleString("SHOW_CONSOLE"));
        this.add(this.killMCButton);
        this.addSeparator();
        this.add(this.showConsoleBox);
        this.addSeparator();
        this.add(this.ftbWebsiteButton);
        this.add(this.quitButton);

        if(LaunchFrame.con != null){
            this.showConsoleBox.setState(LaunchFrame.con.isVisible());
        } else{
            this.showConsoleBox.setState(false);
        }

        this.addActionListeners();
        this.addItemListeners();
    }

    private void addActionListeners(){
        this.killMCButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                GameUtils.killMC();
            }
        });
        this.quitButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                System.exit(0);
            }
        });
        this.ftbWebsiteButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                OSUtils.browse(Locations.FTBSITE);
            }
        });
    }

    private void addItemListeners(){
        this.showConsoleBox.addItemListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent e){
                boolean state = showConsoleBox.getState();
                if(LaunchFrame.con != null){
                    if(state){
                        LaunchFrame.con.setVisible(true);
                        Logger.addListener(LaunchFrame.con);
                    } else{
                        LaunchFrame.con.setVisible(false);
                        Logger.removeListener(LaunchFrame.con);
                    }
                } else{
                    LaunchFrame.con = new LauncherConsole();
                    LaunchFrame.con.setVisible(true);
                    Logger.addListener(LaunchFrame.con);
                }
            }
        });
    }

    public void updateLocale(){
        this.killMCButton.setText(I18N.getLocaleString("KILL_MC"));
        this.quitButton.setText(I18N.getLocaleString("TRAY_QUIT"));
        this.ftbWebsiteButton.setText(I18N.getLocaleString("TRAY_FTB_WEBSITE"));
        this.showConsoleBox.setText(I18N.getLocaleString("SHOW_CONSOLE"));
    }

    public void updateShowConsole(boolean b){
        this.showConsoleBox.setState(b);
    }
}