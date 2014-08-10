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
package net.ftb.gui.news;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.google.common.collect.Lists;
import net.ftb.download.Locations;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class RSSReader {
    
    public static List<NewsArticle> readRSS() {
        try {
            List<NewsArticle> news = Lists.newArrayList();
            
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            URL u = new URL(Locations.feedURL);
            Document doc = builder.parse(u.openStream());
            NodeList nodes = doc.getElementsByTagName("item");
            
            for(int i=0; i<nodes.getLength(); i++) {
                Element element = (Element)nodes.item(i);
                NewsArticle article = new NewsArticle();
                article.setTitle(getTextValue(element, "title"));
                article.setHyperlink(getTextValue(element, "link"));
                article.setBody(getTextValue(element, "content:encoded"));
                article.setDate(getTextValue(element, "pubDate"));
                news.add(article);
            }
            
            return news;
        } catch(Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    private static String getTextValue(Element doc, String tag) {
        String value = "";
        NodeList nl;
        nl = doc.getElementsByTagName(tag);
        if (nl.getLength() > 0 && nl.item(0).hasChildNodes()) {
            value = nl.item(0).getFirstChild().getNodeValue();
        }
        return value;
    }

}
