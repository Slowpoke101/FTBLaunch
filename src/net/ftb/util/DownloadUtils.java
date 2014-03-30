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
package net.ftb.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;

import lombok.NonNull;
import net.ftb.data.Settings;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.dialogs.AdvancedOptionsDialog;
import net.ftb.gui.dialogs.LoadingDialog;
import net.ftb.log.Logger;

import org.apache.commons.io.IOUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DownloadUtils extends Thread {
    public volatile static boolean serversLoaded = false;
    public static HashMap<String, String> downloadServers = new HashMap<String, String>();
    public static HashMap<String, String> backupServers = new HashMap<String, String>();
    public static final String chRepo = new String("http://new.creeperrepo.net");
    public static final String curseRepo = new String("http://ftb.cursecdn.com");
    public static String masterRepo = new String("http://new.creeperrepo.net");
    public static String masterRepoNoHTTP = new String("new.creeperrepo.net");
    public static boolean primaryCH = true;

    /**
     * @param file - the name of the file, as saved to the repo (including extension)
     * @return - the direct link
     * @throws NoSuchAlgorithmException - see md5
     */
    public static String getCreeperhostLink (String file) throws NoSuchAlgorithmException {
        String resolved = (downloadServers.containsKey(Settings.getSettings().getDownloadServer())) ? "http://" + downloadServers.get(Settings.getSettings().getDownloadServer()) : masterRepo;
        resolved += "/FTB2/" + file;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(resolved).openConnection();
            for (String server : downloadServers.values()) {
                if (connection.getResponseCode() != 200) {
                    if (!server.contains("creeper")) {
                        file = file.replaceAll("%5E", "/");
                    }

                    resolved = "http://" + server + "/FTB2/" + file;
                    connection = (HttpURLConnection) new URL(resolved).openConnection();
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
     * @return - the direct link
     */
    public static String getStaticCreeperhostLink (String file) {
        String resolved = (downloadServers.containsKey(Settings.getSettings().getDownloadServer())) ? "http://" + downloadServers.get(Settings.getSettings().getDownloadServer()) : masterRepo;
        resolved += "/FTB2/static/" + file;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(resolved).openConnection();
            if (connection.getResponseCode() != 200) {
                for (String server : downloadServers.values()) {
                    if (connection.getResponseCode() != 200) {
                        resolved = "http://" + server + "/FTB2/static/" + file;
                        connection = (HttpURLConnection) new URL(resolved).openConnection();
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
            BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(getStaticCreeperhostLink(file)).openStream()));
            return !reader.readLine().toLowerCase().contains("not found");
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
            BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(masterRepo + "/FTB2/" + file).openStream()));
            return !reader.readLine().toLowerCase().contains("not found");
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
     */
    public static void downloadToFile (URL url, File file) throws IOException {
        file.getParentFile().mkdirs();
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        FileOutputStream fos = new FileOutputStream(file);
        fos.getChannel().transferFrom(rbc, 0, 1 << 24);
        fos.close();
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
        String resolved = (downloadServers.containsKey(Settings.getSettings().getDownloadServer())) ? "http://" + downloadServers.get(Settings.getSettings().getDownloadServer()) : masterRepo;
        resolved += "/md5/FTB2/" + url;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(resolved).openConnection();
            int response = connection.getResponseCode();
            if (response == 200) {
                scanner = new Scanner(connection.getInputStream());
                scanner.useDelimiter("\\Z");
                content = scanner.next();
            }
            if (response != 200 || (content == null || content.isEmpty())) {
                for (String server : backupServers.values()) {
                    resolved = "http://" + server + "/md5/FTB2/" + url.replace("/", "%5E");
                    connection = (HttpURLConnection) new URL(resolved).openConnection();
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
        return fileHash(file, "md5");
    }

    public static String fileSHA (File file) throws IOException {
        return fileHash(file, "sha1").toLowerCase();
    }

    public static String fileHash (File file, String type) throws IOException {
        if (!file.exists()) {
            return "";
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
        downloadServers.put("Automatic", masterRepoNoHTTP);
        Random r = new Random();
        double choice = r.nextDouble();
        try { // Super catch-all to ensure the launcher always renders
            try {
                // Fetch the percentage json first
                String json = IOUtils.toString(new URL(masterRepo + "/FTB2/balance.json"));
                JsonElement element = new JsonParser().parse(json);

                if (element != null && element.isJsonObject()) {
                    JsonObject jso = element.getAsJsonObject();
                    if (jso != null && jso.get("repoSplitCurse") != null) {
                        JsonElement e = jso.get("repoSplitCurse");
                        if (Settings.getSettings().getDebugLauncher()) {
                            Logger.logInfo("Balance Settings: " + e.getAsDouble() + " > " + choice);
                        }
                        if (e != null && e.getAsDouble() > choice) {
                            Logger.logInfo("Balance has selected Automatic:CurseCDN");
                            masterRepoNoHTTP = curseRepo.replaceAll("http://", "");
                            masterRepo = curseRepo;
                            primaryCH = false;
                            downloadServers.remove("Automatic");
                            downloadServers.put("Automatic", masterRepoNoHTTP);
                        } else {
                            Logger.logInfo("Balance has selected Automatic:CreeperRepo");
                        }
                    }
                }

                // Fetch servers from creeperhost using edges.json first
                parseJSONtoMap(new URL(chRepo + "/edges.json"), "CH", downloadServers, false, "edges.json");
                // Fetch servers list from curse using edges.json second
                parseJSONtoMap(new URL(curseRepo + "/edges.json"), "Curse", downloadServers, false, "edges.json");
                LoadingDialog.setProgress(80);
            } catch (IOException e) {
                int i = 10;

                // If fetching edges.json failed, assume new. is inaccessible
                // Try alternate mirrors from the cached server list in resources
                downloadServers.clear();

                Logger.logInfo("Primary mirror failed, Trying alternative mirrors");
                LoadingDialog.setProgress(i);
                parseJSONtoMap(this.getClass().getResource("/edges.json"), "Backup", downloadServers, true, "edges.json");
            }
            LoadingDialog.setProgress(90);

            if (downloadServers.size() == 0) {
                Logger.logError("Could not find any working mirrors! If you are running a software firewall please allow the FTB Launcher permission to use the internet.");

                // Fall back to new. (old system) on critical failure
                downloadServers.put("Automatic", masterRepoNoHTTP);
            } else if (!downloadServers.containsKey("Automatic")) {
                // Use a random server from edges.json as the Automatic server
                int index = (int) (Math.random() * downloadServers.size());
                List<String> keys = new ArrayList<String>(downloadServers.keySet());
                String defaultServer = downloadServers.get(keys.get(index));

                downloadServers.put("Automatic", defaultServer);
                Logger.logInfo("Selected " + keys.get(index) + " mirror for Automatic assignment");
            }
        } catch (Exception e) {
            Logger.logError(e.getMessage(), e);
            downloadServers.clear();
            downloadServers.put("Automatic", masterRepoNoHTTP);
        }

        LoadingDialog.setProgress(100);
        serversLoaded = true;

        // This line absolutely must be hit, or the console will not be shown
        // and the user/we will not even know why an error has occurred. 
        Logger.logInfo("DL ready");
        LaunchFrame.downloadServersReady();

        try {
            if (LaunchFrame.getInstance() != null && LaunchFrame.getInstance().optionsPane != null) {
                Logger.logInfo("setDL");
                AdvancedOptionsDialog.setDownloadServers();
            }
        } catch (Exception e) {
            Logger.logError("Unknown error setting download servers: " + e.getMessage());
        }

        String selectedMirror = Settings.getSettings().getDownloadServer();
        String selectedHost = downloadServers.get(selectedMirror);
        String resolvedIP = "UNKNOWN";
        String resolvedHost = "UNKNOWN";
        String resolvedMirror = "UNKNOWN";

        try {
            InetAddress ipAddress = InetAddress.getByName(selectedHost);
            resolvedIP = ipAddress.getHostAddress();
        } catch (UnknownHostException e) {
            Logger.logError("Failed to resolve selected mirror: " + e.getMessage());
        }

        try {
            for (String key : downloadServers.keySet()) {
                if (key == "Automatic")
                    continue;

                InetAddress host = InetAddress.getByName(downloadServers.get(key));

                if (resolvedIP.equalsIgnoreCase(host.getHostAddress())) {
                    resolvedMirror = key;
                    resolvedHost = downloadServers.get(key);
                    break;
                }
            }
        } catch (UnknownHostException e) {
            Logger.logError("Failed to resolve mirror: " + e.getMessage());
        }

        Logger.logInfo("Using download server " + selectedMirror + ":" + resolvedMirror + " on host " + resolvedHost + " (" + resolvedIP + ")");
    }

    @NonNull
    public void parseJSONtoMap (URL u, String name, HashMap<String, String> h, boolean testEntries, String location) {
        try {
            String json = IOUtils.toString(u);
            JsonElement element = new JsonParser().parse(json);
            int i = 10;
            if (element.isJsonObject()) {
                JsonObject jso = element.getAsJsonObject();
                for (Entry<String, JsonElement> e : jso.entrySet()) {
                    h.put(e.getKey(), e.getValue().getAsString());
                    if (testEntries) {
                        try {
                            Logger.logInfo("Testing Server:" + e.getKey());
                            IOUtils.toString(new URL("http://" + e.getValue().getAsString() + "/" + location));
                            h.put(e.getKey(), e.getValue().getAsString());
                        } catch (Exception ex) {
                            Logger.logWarn(e.getValue().getAsString().contains("creeper") ? "CreeperHost" : "Curse" + " Server: " + e.getKey() + " was not accessible, ignoring." + ex.getMessage());
                        }

                        if (i < 90)
                            i += 10;
                        LoadingDialog.setProgress(i);
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
