package net.ftb.relaunch;

import net.feed_the_beast.launcher.json.JsonFactory;
import net.feed_the_beast.launcher.json.java.Entry;
import net.feed_the_beast.launcher.json.java.MojangLauncher;
import net.ftb.data.CommandLineSettings;
import net.ftb.data.Settings;
import net.ftb.download.Locations;
import net.ftb.gui.LaunchFrame;
import net.ftb.log.Logger;
import net.ftb.util.DownloadUtils;
import net.ftb.util.ErrorUtils;
import net.ftb.util.FTBFileUtils;
import net.ftb.util.OSUtils;
import net.ftb.util.winreg.JavaInfo;
import net.ftb.util.winreg.JavaVersion;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author progwml6
 */
public class RelaunchManager {
    private static boolean is64;
    private static boolean isWin;
    private static String WIN_TEMPLATE = "${LOC}\\bin\\java.exe %*";
    private static String POSIX_TEMPLATE = "${LOC}/bin/java $@";

    public static void inspectForRelaunch (String args[]) {
        if (!CommandLineSettings.getSettings().isNoRelaunch()) {
            JavaInfo java = Settings.getSettings().getCurrentJava();
            isWin = OSUtils.getCurrentOS() == OSUtils.OS.WINDOWS;
            JavaVersion java7 = JavaVersion.createJavaVersion("1.7.0");
            JavaVersion java8 = JavaVersion.createJavaVersion("1.8.0");
            is64 = OSUtils.is64BitOS();
            if (java.isOlder(java7)) {
                inspect(args, java, java7);
            } else if (java.isOlder(java8)) {
                try {
                    Class.forName("javafx.scene.control.Label");
                } catch (Exception e) {
                    Logger.logWarn("JAVAFX not found requesting java download anyway");
                    inspect(args, java, java8);
                }
                // in the future we might need to get people on java 8
                // however... if javafx is not installed we will ask for a java DL
            } else {
                //we aren't gonna prompt java 8 users to update to mojang's java unless javafx is missing
                try {
                    Class.forName("javafx.scene.control.Label");
                } catch (Exception e) {
                    Logger.logWarn("JAVAFX not found requesting java download anyway");
                    inspect(args, java, java);
                }

            }
        }
    }

    private static void inspect (String args[], JavaVersion current, JavaVersion causedUpdate) {
        Entry mj = null;

        try {
            MojangLauncher ml = null;
            switch (OSUtils.getCurrentOS()) {
            case WINDOWS:
                if (is64) {
                    mj = ml.getWindows().getBits64().getJre();
                } else {
                    mj = ml.getWindows().getBits32().getJre();
                }
                ml = JsonFactory.GSON.fromJson(IOUtils.toString(new URL(Locations.MC_LAUNCHER_META)), MojangLauncher.class);
                break;
            case MACOSX:
                ml = JsonFactory.GSON.fromJson(IOUtils.toString(new URL(DownloadUtils.getStaticCreeperhostLink(Locations.FTB_JAVA_META))), MojangLauncher.class);
                if (OSUtils.canRun8OnMac()) {
                    mj = ml.getMac().getBits64().getJre();
                } else if (OSUtils.canRun7OnMac() && ml.getMac().getBackup64() != null) {
                    mj = ml.getMac().getBackup64().getJre();//grab java 7
                } else {
                    String s = "Mac is running 10.5 or 10.6, There is no java 7+ available for this version! \n We highly reccomend updating in order to continue using minecraft in the future!";
                    ErrorUtils.tossError(s, s);
                }
                //if they can't run java 7+ properly we can't help unfortunately
                break;
            case UNIX:
                ml = JsonFactory.GSON.fromJson(IOUtils.toString(new URL(DownloadUtils.getStaticCreeperhostLink(Locations.FTB_JAVA_META))), MojangLauncher.class);
                if (is64) {
                    mj = ml.getLinux().getBits64().getJre();
                } else {
                    mj = ml.getLinux().getBits32().getJre();
                }

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
                        createShellWrapper(outLocation);
                    } else {
                        //error downloading -- hash NFG
                    }
                } catch (IOException e) {
                }
            }
            if (run) {
                launch(outLocation + File.separator + "bin" + File.separator + "java" + (isWin ? ".exe" : ""), args);//TODO how to handle exe launcher & app launcher here
            }
        }

    }

    private static void launch (String path, String[] args) {
        List<String> arguments = new ArrayList<String>();

        String separator = System.getProperty("file.separator");
        arguments.add(path);
        arguments.add("-jar");
        arguments.add(LaunchFrame.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        Collections.addAll(arguments, args);
        arguments.add("--no-relaunch");

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(arguments);
        try {
            processBuilder.start();
        } catch (IOException e) {
            Logger.logError("Failed relaunch the launcher", e);
        }

    }

    //shell wrapper for server downloads to be able to easily run on our java's
    private static void createShellWrapper (String loc) {
        try {
            String fl = OSUtils.getDynamicStorageLocation();
            if (isWin) {
                fl += File.separator + "FTBJava.bat";
            } else {
                fl += File.separator + "FTBJava.sh";
            }
            FileWriter fw = new FileWriter(fl);
            if (isWin) {
                fw.write(WIN_TEMPLATE.replace("${LOC}", loc));
            } else {
                fw.write(POSIX_TEMPLATE.replace("${LOC}", loc));
            }
            fw.close();
            File f = new File(fl);
            f.setExecutable(true);
        } catch (IOException ioe) {

        }
    }
}
