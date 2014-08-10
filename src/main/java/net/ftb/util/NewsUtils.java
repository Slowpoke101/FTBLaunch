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

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import net.ftb.gui.news.NewsArticle;
import net.ftb.gui.news.RSSReader;

public class NewsUtils {
    
    private static List<NewsArticle> news = null;
    
    public static void initializeNews() {
        news = RSSReader.readRSS();
    }
    
    /**
     * Gets the HTML code for the news pane.
     * 
     * @return The HTML to display on the news pane
     */
    public static String getNewsHTML() {
        if(news == null) {
            NewsUtils.initializeNews();
        }
        String html = "<html>";
        for(NewsArticle article : news) {
            html += article.getHTML();
            if(news.get(news.size() - 1) != article) {
                html += "<hr/>";
            }
        }
        html += "</html>";
        return html;
    }
    public static ArrayList<String> getPubDates() {
        ArrayList<String> s = Lists.newArrayList();
        for( NewsArticle n: news) {
            s.add((n.getDate()));
        }
        return s;
    }

}
