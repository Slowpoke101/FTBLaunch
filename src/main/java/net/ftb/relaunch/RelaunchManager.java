package net.ftb.relaunch;

import net.feed_the_beast.launcher.json.JsonFactory;
import net.feed_the_beast.launcher.json.java.Entry;
import net.feed_the_beast.launcher.json.java.MojangLauncher;
import net.ftb.data.Settings;
import net.ftb.download.Locations;
import net.ftb.log.Logger;
import net.ftb.util.DownloadUtils;
import net.ftb.util.FTBFileUtils;
import net.ftb.util.OSUtils;
import net.ftb.util.winreg.JavaInfo;
import net.ftb.util.winreg.JavaVersion;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Created by progwml6 on 12/12/14.
 */
public class RelaunchManager {
    private static boolean is64;
    private static boolean isWin;

    public static void inspectForRelaunch (String args[]) {
        JavaInfo java = Settings.getSettings().getCurrentJava();
        isWin = OSUtils.getCurrentOS() == OSUtils.OS.WINDOWS;
        JavaVersion java7 = JavaVersion.createJavaVersion("1.7.0");
        JavaVersion java8 = JavaVersion.createJavaVersion("1.8.0");
        is64 = OSUtils.is64BitOS();
        if (java.isOlder(java7)) {
            inspect(args, java, java7);
        } else if (java.isOlder(java8)) {
            // in the future we might need to get people on java 8
            // however... if javafx is not installed we will ask for a java DL
        } else {
            //we aren't gonna prompt java 8 users to update to mojang's java
        }
    }

    private static void inspect (String args[], JavaVersion current, JavaVersion causedUpdate) {
        Entry mj = null;

        try {
            MojangLauncher ml = JsonFactory.GSON.fromJson(IOUtils.toString(new URL(Locations.MC_LAUNCHER_META)), MojangLauncher.class);
            switch (OSUtils.getCurrentOS()) {
            case WINDOWS:
                if (is64) {
                    mj = ml.getWindows().getBits64().getJre();
                } else {
                    mj = ml.getWindows().getBits64().getJre();
                }
                break;
            case MACOSX:
                //not currently supported
                break;
            case UNIX:
                //not currently supported
                break;
            case OTHER:
                //never supported
                break;
            }
        } catch (IOException e) {

        }
        if (mj != null) {
            // loc/runtime/type-x{86 or 64}/version/
            boolean run = false;
            String tempLZMA = Settings.getSettings().getInstallPath() + File.separator + "runtime" + mj.getVersion() + ".lzma";
            String tempZip = tempLZMA.replace(".lzma", "");
            String outLocation =
                    Settings.getSettings().getInstallPath() + File.separator + "runtime" + File.separator + "jre-" + (is64 ? "x64" : "x86") + File.separator + mj.getVersion() + File.separator;
            File out = new File(outLocation);
            File tempZipF = new File(tempZip);
            File tempLZMAF = new File(tempLZMA);
            if (!out.exists()) {
                out.mkdirs();
            } else {
                //TODO check the java, set run to true; and return
            }
            if (!run) {
                try {
                    Logger.logDebug("downloading java from Mojang");
                    DownloadUtils.downloadToFile(tempLZMA, mj.getUrl());
                    if (DownloadUtils.isSHA1Valid(new File(tempLZMA), mj.getSha1())) {
                        FTBFileUtils.extractLZMA(tempLZMA, tempZipF);
                        FTBFileUtils.extractZipTo(tempZip, outLocation);
                        //todo test java, if fails delete the java as well
                        FTBFileUtils.delete(tempZipF);
                        FTBFileUtils.delete(tempLZMAF);

                    } else {
                        //error downloading -- hash NFG
                    }
                } catch (IOException e) {
                }
            }
            if (run) {
                launch(new File(out, "bin/java" + (isWin ? ".exe" : "")), args);//TODO how to handle exe launcher & app launcher here
            }
        }

    }

    private static void launch (File fl, String[] args) {
        //append the arg to not run the relaunching

    }
}
