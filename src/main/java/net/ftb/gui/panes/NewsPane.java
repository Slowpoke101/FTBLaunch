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
package net.ftb.gui.panes;

import java.awt.BorderLayout;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;

import net.ftb.data.Settings;
import net.ftb.download.Locations;
import net.ftb.gui.LaunchFrame;
import net.ftb.util.OSUtils;

@SuppressWarnings("serial")
public class NewsPane extends JPanel implements ILauncherPane {

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
                public void hyperlinkUpdate (HyperlinkEvent e) {
                    if (e.getEventType() == EventType.ACTIVATED) {
                        if (e.getDescription().substring(0, 7).equals("members")) {
                            OSUtils.browse(Locations.forum + e.getDescription());
                        } else {
                            OSUtils.browse(e.getDescription());
                        }
                    }
                }
            });
        }
    };

    public NewsPane () {
        super(new BorderLayout());

        setBorder(null);

        newsPanel = new JScrollPane(this.news_pane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        newsPanel.setBorder(null);
        this.add(newsPanel, BorderLayout.CENTER);
        this.news_pane.setText("No news loaded, please wait");
    }

    @Override
    public void onVisible () {
        Settings.getSettings().setNewsDate();
        Settings.getSettings().save();
        LaunchFrame.getInstance().setNewsIcon();
    }

    public void setContent(String s) {
        this.news_pane.setText(s);
    }

}
