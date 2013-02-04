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
package net.ftb.gui.panes;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;

import net.ftb.data.Settings;
import net.ftb.gui.LaunchFrame;
import net.ftb.log.Logger;
import net.ftb.util.OSUtils;
import net.ftb.util.OSUtils.OS;

@SuppressWarnings("serial")
public class NewsPane extends JPanel implements ILauncherPane {
	private JEditorPane news;
	private JScrollPane newsPanel;

	public NewsPane() {
		super();
		if(OSUtils.getCurrentOS() == OS.WINDOWS) {
			setBorder(new EmptyBorder(-5, -25, -5, 12));
		} else {
			setBorder(new EmptyBorder(-4, -25, -4, -2));
		}
		setLayout(new BorderLayout());

		news = new JEditorPane();
		news.setEditable(false);
		news.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent arg0) {
				if(arg0.getEventType() == EventType.ACTIVATED) {
					try {
						OSUtils.browse(arg0.getURL().toString());
					} catch (Exception e) {
						Logger.logError(e.getMessage(), e);
					}
				}
			}
		});
		newsPanel = new JScrollPane(news);
		newsPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		newsPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		add(newsPanel, BorderLayout.CENTER);
	}

	@Override
	public void onVisible() {
		try {
			news.setPage("http://launcher.feed-the-beast.com/news.php");
			Settings.getSettings().setNewsDate();
			Settings.getSettings().save();
			LaunchFrame.getInstance().setNewsIcon();
		} catch (IOException e1) {
			Logger.logError(e1.getMessage(), e1);
		}
	}
}
