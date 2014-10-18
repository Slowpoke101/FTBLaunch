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
package net.ftb.minecraft;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import net.feed_the_beast.launcher.json.JsonFactory;
import net.feed_the_beast.launcher.json.OldPropertyMapSerializer;
import net.feed_the_beast.launcher.json.assets.AssetIndex;
import net.feed_the_beast.launcher.json.assets.AssetIndex.Asset;
import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.download.Locations;
import net.ftb.log.Logger;
import net.ftb.util.*;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.mojang.authlib.UserAuthentication;
import com.mojang.authlib.UserType;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import com.mojang.util.UUIDTypeAdapter;

import javax.swing.*;

public class MCLauncher {
    public static boolean isLegacy = false;
    private static String separator = File.separator;
    private static String gameDirectory;
    private static StringBuilder cpb;

    public static Process launchMinecraft (String javaPath, String gameFolder, File assetDir, File nativesDir, List<File> classpath, String mainClass, String args, String assetIndex, String rmax,
            String maxPermSize, String version, UserAuthentication authentication, boolean legacy) throws IOException {

        cpb = new StringBuilder("");
        isLegacy = legacy;
        gameDirectory = gameFolder;
        File gameDir = new File(gameFolder);
        assetDir = syncAssets(assetDir, assetIndex);

        for (File f : classpath) {
            cpb.append(OSUtils.getJavaDelimiter());
            cpb.append(f.getAbsolutePath());
        }
        if (isLegacy)
            setupLegacyStuff(gameDirectory, Locations.FORGENAME);
        //Logger.logInfo("ClassPath: " + cpb.toString());

        List<String> arguments = Lists.newArrayList();

        Logger.logInfo("Java Path: " + javaPath);
        Logger.logInfo("Pack: " + ModPack.getSelectedPack().getName() + " " + version);
        arguments.add(javaPath);

        setMemory(arguments, rmax);

        if (OSUtils.getCurrentOS().equals(OSUtils.OS.WINDOWS)) {
            if (!OSUtils.is64BitWindows()) {
                if (maxPermSize == null || maxPermSize.isEmpty()) {
                    if (OSUtils.getOSTotalMemory() > 2046) {
                        maxPermSize = "192m";
                        Logger.logInfo("Defaulting PermSize to 192m");
                    } else {
                        maxPermSize = "128m";
                        Logger.logInfo("Defaulting PermSize to 128m");
                    }
                }
            }
        }

        if (maxPermSize == null || maxPermSize.isEmpty()) {
            // 64-bit or Non-Windows
            maxPermSize = "256m";
            Logger.logInfo("Defaulting PermSize to 256m");
        }

        arguments.add("-XX:PermSize=" + maxPermSize);
        arguments.add("-Djava.library.path=" + nativesDir.getAbsolutePath());
        arguments.add("-Dorg.lwjgl.librarypath=" + nativesDir.getAbsolutePath());
        arguments.add("-Dnet.java.games.input.librarypath=" + nativesDir.getAbsolutePath());
        arguments.add("-Duser.home=" + gameDir.getParentFile().getAbsolutePath());

        if (OSUtils.getCurrentOS() == OSUtils.OS.WINDOWS) {
            arguments.add("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump");
        }

        // Use IPv4 when possible, only use IPv6 when connecting to IPv6 only addresses
        arguments.add("-Djava.net.preferIPv4Stack=true");

        if (Settings.getSettings().getUseSystemProxy()) {
            arguments.add("-Djava.net.useSystemProxies=true");
        }

        arguments.add("-cp");
        arguments.add(cpb.toString());

        String additionalOptions = Settings.getSettings().getAdditionalJavaOptions();
        if (!additionalOptions.isEmpty()) {
            Logger.logInfo("Additional java parameters: " + additionalOptions);
            for (String s : additionalOptions.split("\\s+")) {
                if (s.equalsIgnoreCase("-Dfml.ignoreInvalidMinecraftCertificates=true") && !isLegacy) {
                    String curVersion = (Settings.getSettings().getPackVer().equalsIgnoreCase("recommended version") ? ModPack.getSelectedPack().getVersion() : Settings.getSettings().getPackVer())
                            .replace(".", "_");
                    TrackerUtils.sendPageView("JarmodAttempt", "JarmodAttempt / " + ModPack.getSelectedPack().getName() + " / " + curVersion.replace('_', '.'));
                    ErrorUtils.tossError("JARMODDING DETECTED in 1.6.4+ " + s, "FTB Does not support jarmodding in MC 1.6+ ");
                } else {
                    arguments.add(s);
                }
            }
        }
        if (Settings.getSettings().getOptJavaArgs()) {
            Logger.logInfo("Adding Optimization Arguments");
            Collections.addAll(arguments, "-XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+CICompilerCountPerCPU -XX:+TieredCompilation".split("\\s+"));
        }

        //Undocumented environment variable to control JVM
        String additionalEnvVar = System.getenv("_JAVA_OPTIONS");
        if (additionalEnvVar != null && !additionalEnvVar.isEmpty()) {
            Logger.logDebug("_JAVA_OPTIONS has following arguments: " + additionalEnvVar + " Arguments will  be ignored at MC startup");
        }
        //Documented environment variable to control JVM
        additionalEnvVar = System.getenv("JAVA_TOOL_OPTIONS");
        if (additionalEnvVar != null && !additionalEnvVar.isEmpty()) {
            Logger.logDebug("JAVA_TOOL_OPTIONS has following arguments: " + additionalEnvVar + " Arguments will  be ignored at MC startup");
        }
        //Documented environment variable to control JVM
        additionalEnvVar = System.getenv("JAVA_OPTIONS");
        if (additionalEnvVar != null && !additionalEnvVar.isEmpty()) {
            Logger.logDebug("JAVA_OPTIONS has following arguments: " + additionalEnvVar + " Arguments will  be ignored at MC startup");
        }

        arguments.add(mainClass);
        for (String s : args.split(" ")) {
            boolean done = false;
            if (authentication.getSelectedProfile() != null) {
                if (s.equals("${auth_player_name}")) {
                    arguments.add(authentication.getSelectedProfile().getName());
                    done = true;
                } else if (s.equals("${auth_uuid}")) {
                    arguments.add(UUIDTypeAdapter.fromUUID(authentication.getSelectedProfile().getId()));
                    done = true;
                } else if (s.equals("${user_type}")) {
                    arguments.add(authentication.getUserType().getName());
                    done = true;
                }
            } else {
                if (s.equals("${auth_player_name}")) {
                    arguments.add("Player");
                    done = true;
                } else if (s.equals("${auth_uuid}")) {
                    arguments.add(new UUID(0L, 0L).toString());
                    done = true;
                } else if (s.equals("${user_type}")) {
                    arguments.add(UserType.LEGACY.getName());
                    done = true;
                }
            }
            if (!done) {
                if (s.equals("${auth_session}")) {
                    if (authentication.isLoggedIn() && authentication.canPlayOnline()) {
                        if (authentication instanceof YggdrasilUserAuthentication && !isLegacy) {
                            arguments.add(String.format("token:%s:%s", authentication.getAuthenticatedToken(), UUIDTypeAdapter.fromUUID(authentication.getSelectedProfile().getId())));
                        } else {
                            arguments.add(authentication.getAuthenticatedToken());
                        }
                    } else {
                        arguments.add("-");
                    }
                } else if (s.equals("${auth_access_token}"))
                    arguments.add(authentication.getAuthenticatedToken());
                else if (s.equals("${version_name}"))
                    arguments.add(version);
                else if (s.equals("${game_directory}"))
                    arguments.add(gameDir.getAbsolutePath());
                else if (s.equals("${game_assets}") || s.equals("${assets_root}"))
                    arguments.add(assetDir.getAbsolutePath());
                else if (s.equals("${assets_index_name}"))
                    arguments.add(assetIndex == null ? "legacy" : assetIndex);
                else if (s.equals("${user_properties}"))
                    arguments.add(new GsonBuilder().registerTypeAdapter(PropertyMap.class, new OldPropertyMapSerializer()).create().toJson(authentication.getUserProperties()));
                else if (s.equals("${user_properties_map}"))
                    arguments.add(new GsonBuilder().registerTypeAdapter(PropertyMap.class, new PropertyMap.Serializer()).create().toJson(authentication.getUserProperties()));
                else if (isLegacy)
                    arguments.add(parseLegacyArgs(s));
                else
                    arguments.add(s);
            }
        }
        if (!isLegacy) {//legacy is handled separately
            boolean fullscreen = false;
            if (Settings.getSettings().getLastExtendedState() == JFrame.MAXIMIZED_BOTH) {
                GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
                Rectangle bounds = env.getMaximumWindowBounds();
                arguments.add("--width");
                arguments.add(String.valueOf((int) bounds.getWidth()));
                arguments.add("--height");
                arguments.add(String.valueOf((int) bounds.getHeight()));
                fullscreen = true;
            }
            Dimension def = new Dimension(854, 480);
            if (Settings.getSettings().getLastDimension().getWidth() != def.getWidth() && !fullscreen) {
                arguments.add("--width");
                arguments.add(String.valueOf(Math.abs((int) Settings.getSettings().getLastDimension().getWidth())));
            }
            if (Settings.getSettings().getLastDimension().getHeight() != def.getHeight() && !fullscreen) {
                arguments.add("--height");
                arguments.add(String.valueOf(Math.abs((int) Settings.getSettings().getLastDimension().getHeight())));
            }
        }

        ProcessBuilder builder = new ProcessBuilder(arguments);
        /*StringBuilder tmp = new StringBuilder();
        for (String a : builder.command())
            tmp.append(a).append(' ');
        Logger.logInfo("Launching: " + tmp.toString());*/
        builder.directory(gameDir);
        builder.redirectErrorStream(true);
        OSUtils.cleanEnvVars(builder.environment());
        return builder.start();
    }

    private static void setMemory (List<String> arguments, String rmax) {
        boolean memorySet = false;
        try {
            int min = 256;
            if (rmax != null && Integer.parseInt(rmax) > 0) {
                arguments.add("-Xms" + min + "M");
                Logger.logInfo("Setting MinMemory to " + min);
                arguments.add("-Xmx" + rmax + "M");
                Logger.logInfo("Setting MaxMemory to " + rmax);
                memorySet = true;
            }
        } catch (Exception e) {
            Logger.logError("Error parsing memory settings", e);
        }
        if (!memorySet) {
            arguments.add("-Xms" + 256 + "M");
            Logger.logInfo("Defaulting MinMemory to " + 256);
            arguments.add("-Xmx" + 1024 + "M");
            Logger.logInfo("Defaulting MaxMemory to " + 1024);
        }
    }

    private static File syncAssets (File assetDir, String indexName) throws JsonSyntaxException, JsonIOException, IOException {
        Logger.logInfo("Syncing Assets:");
        final File objects = new File(assetDir, "objects");
        AssetIndex index = JsonFactory.loadAssetIndex(new File(assetDir, "indexes/{INDEX}.json".replace("{INDEX}", indexName)));

        if (!index.virtual)
            return assetDir;

        final File targetDir = new File(assetDir, "virtual/" + indexName);

        final ConcurrentSkipListSet<File> old = new ConcurrentSkipListSet();
        old.addAll(FTBFileUtils.listFiles(targetDir));

        Benchmark.reset("threading");
        Parallel.TaskHandler th = new Parallel.ForEach(index.objects.entrySet()).withFixedThreads(2 * OSUtils.getNumCores())
        //.configurePoolSize(2*2*OSUtils.getNumCores(), 10)
                .apply(new Parallel.F<Entry<String, Asset>, Void>() {
                    public Void apply (Entry<String, Asset> e) {
                        Asset asset = e.getValue();
                        File local = new File(targetDir, e.getKey());
                        File object = new File(objects, asset.hash.substring(0, 2) + "/" + asset.hash);

                        old.remove(local);

                        try {
                            if (local.exists() && !DownloadUtils.fileSHA(local).equals(asset.hash)) {
                                Logger.logInfo("  Changed: " + e.getKey());
                                FTBFileUtils.copyFile(object, local, true);
                            } else if (!local.exists()) {
                                Logger.logInfo("  Added: " + e.getKey());
                                FTBFileUtils.copyFile(object, local);
                            }
                        } catch (Exception ex) {
                            Logger.logError("Asset checking failed: ", ex);
                        }
                        return null;
                    }
                });
        try {
            th.shutdown();
            th.wait(60, TimeUnit.SECONDS);
        } catch (Exception ex) {
            Logger.logError("Asset checking failed: ", ex);
        }
        Benchmark.logBenchAs("threading", "parallel asset(virtual) check");

        for (File f : old) {
            String name = f.getAbsolutePath().replace(targetDir.getAbsolutePath(), "");
            Logger.logInfo("  Removed: " + name.substring(1));
            f.delete();
        }

        return targetDir;
    }

    public static String parseLegacyArgs (String s) {
        if (s.equals("${animation_name}"))
            return (((!ModPack.getSelectedPack().getAnimation().equalsIgnoreCase("empty")) ? OSUtils.getCacheStorageLocation() + "ModPacks" + separator + ModPack.getSelectedPack().getDir()
                    + separator + ModPack.getSelectedPack().getAnimation() : "empty"));
        else if (s.equals("${forge_name}"))
            return Locations.FORGENAME;
        else if (s.equals("${pack_name}"))
            return (ModPack.getSelectedPack().getName() + " v" + (Settings.getSettings().getPackVer().equalsIgnoreCase("recommended version") ? ModPack.getSelectedPack().getVersion() : Settings
                    .getSettings().getPackVer()));
        else if (s.equals("${pack_image}"))
            return (OSUtils.getCacheStorageLocation() + "ModPacks" + separator + ModPack.getSelectedPack().getDir() + separator + ModPack.getSelectedPack().getLogoName());
        else if (s.equals("${extended_state}"))
            return (String.valueOf(Settings.getSettings().getLastExtendedState()));
        else if (s.equals("${width}"))
            return (String.valueOf(Settings.getSettings().getLastDimension().getWidth()));
        else if (s.equals("${height}"))
            return (String.valueOf(Settings.getSettings().getLastDimension().getHeight()));
        else if (s.equals("${minecraft_jar}"))
            return (gameDirectory + File.separator + "bin" + File.separator + Locations.OLDMCJARNAME);
        else
            return s;
    }

    public static void setupLegacyStuff (String workingDir, String forgename) {
        File instModsDir = new File(new File(workingDir).getParentFile(), "instMods/");
        //jarmods are added inside the wrapper

        cpb.append(OSUtils.getJavaDelimiter());
        cpb.append(new File(instModsDir, forgename).getAbsolutePath());
        cpb.append(new File(new File(workingDir, "bin"), Locations.OLDMCJARNAME).getAbsolutePath());
        File libsDir = new File(workingDir, "lib/");
        if (libsDir.isDirectory()) {
            String[] files = libsDir.list();
            Arrays.sort(files);
            for (String name : files) {
                if ((name.toLowerCase().endsWith(".zip") || name.toLowerCase().endsWith(".jar"))) {
                    cpb.append(OSUtils.getJavaDelimiter());
                    cpb.append(new File(libsDir, name).getAbsolutePath());
                }
            }
        } else {
            Logger.logInfo("Not loading any FML libs, as the directory does not exist.");
        }
    }
}
