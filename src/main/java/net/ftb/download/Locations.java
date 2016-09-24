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
package net.ftb.download;

import com.google.common.collect.Maps;

import java.util.HashMap;

//Class used for storage of various constants & location information used by various downloading processes
public class Locations {

    //location of them Mojang server that MC itself & the json's are pulled from
    @Deprecated
    public static final String mc_dl = "https://s3.amazonaws.com/Minecraft.Download/";
    //location of them Mojang server that MC's resources are pulled from
    public static final String mc_versionsmanifest = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    public static final String mc_res = "http://resources.download.minecraft.net/";
    //location of them Mojang server that hosts the Minecraft Maven host
    public static final String mc_libs = "https://libraries.minecraft.net/";
    //location of the FTB maven repo within the FTB2/static part of the repository
    public static final String ftb_maven = "maven/";

    public static final String chRepo = "http://www.creeperrepo.net";
    public static final String curseRepo = "http://ftb.cursecdn.com";
    //this changes based on the primary automatic server in the balancing logic
    //DO NOT ACCESS UNILL 1st DL thread has finished setting up!!!!
    public static String masterRepo = "http://ftb.cursecdn.com";
    public static String masterRepoNoHTTP = "ftb.cursecdn.com";
    // used in mirroring logic
    public static boolean primaryCH = false;
    public static boolean chEnabled = true;

    public static String FTB2 = "FTB2/";
    //FULL location of the FTB maven repo
    public static final String FTBMAVENFULL = curseRepo + "/" + FTB2 + "maven/";

    //various values for mirrors located here so that multiple download threads don't need to re-initialize the data
    //worker's that rely on the server list being in tact should check serversloaded
    public volatile static boolean serversLoaded = false;
    public static boolean hasDLInitialized = false;

    //maps of JSON pairs of the primary/backup download servers
    public static HashMap<String, String> downloadServers = Maps.newHashMap();
    public static HashMap<String, String> backupServers = Maps.newHashMap();

    //Oracle Java Locations
    public static final String java64Win = "http://javadl.sun.com/webapps/download/AutoDL?BundleId=107100";
    public static final String java32Win = "http://javadl.sun.com/webapps/download/AutoDL?BundleId=106238";
    public static final String java64Lin = "http://javadl.sun.com/webapps/download/AutoDL?BundleId=106240";
    public static final String java32Lin = "http://javadl.sun.com/webapps/download/AutoDL?BundleId=97798";
    public static final String jreMac = "http://javadl.sun.com/webapps/download/AutoDL?BundleId=106241";
    public static final String jdkMac = "http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html";

    // Forum and Feed URL
    public static final String forum = "http://forum.feed-the-beast.com/";
    public static final String feedURL = "http://forum.feed-the-beast.com/forum/modpack-and-launcher-news.35/index.rss";

    //folder constants all slashes must be web safe... replace w/ system slash on win
    public static final String MAPS = "Maps/";
    public static final String MODPACKS = "modpacks/";
    public static final String PRIVATEPACKS = "privatepacks/";
    public static final String TEXTUREPACKS = "texturepacks/";

    public static final String MAPXML = "maps.xml";
    public static final String MODPACKXML = "modpacks.xml";
    public static final String THIRDPARTYXML = "thirdparty.xml";
    public static final String TEXTUREPACKXML = "texturepack.xml";
    public static final String SUPPORTSITE = "http://support.feed-the-beast.com/";
    public static final String FTBSITE = "http://www.feed-the-beast.com";

    public static final String PIWIK = "http://stats.feed-the-beast.com/";

    public static final String CURSEVOICE = "http://beta.cursevoice.com/games/minecraft";

    public static final String FTBLOGO = "/image/logo_ftb.png";
    public static final String CHLOGO = "/image/logo_creeperHost.png";
    public static final String TUGLOGO = "/image/logo_TUG.png";
    public static final String CURSELOGO = "/image/logo_curse.png";
    public static final String FORGENAME = "MinecraftForge.zip";
    public static final String OLDMCJARNAME = "minecraft.jar";
    public static final String launcherLogFile = "FTBLauncherLog.txt";
    public static final String minecraftLogFile = "MinecraftLog.txt";

    private Locations() {
    }
}
