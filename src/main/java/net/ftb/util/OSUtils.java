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

import com.sun.jna.Library;
import com.sun.jna.Native;
import lombok.Getter;
import net.ftb.data.CommandLineSettings;
import net.ftb.data.Settings;
import net.ftb.gui.LaunchFrame;
import net.ftb.log.Logger;
import net.ftb.util.winreg.JavaFinder;
import net.ftb.util.winreg.RuntimeStreamer;
import org.apache.commons.io.FileUtils;

import java.awt.*;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.*;
import java.security.CodeSource;
import java.util.*;

import javax.swing.text.html.StyleSheet;

public class OSUtils {
    private static byte[] cachedMacAddress;
    private static String cachedUserHome;

    /**
     * gets the number of cores for use in DL threading
     *
     * @return number of cores on the system
     */
    @Getter
    private static int numCores;
    private static byte[] hardwareID;

    private static UUID clientUUID;

    private OSUtils() {
    }

    public static Proxy getProxy (String url) {
        // this is set explicitly with command line define or by our proxy setting
        String system = System.getProperty("java.net.useSystemProxies");
        // System-wide setting from java control panel or command line define
        String socks = System.getProperty("socksProxyHost");

        if (system != null && system.equals("true")) {
            Logger.logDebug("Detected system proxy");
        }
        if (socks != null && !socks.isEmpty()) {
            Logger.logDebug("Detected socks proxy");
        }

        java.util.List<Proxy> l = null;
        try {
            l = ProxySelector.getDefault().select(new URI(url));
            if (l != null) {
                for (Proxy p: l) {
                    InetSocketAddress address = (InetSocketAddress) p.address();
                    if (address == null) {
                        Logger.logDebug("ProxySelector: type: " + p.type() + ", no proxy for " + url);
                    } else {
                        Logger.logDebug("ProxySelector: type: " + p.type() + ", for " + url);
                    }
                }
                // correct? Can' decide without feedback
                return l.get(0);
            }
        } catch (Exception e) {
            Logger.logDebug("failed", e);
        }
        Logger.logWarn("Proxy was turned on but ProxySelector did not returned proxies for " + url);
        return Proxy.NO_PROXY;
    }

    public static enum OS {
        WINDOWS, UNIX, MACOSX, OTHER,
    }

    static {
        cachedUserHome = System.getProperty("user.home");
        numCores = Runtime.getRuntime().availableProcessors();
    }

    /**
     * Gets the default installation path for the current OS.
     * @return a string containing the default install path for the current OS.
     */
    public static String getDefInstallPath () {
        switch (getCurrentOS()) {
        case WINDOWS:
            String defaultLocation = "c:\\ftb";
            File testFile = new File(defaultLocation);
            // existing directory and we can write
            if (testFile.canWrite()) {
                return defaultLocation;
            }

            // We can create default directory
            if (testFile.getParentFile().canWrite()) {
                return defaultLocation;
            }
            Logger.logWarn("Can't use default installation location. Using current location of the launcher executable.");

        case MACOSX:
            return System.getProperty("user.home") + "/ftb";
        case UNIX:
            return System.getProperty("user.home") + "/ftb";
        default:
            try {
                CodeSource codeSource = LaunchFrame.class.getProtectionDomain().getCodeSource();
                File jarFile;
                jarFile = new File(codeSource.getLocation().toURI().getPath());
                return jarFile.getParentFile().getPath();
            } catch (URISyntaxException e) {
                Logger.logError("Unexcepted error", e);
            }

            return System.getProperty("user.home") + System.getProperty("path.separator") + "FTB";
        }
    }

    /**
     * Used to get the dynamic storage location based off OS
     * @return string containing dynamic storage location
     */
    public static String getDynamicStorageLocation () {
        if (CommandLineSettings.getSettings().getDynamicDir() != null && !CommandLineSettings.getSettings().getDynamicDir().isEmpty()) {
            return CommandLineSettings.getSettings().getDynamicDir();
        }
        switch (getCurrentOS()) {
        case WINDOWS:
            return System.getenv("APPDATA") + "/ftblauncher/";
        case MACOSX:
            return cachedUserHome + "/Library/Application Support/ftblauncher/";
        case UNIX:
            return cachedUserHome + "/.ftblauncher/";
        default:
            return getDefInstallPath() + "/temp/";
        }
    }

    /**
     * Used to get a location to store cached content such as maps,
     * texture packs and pack archives.
     *
     * @return string containing cache storage location
     */
    public static String getCacheStorageLocation () {
        if (CommandLineSettings.getSettings().getCacheDir() != null && !CommandLineSettings.getSettings().getCacheDir().isEmpty()) {
            return CommandLineSettings.getSettings().getCacheDir();
        }
        switch (getCurrentOS()) {
        case WINDOWS:
            if (System.getenv("LOCALAPPDATA") != null && System.getenv("LOCALAPPDATA").length() > 5) {
                return System.getenv("LOCALAPPDATA") + "/ftblauncher/";
            } else {
                return System.getenv("APPDATA") + "/ftblauncher/";
            }
        case MACOSX:
            return cachedUserHome + "/Library/Application Support/ftblauncher/";
        case UNIX:
            return cachedUserHome + "/.ftblauncher/";
        default:
            return getDefInstallPath() + "/temp/";
        }
    }

    public static void createStorageLocations () {
        File cacheDir = new File(OSUtils.getCacheStorageLocation());
        File dynamicDir = new File(OSUtils.getDynamicStorageLocation());

        if (!cacheDir.exists()) {
            cacheDir.mkdirs();

            if (dynamicDir.exists() && !cacheDir.equals(dynamicDir)) {
                // Migrate cached archives from the user's roaming profile to their local cache

                Logger.logInfo("Migrating cached Maps from Roaming to Local storage");
                FTBFileUtils.move(new File(dynamicDir, "Maps"), new File(cacheDir, "Maps"));

                Logger.logInfo("Migrating cached Modpacks from Roaming to Local storage");
                FTBFileUtils.move(new File(dynamicDir, "ModPacks"), new File(cacheDir, "ModPacks"));

                Logger.logInfo("Migrating cached Texturepacks from Roaming to Local storage");
                FTBFileUtils.move(new File(dynamicDir, "TexturePacks"), new File(cacheDir, "TexturePacks"));

                Logger.logInfo("Migration complete.");
            }
        }

        if (!dynamicDir.exists()) {
            dynamicDir.mkdirs();
        }

        if (getCurrentOS() == OS.WINDOWS) {
            File oldLoginData = new File(dynamicDir, "logindata");
            File newLoginData = new File(cacheDir, "logindata");
            try {
                if (oldLoginData.exists() && !oldLoginData.getCanonicalPath().equals(newLoginData.getCanonicalPath())) {
                    newLoginData.delete();
                }
            } catch (Exception e) {
                Logger.logError("Error deleting login data", e);
            }
        }
    }

    public static long getOSTotalMemory () {
        return getOSMemory("getTotalPhysicalMemorySize", "Could not get RAM Value");
    }

    public static long getOSFreeMemory () {
        return getOSMemory("getFreePhysicalMemorySize", "Could not get free RAM Value");
    }

    private static long getOSMemory (String methodName, String warning) {
        long ram = 0;

        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        Method m;
        try {
            m = operatingSystemMXBean.getClass().getDeclaredMethod(methodName);
            m.setAccessible(true);
            Object value = m.invoke(operatingSystemMXBean);
            if (value != null) {
                ram = Long.valueOf(value.toString()) / 1024 / 1024;
            } else {
                Logger.logWarn(warning);
                ram = 1024;
            }
        } catch (Exception e) {
            Logger.logError("Error while getting OS memory info", e);
        }

        return ram;
    }

    /**
     * Used to get the java delimiter for current OS
     * @return string containing java delimiter for current OS
     */
    public static String getJavaDelimiter () {
        switch (getCurrentOS()) {
        case WINDOWS:
            return ";";
        case UNIX:
            return ":";
        case MACOSX:
            return ":";
        default:
            return ";";
        }
    }

    /**
     * Used to get the current operating system
     * @return OS enum representing current operating system
     */
    public static OS getCurrentOS () {
        String osString = System.getProperty("os.name").toLowerCase();
        if (osString.contains("win")) {
            return OS.WINDOWS;
        } else if (osString.contains("nix") || osString.contains("nux")) {
            return OS.UNIX;
        } else if (osString.contains("mac")) {
            return OS.MACOSX;
        } else {
            return OS.OTHER;
        }
    }

    /**
     * Used to check if Windows is 64-bit
     * @return true if 64-bit Windows
     */
    public static boolean is64BitWindows () {
        String arch = System.getenv("PROCESSOR_ARCHITECTURE");
        String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
        return (arch.endsWith("64") || (wow64Arch != null && wow64Arch.endsWith("64")));
    }

    /**
     * Used to check if a posix OS is 64-bit
     * @return true if 64-bit Posix OS
     */
    public static boolean is64BitPosix () {
        String line, result = "";
        try {
            Process command = Runtime.getRuntime().exec("uname -m");
            BufferedReader in = new BufferedReader(new InputStreamReader(command.getInputStream()));
            while ((line = in.readLine()) != null) {
                result += (line + "\n");
            }
        } catch (Exception e) {
            Logger.logError("Posix bitness check failed", e);
        }
        // 32-bit Intel Linuces, it returns i[3-6]86. For 64-bit Intel, it says x86_64
        return result.contains("_64");
    }

    /**
     * Used to check if OS X is 64-bit
     * @return true if 64-bit OS X
     */

    public static boolean is64BitOSX () {
        String line, result = "";
        if (!(System.getProperty("os.version").startsWith("10.6") || System.getProperty("os.version").startsWith("10.5"))) {
            return true;//10.7+ only shipped on hardware capable of using 64 bit java
        }
        try {
            Process command = Runtime.getRuntime().exec("/usr/sbin/sysctl -n hw.cpu64bit_capable");
            BufferedReader in = new BufferedReader(new InputStreamReader(command.getInputStream()));
            while ((line = in.readLine()) != null) {
                result += (line + "\n");
            }
        } catch (Exception e) {
            Logger.logError("OS X bitness check failed", e);
        }
        return result.equals("1");
    }

    /**
     * Used to check if operating system is 64-bit
     * @return true if 64-bit operating system
     */
    public static boolean is64BitOS () {
        switch (getCurrentOS()) {
        case WINDOWS:
            return is64BitWindows();
        case UNIX:
            return is64BitPosix();
        case MACOSX:
            return is64BitOSX();
        case OTHER:
            return true;
        default:
            return true;
        }
    }

    /**
     * Used to get check if JVM is 64-bit
     * @return true if 64-bit JVM
     */
    public static Boolean is64BitVM () {
        Boolean bits64;
        if ((getCurrentOS() == OS.WINDOWS || getCurrentOS() == OS.MACOSX) && JavaFinder.parseJavaVersion() != null) {
            bits64 = JavaFinder.parseJavaVersion().is64bits;
        } else {
            bits64 = System.getProperty("sun.arch.data.model").equals("64");
        }
        return bits64;
    }

    /**
     * Used to get the OS name for use in google analytics
     * @return Linux/OSX/Windows/other/
     */
    public static String getOSString () {
        String osString = System.getProperty("os.name").toLowerCase();
        if (osString.contains("win")) {
            return "Windows";
        } else if (osString.contains("linux")) {
            return "linux";
        } else if (osString.contains("mac")) {
            return "OSX";
        } else {
            return osString;
        }
    }

    /**
     * sees if the hash of the UUID matches the one stored in the config
     * @return true if UUID matches hash or false if it does not
     */
    public static boolean verifyUUID () {
        return true;
    }

    /**
     * Grabs the mac address of computer and makes it 10 times longer
     * @return a byte array containing mac address
     */
    public static byte[] getMacAddress () {
        if (cachedMacAddress != null && cachedMacAddress.length >= 10) {
            return cachedMacAddress;
        }
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface network = networkInterfaces.nextElement();
                byte[] mac = network.getHardwareAddress();
                if (mac != null && mac.length > 0 && !network.isLoopback() && !network.isVirtual() && !network.isPointToPoint() && network.getName().substring(0,3) != "ham") {
                    Logger.logDebug("Interface: " + network.getDisplayName() + " : " + network.getName());
                    cachedMacAddress = new byte[mac.length * 10];
                    for (int i = 0; i < cachedMacAddress.length; i++) {
                        cachedMacAddress[i] = mac[i - (Math.round(i / mac.length) * mac.length)];
                    }
                    return cachedMacAddress;
                }
            }
        } catch (SocketException e) {
            Logger.logWarn("Exception getting MAC address", e);
        }

        Logger.logWarn("Failed to get MAC address, using default logindata key");
        return new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };
    }

    /**
     *
     * @return Unique Id based on hardware
     */
    public static byte[] getHardwareID () {
        if (hardwareID == null) {
            hardwareID = genHardwareID();
        }
        return hardwareID;
    }

    private static byte[] genHardwareID () {
        switch (getCurrentOS()) {
        case WINDOWS:
            return genHardwareIDWINDOWS();
        case UNIX:
            return genHardwareIDUNIX();
        case MACOSX:
            return genHardwareIDMACOSX();
        default:
            return null;
        }
    }

    private static byte[] genHardwareIDUNIX () {
        String line;
        if (CommandLineSettings.getSettings().isUseMac()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader("/etc/machine-id"));
                line = reader.readLine();
            } catch (Exception e) {
                Logger.logDebug("failed", e);
                return new byte[] { };
            }
            return line.getBytes();
        } else {
            return new byte[] { };
        }
    }

    private static byte[] genHardwareIDMACOSX () {
        String line;
        try {
            Process command = Runtime.getRuntime().exec(new String[] { "system_profiler", "SPHardwareDataType" });
            BufferedReader in = new BufferedReader(new InputStreamReader(command.getInputStream()));
            while ((line = in.readLine()) != null) {
                if (line.contains("Serial Number"))
                //TODO: does that more checks?
                {
                    return line.split(":")[1].trim().getBytes();
                }
            }
            return new byte[] { };
        } catch (Exception e) {
            Logger.logDebug("failed", e);
            return new byte[] { };
        }
    }

    private static byte[] genHardwareIDWINDOWS () {
        String processOutput;
        try {
            processOutput = RuntimeStreamer.execute(new String[] { "wmic", "bios", "get", "serialnumber" });
            /*
             * wmic's output has special formatting:
             * SerialNumber<SP><SP><SP><CR><CR><LF>
             * 00000000000000000<SP><CR><CR><LF><CR><CR><LF>
             *
             * readLin()e uses <LF>, <CR> or <CR><LF> as line ending => we need to get third line from RuntimeStreamers output
             */
            String line = processOutput.split("\n")[2].trim();
            // at least VM will report serial to be 0. Does real hardware do it?
            if (line.equals("0")) {
                return new byte[] { };
            } else {
                return line.trim().getBytes();
            }
        } catch (Exception e) {
            Logger.logDebug("failed", e);
            return new byte[] { };
        }
    }

    /**
     * Opens the given URL in the default browser
     * @param url The URL
     */
    public static void browse (String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url.replace(" ", "+")));
            } else if (getCurrentOS() == OS.UNIX && (new File("/usr/bin/xdg-open").exists() || new File("/usr/local/bin/xdg-open").exists())) {
                // Work-around to support non-GNOME Linux desktop environments with xdg-open installed
                new ProcessBuilder("xdg-open", url).start();
            } else {
                Logger.logWarn("Could not open Java Download url, not supported");
            }
        } catch (Exception e) {
            Logger.logError("Could not open link: " + url, e);
        }
    }

    /**
     * Opens the given path with the default application
     * @param path The path
     */
    public static void open (File path) {
        if (!path.exists()) {
            return;
        }
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(path);
            } else if (getCurrentOS() == OS.UNIX) {
                // Work-around to support non-GNOME Linux desktop environments with xdg-open installed
                if (new File("/usr/bin/xdg-open").exists() || new File("/usr/local/bin/xdg-open").exists()) {
                    new ProcessBuilder("xdg-open", path.toString()).start();
                }
            }
        } catch (Exception e) {
            Logger.logError("Could not open file", e);
        }
    }

    /**
     * @return if java 7+ can be ran on that version of osx
     */
    public static boolean canRun7OnMac () {
        return getCurrentOS() == OS.MACOSX && !(System.getProperty("os.version").startsWith("10.6") || System.getProperty("os.version").startsWith("10.5"));
    }

    /**
     * Removes environment variables which may cause faulty JVM memory allocations
     */
    public static void cleanEnvVars (Map<String, String> environment) {
        environment.remove("_JAVA_OPTIONS");
        environment.remove("JAVA_TOOL_OPTIONS");
        environment.remove("JAVA_OPTIONS");
    }

    public static StyleSheet makeStyleSheet (String name) {
        try {
            StyleSheet sheet = new StyleSheet();
            Reader reader = new InputStreamReader(System.class.getResourceAsStream("/css/" + name + ".css"));
            sheet.loadRules(reader, null);
            reader.close();

            return sheet;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static UUID getClientToken() {
        if (clientUUID != null) {
            return clientUUID;
        } else {
            String s = null;

            File tokenFile = new File(getCacheStorageLocation() + File.separator + "clientToken");
            if (tokenFile.exists() && tokenFile.isFile()) {
                try {
                    s = FileUtils.readFileToString(tokenFile);
                } catch (IOException e) {
                    s = null;
                    Logger.logError("Client token read failed:", e);
                }
            }

            if (s != null) {
                try {
                    clientUUID = UUID.fromString(s);
                } catch (IllegalArgumentException e) {
                    Logger.logError("Client token read failed", e);
                    clientUUID = createUUID();
                }
            } else {
                clientUUID = createUUID();
            }
            return clientUUID;
        }
    }

    public static void setClientToken(UUID u) {
        File tokenFile = new File(getCacheStorageLocation() + File.separator + "clientToken");
        try {
            FileUtils.writeStringToFile(tokenFile, u.toString());
        } catch (IOException e) {
            Logger.logError("Client token write failed", e);
        }
    }

    private static UUID createUUID() {
        UUID u =  UUID.randomUUID();
        setClientToken(u);
        return u;
    }

    /**
     *
     * @return pid of the running process. -1 if fail
     */
    public static long getPID () {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        String pid = name.split("@")[0];
        long numericpid = -1;
        try {
            numericpid = Long.parseLong(pid);
        } catch (Exception e) {
            numericpid = -1;
            Logger.logDebug("failed", e);
        }
        return numericpid;
    }

    public static long getPID (Process process) {
        // windows
        if (getCurrentOS()==OS.WINDOWS && (process.getClass().getName().equals("java.lang.Win32Process") ||
                process.getClass().getName().equals("java.lang.ProcessImpl"))) {
            long pid = -1;
            try {
                Field f = process.getClass().getDeclaredField("handle");
                f.setAccessible(true);
                pid = Kernel32.INSTANCE.GetProcessId((Long) f.get(process));

            } catch (Exception e) {
                pid = -1;
                Logger.logDebug("failed", e);
            }

            return pid;
        }

        // java 9 removes java.lang.UNIXProcess and uses revised java.lang.ProcessImple which includes field pid
        // http://openjdk.java.net/jeps/102
        if (process.getClass().getName().equals("java.lang.UNIXProcess") || process.getClass().getName().equals("java.lang.ProcessImpl")) {
        /* get the PID on unix/linux systems */
            long pid = -1;
            try {
                Field f = process.getClass().getDeclaredField("pid");
                f.setAccessible(true);
                pid = f.getInt(process);

            } catch (Throwable e) {
                pid = -1;
                Logger.logDebug("failed", e);
            }
            return pid;
        }

        Logger.logWarn("Unable to find getpid implementation");
        return -1;
    }

    public static boolean genThreadDump(long pid) {
        if (OSUtils.getCurrentOS()==OS.WINDOWS) {
            File directory = null;
            File sendsignal = null;
            try {
                directory = new File(OSUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile();
            } catch (Exception e) {
                Logger.logDebug("failed" , e);
            }

            if (directory != null && directory.exists() && directory.isDirectory()) {
                sendsignal = new File(directory, "sendsignal.exe");
                if (!sendsignal.exists()) {
                    // try to download file automatically
                    try {
                        Logger.logInfo("Downloading sendsignal.exe");
                        String address;
                        if (is64BitOS()) {
                            address = DownloadUtils.getCreeperhostLink("launcher/tools/sendsignal.exe ");
                        } else {
                            address = DownloadUtils.getCreeperhostLink("launcher/tools/sendsignal32.exe ");
                        }
                        DownloadUtils.downloadToFile(sendsignal.getCanonicalPath(), address);
                    } catch (Exception e) {
                        Logger.logDebug("failed" , e);
                    }
                }
            }

            // Now file is downloaded by the launcher or user. Try to run sendsignal.exe from %path% or %cd%
            try {
                Runtime runtime = Runtime.getRuntime();
                runtime.exec(new String[] { "sendsignal.exe", Long.toString(pid) });
            } catch (Exception e) {
                Logger.logError("Failed. You need to install sendsignal.exe in your path to eanble this functionality");
                Logger.logError("Failed", e);
                return false;
            }
            return true;
        } else if (OSUtils.getCurrentOS()==OS.UNIX || OSUtils.getCurrentOS()==OS.MACOSX) {
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec(new String[] { "kill", "-3", Long.toString(pid) });
            } catch (Exception e) {
                Logger.logError("Failed", e);
                return false;
            }
            return true;
        } else {
            Logger.logError("Unable to find genThreadDump implementation");
            return false;
        }
    }

    public static void printGPUinformation() {
        if (getCurrentOS() == OS.WINDOWS) {
            String result = RuntimeStreamer.execute(new String[] { "wmic", "path", "win32_VideoController",
                    "get", "description,adapterRAM,driverDate,DriverVersion,InstalledDisplayDrivers"
            });
            if (result != null) {
                Logger.logDebug("GPU information:\n" + result.replace("\n\n", "\n").trim().replaceAll("[ ]*\n", "\n"));
            } else {
                Logger.logError("Getting GPU information failed!! Launcher has detected windows path environment variable issues, "
                        + "please see http://support.feed-the-beast.com/t/windows-path-issues/19630 for more information.");
            }
        } else {
            // not implemented yet
        }
    }

    static interface Kernel32 extends Library {
        public static Kernel32 INSTANCE = (Kernel32) Native.loadLibrary("kernel32", Kernel32.class);
        public int GetProcessId (Long hProcess);
    }
}