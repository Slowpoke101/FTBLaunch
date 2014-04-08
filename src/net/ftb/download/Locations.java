package net.ftb.download;

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

    public static final String chRepo = new String("http://new.creeperrepo.net");
    public static final String curseRepo = new String("http://ftb.cursecdn.com");
    //this changes based on the primary automatic server in the balancing logic
    public static String masterRepo = new String("http://new.creeperrepo.net");
    public static String masterRepoNoHTTP = new String("new.creeperrepo.net");
    // used in mirroring logic
    public static boolean primaryCH = true;

    //various values for mirrors located here so that multiple download threads don't need to re-initialize the data
    public volatile static boolean serversLoaded = false;
    public static boolean hasDLInitialized = false;

    //maps of JSON pairs of the primary/backup download servers
    public static HashMap<String, String> downloadServers = new HashMap<String, String>();
    public static HashMap<String, String> backupServers = new HashMap<String, String>();

}
