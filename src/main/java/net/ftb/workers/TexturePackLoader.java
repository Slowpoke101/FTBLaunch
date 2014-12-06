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
package net.ftb.workers;

import net.ftb.data.TexturePack;
import net.ftb.download.Locations;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.panes.TexturepackPane;
import net.ftb.log.Logger;
import net.ftb.util.AppUtils;
import net.ftb.util.Benchmark;
import net.ftb.util.DownloadUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.URL;

public class TexturePackLoader extends Thread {

    public TexturePackLoader () {
    }

    @Override
    public void run () {
        try {//TODO ASAP thread this!!!
            Benchmark.start("TexturepackLoader");
            Logger.logInfo("loading texture pack information...");
            Document doc = AppUtils.downloadXML(new URL(DownloadUtils.getStaticCreeperhostLink(Locations.TEXTUREPACKXML)));
            if (doc == null) {
                Logger.logError("Error: Could not load texture pack data!");
            }
            NodeList texturePacks = doc.getElementsByTagName("texturepack");
            for (int i = 0; i < texturePacks.getLength(); i++) {
                Node texturePack = texturePacks.item(i);
                NamedNodeMap textureAttr = texturePack.getAttributes();
                TexturePack.addTexturePack(new TexturePack(textureAttr.getNamedItem("name").getTextContent(), textureAttr.getNamedItem("author").getTextContent(), textureAttr.getNamedItem("version")
                        .getTextContent(), textureAttr.getNamedItem("url").getTextContent(), textureAttr.getNamedItem("logo").getTextContent(), textureAttr.getNamedItem("image").getTextContent(),
                        textureAttr.getNamedItem("mcversion").getTextContent(), textureAttr.getNamedItem("compatible").getTextContent(),
                        textureAttr.getNamedItem("description") == null ? null : textureAttr.getNamedItem("description").getTextContent().replace(
                                "\\n", "\n"),
                        textureAttr.getNamedItem("resolution").getTextContent(), i));
            }
        } catch (Exception e) {
            Logger.logError("Error loading texture pack info", e);
        } finally {
            TexturepackPane.loaded = true;
            Benchmark.logBenchAs("TexturepackLoader", "Texture Pack Load ");
            LaunchFrame.checkDoneLoading();
        }
    }
}
