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
package net.ftb.util;

import net.ftb.data.Settings;
import net.ftb.gui.LaunchFrame;
import net.ftb.log.Logger;

public class TrackerUtils {
    public static boolean googleEnabled = true;
    public static boolean piwikEnabled = false;
    public TrackerUtils() {
    }

    /**
     * Method to send page view to google analytics -- checks if analytics are enabled before attempting to send
     * @param pageUrl URL for Launcher Analytics Page view -- usually the classpath
     * @param pageTitle Entry for view such as pack name & pack version, etc.
     */
    public static void sendPageView (String pageUrl, String pageTitle) {
        if (!Settings.getSettings().getSnooper()) {
            if(googleEnabled) {
                LaunchFrame.tracker.trackPageViewFromReferrer(pageUrl, pageTitle, "Feed The Beast", "http://www.feed-the-beast.com", "/");
            }
            if(piwikEnabled) {
                try {
                    //TODO make sure this gets sent!!!! LaunchFrame.piwik.sendRequest(LaunchFrame.piwik.getPageUrl())???
                    LaunchFrame.piwik.setPageCustomVariable(pageUrl, pageTitle);
                } catch(Exception e) {
                    Logger.logError(e.getMessage(), e);
                }
            }
        }
    }
}
