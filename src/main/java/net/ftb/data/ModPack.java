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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import net.ftb.events.PackChangeEvent;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.panes.FTBPacksPane;
import net.ftb.gui.panes.ThirdPartyPane;
import net.ftb.log.Logger;
import net.ftb.main.Main;
import net.ftb.util.DownloadUtils;
import net.ftb.util.OSUtils;
import net.ftb.workers.ModpackLoader;

import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

public class ModPack {
    private String name, author, version, url, dir, mcVersion, serverUrl, logoName, imageName, info, animation, maxPermSize, sep = File.separator, xml;
    private String[] mods, oldVersions;
    private Image logo, image;
    private int index;
    private boolean updated = false, hasCustomTP, hasbundledmap;
    @Getter
    private boolean thirdPartyTab;
    private final static ArrayList<ModPack> packs = Lists.newArrayList();
    private boolean privatePack;
    @Getter
    private String minJRE;
    @Getter
    private int minLaunchSpec;
    @Getter
    private String disclaimer;
    private static ModPack selectedPack;
    /**
     * @return map of <String packversion, String MCVersion>
     */
    @Getter
    private HashMap<String, String> customMCVersions = Maps.newHashMap();

    /**
     * Loads the modpack.xml and adds it to the modpack array in this class
     */
    public static void loadXml (ArrayList<String> xmlFile) {
        ModpackLoader loader = new ModpackLoader(xmlFile, false);
        loader.start();
    }

    // Used by PrivatePackDialog when adding packs
    public static void loadXml (String xmlFile) {
        ArrayList<String> temp = Lists.newArrayList();
        temp.add(xmlFile);
        ModpackLoader loader = new ModpackLoader(temp, true);
        loader.start();
    }

    /**
     * Adds modpack to the modpacks array
     * @param pack - a ModPack instance
     */
    public static void addPack (ModPack pack) {
        synchronized (packs) {
            packs.add(pack);
        }
    }

    /**
     * Adds modpack to the modpacks array
     * @param packs_ - an array list of ModPack instances
     */
    public static void addPacks (ArrayList<ModPack> packs_) {
        synchronized (packs) {
            for (ModPack p : packs_) {
                packs.add(p);
            }
        }
    }

    public static void removePacks (String xml) {
        ArrayList<ModPack> remove = Lists.newArrayList();
        int removed = -1; // TODO: if private xmls ever contain more than one modpack, we need to change this
        for (ModPack pack : packs) {
            if (pack.getParentXml().equalsIgnoreCase(xml)) {
                remove.add(pack);
            }
        }
        for (ModPack pack : remove) {
            removed = pack.getIndex();
            packs.remove(pack);
        }
        for (ModPack pack : packs) {
            if (removed != -1 && pack.getIndex() > removed) {
                pack.setIndex(pack.getIndex() - 1);
            }
        }
        Main.getEventBus().post(new PackChangeEvent(PackChangeEvent.TYPE.REMOVE, true, xml));//makes sure the pack gets removed from the pane
    }

    /**
     * Used to get the List of modpacks
     * @return - the array containing all the modpacks
     */
    public static ArrayList<ModPack> getPackArray () {
        return packs;
    }

    /**
     * Gets the ModPack form the array and the given index
     * @param i - the value in the array
     * @return - the ModPack based on the i value
     */
    public static ModPack getPack (int i) {
        return packs.get(i);
    }

    public static ModPack getPack (String dir) {
        for (ModPack pack : packs) {
            if (pack.getDir().equalsIgnoreCase(dir)) {
                return pack;
            }
        }
        return null;
    }

    public static void setSelectedPack (String dir) {
        selectedPack = getPack(dir);
    }

    public static void setSelectedPack (ModPack pack) {
        selectedPack = pack;
    }

    /**
     * Used to grab the currently selected ModPack to being launched
     * @return ModPack - the currently selected ModPack
     */
    public static ModPack getSelectedPack () {
        if (selectedPack == null) {
            return null;
        } else {
            return selectedPack;
        }
    }
    
    /**
     * Constructor for ModPack class
     * @param name - the name of the ModPack
     * @param author - the author of the ModPack
     * @param version - the version of the ModPack
     * @param logo - the logo file name for the ModPack
     * @param url - the ModPack file name
     * @param image - the splash image file name for the ModPack
     * @param dir - the directory for the ModPack
     * @param mcVersion - the minecraft version required for the ModPack
     * @param serverUrl - the server file name of the ModPack
     * @param info - the description for the ModPack
     * @param mods - string containing a list of mods included in the ModPack by default
     * @param oldVersions - string containing all available old versions of the ModPack
     * @param animation - the animation to display before minecraft launches
     * @param idx - the actual position of the modpack in the index
     * @param bundledMap - pack has map bundled inside it
     * @param customTP - pack does not use primary TP's for MC version
     * @param minJRE - minimum JRE version needed to run pack (optional in xml)
     * @param thirdpartyTab - should this pack be in the FTB or third party tabs?
     * @param minLaunchSpec - minimum launcher build needed to run latest pack version(optional in xml)
     * @param disclaimer - disclaimer for unstable packs
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public ModPack (String name, String author, String version, String logo, String url, String image, String dir, String mcVersion, String serverUrl, String info, String mods, String oldVersions,
            String animation, String maxPermSize, int idx, boolean privatePack, String xml, boolean bundledMap, boolean customTP, String minJRE, boolean thirdpartyTab, int minLaunchSpec,
            String disclaimer, String customMCVersions) throws IOException, NoSuchAlgorithmException {
        index = idx;
        this.name = name;
        this.author = author;
        this.version = version;
        this.dir = dir;
        this.mcVersion = mcVersion;
        this.url = url;
        this.serverUrl = serverUrl;
        this.privatePack = privatePack;
        this.xml = xml;
        this.maxPermSize = maxPermSize;
        this.hasbundledmap = bundledMap;
        this.hasCustomTP = customTP;
        this.minLaunchSpec = minLaunchSpec;
        this.minJRE = minJRE;
        if (!animation.isEmpty()) {
            this.animation = animation;
        } else {
            this.animation = "empty";
        }
        logoName = logo;
        imageName = image;
        this.info = info;
        this.disclaimer = disclaimer;
        if (mods.isEmpty()) {
            this.mods = null;
        } else {
            this.mods = mods.split("; ");
        }
        if (oldVersions == null || oldVersions.isEmpty()) {
            this.oldVersions = null;
        } else {
            this.oldVersions = oldVersions.split(";");
        }
        if (customMCVersions != null && !customMCVersions.isEmpty()) {
            String[] tmp = customMCVersions.split(";");
            if (tmp == null) {
                tmp = new String[] { customMCVersions };
            }
            for (String s : tmp) {
                String[] s2 = s.split("\\^");
                this.customMCVersions.put(s2[0], s2[1]);
            }
        }

        String installPath = OSUtils.getCacheStorageLocation();
        File tempDir = new File(installPath, "ModPacks" + sep + dir);
        File verFile = new File(tempDir, "version");
        this.thirdPartyTab = thirdpartyTab;

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

    /**
     * Used to check if the cached items are up to date
     * @param verFile - the version file to check
     * @return checks the version file against the current modpack version
     */
    private boolean upToDate (File verFile) {
        String storedVersion = getStoredVersion(verFile).replace(".", "");
        int storedVersion_ = -1;
        if (!storedVersion.isEmpty()) {
            try {
                storedVersion_ = Integer.parseInt(storedVersion);
            } catch (NumberFormatException e) {
                Logger.logWarn("Automatically fixing malformed version file for " + name, e);
                storedVersion = "";
            }
        }

        if (storedVersion.isEmpty() || storedVersion_ != Integer.parseInt(version.replace(".", ""))) {
            try {
                if (!verFile.exists()) {
                    verFile.getParentFile().mkdirs();
                    verFile.createNewFile();
                }
                BufferedWriter out = new BufferedWriter(new FileWriter(verFile));
                out.write(version);
                out.flush();
                out.close();
                return false;
            } catch (IOException e) {
                Logger.logError("Error while checking modpack version", e);
                return false;
            }
        }

        return true;
    }

    public boolean needsUpdate (File verFile) {
        return Integer.parseInt(getStoredVersion(verFile).replace(".", "")) != Integer.parseInt(version.replace(".", ""));
    }

    public String getStoredVersion (File verFile) {
        String result = "";
        try {
            if (!verFile.exists()) {
                verFile.getParentFile().mkdirs();
                verFile.createNewFile();
            }
            BufferedReader in = new BufferedReader(new FileReader(verFile));
            String line;
            if ((line = in.readLine()) != null) {
                result = line;
            }
            in.close();
        } catch (IOException e) {
            Logger.logError("Error while getting modpack version", e);
        }
        return result;
    }

    /**
     * Used to get index of modpack
     * @return - the index of the modpack in the GUI
     */
    public int getIndex () {
        return index;
    }

    public void setIndex (int index) {
        this.index = index;
    }

    /**
     * Used to get name of modpack
     * @return - the name of the modpack
     */
    public String getName () {
        return name;
    }

    /**
     * Gets a formatted name with includes the Mod Pack label and minecraft version
     * <p>
     * This label is used in places where a list or group of mod packs may contain multiple versions of the 
     * same pack, such as the list of packs supported by a texture pack
     * </p>
     * @return The name of the mod pack and the minecraft version supported, if provided
     */
    public String getNameWithVersion () {
        StringBuilder name = new StringBuilder(getName());

        if (getMcVersion() != null) {
            name.append(" (").append(getMcVersion()).append(")");
        }

        return name.toString();
    }

    /**
     * Used to get Author of modpack
     * @return - the modpack's author
     */
    public String getAuthor () {
        return author;
    }

    /**
     * Used to get the version of the modpack
     * @return - the modpacks version
     */
    public String getVersion () {
        return version;
    }

    /**
     * Used to get an Image variable of the modpack's logo
     * @return - the modpacks logo
     */
    public Image getLogo () {
        return logo;
    }

    /**
     * Used to get the URL or File name of the modpack
     * @return - the modpacks URL
     */
    public String getUrl () {
        return url;
    }

    /**
     * Used to get an Image variable of the modpack's splash image
     * @return - the modpacks splash image
     */
    public Image getImage () {
        return image;
    }

    /**
     * Used to get the directory of the modpack
     * @return - the directory for the modpack
     */
    public String getDir () {
        return dir;
    }

    /**
     * Used to get the minecraft version required for the modpack
     * @return - the minecraft version
     */
    public String getMcVersion () {
        return mcVersion;
    }

    /**
     * Used to get the minecraft version required for the modpack
     * @param packVersion the version of the modpack you need the MC version for
     * @return - the minecraft version
     */
    public String getMcVersion (String packVersion) {
        if (customMCVersions != null && customMCVersions.containsKey(packVersion)) {
            return customMCVersions.get(packVersion);
        }
        return mcVersion;
    }

    /**
     * Used to get the info or description of the modpack
     * @return - the info for the modpack
     */
    public String getInfo () {
        return info;
    }

    /**
     * Used to get an array of mods inside the modpack
     * @return - string array of all mods contained
     */
    public String[] getMods () {
        return mods;
    }

    /**
     * Used to get the name of the server file for the modpack
     * @return - string representing server file name
     */
    public String getServerUrl () {
        return serverUrl;
    }

    /**
     * Used to get the logo file name
     * @return - the logo name as saved on the repo
     */
    public String getLogoName () {
        return logoName;
    }

    /**
     * Used to get the splash file name
     * @return - the splash image name as saved on the repo
     */
    public String getImageName () {
        return imageName;
    }

    /**
     * Used to set whether the modpack has been updated
     * @param result - the status of whether the modpack has been updated or not
     */
    public void setUpdated (boolean result) {
        updated = result;
    }

    /**
     * Used to check if the modpack has been updated
     * @return - the boolean representing whether the modpack has been updated
     */
    public boolean isUpdated () {
        return updated;
    }

    /**
     * Used to get all available old versions of the modpack
     * @return - string array containing all available old version of the modpack
     */
    public String[] getOldVersions () {
        return oldVersions;
    }

    /**
     * Used to set the minecraft version required of the pack to a custom version
     * @param version - the version of minecraft for the pack
     */
    public void setMcVersion (String version) {
        mcVersion = version;
    }

    /**
     * @return the filename of the gif animation to display before minecraft loads
     */
    public String getAnimation () {
        return animation;
    }

    public boolean isPrivatePack () {
        return privatePack;
    }

    public String getParentXml () {
        return xml;
    }

    public String getMaxPermSize () {
        return maxPermSize;
    }

    public boolean getBundledMap () {
        return hasbundledmap;
    }

    public boolean hasCustomTP () {
        return hasCustomTP;
    }

    public static void setVanillaPackMCVersion (String string) {
        for (int i = 0; i < packs.size(); i++) {
            if (packs.get(i).getDir().equals("mojang_vanilla")) {
                ModPack temp = packs.get(i);
                temp.setMcVersion(string);
                packs.remove(i);
                packs.add(i, temp);
                return;
            }
        }
    }
}
