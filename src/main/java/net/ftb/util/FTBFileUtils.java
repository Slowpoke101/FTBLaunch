/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2016, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.log.Logger;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FTBFileUtils {
    private FTBFileUtils() {
    }

    /**
     * @param sourceFolder - the folder to be moved
     * @param destinationFolder - where to move to
     * @throws IOException
     */
    public static void copyFolder (File sourceFolder, File destinationFolder) throws IOException {
        copyFolder(sourceFolder, destinationFolder, true, null);
    }

    public static void copyFolder (File sourceFolder, File destinationFolder, List<String> blacklist) throws IOException {
        copyFolder(sourceFolder, destinationFolder, true, blacklist);
    }

    public static void copyFolder (File sourceFolder, File destinationFolder, boolean overwrite) throws IOException {
        copyFolder(sourceFolder, destinationFolder, overwrite, null);
    }

    public static void copyFolder (File sourceFolder, File destinationFolder, boolean overwrite, List<String> blacklist) throws IOException {
        if (sourceFolder.isDirectory()) {
            if (!destinationFolder.exists()) {
                destinationFolder.mkdirs();
            }
            String files[] = sourceFolder.list();
            for (String file : files) {
                File srcFile = new File(sourceFolder, file);
                File destFile = new File(destinationFolder, file);
                copyFolder(srcFile, destFile, overwrite, blacklist);
            }
        } else if ( blacklist == null || !blacklist.contains(sourceFolder)) {
            copyFile(sourceFolder, destinationFolder, overwrite);
        }
    }

    /**
     * @param sourceFile - the file to be moved
     * @param destinationFile - where to move to
     * @throws IOException
     */
    public static void copyFile (File sourceFile, File destinationFile) throws IOException {
        copyFile(sourceFile, destinationFile, true);
    }

    public static void copyFile (File sourceFile, File destinationFile, boolean overwrite) throws IOException {
        if (sourceFile.exists()) {
            if (!destinationFile.exists()) {
                destinationFile.getParentFile().mkdirs();
                destinationFile.createNewFile();
            } else if (!overwrite) {
                return;
            }
            FileChannel sourceStream = null, destinationStream = null;
            try {
                sourceStream = new FileInputStream(sourceFile).getChannel();
                destinationStream = new FileOutputStream(destinationFile).getChannel();
                destinationStream.transferFrom(sourceStream, 0, sourceStream.size());
            } finally {
                if (sourceStream != null) {
                    sourceStream.close();
                }
                if (destinationStream != null) {
                    destinationStream.close();
                }
            }
        }
    }

    /**
     * @param resource - the resource to delete
     * @return whether deletion was successful
     * @throws IOException
     */
    public static boolean delete (File resource) throws IOException {
        if (resource.isDirectory()) {
            File[] childFiles = resource.listFiles();
            for (File child : childFiles) {
                delete(child);
            }
        }
        return resource.delete();
    }

    /**
     * Extracts given zip to given location
     * @param zipLocation - the location of the zip to be extracted
     * @param outputLocation - location to extract to
     */
    public static boolean extractZipTo (String zipLocation, String outputLocation) {
        boolean success = true;
        boolean backupSuccess = true;
        ZipInputStream zipinputstream = null;
        try {
            byte[] buf = new byte[1024];
            zipinputstream = new ZipInputStream(new FileInputStream(zipLocation));
            ZipEntry zipentry = zipinputstream.getNextEntry();
            while (zipentry != null) {
                String entryName = zipentry.getName();
                int n;
                if (!zipentry.isDirectory() && !entryName.equalsIgnoreCase("minecraft") && !entryName.equalsIgnoreCase(".minecraft") && !entryName.equalsIgnoreCase("instMods")) {
                    new File(outputLocation + File.separator + entryName).getParentFile().mkdirs();
                    FileOutputStream fileoutputstream = new FileOutputStream(outputLocation + File.separator + entryName);
                    while ((n = zipinputstream.read(buf, 0, 1024)) > -1) {
                        fileoutputstream.write(buf, 0, n);
                    }
                    fileoutputstream.close();
                }
                zipinputstream.closeEntry();
                zipentry = zipinputstream.getNextEntry();
            }
        } catch (Exception e) {
            success = false;
            Logger.logError("Error while extracting zip", e);
            backupSuccess = backupExtract(zipLocation, outputLocation);
        } finally {
            try {
                zipinputstream.close();
            } catch (IOException e) {
            }
        }
        if (!success) {
            return backupSuccess;
        }
        return true;
    }

    public static boolean backupExtract (String zipLocation, String outputLocation) {
        boolean success = true;
        Logger.logInfo("Extracting (Backup way)");
        byte[] buffer = new byte[1024];
        ZipInputStream zis = null;
        ZipEntry ze;
        try {
            File folder = new File(outputLocation);
            if (!folder.exists()) {
                folder.mkdir();
            }
            zis = new ZipInputStream(new FileInputStream(zipLocation));
            ze = zis.getNextEntry();
            while (ze != null) {
                File newFile = new File(outputLocation, ze.getName());
                newFile.getParentFile().mkdirs();
                if (!ze.isDirectory()) {
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.flush();
                    fos.close();
                }
                ze = zis.getNextEntry();
            }
        } catch (IOException ex) {
            Logger.logError("Error while extracting zip", ex);
            success = false;
        } finally {
            try {
                zis.closeEntry();
                zis.close();
            } catch (IOException e) {
            }
        }
        return success;
    }

    /**
     * deletes the META-INF
     */
    public static void killMetaInf () {
        File inputFile = new File(Settings.getSettings().getInstallPath() + "/" + ModPack.getSelectedPack().getDir() + "/minecraft/bin", "minecraft.jar");
        File outputTmpFile = new File(Settings.getSettings().getInstallPath() + "/" + ModPack.getSelectedPack().getDir() + "/minecraft/bin", "minecraft.jar.tmp");
        try {
            JarInputStream input = new JarInputStream(new FileInputStream(inputFile));
            JarOutputStream output = new JarOutputStream(new FileOutputStream(outputTmpFile));
            JarEntry entry;

            while ((entry = input.getNextJarEntry()) != null) {
                if (entry.getName().contains("META-INF")) {
                    continue;
                }
                output.putNextEntry(entry);
                byte buffer[] = new byte[1024];
                int amo;
                while ((amo = input.read(buffer, 0, 1024)) != -1) {
                    output.write(buffer, 0, amo);
                }
                output.closeEntry();
            }

            input.close();
            output.close();

            if (!inputFile.delete()) {
                Logger.logError("Failed to delete Minecraft.jar.");
                return;
            }
            outputTmpFile.renameTo(inputFile);
        } catch (FileNotFoundException e) {
            Logger.logError("Error while killing META-INF", e);
        } catch (IOException e) {
            Logger.logError("Error while killing META-INF", e);
        }
    }

    public static List<File> listDirs (File path) {
        List<File> ret = Lists.newArrayList();
        if (path.exists()) {
            listDirs(path, ret);
        }
        Collections.sort(ret, new Comparator<File>() {
            @Override
            public int compare (File o1, File o2) {
                return o2.compareTo(o1);
            }
        });
        return ret;
    }

    private static void listDirs (File path, List<File> list) {
        for (File f : path.listFiles()) {
            if (f.isDirectory()) {
                listDirs(f, list);
                list.add(f);
            }
        }
    }

    public static Set<File> listFiles (File path) {
        Set<File> set = Sets.newHashSet();
        if (path.exists()) {
            listFiles(path, set);
        }
        return set;
    }

    private static void listFiles (File path, Set<File> set) {
        for (File f : path.listFiles()) {
            if (f.isDirectory()) {
                listFiles(f, set);
            } else {
                set.add(f);
            }
        }
    }

    public static void move (File oldFile, File newFile) {
        try {
            if (oldFile.exists() && !newFile.exists()) {
                FileUtils.moveFile(oldFile, newFile);
            }
        } catch (IOException e) {
            Logger.logWarn("Exception occurred while moving " + oldFile.toString() + " : " + e.getMessage());
        }
    }
}
