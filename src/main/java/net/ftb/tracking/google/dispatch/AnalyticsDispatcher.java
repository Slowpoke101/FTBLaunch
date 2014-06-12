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
package net.ftb.tracking.google.dispatch;

import java.net.URI;

import net.ftb.log.Logger;

public abstract class AnalyticsDispatcher {
    private String userAgent;
    private String host;
    private int port;

    public AnalyticsDispatcher(String userAgent, String host, int port) {
        this.userAgent = userAgent;
        this.host = host;
        this.port = port;
    }

    public void dispatch (String analyticsString) {
        URI uri = URI.create(analyticsString);
        String timeDispatched = getQueryParameter(uri.getQuery(), "utmht");
        if (timeDispatched != null) {
            try {
                Long time = Long.parseLong(timeDispatched);
                analyticsString = analyticsString + "&utmqt=" + (System.currentTimeMillis() - time);
            } catch (NumberFormatException e) {
                Logger.logError("Error parsing utmht parameter: ", e);
            }
        } else {
            Logger.logWarn("Unable to find utmht parameter: " + analyticsString);
        }
        dispatchToNetwork(analyticsString);
    }

    protected abstract void dispatchToNetwork (String analyticsString);

    protected static String getQueryParameter (String query, String parameter) {
        String[] params = query.split("&");
        for (String param : params) {
            String[] nameValue = param.split("=");
            if (nameValue[0].equals(parameter)) {
                return nameValue[1];
            }
        }
        return null;
    }
}