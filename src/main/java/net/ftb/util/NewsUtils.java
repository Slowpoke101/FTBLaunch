/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2016, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
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

import com.google.common.collect.Lists;
import net.ftb.data.news.NewsArticle;
import net.ftb.data.news.RSSReader;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public final class NewsUtils {

    private static List<NewsArticle> news = null;
    private static DateFormat dateFormatterRss = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");

    private NewsUtils () {
    }

    public static void initializeNews () {
        news = RSSReader.readRSS();
    }

    /**
     * Gets the HTML code for the news pane.
     *
     * @return The HTML to display on the news pane
     */
    public static String getNewsHTML () {
        // if news not fetched try to fetch. Blocks thread.
        Benchmark.start("NewsUtils");
        if (news == null) {
            NewsUtils.initializeNews();
        }

        final StringBuilder newsBuilder = new StringBuilder();

        newsBuilder.append("<html>");
        if (news != null) {
            for (NewsArticle article : news) {
                newsBuilder.append(article.getHTML());
                if (news.get(news.size() - 1) != article) {
                    newsBuilder.append("<hr/>");
                }
            }
        } else {
            newsBuilder.append("No network connection, no news.");
        }
        newsBuilder.append("</html>");
        Benchmark.logBench("NewsUtils");
        return newsBuilder.toString();
    }

    public static List<String> getPubDates () {
        final List<String> s = Lists.newArrayList();

        if (news != null) {
            for (NewsArticle n : news) {
                s.add(getUnixDate(n.getDate()));
            }
        }

        return s;
    }

    private static String getUnixDate (String s) {
        try {
            Date dte = dateFormatterRss.parse(s);
            return String.valueOf(dte.getTime() / 1000);
        } catch (Exception ignored) {
        }
        return "00000000";
    }

}
