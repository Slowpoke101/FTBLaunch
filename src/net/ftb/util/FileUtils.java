package net.ftb.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.gui.LaunchFrame;
import net.ftb.log.Logger;

public class FileUtils {
	/**
	 * @param sourceFolder - the folder to be moved
	 * @param destinationFolder - where to move to
	 * @throws IOException
	 */
	public static void copyFolder(File sourceFolder, File destinationFolder) throws IOException {
		if (sourceFolder.isDirectory()) {
			if (!destinationFolder.exists()) {
				destinationFolder.mkdirs();
			}
			String files[] = sourceFolder.list();
			for (String file : files) {
				File srcFile = new File(sourceFolder, file);
				File destFile = new File(destinationFolder, file);
				copyFolder(srcFile, destFile);
			}
		} else {
			copyFile(sourceFolder, destinationFolder);
		}
	}

	/**
	 * @param sourceFile - the file to be moved
	 * @param destinationFile - where to move to
	 * @throws IOException
	 */
	public static void copyFile(File sourceFile, File destinationFile) throws IOException {
		if (sourceFile.exists()) {
			if(!destinationFile.exists()) {
				destinationFile.createNewFile();
			}
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
		try {
			System.out.println("Extracting");
			File fSourceZip = new File(zipLocation);
			File temp = new File(outputLocation);
			temp.mkdir();
			ZipFile zipFile = new ZipFile(fSourceZip);
			Enumeration<?> e = zipFile.entries();
			while (e.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) e.nextElement();
				File destinationFilePath = new File(outputLocation, entry.getName());
				destinationFilePath.getParentFile().mkdirs();
				if (!entry.isDirectory() && !entry.getName().equals(".minecraft")) {
					BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));
					int b;
					byte buffer[] = new byte[1024];
					FileOutputStream fos = new FileOutputStream(destinationFilePath);
					BufferedOutputStream bos = new BufferedOutputStream(fos, 1024);
					while ((b = bis.read(buffer, 0, 1024)) != -1) {
						bos.write(buffer, 0, b);
					}
					bos.flush();
					bos.close();
					bis.close();
					fos.close();
				}
			}
			zipFile.close();
		} catch (IOException ioe) { }
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
		} catch (IOException e) { }
	}
}
