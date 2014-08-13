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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;
import net.ftb.data.news.NewsArticle;
import net.ftb.data.news.RSSReader;

public class NewsUtils {
    
    private static List<NewsArticle> news = null;
    private static DateFormat dateFormatterRss = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
    public static void initializeNews() {
        news = RSSReader.readRSS();
    }
    
    /**
     * Gets the HTML code for the news pane.
     * 
     * @return The HTML to display on the news pane
     */
    public static String getNewsHTML() {
        // if news not fetched try to fetch. Blocks thread.
        if(news == null) {
            NewsUtils.initializeNews();
        }

        String html;
        html = "<html>";
        if (news != null) {
            for (NewsArticle article : news) {
                html += article.getHTML();
                if (news.get(news.size() - 1) != article) {
                    html += "<hr/>";
                }
            }
        } else {
            html += "No network connection, no news.";
        }
        html += "</html>";
        return html;

    }
    public static ArrayList<String> getPubDates() {
        ArrayList<String> s = Lists.newArrayList();
        if (news != null) {
            for (NewsArticle n : news) {
                s.add(getUnixDate(n.getDate()));
            }
        }
        return s;
    }
    private static String getUnixDate(String s) {
        try {
            Date dte = dateFormatterRss.parse(s);
            return String.valueOf(dte.getTime()/1000);
        } catch(Exception e) {

        }
        return "00000000";
    }

}
