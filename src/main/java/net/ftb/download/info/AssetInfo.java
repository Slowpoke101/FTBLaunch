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
package net.ftb.download.info;

import net.ftb.download.Locations;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public final class AssetInfo extends DownloadInfo {
    public final String etag;

    private AssetInfo (File root, Element node) throws MalformedURLException {
        url = new URL(Locations.mc_res + getText(node, "Key", null));
        name = getText(node, "Key", "");
        etag = getText(node, "ETag", "").replace("\"", "");
        size = Long.parseLong(getText(node, "Size", "0"));
        local = new File(root, name);
    }

    private String getText (Element node, String name, String def) {
        NodeList lst = node.getElementsByTagName(name);
        if (lst == null) {
            return def;
        }
        return lst.item(0).getChildNodes().item(0).getNodeValue();
    }

    public String toString () {
        return etag + " " + name + " " + size;
    }
}
