package net.ftb.gui.panes;

import java.io.IOException;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import net.ftb.data.Settings;
import net.ftb.gui.LaunchFrame;
import net.ftb.log.Logger;

public class NewsPane extends JPanel implements ILauncherPane {
	private static final long serialVersionUID = 1L;

	private JEditorPane news;
	private JScrollPane newsPanel;

	public NewsPane() {
		super();
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(null);

		news = new JEditorPane();
		news.setEditable(false);
		newsPanel = new JScrollPane(news);
		newsPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		newsPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		newsPanel.setBounds(10, 10, 790, 290);
		add(newsPanel);
	}

	@Override
	public void onVisible() {
		try {
//			if(!Settings.getSettings().getSnooper()) {
//				LaunchFrame.tracker.trackPageViewFromReferrer("net/ftb/gui/NewsPane.java", "News Tab View", "Feed The Beast", "http://www.feed-the-beast.com", "/");
//			}
			news.setPage("http://launcher.feed-the-beast.com/news.php");
			Settings.getSettings().setNewsDate();
			Settings.getSettings().save();
			LaunchFrame.getInstance().setNewsIcon();
		} catch (IOException e1) {
			Logger.logError(e1.getMessage(), e1);
		}
	}
}