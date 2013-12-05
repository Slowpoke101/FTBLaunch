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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.log.Logger;

public class FileUtils {
	/**
	 * @param sourceFolder - the folder to be moved
	 * @param destinationFolder - where to move to
	 * @throws IOException
	 */
    public static void copyFolder(File sourceFolder, File destinationFolder) throws IOException {
        copyFolder(sourceFolder, destinationFolder, true);
    }
    public static void copyFolder(File sourceFolder, File destinationFolder, boolean overwrite) throws IOException {
		if (sourceFolder.isDirectory()) {
			if (!destinationFolder.exists()) {
				destinationFolder.mkdirs();
			}
			String files[] = sourceFolder.list();
			for (String file : files) {
				File srcFile = new File(sourceFolder, file);
				File destFile = new File(destinationFolder, file);
				copyFolder(srcFile, destFile, overwrite);
			}
		} else {
			copyFile(sourceFolder, destinationFolder, overwrite);
		}
	}

	/**
	 * @param sourceFile - the file to be moved
	 * @param destinationFile - where to move to
	 * @throws IOException
	 */
    public static void copyFile(File sourceFile, File destinationFile) throws IOException {
        copyFile(sourceFile, destinationFile, true);
    }
    public static void copyFile(File sourceFile, File destinationFile, boolean overwrite) throws IOException {
		if (sourceFile.exists()) {
			if(!destinationFile.exists()) {
				destinationFile.createNewFile();
			} 
			else if (!overwrite) return;
			FileChannel sourceStream = null, destinationStream = null;
			try {
				sourceStream = new FileInputStream(sourceFile).getChannel();
				destinationStream = new FileOutputStream(destinationFile).getChannel();
				destinationStream.transferFrom(sourceStream, 0, sourceStream.size());
			} finally {
				if(sourceStream != null) {
					sourceStream.close();
				}
				if(destinationStream != null) {
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
	public static boolean delete(File resource) throws IOException {
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
	public static void extractZipTo(String zipLocation, String outputLocation) {
		ZipInputStream zipinputstream = null;
		try {
			byte[] buf = new byte[1024];
			zipinputstream = new ZipInputStream(new FileInputStream(zipLocation));
			ZipEntry zipentry = zipinputstream.getNextEntry();
			while (zipentry != null) { 
				String entryName = zipentry.getName();
				int n;
				if(!zipentry.isDirectory() && !entryName.equalsIgnoreCase("minecraft") && !entryName.equalsIgnoreCase(".minecraft") && !entryName.equalsIgnoreCase("instMods")) {
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
			Logger.logError(e.getMessage(), e);
			backupExtract(zipLocation, outputLocation);
		} finally {
			try {
				zipinputstream.close();
			} catch (IOException e) { }
		}
	}

	public static void backupExtract(String zipLocation, String outputLocation){
		Logger.logInfo("Extracting (Backup way)");
		byte[] buffer = new byte[1024];
		ZipInputStream zis = null;
		ZipEntry ze = null;
		try{
			File folder = new File(outputLocation);
			if(!folder.exists()){
				folder.mkdir();
			}
			zis = new ZipInputStream(new FileInputStream(zipLocation));
			ze = zis.getNextEntry();
			while(ze != null){
				File newFile = new File(outputLocation, ze.getName());
				newFile.getParentFile().mkdirs();
				if(!ze.isDirectory()) {
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
		} catch(IOException ex) {
			Logger.logError(ex.getMessage(), ex);
		} finally {
			try {
				zis.closeEntry();
				zis.close();
			} catch (IOException e) { }	
		}
	}    

	/**
	 * deletes the META-INF
	 */
	public static void killMetaInf() {
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

			if(!inputFile.delete()) {
				Logger.logError("Failed to delete Minecraft.jar.");
				return;
			}
			outputTmpFile.renameTo(inputFile);
		} catch (FileNotFoundException e) { 
			Logger.logError(e.getMessage(), e);
		} catch (IOException e) {
			Logger.logError(e.getMessage(), e);
		}
	}
}