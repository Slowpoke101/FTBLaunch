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
package net.ftb.util;

import lombok.Getter;
import lombok.Setter;
import net.ftb.data.Settings;
import net.ftb.gui.LaunchFrame;
import net.ftb.log.Logger;
import net.ftb.tracking.piwik.PiwikTracker;

import java.util.Map;
import java.util.Set;

public class TrackerUtils {
    public static boolean googleEnabled = true;
    public static boolean piwikEnabled = true;
    public TrackerUtils() {
    }

    /**
     * Method to send page view to google analytics -- checks if analytics are enabled before attempting to send
     * @param pageUrl URL for Launcher Analytics Page view -- usually the classpath
     * @param pageTitle Entry for view such as pack name & pack version, etc.
     */
    public static void sendPageView (String pageUrl, String pageTitle) {
        sendPageView(pageUrl, pageTitle, null);
    }

    /**
     * Method to send page view to google analytics -- checks if analytics are enabled before attempting to send
     * @param pageUrl URL for Launcher Analytics Page view -- usually the classpath
     * @param pageTitle Entry for view such as pack name & pack version, etc.
     */
    public static void sendPageView (String pageUrl, String pageTitle, Map<String, String> extraData) {

        if (!Settings.getSettings().getSnooper()) {
            if(googleEnabled) {
                LaunchFrame.tracker.trackPageViewFromReferrer(pageUrl, pageTitle, "Feed The Beast", "http://www.feed-the-beast.com", "/");
            }
            if(piwikEnabled) {
                try {
                    PiwikTracker p = new PiwikTracker(pageTitle, pageUrl);
                    if(extraData != null) {
                        for(Map.Entry<String, String> s: extraData.entrySet()) {
                            p.addExtraPair(s.getKey(), s.getValue());
                        }
                    }
                    p.start();
                } catch(Exception e) {
                    Logger.logError(e.getMessage(), e);
                }
            }
        }
    }
}
