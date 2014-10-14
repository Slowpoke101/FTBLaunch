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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import net.ftb.download.Locations;
import net.ftb.locale.I18N;
import net.ftb.log.Logger;
import net.ftb.util.GameUtils;
import net.ftb.util.OSUtils;

public class TrayMenu extends PopupMenu {
	
	private static final long serialVersionUID = 1L;
	
	public TrayMenu() {
		super();
		
		killMCButton.setLabel(I18N.getLocaleString("KILL_MC"));
		quitButton.setLabel(I18N.getLocaleString("TRAY_QUIT"));
		ftbWebsite.setLabel(I18N.getLocaleString("TRAY_FTB_WEBSITE"));
		showConsole.setLabel(I18N.getLocaleString("SHOW_CONSOLE"));
		
		this.add(this.killMCButton);
		this.addSeparator();
		this.add(this.showConsole);
		this.addSeparator();
		this.add(this.ftbWebsite);
		this.add(this.quitButton);
	}
	
	public void updateLocale() {
		killMCButton.setLabel(I18N.getLocaleString("KILL_MC"));
		quitButton.setLabel(I18N.getLocaleString("TRAY_QUIT"));
		ftbWebsite.setLabel(I18N.getLocaleString("TRAY_FTB_WEBSITE"));
		showConsole.setLabel(I18N.getLocaleString("SHOW_CONSOLE"));
	}
	
	private final MenuItem killMCButton = new MenuItem() {
		{
			this.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					GameUtils.killMC();
				}
			});
		}
	};
	
	private final MenuItem quitButton = new MenuItem() {
        {
            this.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    System.exit(0);
                }
            });
        }
    };
    
    private final MenuItem ftbWebsite = new MenuItem() {
    	{
    		this.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    OSUtils.browse(Locations.FTBSITE);
                }
            });
    	}
    };
    
    private final CheckboxMenuItem showConsole = new CheckboxMenuItem() {
    	{
            if (LaunchFrame.con != null ) {
                this.setState(LaunchFrame.con.isVisible());
            } else {
                this.setState(false);
            }

    		this.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
                    boolean newState = showConsole.getState();
                    if (LaunchFrame.con != null) {
                        if (newState) {
                            LaunchFrame.con.setVisible(true);
                            Logger.addListener(LaunchFrame.con);
                        } else {
                            Logger.removeListener(LaunchFrame.con);
                            LaunchFrame.con.dispose();
                        }
                    } else {
                        LaunchFrame.con = new LauncherConsole();
                        Logger.addListener(LaunchFrame.con);
                        LaunchFrame.con.refreshLogs();
                        LaunchFrame.con.setVisible(true);
                    }
				}
    		});
    	}
    };
    
    public void updateShowConsole(boolean b) {
    	showConsole.setState(b);
    }
}
