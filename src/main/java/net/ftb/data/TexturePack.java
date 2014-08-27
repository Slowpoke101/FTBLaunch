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
package net.ftb.data;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.ftb.data.events.TexturePackListener;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.panes.TexturepackPane;
import net.ftb.log.Logger;
import net.ftb.util.DownloadUtils;
import net.ftb.util.OSUtils;
import net.ftb.workers.TexturePackLoader;

public class TexturePack {
    @Getter
    private String name, author, version, url, mcversion, logoName, imageName, info, resolution;
    private String sep = File.separator;
    @Getter
    private Image logo, image;
    @Getter
    private List<String> compatible = Lists.newArrayList();
    @Getter
    private List<String> masters = Lists.newArrayList();

    @Getter
    private int index;
    @Getter
    private final static ArrayList<TexturePack> texturePackArray =Lists.newArrayList();
    private static List<TexturePackListener> listeners = Lists.newArrayList();

    public static void addListener (TexturePackListener listener) {
        listeners.add(listener);
    }

    public static void loadAll () {
        TexturePackLoader loader = new TexturePackLoader();
        loader.start();
    }

    public static void addTexturePack (TexturePack texturePack) {
        synchronized (texturePackArray) {
            texturePackArray.add(texturePack);
        }
        for (TexturePackListener listener : listeners) {
            listener.onTexturePackAdded(texturePack);
        }
    }

    public static TexturePack getTexturePack (int i) {
        return texturePackArray.get(i);
    }

    /**
     * Used to grab the currently selected TexturePack based off the selected index from TexturepackPane
     * @return TexturePack - the currently selected TexturePack
     */
    public static TexturePack getSelectedTexturePack () {
        return getTexturePack(TexturepackPane.getSelectedTexturePackIndex());
    }

    public TexturePack(String name, String author, String version, String url, String logo, String image, String mcversion, String compatible, String info, String resolution, int idx)
            throws NoSuchAlgorithmException, IOException {
        index = idx;
        this.name = name;
        this.author = author;
        this.version = version;
        this.url = url;
        this.version = version;
        String installPath = OSUtils.getCacheStorageLocation();
        logoName = logo;
        imageName = image;
        String[] tmp = compatible.split(",");
        Collections.addAll(this.compatible, tmp);
        this.info = info;
        this.resolution = resolution;
        for (Iterator<String> it = this.compatible.iterator(); it.hasNext();) {
            String s = it.next();
            if (s.toLowerCase().startsWith("master")) {
                masters.add(s.replace("master_", ""));
                it.remove();
            }
        }
        for (ModPack p : ModPack.getPackArray()) {
            if (!p.hasCustomTP() && !this.compatible.contains(p.getDir()) && masters.contains(p.getMcVersion().replace(".", "_")))
                this.compatible.add(p.getDir());
        }
        File tempDir = new File(installPath, "TexturePacks" + sep + name);
        File verFile = new File(tempDir, "version");

        if (!upToDate(verFile)) {
            DownloadUtils.saveImage(logo, tempDir, "png");
            DownloadUtils.saveImage(image, tempDir, "png");

        } else {
            if (!new File(tempDir, logo).exists()) {
                DownloadUtils.saveImage(logo, tempDir, "png");
            }
            if (!new File(tempDir, image).exists()) {
                DownloadUtils.saveImage(image, tempDir, "png");
            }
        }

        // image and logo should now exists, if not use placeholder images
        if (!new File(tempDir, logo).exists()) {
            this.logoName = logo = "logo_ftb.png";
            DownloadUtils.saveImage(logo, tempDir, "png");
        }
        this.logo = Toolkit.getDefaultToolkit().createImage(tempDir.getPath() + sep + logo);

        if (!new File(tempDir, image).exists()) {
            this.imageName = image = "default_splash.png";
            DownloadUtils.saveImage(image, tempDir, "png");
        }
        this.image = Toolkit.getDefaultToolkit().createImage(tempDir.getPath() + sep + image);
    }

    private boolean upToDate (File verFile) {
        boolean result = true;
        try {
            if (!verFile.exists()) {
                verFile.getParentFile().mkdirs();
                verFile.createNewFile();
                result = false;
            }
            BufferedReader in = new BufferedReader(new FileReader(verFile));
            String line = in.readLine();
            int storedVersion = -1;
            if (line != null) {
                try {
                    storedVersion = Integer.parseInt(line.replace(".", ""));
                } catch (NumberFormatException e) {
                    Logger.logWarn("Automatically fixing malformed version file for " + name, e);
                    line = null;
                }
            }

            if (line == null || Integer.parseInt(version.replace(".", "")) != storedVersion) {
                BufferedWriter out = new BufferedWriter(new FileWriter(verFile));
                out.write(version);
                out.flush();
                out.close();
                result = false;
            }
            in.close();
        } catch (IOException e) {
            Logger.logError("Error while checking texturepack version", e);
        }
        return result;
    }

    /**
     * Used to get the selected mod pack
     * @return - the compatible pack based on the selected texture pack
     */
    public String getSelectedCompatible () {
        return compatible.get(LaunchFrame.getSelectedTPInstallIndex()).trim();
    }

    public boolean isCompatible (String packName) {
        for (String aCompatible : compatible) {
            ModPack pack = ModPack.getPack(aCompatible);
            if (pack == null) {
                Logger.logDebug("Texturepack is compatible with "  + packName + " , but modpack not found");
            }
            else {
                return pack.getName().equals(packName);
            }
        }
        return false;
    }
}
