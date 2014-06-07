package net.ftb.download;

import com.google.common.collect.Maps;

import java.util.HashMap;

//Class used for storage of various constants & location information used by various downloading processes
public class Locations {
    //location of them Mojang server that MC itself & the json's are pulled from
    public final static String mc_dl = "https://s3.amazonaws.com/Minecraft.Download/";
    //location of them Mojang server that MC's resources are pulled from
    public final static String mc_res = "http://resources.download.minecraft.net/";
    //location of them Mojang server that hosts the Minecraft Maven host
    public final static String mc_libs = "https://libraries.minecraft.net/";
    //location of the FTB maven repo within the FTB2/static part of the repository
    public final static String ftb_maven = "maven/";

    public static final String chRepo = "http://new.creeperrepo.net";
    public static final String curseRepo = "http://ftb.cursecdn.com";
    //this changes based on the primary automatic server in the balancing logic
    //DO NOT ACCESS UNILL 1st DL thread has finished setting up!!!!
    public static String masterRepo = "http://ftb.cursecdn.com";
    public static String masterRepoNoHTTP = "ftb.cursecdn.com";
    // used in mirroring logic
    public static boolean primaryCH = false;
    public static boolean chEnabled = true;

   public static String FTB2 = "FTB2";
    //FULL location of the FTB maven repo
    public final static String FTBMAVENFULL = curseRepo + FTB2 + "maven/";


    //various values for mirrors located here so that multiple download threads don't need to re-initialize the data
    //worker's that rely on the server list being in tact should check serversloaded
    public volatile static boolean serversLoaded = false;
    public static boolean hasDLInitialized = false;

    //maps of JSON pairs of the primary/backup download servers
    public static HashMap<String, String> downloadServers = Maps.newHashMap();
    public static HashMap<String, String> backupServers =  Maps.newHashMap();

    //Oracle Java Locations
    public final static String java64Win = "http://javadl.sun.com/webapps/download/AutoDL?BundleId=87443";
    public final static String java32Win = "http://javadl.sun.com/webapps/download/AutoDL?BundleId=86895";
    public final static String java64Lin = "http://javadl.sun.com/webapps/download/AutoDL?BundleId=87437";
    public final static String java32Lin = "http://javadl.sun.com/webapps/download/AutoDL?BundleId=87435";
    public final static String jreMac = "http://javadl.sun.com/webapps/download/AutoDL?BundleId=87438";
    public final static String jdkMac = "http://javadl.sun.com/webapps/download/AutoDL?BundleId=87438";

    //folder constants all slashes must be web safe... replace w/ system slash on win
    public final static String MAPS = "Maps/";
    public final static String MODPACKS ="modpacks/";
    public final static String PRIVATEPACKS ="privatepacks/";
    public final static String TEXTUREPACKS = "texturepacks/";

    public final static String MAPXML = "maps.xml";
    public final static String MODPACKXML = "modpacks.xml";
    public final static String THIRDPARTYXML = "thirdparty.xml";
    public final static String TEXTUREPACKXML= "texturepack.xml";
    public final static String NEWSUPDATEPHP = "http://launcher.feed-the-beast.com/newsupdate.php";
    public final static String FTBSITE = "http://www.feed-the-beast.com";
}
