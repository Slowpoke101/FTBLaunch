/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2013, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
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
package net.ftb.mclauncher;

import java.applet.Applet;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.log.LogLevel;
import net.ftb.log.Logger;
import net.ftb.util.OSUtils;

public class MinecraftLauncher {

    public static Process launchMinecraft (String javaPath, String workingDir, String username, String password, String forgename, String rmax, String maxPermSize, String legacyLaunchLocation) throws IOException {
        String[] jarFiles = new String[] { "minecraft.jar", "lwjgl.jar", "lwjgl_util.jar", "jinput.jar" };
        StringBuilder cpb = new StringBuilder("");
        File instModsDir = new File(new File(workingDir).getParentFile(), "instMods/");
        if (instModsDir.isDirectory()) {
            String[] files = instModsDir.list();
            Arrays.sort(files);
            for (String name : files) {
                if (!name.equals(forgename)) {
                    if (name.toLowerCase().contains("forge") && name.toLowerCase().contains("minecraft") && name.toLowerCase().endsWith(".zip")) {
                        if (new File(instModsDir, forgename).exists()) {
                            if (!new File(instModsDir, forgename).equals(new File(instModsDir, name))) {
                                new File(instModsDir, name).delete();
                            }
                        } else {
                            new File(instModsDir, name).renameTo(new File(instModsDir, forgename));
                        }
                    } else if (!name.equalsIgnoreCase(forgename) && (name.toLowerCase().endsWith(".zip") || name.toLowerCase().endsWith(".jar"))) {
                        cpb.append(OSUtils.getJavaDelimiter());
                        cpb.append(new File(instModsDir, name).getAbsolutePath());
                    }
                }
            }
        } else {
            Logger.logInfo("Not loading any instMods (minecraft jar mods), as the directory does not exist.");
        }

        cpb.append(OSUtils.getJavaDelimiter());
        cpb.append(new File(instModsDir, forgename).getAbsolutePath());

        for (String jarFile : jarFiles) {
            cpb.append(OSUtils.getJavaDelimiter());
            cpb.append(new File(new File(workingDir, "bin"), jarFile).getAbsolutePath());
        }
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

        List<String> arguments = new ArrayList<String>();

        String separator = System.getProperty("file.separator");
        Logger.logInfo("Java Path: " + javaPath);
        Logger.logInfo("Pack: " + ModPack.getSelectedPack().getName() + " " + ModPack.getSelectedPack().getVersion());
        arguments.add(javaPath);

        setMemory(arguments, rmax);

        //arguments.add("-XX:+UseConcMarkSweepGC");
        //arguments.add("-XX:+CMSIncrementalMode");
        //arguments.add("-XX:+AggressiveOpts");
        //arguments.add("-XX:+CMSClassUnloadingEnabled");
        if (maxPermSize.isEmpty()) {
            arguments.add("-XX:PermSize=128m");
        } else {
            arguments.add("-XX:PermSize=" + maxPermSize);
        }

        // Use IPv4 when possible, only use IPv6 when connecting to IPv6 only addresses
        arguments.add("-Djava.net.preferIPv4Stack=true");

        arguments.add("-cp");
        arguments.add(cpb.toString() + OSUtils.getJavaDelimiter() + legacyLaunchLocation);

        arguments.add("net.ftb.legacylaunch.Launch");//legacy launch entry point

        arguments.add(username);//done
        arguments.add(password);//done

        arguments.add("--gameDir");
        arguments.add(workingDir);
        arguments.add("--animationName");
        arguments.add(((!ModPack.getSelectedPack().getAnimation().equalsIgnoreCase("empty")) ? OSUtils.getCacheStorageLocation() + "ModPacks" + separator + ModPack.getSelectedPack().getDir()
                + separator + ModPack.getSelectedPack().getAnimation() : "empty"));
        arguments.add("--forgeName");
        arguments.add(forgename);
        arguments.add("--packName");
        arguments.add(ModPack.getSelectedPack().getName() + " v"
                + (Settings.getSettings().getPackVer().equalsIgnoreCase("recommended version") ? ModPack.getSelectedPack().getVersion() : Settings.getSettings().getPackVer()));
        arguments.add("--packImage");
        arguments.add(OSUtils.getCacheStorageLocation() + "ModPacks" + separator + ModPack.getSelectedPack().getDir() + separator + ModPack.getSelectedPack().getLogoName());

        String additionalOptions = Settings.getSettings().getAdditionalJavaOptions();
        if (!additionalOptions.isEmpty()) {
            Collections.addAll(arguments, additionalOptions.split("\\s+"));
        }
        if (Settings.getSettings().getOptJavaArgs()) {
            Logger.logInfo("Adding Optimization Arguments");
            Collections.addAll(arguments, "-XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+CICompilerCountPerCPU -XX:+TieredCompilation".split("\\s+"));
        }
        ProcessBuilder processBuilder = new ProcessBuilder(arguments);
        processBuilder.redirectErrorStream(true);
        OSUtils.cleanEnvVars(processBuilder.environment());
        return processBuilder.start();
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

}
