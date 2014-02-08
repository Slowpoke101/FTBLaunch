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
package net.ftb.workers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.SwingWorker;

import net.ftb.data.Settings;
import net.ftb.gui.LaunchFrame;
import net.ftb.log.Logger;
import net.ftb.util.OSUtils;

/**
 * SwingWorker that downloads Minecraft. Returns true if successful, false if it
 * fails.
 */
public class AuthlibDLWorker extends SwingWorker<Boolean, Void>
{
    protected String status, reqVersion;
    protected File binDir;
    protected String authlibVersion;
    protected URL jarURLs;
    protected boolean debugVerbose = Settings.getSettings().getDebugLauncher();
    protected String debugTag = "debug: AuthlibDLWorker: ";

    public AuthlibDLWorker(String DLFolder, String authver)
    {
        this.binDir = new File(DLFolder);
        this.authlibVersion = authver;
        this.status = "";
        doInBackground();
    }

    @Override
    protected Boolean doInBackground ()
    {
        if (debugVerbose)
        {
            Logger.logInfo(debugTag + "Loading Authlib...");
        }
        if (!binDir.exists())
            binDir.mkdirs();
        Logger.logInfo("Downloading Jars");
        if (!downloadJars())
        {
            Logger.logError("Download Failed");
            return false;
        }
        setStatus("Adding Authlib to Classpath");
        Logger.logInfo("Adding Authlib to Classpath");
        return addToClasspath(binDir + "authlib" + authlibVersion + ".jar");
    }

    protected boolean addToClasspath (String location)
    {
        return addSoftwareLibrary(new File(location));
    }

    private static boolean addSoftwareLibrary (File file)
    {
        Method method;
        try
        {
            method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { URL.class });
            method.setAccessible(true);
            method.invoke(ClassLoader.getSystemClassLoader(), new Object[] { file.toURI().toURL() });

        }
        catch (Exception e)
        {
            Logger.logError("ERROR adding Authlib to the java classpath");
            return false;
        }
        LaunchFrame.canUseAuthlib = true;
        return true;
    }

    protected boolean downloadJars ()
    {
        try
        {
            jarURLs = new URL("https://libraries.minecraft.net/com/mojang/authlib/" + authlibVersion + "/authlib-" + authlibVersion + ".jar");
        }
        catch (MalformedURLException e)
        {
            Logger.logError(e.getMessage(), e);
            return false;
        }
        double totalDownloadSize = 0, totalDownloadedSize = 0;
        int[] fileSizes = new int[1];
        for (int i = 0; i < 1; i++)
        {
            try
            {
                fileSizes[i] = jarURLs.openConnection().getContentLength();
                totalDownloadSize += fileSizes[i];
            }
            catch (IOException e)
            {
                Logger.logError(e.getMessage(), e);
                return false;
            }
        }

        int attempt = 0;
        final int attempts = 5;
        int lastfile = -1;
        boolean downloadSuccess = false;
        while (!downloadSuccess && (attempt < attempts))
        {
            try
            {
                attempt++;
                if (debugVerbose)
                {
                    Logger.logInfo("Connecting.. Try " + attempt + " of " + attempts + " for: " + jarURLs.toURI());
                }
                URLConnection dlConnection = jarURLs.openConnection();
                if (dlConnection instanceof HttpURLConnection)
                {
                    dlConnection.setRequestProperty("Cache-Control", "no-cache");
                    dlConnection.connect();
                }
                String jarFileName = getFilename(jarURLs);
                if (new File(binDir, jarFileName).exists())
                {
                    new File(binDir, jarFileName).delete();
                }
                InputStream dlStream = dlConnection.getInputStream();
                FileOutputStream outStream = new FileOutputStream(new File(binDir, jarFileName));
                setStatus("Downloading " + jarFileName + "...");
                byte[] buffer = new byte[24000];
                int readLen;
                int currentDLSize = 0;
                while ((readLen = dlStream.read(buffer, 0, buffer.length)) != -1)
                {
                    outStream.write(buffer, 0, readLen);
                    currentDLSize += readLen;
                    totalDownloadedSize += readLen;
                    int prog = (int) ((totalDownloadedSize / totalDownloadSize) * 100);
                    if (prog > 100)
                    {
                        prog = 100;
                    }
                    else if (prog < 0)
                    {
                        prog = 0;
                    }
                    setProgress(prog);
                }
                dlStream.close();
                outStream.close();
                if (dlConnection instanceof HttpURLConnection && (currentDLSize == fileSizes[0] || fileSizes[0] <= 0))
                {
                    downloadSuccess = true;
                }
            }
            catch (Exception e)
            {
                downloadSuccess = false;
                e.printStackTrace();
                Logger.logWarn("Connection failed, trying again");
            }
        }
        if (!downloadSuccess)
        {
            return false;
        }
        return true;
    }

    protected String getFilename (URL url)
    {
        String string = url.getFile();
        if (string.contains("?"))
        {
            string = string.substring(0, string.indexOf('?'));
        }
        return string.substring(string.lastIndexOf('/') + 1);
    }

    protected void setStatus (String newStatus)
    {
        String oldStatus = status;
        status = newStatus;
        firePropertyChange("status", oldStatus, newStatus);
    }

    public String getStatus ()
    {
        return status;
    }
}