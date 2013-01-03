package net.ftb.gui.panes;

import java.awt.BorderLayout;
import java.io.IOException;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import net.ftb.data.Settings;
import net.ftb.gui.LaunchFrame;
import net.ftb.log.Logger;
import net.ftb.util.OSUtils;
import net.ftb.util.OSUtils.OS;

public class NewsPane extends JPanel implements ILauncherPane {
	private static final long serialVersionUID = 1L;

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
		newsPanel = new JScrollPane(news);
		newsPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		newsPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		add(newsPanel, BorderLayout.CENTER);
	}

	@Override
	public void onVisible() {
		try {
			if(!Settings.getSettings().getSnooper()) {
				LaunchFrame.tracker.trackPageViewFromReferrer("net/ftb/gui/NewsPane.java", "News Tab View", "Feed The Beast", "http://www.feed-the-beast.com", "/");
			}
			news.setPage("http://launcher.feed-the-beast.com/news.php");
			Settings.getSettings().setNewsDate();
			Settings.getSettings().save();
			LaunchFrame.getInstance().setNewsIcon();
		} catch (IOException e1) {
			Logger.logError(e1.getMessage(), e1);
		}
	}
}