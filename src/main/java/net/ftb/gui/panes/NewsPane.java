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
package net.ftb.gui.panes;

import net.ftb.data.Settings;
import net.ftb.gui.LaunchFrame;
import net.ftb.util.NewsUtils;
import net.ftb.util.OSUtils;
import net.ftb.util.OSUtils.OS;

import java.awt.BorderLayout;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import net.ftb.download.Locations;

@SuppressWarnings("serial")
public class NewsPane extends JPanel implements ILauncherPane {
    private final HTMLEditorKit html_kit = new HTMLEditorKit();

    private JScrollPane newsPanel;
    
    private final HTMLEditorKit news_kit = new HTMLEditorKit() {
        {
            this.setStyleSheet(OSUtils.makeStyleSheet("news"));
        }
    };
    
    private final JEditorPane news_pane = new JEditorPane("text/html", "") {
        {
            this.setEditable(false);
            this.setEditorKit(news_kit);
            this.addHyperlinkListener(new HyperlinkListener() {
                @Override
                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if (e.getEventType() == EventType.ACTIVATED) {
                        if(e.getDescription().substring(0, 7).equals("members")) {
                            OSUtils.browse(Locations.forum + e.getDescription());
                        } else {
                            OSUtils.browse(e.getDescription());
                        }
                    }
                }
            });
        }
    };

    public NewsPane(){
        super(new BorderLayout());

        if(OSUtils.getCurrentOS() == OS.WINDOWS){
            this.setBorder(new EmptyBorder(-5, 0, -5, 12));
        } else{
            this.setBorder(new EmptyBorder(-4, 0, -4, -2));
        }

        this.html_kit.setStyleSheet(OSUtils.makeStyleSheet("news"));
        this.news_pane.setEditorKit(this.html_kit);
        this.news_pane.setEditable(false);
        this.news_pane.addHyperlinkListener(new HyperlinkListener(){
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e){
                if(e.getEventType() == EventType.ACTIVATED){
                    OSUtils.browse(e.getURL().toString());
                }
            }
        });
        this.news_pane.setText(NewsUtils.getNewsHTML());
        this.news_pane.setCaretPosition(0);
        this.add(new JScrollPane(this.news_pane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
    }

    @Override
    public void onVisible () {
        Settings.getSettings().setNewsDate();
        Settings.getSettings().save();
        LaunchFrame.getInstance().setNewsIcon();
    }
}
