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
package net.ftb.workers;

import java.net.URL;

import net.ftb.data.Map;
import net.ftb.gui.panes.MapsPane;
import net.ftb.log.Logger;
import net.ftb.util.AppUtils;
import net.ftb.util.DownloadUtils;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MapLoader extends Thread {
	private static String MAPFILE;

	public MapLoader() { }

	@Override
	public void run() {
		try {
			Logger.logInfo("loading map information...");
			MAPFILE = DownloadUtils.getStaticCreeperhostLink("maps.xml");
			Document doc = AppUtils.downloadXML(new URL(MAPFILE));
			if(doc == null) {
				Logger.logError("Error: Could not load map data!");
			}
			NodeList maps = doc.getElementsByTagName("map");
			for(int i = 0; i < maps.getLength(); i++) {
				Node map = maps.item(i);
				NamedNodeMap mapAttr = map.getAttributes();
				Map.addMap(new Map(mapAttr.getNamedItem("name").getTextContent(), mapAttr.getNamedItem("author").getTextContent(),
						mapAttr.getNamedItem("version").getTextContent(), mapAttr.getNamedItem("url").getTextContent(),
						mapAttr.getNamedItem("logo").getTextContent(), mapAttr.getNamedItem("image").getTextContent(),
						mapAttr.getNamedItem("compatible").getTextContent(), mapAttr.getNamedItem("mcversion").getTextContent(), 
						mapAttr.getNamedItem("mapname").getTextContent(), mapAttr.getNamedItem("description").getTextContent(), i));
			}
			MapsPane.loaded = true;
		} catch (Exception e) { 
			Logger.logError(e.getMessage(), e);
		}
	}
}
