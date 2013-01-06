package net.ftb.util;

import net.ftb.data.Settings;
import net.ftb.gui.LaunchFrame;

public class TrackerUtils {
	public TrackerUtils() { }

	public static void sendPageView(String pageUrl, String pageTitle) {
		if(!Settings.getSettings().getSnooper()) {
			LaunchFrame.tracker.trackPageViewFromReferrer(pageUrl, pageTitle, "Feed The Beast", "http://www.feed-the-beast.com", "/");
		}
	}
}
