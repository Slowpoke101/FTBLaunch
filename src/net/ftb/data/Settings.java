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
package net.ftb.data;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import lombok.Getter;
import lombok.Setter;
import net.ftb.gui.LaunchFrame;
import net.ftb.log.Logger;
import net.ftb.util.ErrorUtils;
import net.ftb.util.OSUtils;
import net.ftb.util.OSUtils.OS;
import net.ftb.util.winreg.JavaFinder;
import net.ftb.util.winreg.JavaInfo;

@SuppressWarnings("serial")
public class Settings extends Properties {
    @Getter
    private static Settings settings;
    private File configFile;
    @Getter
    @Setter
    private boolean forceUpdateEnabled = false;

    static {
        try {
            settings = new Settings(new File(OSUtils.getDynamicStorageLocation(), "ftblaunch.cfg"));
        } catch (IOException e) {
            Logger.logError("Failed to load settings", e);
        }
    }

    public Settings(File file) throws IOException {
        configFile = file;
        if (file.exists()) {
            load(new FileInputStream(file));
        } else {
            LaunchFrame.noConfig = true;
        }
    }

    public void save () {
        try {
            store(new FileOutputStream(configFile), "FTBLaunch Config File");
        } catch (IOException e) {
            Logger.logError("Failed to save settings", e);
        }
    }

    public String getRamMax () {
        if (getJavaVersion().is64bits && OSUtils.getOSTotalMemory() > 6144)//6gb or more default to 2gb of ram for MC
            return getProperty("ramMax", Integer.toString(2048));
        else if (getJavaVersion().is64bits)//on 64 bit java default to 1.5gb newer pack's need more than a gig
            return getProperty("ramMax", Integer.toString(1536));
        return getProperty("ramMax", Integer.toString(1024));
    }

    public void setRamMax (String max) {
        setProperty("ramMax", max);
    }

    public String getLastUser () {
        return getProperty("lastUser", null);
    }

    public void setLastUser (String user) {
        setProperty("lastUser", user);
    }

    public String getInstallPath () {
        return getProperty("installPath", OSUtils.getDefInstallPath());
    }

    public void setInstallPath (String path) {
        setProperty("installPath", path);
    }

    public String getJavaPath () {
        String javaPath = getProperty("javaPath", getDefaultJavaPath());
        if (javaPath == null || !new File(javaPath).isFile())
            ErrorUtils.tossError("Unable to find java; point to java executable file in Advanced Options or game will fail to launch.");
        return javaPath;
    }

    /**
     * Returns user selected or automatically selected JVM's
     * JavaInfo object.
     */
    public JavaInfo getJavaVersion () {
        JavaInfo javaVersion = new JavaInfo(getJavaPath());
        return javaVersion;
        //return new int[]{javaVersion.getMajor(), javaVersion.getMinor()};
    }

    private String getDefaultJavaPath () {
        String separator = System.getProperty("file.separator");
        String defaultPath = null;
        JavaInfo javaVersion;

        if (OSUtils.getCurrentOS() == OS.MACOSX) {
            javaVersion = JavaFinder.parseJavaVersion();

            if (javaVersion != null && javaVersion.path != null)
                return javaVersion.path;
        } else if (OSUtils.getCurrentOS() == OS.WINDOWS) {
            javaVersion = JavaFinder.parseJavaVersion();

            if (javaVersion != null && javaVersion.path != null)
                return javaVersion.path.replace(".exe", "w.exe");
        }

        return System.getProperty("java.home") + ("/bin/java" + (OSUtils.getCurrentOS() == OS.WINDOWS ? "w" : "")).replace("/", separator);
    }

    public void setJavaPath (String path) {
        if (getDefaultJavaPath().equals(path) || path.isEmpty()) {
            remove("javaPath");
        } else {
            setProperty("javaPath", path);
        }
    }

    public String getStyle () {
        return getProperty("style", "defaultStyle.cfg");
    }

    public void setStyle (String path) {
        setProperty("style", path);
    }

    public void setConfigFile (File path) {
        configFile = path;
    }

    public String getLocale () {
        return getProperty("locale", "enUS");
    }

    public void setLocale (String locale) {
        setProperty("locale", locale);
    }

    public File getConfigFile () {
        return configFile;
    }

    public void setLastPack (String name) {
        setProperty("lastPack", name);
    }

    public String getLastPack () {
        return getProperty("lastPack", ModPack.getPack(0).getDir());
    }

    public void setDownloadServer (String server) {
        setProperty("downloadServer", server);
    }

    public String getDownloadServer () {
        return getProperty("downloadServer", "Automatic");
    }

    public void setConsoleActive (boolean console) {
        setProperty("consoleActive", String.valueOf(console));
    }

    public boolean getConsoleActive () {
        return Boolean.valueOf(getProperty("consoleActive", "true"));
    }

    public void setOptJavaArgs (boolean console) {
        setProperty("optJavaArgs", String.valueOf(console));
    }

    public boolean getOptJavaArgs () {
        return Boolean.valueOf(getProperty("optJavaArgs", "false"));
    }

    public void setPackVer (String string) {
        setProperty(ModPack.getSelectedPack().getDir(), string);
    }

    public String getPackVer () {
        return getProperty(ModPack.getSelectedPack().getDir(), "Recommended Version");
    }

    public String getPackVer (String packDir) {
        return getProperty(packDir, "Recommended Version");
    }

    public String getLastAddPath () {
        return getProperty("lastAddPath", "");
    }

    public void setLastAddPath (String string) {
        setProperty("lastAddPath", string);
    }

    public void addPrivatePack (String code) {
        if (code == null || code.isEmpty()) {
            return;
        }
        if (getProperty("privatePacks") != null) {
            ArrayList<String> packList = getPrivatePacks();
            if (!packList.contains(code)) {
                packList.add(code);
                setPrivatePacks(packList);
            }
        } else {
            setProperty("privatePacks", code);
        }
    }

    public void removePrivatePack (String code) {
        ArrayList<String> codes = getPrivatePacks();
        if (codes.contains(code)) {
            codes.remove(code);
        }
        setPrivatePacks(codes);
    }

    public void setPrivatePacks (List<String> codes) {
        String out = "";
        String sep = "";
        for (String s : codes) {
            out += sep + s;
            sep = ",";
        }
        setProperty("privatePacks", out);
    }

    public ArrayList<String> getPrivatePacks () {
        String[] temp = getProperty("privatePacks", "").split(",");
        if (temp.length > 0) {
            ArrayList<String> packs = new ArrayList<String>();
            Collections.addAll(packs, temp);
            return packs;
        }
        return null;
    }

    public void setNewsDate () {
        setProperty("newsDate", Long.toString(Calendar.getInstance().getTime().getTime()));
    }

    public String getNewsDate () {
        return getProperty("newsDate", Long.toString(new Date(0).getTime()));
    }

    public void setLastExtendedState (int lastExtendedState) {
        setProperty("lastExtendedState", String.valueOf(lastExtendedState));
    }

    public int getLastExtendedState () {
        return Integer.valueOf(getProperty("lastExtendedState", String.valueOf(Frame.MAXIMIZED_BOTH)));
    }

    public void setKeepLauncherOpen (boolean state) {
        setProperty("keepLauncherOpen", String.valueOf(state));
    }

    public boolean getKeepLauncherOpen () {
        return Boolean.parseBoolean(getProperty("keepLauncherOpen", "false"));
    }

    public void setSnooper (boolean state) {
        setProperty("snooperDisable", String.valueOf(state));
    }

    public boolean getSnooper () {
        return Boolean.parseBoolean(getProperty("snooperDisable", "false"));
    }

    public void setDebugLauncher (boolean state) {
        setProperty("debugLauncher", String.valueOf(state));
    }

    public boolean getDebugLauncher () {
        return Boolean.parseBoolean(getProperty("debugLauncher", "false"));
    }

    public void setLoaded (boolean state) {
        setProperty("loaded", String.valueOf(state));
    }

    public boolean getLoaded () {
        return Boolean.parseBoolean(getProperty("loaded", "false"));
    }

    public String getAdditionalJavaOptions () {
        return getProperty("additionalJavaOptions", "");
    }

    public void setAdditionalJavaOptions (String opts) {
        setProperty("additionalJavaOptions", opts);
    }

    public void setLastPosition (Point lastPosition) {
        int x = lastPosition.x;
        int y = lastPosition.y;
        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }
        Point p = new Point(x, y);
        setObjectProperty("lastPosition", p);
    }

    public Point getLastPosition () {
        Point lastPosition = (Point) getObjectProperty("lastPosition");
        if (lastPosition == null) {
            lastPosition = new Point(300, 300);
        }
        return lastPosition;
    }

    public void setLastDimension (Dimension lastDimension) {
        setObjectProperty("lastDimension", lastDimension);
    }

    public Dimension getLastDimension () {
        Dimension lastDimension = (Dimension) getObjectProperty("lastDimension");
        if (lastDimension == null) {
            lastDimension = new Dimension(854, 480);
        }
        return lastDimension;
    }

    public void setObjectProperty (String propertyName, Serializable value) {
        setProperty(propertyName, objectToString(value));
    }

    public Object getObjectProperty (String propertyName) {
        return objectFromString(getProperty(propertyName, ""));
    }

    public static Object objectFromString (String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        byte[] data = javax.xml.bind.DatatypeConverter.parseBase64Binary(s);
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            try {
                return ois.readObject();
            } finally {
                ois.close();
            }
        } catch (Exception e) {
            Logger.logError("Failed to read object from string: " + s, e);
            return null;
        }
    }

    private static String objectToString (Serializable o) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            try {
                oos.writeObject(o);
                return javax.xml.bind.DatatypeConverter.printBase64Binary(baos.toByteArray());
            } finally {
                baos.close();
                oos.close();
            }
        } catch (Exception e) {
            Logger.logError("Failed to write object to string" + o, e);
            return null;
        }
    }
}
