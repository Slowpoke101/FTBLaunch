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
package net.ftb.util;

import static com.google.common.net.HttpHeaders.CACHE_CONTROL;
import static net.ftb.download.Locations.backupServers;
import static net.ftb.download.Locations.downloadServers;

import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.NonNull;
import net.ftb.data.Settings;
import net.ftb.download.Locations;
import net.ftb.gui.LaunchFrame;
import net.ftb.log.Logger;
import org.apache.commons.io.IOUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;

import javax.imageio.ImageIO;

public class DownloadUtils extends Thread {

    /**
     * @param file - the name of the file, as saved to the repo (including extension)
     * @return - the direct link
     */
    public static String getCreeperhostLink (String file) {
        String resolved = (downloadServers.containsKey(Settings.getSettings().getDownloadServer())) ? "http://" + downloadServers.get(Settings.getSettings().getDownloadServer())
                : Locations.masterRepo;
        resolved += "/FTB2/" + file;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(resolved).openConnection();
            connection.setRequestProperty(CACHE_CONTROL, "no-transform");
            connection.setRequestMethod("HEAD");
            for (String server : downloadServers.values()) {
                // TODO: should we return null or "" or raise Exception when getting 404 from  server? Otherwise it loops through all servers
                if (connection.getResponseCode() != 200) {
                    Logger.logDebug("failed");
                    AppUtils.debugConnection(connection);
                    resolved = "http://" + server + "/FTB2/" + file;
                    connection = (HttpURLConnection) new URL(resolved).openConnection();
                    connection.setRequestProperty(CACHE_CONTROL, "no-transform");
                    connection.setRequestMethod("HEAD");
                } else {
                    break;
                }
            }
        } catch (IOException e) {
        }
        connection.disconnect();
        return resolved;
    }

    /**
     * @param file - the name of the file, as saved to the repo (including extension)
     * @param backupLink - the link of the location to backup to if the repo copy isn't found
     * @return - the direct static link or the backup link if the file isn't found
     */
    public static String getStaticCreeperhostLinkOrBackup (String file, String backupLink) {
        String resolved = (downloadServers.containsKey(Settings.getSettings().getDownloadServer())) ? "http://" + downloadServers.get(Settings.getSettings().getDownloadServer())
                : Locations.masterRepo;
        resolved += "/FTB2/static/" + file;
        HttpURLConnection connection = null;
        boolean good = false;
        try {
            connection = (HttpURLConnection) new URL(resolved).openConnection();
            connection.setRequestProperty(CACHE_CONTROL, "no-transform");
            connection.setRequestMethod("GET");
            for (String server : downloadServers.values()) {
                if (connection.getResponseCode() != 200) {
                    Logger.logDebug("failed");
                    // TODO: remove responseCode test later.
                    AppUtils.debugConnection(connection, connection.getResponseCode()!=404);
                    resolved = "http://" + server + "/FTB2/static/" + file;
                    connection = (HttpURLConnection) new URL(resolved).openConnection();
                    connection.setRequestProperty(CACHE_CONTROL, "no-transform");
                    connection.setRequestMethod("HEAD");
                } else {
                    good = true;
                    break;
                }
            }
        } catch (IOException e) {
        }
        connection.disconnect();
        if (good) {
            return resolved;
        } else {
            Logger.logWarn("Using backupLink for " + file);
            if (!file.contains("1.8")) {
                // FTB hosts own version.json fails. If we are here something failed. Why?
                Logger.logError("GET request for " + file + " failed. Please Send log to launcher team and provide your public IP address if possible.");
                TrackerUtils.sendPageView("getStaticCreeperhostLinkOrBackup", "GET_failed: " + file);
        }
            return backupLink;
        }
    }

    /**
     * @param file - the name of the file, as saved to the repo (including extension)
     * @return - the direct link
     */
    public static String getStaticCreeperhostLink (String file) {
        String resolved = (downloadServers.containsKey(Settings.getSettings().getDownloadServer())) ? "http://" + downloadServers.get(Settings.getSettings().getDownloadServer())
                : Locations.masterRepo;
        resolved += "/FTB2/static/" + file;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(resolved).openConnection();
            connection.setRequestProperty(CACHE_CONTROL, "no-transform");
            connection.setRequestMethod("HEAD");
            if (connection.getResponseCode() != 200) {
                for (String server : downloadServers.values()) {
                    if (connection.getResponseCode() != 200) {
                        Logger.logDebug("failed");
                        AppUtils.debugConnection(connection);
                        resolved = "http://" + server + "/FTB2/static/" + file;
                        connection = (HttpURLConnection) new URL(resolved).openConnection();
                        connection.setRequestProperty(CACHE_CONTROL, "no-transform");
                        connection.setRequestMethod("HEAD");
                    } else {
                        break;
                    }
                }
            }
        } catch (IOException e) {
        }
        connection.disconnect();
        return resolved;
    }

    /**
     * @param file - file on the repo in static
     * @return boolean representing if the file exists 
     */
    public static boolean staticFileExists (String file) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(getStaticCreeperhostLink(file)).openConnection();
            connection.setRequestProperty(CACHE_CONTROL, "no-transform");
            connection.setRequestMethod("HEAD");
            return (connection.getResponseCode() == 200);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @param file - file on the repo
     * @return boolean representing if the file exists 
     */
    public static boolean fileExists (String file) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(Locations.masterRepo + "/FTB2/" + file).openConnection();
            connection.setRequestProperty(CACHE_CONTROL, "no-transform");
            connection.setRequestMethod("HEAD");
            return (connection.getResponseCode() == 200);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @param url for file
     * @return true if file is found
     */
    public static boolean fileExistsURL (String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty(CACHE_CONTROL, "no-transform");
            connection.setRequestMethod("HEAD");
            int code = connection.getResponseCode();
            return (code == 200);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @param repoURL - URL on the repo
     * @param fullDebug - should this dump the full cloudflare debug info in the console
     * @return boolean representing if the file exists
     */
    public static boolean CloudFlareInspector (String repoURL, boolean fullDebug) {
        try {
            boolean ret;
            HttpURLConnection connection = (HttpURLConnection) new URL(repoURL + "cdn-cgi/trace").openConnection();
            if (!fullDebug) {
                connection.setRequestMethod("HEAD");
            }
            Logger.logDebug("CF-RAY: " + connection.getHeaderField("CF-RAY"));
            if (fullDebug) {
                Logger.logDebug("CF Debug Info: \n" + IOUtils.toString(connection.getInputStream()));
            }
            ret = connection.getResponseCode() == 200;
            IOUtils.close(connection);
            return ret;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Downloads data from the given URL and saves it to the given file
     * @param filename - String of destination
     * @param urlString - http location of file to download
     */
    public static void downloadToFile (String filename, String urlString) throws IOException {
        downloadToFile(new URL(urlString), new File(filename));
    }

    /**
     * Downloads data from the given URL and saves it to the given file
     * @param url The url to download from
     * @param file The file to save to.
     *
     * TODO: how to handle partial downloads? Old file is overwritten as soon as FileOutputStream is created.
     *       how to handle headers? in some cases we want to print those and in other we don't
     */
    public static void downloadToFile (URL url, File file) throws IOException {
        file.getParentFile().mkdirs();
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        FileOutputStream fos = new FileOutputStream(file);
        fos.getChannel().transferFrom(rbc, 0, 1 << 24);
        fos.close();
    }

    /**
     * Download data from the given URL and saves it to the given file, tries to download attempts times
     * @param url The url to download from
     * @param file The file to save to
     * @param attempts attempts to download file if downloadToFile(URL url, File file) fails
     */
    public static void downloadToFile (URL url, File file, int attempts) {
        int attempt = 0;
        boolean success = false;
        Exception reason = null;
        while ((attempt < attempts) && !success) {
            try {
                success = true;
                DownloadUtils.downloadToFile(url, file);
            } catch (Exception e) {
                success = false;
                reason = e;
                attempt++;
            }
            if (attempt == attempts && !success) {
                Logger.logError("library JSON download failed", reason);
                //TODO: check fail reason and delete malformed JSON
                return;
            }
        }
    }

    /**
     * Used to download pack images from repo to hard disk
     * @param file Name of the image
     * @param location Image save location in hard disk
     * @param type image type to use when saving
     */
    public static void saveImage (String file, File location, String type) {
        // stupid code: tries to find working server twice.
        if (DownloadUtils.staticFileExists(file)) {
            try {
                URL url_ = new URL(DownloadUtils.getStaticCreeperhostLink(file));
                BufferedImage tempImg = ImageIO.read(url_);
                ImageIO.write(tempImg, type, new File(location, file));
                tempImg.flush();
            } catch (IOException e) {
                Logger.logWarn("image download/save failed", e);
                new File(location, file).delete();
            }
        }
    }

    /**
     * Checks the file for corruption.
     * @param file - File to check
     * @param md5 - remote MD5 to compare against
     * @return boolean representing if it is valid
     * @throws IOException
     */
    public static boolean isValid (File file, String md5) throws IOException {
        String result = fileMD5(file);
        Logger.logInfo("Local: " + result.toUpperCase());
        Logger.logInfo("Remote: " + md5.toUpperCase());
        return md5.equalsIgnoreCase(result);
    }

    /**
     * Checks the file for corruption.
     * @param file - File to check
     * @param url - base url to grab md5 with old method
     * @return boolean representing if it is valid
     * @throws IOException
     */
    public static boolean backupIsValid (File file, String url) throws IOException {
        Logger.logInfo("Issue with new md5 method, attempting to use backup method.");
        String content = null;
        Scanner scanner = null;
        //String resolved = (downloadServers.containsKey(Settings.getSettings().getDownloadServer())) ? "http://" + downloadServers.get(Settings.getSettings().getDownloadServer()) : Locations.masterRepo;

        // Only curse has /md5/ do not try to use creeperrepo even if user has selected it
        String resolved = Locations.curseRepo;
        resolved += "/md5/FTB2/" + url;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(resolved).openConnection();
            connection.setRequestProperty(CACHE_CONTROL, "no-transform");
            int response = connection.getResponseCode();
            if (response == 200) {
                scanner = new Scanner(connection.getInputStream());
                scanner.useDelimiter("\\Z");
                content = scanner.next();
            }
            if (response != 200 || (content == null || content.isEmpty())) {
                for (String server : backupServers.values()) {
                    resolved = "http://" + server + "/md5/FTB2/" + url;
                    connection = (HttpURLConnection) new URL(resolved).openConnection();
                    connection.setRequestProperty(CACHE_CONTROL, "no-transform");
                    response = connection.getResponseCode();
                    if (response == 200) {
                        scanner = new Scanner(connection.getInputStream());
                        scanner.useDelimiter("\\Z");
                        content = scanner.next();
                        if (content != null && !content.isEmpty()) {
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
        } finally {
            connection.disconnect();
            if (scanner != null) {
                scanner.close();
            }
        }
        String result = fileMD5(file);
        Logger.logInfo("Local: " + result.toUpperCase());
        Logger.logInfo("Remote: " + content.toUpperCase());
        return content.equalsIgnoreCase(result);
    }

    /**
     * Gets the md5 of the downloaded file
     * @param file - File to check
     * @return - string of file's md5
     * @throws IOException
     */
    public static String fileMD5 (File file) throws IOException {
        if (file.exists()) {
            return Files.hash(file, Hashing.md5()).toString();
        } else {
            return "";
        }
    }

    public static String fileSHA (File file) throws IOException {
        if (file.exists()) {
            return Files.hash(file, Hashing.sha1()).toString();
        } else {
            return "";
        }
    }

    public static String fileHash (File file, String type) throws IOException {
        if (!file.exists()) {
            return "";
        }
        if (type.equalsIgnoreCase("md5")) {
            return fileMD5(file);
        }
        if (type.equalsIgnoreCase("sha1")) {
            return fileSHA(file);
        }
        URL fileUrl = file.toURI().toURL();
        MessageDigest dgest = null;
        try {
            dgest = MessageDigest.getInstance(type);
        } catch (NoSuchAlgorithmException e) {
        }
        InputStream str = fileUrl.openStream();
        byte[] buffer = new byte[65536];
        int readLen;
        while ((readLen = str.read(buffer, 0, buffer.length)) != -1) {
            dgest.update(buffer, 0, readLen);
        }
        str.close();
        Formatter fmt = new Formatter();
        for (byte b : dgest.digest()) {
            fmt.format("%02X", b);
        }
        String result = fmt.toString();
        fmt.close();
        return result;
    }

    /**
     * Used to load all available download servers in a thread to prevent wait.
     */
    @Override
    public void run () {
        boolean bothReposFailed = false;
        boolean curseFailed = false;
        boolean creeperFailed = false;
        setName("DownloadUtils");
        // test for proxies
        OSUtils.getProxy(Locations.curseRepo);
        OSUtils.getProxy(Locations.chRepo);
        if (!Locations.hasDLInitialized) {
            Benchmark.start("DlUtils");
            Logger.logDebug("DownloadUtils.run() starting");
            downloadServers.put("Automatic", Locations.masterRepoNoHTTP);
            Random r = new Random();
            double choice = r.nextDouble();
            try { // Super catch-all to ensure the launcher always renders
                String json = null;
                // Fetch the percentage json first
                try {
                    json = IOUtils.toString(new URL(Locations.curseRepo + "/FTB2/static/balance.json"));
                } catch (IOException e) {
                    curseFailed = true;
                }
                Benchmark.logBenchAs("DlUtils", "Download Utils Balance (curse)");

                if (curseFailed) {
                    try {
                        json = IOUtils.toString(new URL(Locations.chRepo + "/FTB2/static/balance.json"));
                    } catch (IOException e) {
                        creeperFailed = true;
                        bothReposFailed = true;
                    }
                    Benchmark.logBenchAs("DlUtils", "Download Utils Balance (creeper)");
                }

                // ok we got working balance.json
                if (!bothReposFailed) {
                    // should we catch network failures here and try to fetch balance from creeperrepo
                    // and if it also fails we can automatically start parsing hardcoded edges.json
                    JsonElement element = new JsonParser().parse(json);

                    if (element != null && element.isJsonObject()) {
                        JsonObject jso = element.getAsJsonObject();
                        if (jso != null && jso.get("minUsableLauncherVersion") != null) {
                            LaunchFrame.getInstance().minUsable = jso.get("minUsableLauncherVersion").getAsInt();
                        }
                        if (jso != null && jso.get("chEnabled") != null) {
                            Locations.chEnabled = jso.get("chEnabled").getAsBoolean();
                        }
                        if (jso != null && jso.get("repoSplitCurse") != null && Locations.chEnabled) {
                            JsonElement e = jso.get("repoSplitCurse");
                            Logger.logDebug("Balance Settings: " + e.getAsDouble() + " > " + choice);
                            if (e != null && e.getAsDouble() > choice) {
                                Logger.logInfo("Balance has selected Automatic:CurseCDN");
                            } else {
                                Logger.logInfo("Balance has selected Automatic:CreeperRepo");
                                Locations.masterRepoNoHTTP = Locations.chRepo.replaceAll("http://", "");
                                Locations.masterRepo = Locations.chRepo;
                                Locations.primaryCH = true;
                                downloadServers.remove("Automatic");
                                downloadServers.put("Automatic", Locations.masterRepoNoHTTP);
                            }
                        }
                    }
                    Benchmark.logBenchAs("DlUtils", "Download Utils Balance");
                    if (Locations.chEnabled) {
                        // Fetch servers from creeperhost using edges.json first
                        parseJSONtoMap(new URL(Locations.chRepo + "/edges.json"), "CH", downloadServers, false, "edges.json");
                        Benchmark.logBenchAs("DlUtils", "Download Utils CH edges.json");
                    }
                    // Fetch servers list from curse using edges.json second
                    parseJSONtoMap(new URL(Locations.curseRepo + "/edges.json"), "Curse", downloadServers, false, "edges.json");
                    Benchmark.logBenchAs("DlUtils", "Download Utils Curse edges.json");

                } else {
                    //both repos failed. use builtin edges.json, remove previously selected Automatic entry
                    downloadServers.clear();
                    Logger.logWarn("Primary mirror failed, Trying alternative mirrors");
                    parseJSONtoMap(this.getClass().getResource("/edges.json"), "Backup", downloadServers, true, "edges.json");
                    Benchmark.logBenchAs("DlUtils", "Download Utils Builtin servers tested");
                }

                if (downloadServers.size() == 0) {
                    // only if previous else block was executed and did not find working server. (e.g. network is down)
                    Logger.logError("Could not find any working mirrors! If you are running a software firewall please allow the FTB Launcher permission to use the internet.");

                    // Fall back to new. (old system) on critical failure
                    downloadServers.put("Automatic", Locations.masterRepoNoHTTP);
                } else if (!downloadServers.containsKey("Automatic")) {
                    // only if previous else block found working servers
                    // Use a random server from builtin edges.json as the Automatic server
                    int index = (int) (Math.random() * downloadServers.size());
                    List<String> keys = Lists.newArrayList(downloadServers.keySet());
                    String defaultServer = downloadServers.get(keys.get(index));
                    downloadServers.put("Automatic", defaultServer);
                    Logger.logInfo("Selected " + keys.get(index) + " mirror for Automatic assignment");
                }
            } catch (Exception e) {
                Logger.logError("Error while selecting server", e);
                downloadServers.clear();
                downloadServers.put("Automatic", Locations.masterRepoNoHTTP);
            }

            Locations.serversLoaded = true;

            // This line absolutely must be hit, or the console will not be shown
            // and the user/we will not even know why an error has occurred. 
            Logger.logDebug("DL ready");

            String selectedMirror = Settings.getSettings().getDownloadServer();
            String selectedHost = downloadServers.get(selectedMirror);
            String resolvedIP = "UNKNOWN";
            String resolvedHost = "UNKNOWN";
            String resolvedMirror = "UNKNOWN";

            try {
                InetAddress ipAddress = InetAddress.getByName(selectedHost);
                resolvedIP = ipAddress.getHostAddress();
            } catch (UnknownHostException e) {
                Logger.logWarn("Failed to resolve selected mirror: " + e.getMessage());
            }

            try {
                for (String key : downloadServers.keySet()) {
                    if (key.equals("Automatic")) {
                        continue;
                    }

                    InetAddress host = InetAddress.getByName(downloadServers.get(key));

                    if (resolvedIP.equalsIgnoreCase(host.getHostAddress())) {
                        resolvedMirror = key;
                        resolvedHost = downloadServers.get(key);
                        break;
                    }
                }
            } catch (UnknownHostException e) {
                Logger.logWarn("Failed to resolve mirror: " + e.getMessage());
            }

            Logger.logInfo("Using download server " + selectedMirror + ":" + resolvedMirror + " on host " + resolvedHost + " (" + resolvedIP + ")");
            Benchmark.logBenchAs("DlUtils", "Download Utils Init");
        }
        Locations.hasDLInitialized = true;
    }

    /**
     * method to parse & test if needed server listing
     * @param u - URL of file to download & parse
     * @param name - json server's nickname for use in error reports
     * @param h - map to be written to
     * @param testEntries - should the locations be tested?
     * @param location - location to test on the repo ex: edges.json would test ${repoURL}/edges.json
     */
    @NonNull
    public void parseJSONtoMap (URL u, String name, HashMap<String, String> h, boolean testEntries, String location) {
        try {
            String json = IOUtils.toString(u);
            JsonElement element = new JsonParser().parse(json);
            int i = 10;
            if (element.isJsonObject()) {
                JsonObject jso = element.getAsJsonObject();
                for (Entry<String, JsonElement> e : jso.entrySet()) {
                    if (testEntries) {
                        //TODO: this should  be threaded or at least use sensible timeout for connect()
                        try {
                            Logger.logDebug("Testing Server:" + e.getKey());
                            //test that the server will properly handle file DL's if it doesn't throw an error the web daemon should be functional
                            IOUtils.toString(new URL("http://" + e.getValue().getAsString() + "/" + location));
                            h.put(e.getKey(), e.getValue().getAsString());
                        } catch (Exception ex) {
                            Logger.logWarn((e.getValue().getAsString().contains("creeper") ? "CreeperHost" : "Curse") + " Server: " + e.getKey() + " was not accessible, ignoring." + ex.getMessage());
                        }

                        if (i < 90) {
                            i += 10;
                        }
                    } else {
                        h.put(e.getKey(), e.getValue().getAsString());
                    }
                }
            }
        } catch (Exception e2) {
            Logger.logError("Error parsing JSON " + name + " " + location, e2);
        }
    }
}
