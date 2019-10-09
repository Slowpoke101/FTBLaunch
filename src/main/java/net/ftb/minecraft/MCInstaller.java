/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2018, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
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

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.feed_the_beast.launcher.json.JsonFactory;
import net.feed_the_beast.launcher.json.assets.AssetIndex;
import net.feed_the_beast.launcher.json.forge.InstallProfile;
import net.feed_the_beast.launcher.json.forge.InstallerData;
import net.feed_the_beast.launcher.json.forge.InstallerProcessor;
import net.feed_the_beast.launcher.json.versions.DownloadType;
import net.feed_the_beast.launcher.json.versions.LaunchStrings;
import net.feed_the_beast.launcher.json.versions.Library;
import net.feed_the_beast.launcher.json.versions.SlimVersion;
import net.feed_the_beast.launcher.json.versions.Version;
import net.feed_the_beast.launcher.json.versions.VersionManifest;
import net.ftb.data.CommandLineSettings;
import net.ftb.data.LauncherStyle;
import net.ftb.data.LoginResponse;
import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.download.Locations;
import net.ftb.download.info.DownloadInfo;
import net.ftb.download.workers.AssetDownloader;
import net.ftb.events.EnableObjectsEvent;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.panes.OptionsPane;
import net.ftb.log.LogEntry;
import net.ftb.log.LogLevel;
import net.ftb.log.Logger;
import net.ftb.log.StreamLogger;
import net.ftb.main.Main;
import net.ftb.tools.ProcessMonitor;
import net.ftb.util.AppUtils;
import net.ftb.util.Benchmark;
import net.ftb.util.ComparableVersion;
import net.ftb.util.DownloadUtils;
import net.ftb.util.ErrorUtils;
import net.ftb.util.FTBFileUtils;
import net.ftb.util.OSUtils;
import net.ftb.util.Parallel;
import net.ftb.util.TrackerUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.processing.Processor;
import javax.swing.*;

public class MCInstaller {
    private static String packmcversion = new String();
    private static String packbasejson = new String();

    private static String getlocationofmavenfile (Library any, String location, File local) {
        return new File(local, any.getartifactforstring(location).getPath()).getAbsolutePath();
    }

    private static File getfileofmavenfile (Library any, String location, File local) {
        return new File(local, any.getartifactforstring(location).getPath());
    }

    private static URL getmavenlocalurl (Library any, String location, File local) throws MalformedURLException {
        return new File(local, any.getartifactforstring(location).getPath()).toURI().toURL();
    }

    private static boolean checkoutputs (InstallerProcessor p, InstallProfile profile, Library any, File libroot) throws IOException {
        for (Map.Entry<String, String> entry : p.getOutputs().entrySet()) {
            String key = entry.getKey();
            char start = key.charAt(0);
            char end = key.charAt(key.length() - 1);
            String outputsha = null;
            if (start == '{' && end == '}') {
                InstallerData d = profile.getData().get(key.substring(1, key.length() - 1));
                if (d == null) {
                    return false;
                }
                String dataval = d.getClient();
                char cstart = dataval.charAt(0);
                char cend = dataval.charAt(key.length() - 1);
                File op = getfileofmavenfile(any, dataval.substring(1, dataval.length() - 1), libroot);
                if (!op.exists()) {
                    return false;
                }
                outputsha = DownloadUtils.fileSHA(op);
                String value = entry.getValue();
                char valueStart = value.charAt(0);
                char valueEnd = value.charAt(value.length() - 1);

                if (valueStart == '{' && valueEnd == '}') {
                    String ev = profile.getData().get(dataval.substring(1, dataval.length() - 1)).getClient();

                    String expectedHash = ev.charAt(0) == '\'' ? ev.substring(1, ev.length() - 1) : ev;

                    Logger.logDebug("Expecting " + outputsha + " to equal " + expectedHash);
                    if (!outputsha.equals(expectedHash)) {
                        FileUtils.deleteQuietly(op);
                        return false;
                    }
                }

            }

        }
        return true;
    }

    private static void installmodlauncher (final String installPath, final Version packversion, final ModPack pack, final File root) throws IOException {
        Boolean forceUpdate = Settings.getSettings().isForceUpdateEnabled();
        InstallProfile profile = packversion.get_forgeprofile();
        Library libfake = new Library();
        File local = new File(root, "libraries/");
        File instjar = new File(local, profile.getInstallerjar().get_artifact().getPath());
        File extractedDir = new File(instjar.getParentFile(), "extracted");
        if (forceUpdate) {
            if (extractedDir.exists()) {
                FileUtils.deleteDirectory(extractedDir);
            }
            //if extract exists delete it
        }
        if (!extractedDir.exists()) {
            FTBFileUtils.extractZipTo(instjar.getAbsolutePath(), extractedDir.getAbsolutePath());
        }
        Map<String, InstallerData> data = profile.getData();
        for (InstallerProcessor p : profile.getProcessors()) {
            String jarloc = getlocationofmavenfile(libfake, p.getJar(), local);
            Logger.logDebug("Processor jar path is " + jarloc);
            JarFile jarFile = new JarFile(jarloc);
            String mainClass = jarFile.getManifest().getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
            jarFile.close();

            if (mainClass == null || mainClass.isEmpty()) {
                Logger.logError("did not find Mainclass for " + jarloc + " as the mainclass wasn't found");
                //TODO fail out here
            }
            List<URL> cp = Lists.newArrayList();
            cp.add(new File(jarloc).toURI().toURL());
            for (String l : p.getClasspath()) {
                cp.add(getmavenlocalurl(libfake, l, local));
            }
            List<String> args = Lists.newArrayList();
            for (String arg : p.getArgs()) {
                char start = arg.charAt(0);
                char end = arg.charAt(arg.length() - 1);
                if (start == '{' && end == '}') {
                    String key = arg.substring(1, arg.length() - 1);
                    InstallerData d = data.get(key);
                    // TODO throw error and cancel install if no installer data is found
                    String dataval = d.getClient();
                    char cstart = dataval.charAt(0);
                    char cend = dataval.charAt(arg.length() - 1);
                    if (cstart == '[' && cend == ']') {
                        dataval = getlocationofmavenfile(libfake, dataval.substring(1, dataval.length() - 1), local);
                    } else {
                        dataval = d.getClient();
                    }
                    if (dataval.contains(local.getAbsolutePath())) {
                        args.add(dataval);
                    } else {
                        if (dataval.charAt(0) == '/') {
                            File localFile = new File(extractedDir, dataval.substring(1));
                            args.add(localFile.getAbsolutePath());

                            // TODO handle Local extract data
                        } else {
                            args.add(dataval);
                        }
                    }
                } else if (start == '[' && end == ']') {
                    args.add(getlocationofmavenfile(libfake, arg.substring(1, arg.length() - 1), local));
                    //TODO make sure this file exists and exit if its not
                } else {
                    args.add(arg);
                }
            }
            // we pass in some extra params for the forge installer tools DEOBF_REALMS task
            if (p.getArgs().contains("DEOBF_REALMS")) {
                args.add("--json");
                //replace this with location of version json on disk from launchermeta
                args.add(new File(root, "versions/{MC_VER}/{MC_VER}.json".replace("{MC_VER}", packbasejson)).getAbsolutePath());
                args.add("--libs");
                args.add(local.getAbsolutePath());
            }

            ClassLoader cl = new URLClassLoader(cp.toArray(new URL[cp.size()]),
                    Processor.class.getClassLoader());
            try {
                Logger.logDebug("Running processor");
                Class<?> cls = Class.forName(mainClass, true, cl);
                Method main = cls.getDeclaredMethod("main", String[].class);
                main.invoke(null, (Object) args.toArray(new String[args.size()]));
            } catch (Throwable e) {
                Logger.logError("error processing " + jarloc, e);
                //TODO throw error
            }
            //TODO check outputs here
            if (p.getOutputs() != null && !p.getOutputs().isEmpty()) {
                checkoutputs(p, profile, libfake, root);
            }
        }

    }

    public static void setupNewStyle (final String installPath, final ModPack pack, final boolean isLegacy, final LoginResponse RESPONSE) {
        packmcversion = pack.getMcVersion(Settings.getSettings().getPackVer(pack.getDir()));
        packbasejson = "";
        Pair<List<DownloadInfo>, Version> pr = gatherAssets(new File(installPath), installPath, isLegacy);
        List<DownloadInfo> assets = pr.getLeft();
        final Version packversion = pr.getRight();
        if (assets != null && assets.size() > 0) {
            Logger.logInfo("Checking/Downloading " + assets.size() + " assets, this may take a while...");

            final ProgressMonitor prog = new ProgressMonitor(LaunchFrame.getInstance(), "Downloading Files...", "", 0, 100);
            prog.setMaximum(assets.size() * 100);

            final AssetDownloader downloader = new AssetDownloader(prog, assets) {
                @Override
                public void done () {
                    try {
                        prog.close();
                        if (get()) {
                            Logger.logInfo("Asset downloading complete");
                            if (packversion != null && packversion.get_forgeprofile() != null) {
                                // TODO only do this if it is needed or force is on
                                if (Settings.getSettings().isForceUpdateEnabled()) {
                                    installmodlauncher(installPath, packversion, pack, new File(installPath));
                                } else {
                                    boolean iml = false;
                                    for (InstallerProcessor p : packversion.get_forgeprofile().getProcessors()) {
                                        if (!checkoutputs(p, packversion.get_forgeprofile(), new Library(), new File(installPath))) {
                                            iml = true;
                                        }
                                    }
                                    if (iml) {
                                        installmodlauncher(installPath, packversion, pack, new File(installPath));
                                    }
                                }
                            }
                            launchMinecraft(installPath, pack, RESPONSE, isLegacy);
                        } else {
                            ErrorUtils.tossError("Error occurred during downloading the assets");
                        }
                    } catch (CancellationException e) {
                        Logger.logInfo("Asset download interrupted by user");
                    } catch (Exception e) {
                        ErrorUtils.tossError("Failed to download files.", e);
                    } finally {
                        Main.getEventBus().post(new EnableObjectsEvent());
                    }
                }
            };

            downloader.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange (PropertyChangeEvent evt) {
                    if (prog.isCanceled()) {
                        downloader.cancel(false);
                        prog.close();
                    } else if (!downloader.isCancelled()) {
                        if ("ready".equals(evt.getPropertyName())) {
                            prog.setProgress(downloader.getReady());
                        }
                        if ("status".equals(evt.getPropertyName())) {
                            prog.setNote(downloader.getStatus());
                        }
                    }
                }
            });

            downloader.execute();
        } else if (assets == null) {
            Main.getEventBus().post(new EnableObjectsEvent());
        } else {
            launchMinecraft(installPath, pack, RESPONSE, isLegacy);
        }
    }

    private static Optional<DownloadInfo> checkDep (Library lib, final File root, boolean forceUpdate, File libDir, ModPack pack, String installDir, boolean modlauncher) throws MalformedURLException {
        DownloadInfo ret = null;
        Library.Artifact a;
        File local = new File(root, "libraries/" + lib.getPath());
        if (!new File(libDir, lib.getPath()).exists() || forceUpdate) {
            if (lib.checksums != null) {
                ret = (new DownloadInfo(new URL(lib.getUrl() + lib.getPath()), local, lib.getPath(), lib.checksums, "sha1",
                        DownloadInfo.DLType.NONE, DownloadInfo.DLType.NONE));
            } else if (lib.download != null && lib.download) {
                ret = (new DownloadInfo(new URL(lib.getUrl() + lib.getPath()), local, lib.getPath()));
            }
        }
        a = lib.get_artifact();
        if (a.getDomain().equalsIgnoreCase("net.minecraftforge") && (a.getName().equalsIgnoreCase("forge") || a.getName().equalsIgnoreCase("minecraftforge")) && !modlauncher) {
            grabJava8CompatFix(a, pack, packmcversion, installDir + "/" + pack.getDir());
        }
        return Optional.fromNullable(ret);
    }

    /**
     * Gather assets to check/download. If force update is enabled this will return all assets
     *
     * @return null if failed and MC can't be started, otherwise, ArrayList containing assets to be checked/downloaded
     *              Normally, if offline mode works, setupNewStyle() and gatherAssets() are not called and error situation is impossible
     *              Returning null just in case of network breakge after authentication process
     */
    private static Pair<List<DownloadInfo>, Version> gatherAssets (final File root, String installDir, boolean isLegacy) {
        try {

            Logger.logInfo("Checking local assets file, for MC version " + packmcversion + " Please wait! ");
            List<DownloadInfo> list = Lists.newArrayList();
            Boolean forceUpdate = Settings.getSettings().isForceUpdateEnabled();
            //Pack JSON Libraries
            Logger.logDebug("Checking pack libraries");
            ModPack pack = ModPack.getSelectedPack();
            File packDir = new File(installDir, pack.getDir());
            File gameDir = new File(packDir, "minecraft");
            File libDir = new File(installDir, "libraries");
            // if (!pack.getDir().equals("mojang_vanilla")) {
            if (!pack.getDir().equals("mojang_vanilla")) {
                if (isLegacy) {
                    extractLegacyJson(new File(gameDir, "pack.json"));
                }
            }
            Version packjson = null;
            boolean modlauncher = false;
            if (new File(gameDir, "pack.json").exists()) {
                packjson = JsonFactory.loadVersion(new File(gameDir, "pack.json"));
                if (packjson.jar != null && !packjson.jar.isEmpty()) {
                    packmcversion = packjson.jar;
                }
                ComparableVersion version_ml = new ComparableVersion("1.13");
                if (version_ml.isOlder(packmcversion)) {
                    modlauncher = true;
                    InstallProfile forgeprofile = packjson.get_forgeprofile();
                    Optional<DownloadInfo> depfi = checkDep(forgeprofile.getInstallerjar(), root, forceUpdate, libDir, pack, installDir, modlauncher);
                    if (depfi.isPresent()) {
                        list.add(depfi.get());
                    }
                    for (Library lib : packjson.get_forgeprofile().getLibraries()) {
                        Optional<DownloadInfo> dep = checkDep(lib, root, forceUpdate, libDir, pack, installDir, modlauncher);
                        if (dep.isPresent()) {
                            list.add(dep.get());
                        }
                    }

                }
                if (packjson.inheritsFrom != null && !packjson.inheritsFrom.isEmpty()) {
                    packbasejson = packjson.inheritsFrom;
                }
                for (Library lib : packjson.getLibraries()) {
                    //Logger.logError(new File(libDir, lib.getPath()).getAbsolutePath());
                    // These files are shipped inside pack.zip, can't do force update check yet
                    Optional<DownloadInfo> dep = checkDep(lib, root, forceUpdate, libDir, pack, installDir, modlauncher);
                    if (dep.isPresent()) {
                        list.add(dep.get());
                    }
                }
                //}
            } else {
                if (!pack.getDir().equals("mojang_vanilla")) {
                    Logger.logError("pack.json file not found-Forge/Liteloader will not be able to load!");
                } else {
                    Logger.logInfo("pack.json not found in vanilla pack(this is expected)");
                }
                //TODO handle vanilla packs w/ tweakers w/ this stuffs !!!
            }

            /*
             * <ftb installation location>/libraries/*
             */
            //check if our copy exists of the version json if not backup to mojang's copy
            Logger.logDebug("Checking minecraft version json");
            if (packbasejson == null || packbasejson.isEmpty()) {
                packbasejson = packmcversion;
            }

            URL mcvsn = new URL(Locations.mc_versionsmanifest);
            File base = new File(root, "versions/version_manifest.json");
            DownloadUtils.downloadToFile(mcvsn, base, 3);
            if (!base.exists()) {
                Logger.logError("version manifest JSON not found");
                return null;
            }
            VersionManifest versionManifest = JsonFactory.loadVersionManifest(base);
            SlimVersion vsn = versionManifest.getVersionByName(packmcversion);
            URL url = new URL(DownloadUtils.getStaticCreeperhostLinkOrBackup("mcjsons/versions/{MC_VER}/{MC_VER}.json".replace("{MC_VER}", packbasejson), vsn.getUrl().toString()));

            // URL url = new URL(DownloadUtils.getStaticCreeperhostLinkOrBackup("mcjsons/versions/{MC_VER}/{MC_VER}.json".replace("{MC_VER}", packbasejson), Locations.mc_dl
            //        + "versions/{MC_VER}/{MC_VER}.json".replace("{MC_VER}", packbasejson)));
            File json = new File(root, "versions/{MC_VER}/{MC_VER}.json".replace("{MC_VER}", packbasejson));

            DownloadUtils.downloadToFile(url, json, 3);
            if (!json.exists()) {
                Logger.logError("library JSON not found");
                return null;
            }
            File local;
            Version version = JsonFactory.loadVersion(json);
            Logger.logDebug("checking minecraft libraries");
            for (Library lib : version.getLibraries()) {
                if (lib.natives == null) {
                    local = new File(root, "libraries/" + lib.getPath());
                    if (!local.exists() || forceUpdate) {
                        if (!lib.getUrl().toLowerCase().equalsIgnoreCase(Locations.ftb_maven)) {//DL's shouldn't be coming from maven repos but ours or mojang's
                            if (lib.downloads != null) {
                                list.add(new DownloadInfo(lib.downloads.artifact, local));
                            } else {
                                list.add(new DownloadInfo(new URL(lib.getUrl() + lib.getPath()), local, lib.getPath()));
                            }

                        } else {
                            if (lib.downloads != null) {
                                list.add(new DownloadInfo(lib.downloads.artifact, local));
                            } else {
                                list.add(new DownloadInfo(new URL(DownloadUtils.getCreeperhostLink(lib.getUrl() + lib.getPath())), local, lib.getPath(), true));
                            }
                        }
                    }
                } else if (!lib.hasNativesForOS()) {
                    //do nothing as this lib doesn't need natives and the raw jars are listed separately
                } else {
                    local = new File(root, "libraries/" + lib.getPathNatives());
                    if (!local.exists() || forceUpdate) {
                        if (lib.downloads != null) {
                            list.add(new DownloadInfo(lib.downloads.classifiers.get(lib.getNativeName()), local));
                        } else {
                            list.add(new DownloadInfo(new URL(lib.getUrl() + lib.getPathNatives()), local, lib.getPathNatives()));
                        }
                    }

                }
            }
            /*
             * vanilla minecraft.jar
             */

            local = new File(root, "versions/{MC_VER}/{MC_VER}.jar".replace("{MC_VER}", packmcversion));
            if (!local.exists() || forceUpdate) {
                list.add(new DownloadInfo(version.getDownload(DownloadType.CLIENT), local));
            }

            // Move the old format to the new:
            File test = new File(root, "assets/READ_ME_I_AM_VERY_IMPORTANT.txt");
            if (test.exists()) {
                Logger.logDebug("Moving old format");
                File assets = new File(root, "assets");
                Set<File> old = FTBFileUtils.listFiles(assets);
                File objects = new File(assets, "objects");
                String[] skip = new String[] { objects.getAbsolutePath(), new File(assets, "indexes").getAbsolutePath(), new File(assets, "virtual").getAbsolutePath() };

                for (File f : old) {
                    String path = f.getAbsolutePath();
                    boolean move = true;
                    for (String prefix : skip) {
                        if (path.startsWith(prefix)) {
                            move = false;
                        }
                    }
                    if (move) {
                        String hash = DownloadUtils.fileSHA(f);
                        File cache = new File(objects, hash.substring(0, 2) + "/" + hash);
                        Logger.logInfo("Caching Asset: " + hash + " - " + f.getAbsolutePath().replace(assets.getAbsolutePath(), ""));
                        if (!cache.exists()) {
                            cache.getParentFile().mkdirs();
                            f.renameTo(cache);
                        }
                        f.delete();
                    }
                }

                List<File> dirs = FTBFileUtils.listDirs(assets);
                for (File dir : dirs) {
                    if (dir.listFiles().length == 0) {
                        dir.delete();
                    }
                }
            }

            /*
             * assets/*
             */
            Logger.logDebug("Checking minecraft assets");
            url = version.assetIndex.getUrl();
            json = new File(root, "assets/indexes/{INDEX}.json".replace("{INDEX}", version.getAssets()));

            //TODO add hash support for version asset index if it exists here
            DownloadUtils.downloadToFile(url, json, 3);
            if (!json.exists()) {
                Logger.logError("asset JSON not found");
                return null;
            }

            AssetIndex index = JsonFactory.loadAssetIndex(json);

            Benchmark.start("threading");
            Collection<DownloadInfo> tmp;
            Logger.logDebug("Starting TaskHandler to check MC assets");
            Parallel.TaskHandler th = new Parallel.ForEach(index.objects.entrySet())
                    .withFixedThreads(2 * OSUtils.getNumCores())
                    //.configurePoolSize(2*2*OSUtils.getNumCores(), 10)
                    .apply(new Parallel.F<Map.Entry<String, AssetIndex.Asset>, DownloadInfo>() {
                        public DownloadInfo apply (Map.Entry<String, AssetIndex.Asset> e) {
                            try {
                                String name = e.getKey();
                                AssetIndex.Asset asset = e.getValue();
                                String path = asset.hash.substring(0, 2) + "/" + asset.hash;
                                final File local = new File(root, "assets/objects/" + path);
                                if (local.exists() && !asset.hash.equals(DownloadUtils.fileSHA(local))) {
                                    local.delete();
                                }
                                if (!local.exists()) {
                                    return (new DownloadInfo(new URL(Locations.mc_res + path), local, name, Lists.newArrayList(asset.hash), "sha1"));
                                }
                            } catch (Exception ex) {
                                Logger.logError("Asset hash check failed", ex);
                            }
                            // values() will drop null entries
                            return null;
                        }
                    });
            tmp = th.values();
            list.addAll(tmp);
            // kill executorservice
            th.shutdown();
            Benchmark.logBenchAs("threading", "parallel asset check");

            return Pair.of(list, packjson);
        } catch (Exception e) {
            Logger.logError("Error while gathering assets", e);
        }
        return null;
    }

    public static void launchMinecraft (String installDir, ModPack pack, LoginResponse resp, boolean isLegacy) {
        try {
            File packDir = new File(installDir, pack.getDir());
            String gameFolder = installDir + File.separator + pack.getDir() + File.separator + "minecraft";
            File gameDir = new File(packDir, "minecraft");
            File assetDir = new File(installDir, "assets");
            File libDir = new File(installDir, "libraries");
            File natDir = new File(packDir, "natives");
            final String packVer = Settings.getSettings().getPackVer(pack.getDir());

            Logger.logInfo("Setting up native libraries for " + pack.getName() + " v " + packVer + " MC " + packmcversion);
            if (!gameDir.exists()) {
                gameDir.mkdirs();
            }

            if (natDir.exists()) {
                natDir.delete();
            }
            natDir.mkdirs();

            packmcversion = pack.getMcVersion(Settings.getSettings().getPackVer(pack.getDir()));
            packbasejson = "";
            if (new File(gameDir, "pack.json").exists()) {
                Version packjson = JsonFactory.loadVersion(new File(gameDir, "pack.json"));
                if (packjson.jar != null && !packjson.jar.isEmpty()) {
                    packmcversion = packjson.jar; // is this needed or not?
                }
                if (packjson.inheritsFrom != null && !packjson.inheritsFrom.isEmpty()) {
                    packbasejson = packjson.inheritsFrom;
                }
            }

            if (packbasejson == null || packbasejson.isEmpty()) {
                packbasejson = packmcversion;
            }

            Logger.logDebug("packbaseJSON " + packbasejson);
            Version base = JsonFactory.loadVersion(new File(installDir, "versions/{MC_VER}/{MC_VER}.json".replace("{MC_VER}", packbasejson)));
            byte[] buf = new byte[1024];
            for (Library lib : base.getLibraries()) {
                if (lib.natives != null && lib.hasNativesForOS()) {
                    File local = new File(libDir, lib.getPathNatives());
                    ZipInputStream input = null;
                    try {
                        input = new ZipInputStream(new FileInputStream(local));
                        ZipEntry entry = input.getNextEntry();
                        while (entry != null) {
                            String name = entry.getName();
                            int n;
                            if (lib.extract == null || !lib.extract.exclude(name)) {
                                File output = new File(natDir, name);
                                output.getParentFile().mkdirs();
                                FileOutputStream out = new FileOutputStream(output);
                                while ((n = input.read(buf, 0, 1024)) > -1) {
                                    out.write(buf, 0, n);
                                }
                                out.close();
                            }
                            input.closeEntry();
                            entry = input.getNextEntry();
                        }
                    } catch (Exception e) {
                        ErrorUtils.tossError("Error extracting native libraries");
                        Logger.logError("", e);
                    } finally {
                        try {
                            input.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }
            List<File> classpath = Lists.newArrayList();
            Version packjson = new Version();
            if (new File(gameDir, "pack.json").exists()) {
                packjson = JsonFactory.loadVersion(new File(gameDir, "pack.json"));
                for (Library lib : packjson.getLibraries()) {
                    //Logger.logError(new File(libDir, lib.getPath()).getAbsolutePath());
                    classpath.add(new File(libDir, lib.getPath()));
                }
                //}
            } else {
                packjson = base;
            }
            if (!isLegacy) //we copy the jar to a new location for legacy
            {
                classpath.add(new File(installDir, "versions/{MC_VER}/{MC_VER}.jar".replace("{MC_VER}", packmcversion)));
            } else {
                FTBFileUtils.copyFile(new File(installDir, "versions/{MC_VER}/{MC_VER}.jar".replace("{MC_VER}", packmcversion)), new File(gameDir, "bin/" + Locations.OLDMCJARNAME));
                FTBFileUtils.killMetaInf();
            }
            for (Library lib : base.getLibraries()) {
                classpath.add(new File(libDir, lib.getPath()));
            }
            LaunchStrings pj = packjson.getArgumentsToLaunch();
            LaunchStrings bjson = base.getArgumentsToLaunch();
            LaunchStrings ls = pj != null ? pj : bjson;
            Process minecraftProcess = MCLauncher.launchMinecraft(Settings.getSettings().getJavaPath(), gameFolder, assetDir, natDir, classpath,
                    packjson.mainClass != null ? packjson.mainClass : base.mainClass, ls.arguments,
                    packjson.assets != null ? packjson.assets : base.getAssets(), Settings.getSettings().getRamMax(), pack.getMaxPermSize(), pack.getMcVersion(packVer), resp.getAuth(), isLegacy,
                    packjson.type, ls.jvm);
            LaunchFrame.MCRunning = true;

            if (!CommandLineSettings.getSettings().isDisableMCLogging()) {
                StreamLogger.prepare(minecraftProcess.getInputStream(), new LogEntry().level(LogLevel.UNKNOWN));
                String[] ignore = { "Session ID is token" };
                StreamLogger.setIgnore(ignore);
                StreamLogger.doStart();
            } else {
                // stderr is combined with stdout
                AppUtils.voidInputStream(minecraftProcess.getInputStream());
                Logger.logWarn("Not logging MC messages via launcher!");
            }
            Logger.logDebug("MC PID: " + OSUtils.getPID(minecraftProcess));
            String curVersion = (Settings.getSettings().getPackVer().equalsIgnoreCase("recommended version") ? pack.getVersion() : Settings.getSettings().getPackVer()).replace(".", "_");
            TrackerUtils.sendPageView(ModPack.getSelectedPack().getName(), "Launched / " + ModPack.getSelectedPack().getName() + " / " + curVersion.replace('_', '.'));
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
            }
            try {
                minecraftProcess.exitValue();
            } catch (IllegalThreadStateException e) {
                LaunchFrame.getInstance().setVisible(false);
                LaunchFrame.setProcMonitor(ProcessMonitor.create(minecraftProcess, new Runnable() {
                    @Override
                    public void run () {
                        if (!Settings.getSettings().getKeepLauncherOpen()) {
                            System.exit(0);
                        } else {
                            if (LaunchFrame.con != null) {
                                LaunchFrame.con.minecraftStopped();
                            }
                            LaunchFrame launchFrame = LaunchFrame.getInstance();
                            launchFrame.setVisible(true);
                            Main.getEventBus().post(new EnableObjectsEvent());
                            try {
                                Settings.getSettings().load(new FileInputStream(Settings.getSettings().getConfigFile()));
                                LaunchFrame.getInstance().tabbedPane.remove(1);
                                LaunchFrame.getInstance().optionsPane = new OptionsPane(Settings.getSettings());
                                LaunchFrame.getInstance().tabbedPane.add(LaunchFrame.getInstance().optionsPane, 1);
                                LaunchFrame.getInstance().tabbedPane.setIconAt(1, LauncherStyle.getCurrentStyle().filterHeaderIcon(this.getClass().getResource("/image/tabs/options.png")));
                            } catch (Exception e1) {
                                Logger.logError("Failed to reload settings after launcher closed", e1);
                            }
                        }
                        LaunchFrame.MCRunning = false;
                    }
                }));
                if (LaunchFrame.con != null) {
                    LaunchFrame.con.minecraftStarted();
                }
            }
        } catch (Exception e) {
            Logger.logError("Error while running launchMinecraft()", e);
        }
    }

    /**
     * @param modPackName - The pack to install (should already be downloaded)
     * @throws IOException
     */
    public static void installMods (String modPackName, boolean softUpdate) throws IOException {
        String installpath = Settings.getSettings().getInstallPath();
        String temppath = OSUtils.getCacheStorageLocation();

        ModPack pack = ModPack.getSelectedPack();
        List<String> blacklist = Lists.newArrayList();
        if (!softUpdate) {
            blacklist.add("options.txt");
        }

        String packDir = pack.getDir();

        Logger.logInfo("dirs mk'd");

        File source = new File(temppath, "ModPacks/" + packDir + "/.minecraft");
        if (!source.exists()) {
            source = new File(temppath, "ModPacks/" + packDir + "/minecraft");
        }

        Logger.logDebug("install path: " + installpath);
        Logger.logDebug("temp path: " + temppath);
        Logger.logDebug("source: " + source);
        Logger.logDebug("packDir: " + packDir);

        FTBFileUtils.copyFolder(source, new File(installpath, packDir + "/minecraft/"), blacklist);
        FTBFileUtils.copyFolder(new File(temppath, "ModPacks/" + packDir + "/instMods/"), new File(installpath, packDir + "/instMods/"));
        FTBFileUtils.copyFolder(new File(temppath, "ModPacks/" + packDir + "/libraries/"), new File(installpath, "/libraries/"), false);
    }

    private static void extractLegacyJson (File newLoc) {
        try {
            if (!new File(newLoc.getParent()).exists()) {
                new File(newLoc.getParent()).mkdirs();
            }
            if (newLoc.exists()) {
                newLoc.delete();//we want to have the current version always!!!
            }
            URL u = LaunchFrame.class.getResource("/launch/legacypack.json");
            FileUtils.copyURLToFile(u, newLoc);
        } catch (Exception e) {
            Logger.logError("Error extracting legacy json to maven directory");
        }
    }

    private static void grabJava8CompatFix (Library.Artifact forgeArtifact, ModPack pack, String packmcversion, String installBase) {
        String fgVsn = forgeArtifact.getVersion();
        String fgRelease;
        int vsn_ = 0;
        int count = StringUtils.countMatches(fgVsn, "-");
        if (count == 2) {
            // forge > 1291 has three subsection, third section is name of the branch
            // e.g. 1.7.10-10.13.2.1352-1.7.10 or
            fgRelease = fgVsn.substring((StringUtils.indexOf(fgVsn, "-") + 1), (StringUtils.lastIndexOf(fgVsn, "-")));
            fgRelease = fgRelease.substring(StringUtils.lastIndexOf(fgRelease, ".") + 1);
            vsn_ = Integer.parseInt(fgRelease);
        } else if (count == 1 || count == 0) {
            // e.g. 1.7.10-10.13.2.1291 or 9.11.1.965
            fgRelease = fgVsn.substring(StringUtils.lastIndexOf(fgVsn, ".") + 1);
            vsn_ = Integer.parseInt(fgRelease);
        }

        if (vsn_ >= Settings.getSettings().getMinJava8HackVsn() && vsn_ <= Settings.getSettings().getMaxJava8HackVsn()) {
            Logger.logDebug("adding legacyjavafixer to modpack as it is needed for this forge version to make java 8 function correctly");
            String json = "{\"url\":\"https://ftb.forgecdn.net/FTB2/maven/\",\"name\":\"net.minecraftforge.lex:legacyjavafixer:1.0\",\"checksums\":[\"a11b502bef19f49bfc199722b94da5f3d7b470a8\"]}";
            Library l = JsonFactory.loadLibrary(json);//TODO this should be pulled from the same json file
            try {//TODO we should have a method to grab a single library file to a location
                DownloadUtils.downloadToFile(installBase + "/minecraft/mods/legacyjavafixer-1.0.jar", l.getUrl() + l.getPath());
            } catch (Exception e) {
                Logger.logError("Error grabbing legacy java wrapper library", e);
            }
        }
    }
}
