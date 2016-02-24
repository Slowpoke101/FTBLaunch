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
package net.ftb.tracking.google.system;

import net.ftb.tracking.google.AnalyticsConfigData;

import java.awt.*;

public class AWTSystemPopulator {
    private AWTSystemPopulator() {
    }

    public static final void populateConfigData (AnalyticsConfigData data) {
        data.setEncoding(System.getProperty("file.encoding"));
        String region = System.getProperty("user.region");
        if (region == null) {
            region = System.getProperty("user.country");
        }
        data.setUserLanguage(System.getProperty("user.language") + "-" + region);
        try {
            int screenHeight = 0;
            int screenWidth = 0;
            GraphicsEnvironment ge;
            GraphicsDevice[] gs;
            ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            gs = ge.getScreenDevices();
            for (GraphicsDevice g : gs) {
                DisplayMode dm = g.getDisplayMode();
                screenWidth += dm.getWidth();
                screenHeight += dm.getHeight();
            }
            if (screenHeight != 0 && screenWidth != 0) {
                data.setScreenResolution(screenWidth + "x" + screenHeight);
            }
            if (gs[0] != null) {
                String colorDepth = gs[0].getDisplayMode().getBitDepth() + "";
                for (int i = 1; i < gs.length; i++) {
                    colorDepth += ", " + gs[i].getDisplayMode().getBitDepth();
                }
                data.setColorDepth(colorDepth);
            }
        } catch (HeadlessException e) {
            data.setScreenResolution("NA");
            data.setColorDepth("NA");
        }
    }
}