package net.ftb.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.gui.LaunchFrame;
import net.ftb.log.Logger;
import net.ftb.util.FileUtils;

public class MinecraftVersionDetector {
	public MinecraftVersionDetector() { }

	// The new design of this class is thanks to LexManos. You always think you're right and sometimes you're completely right.

	/**
	 * Finds out using some clever tricks the current minecraft version version
	 * @param jarFilePath The .minecraft directory
	 * @return The version of the jar file
	 */
	public String getMinecraftVersion(String jarFilePath) {
		String[] jarFiles = new String[] { "bckminecraft.jar", "bcklwjgl.jar" };

		if(new File(jarFilePath + "/bin/bckminecraft.jar").exists()) {
			new File(jarFilePath + "/bin/bckminecraft.jar").delete();
		}
		try {
			FileUtils.copyFile(new File(jarFilePath + "/bin/minecraft.jar"), new File(jarFilePath + "/bin/bckminecraft.jar"));
		} catch (IOException e2) {
			Logger.logError(e2.getMessage(), e2);
		}

		if(new File(jarFilePath + "/bin/bcklwjgl.jar").exists()) {
			new File(jarFilePath + "/bin/bcklwjgl.jar").delete();
		}
		try {
			FileUtils.copyFile(new File(jarFilePath + "/bin/lwjgl.jar"), new File(jarFilePath + "/bin/bcklwjgl.jar"));
		} catch (IOException e2) {
			Logger.logError(e2.getMessage(), e2);
		}

		URL[] urls = new URL[jarFiles.length];

		for (int i = 0; i < urls.length; i++) {
			try {
				File f = new File(new File(jarFilePath, "bin"), jarFiles[i]);
				urls[i] = f.toURI().toURL();
			} catch (MalformedURLException e) {
				Logger.logError(e.getMessage(), e);
				return "unknown";
			}
		}

		try {
			ZipInputStream file = new ZipInputStream(new FileInputStream(new File(jarFilePath + "/bin", "minecraft.jar")));
			ZipEntry ent;

			ent = file.getNextEntry();

			while (ent != null) {
				if (ent.getName().contains("Minecraft.class")) {
					StringBuilder sb = new StringBuilder();
					for (int c = file.read(); c != -1; c = file.read()) {
						sb.append((char)c);
					}
					String data = sb.toString();
					String search = "Minecraft 1";
					file.closeEntry();
					file.close();
					return data.substring(data.indexOf(search) + 10, data.indexOf(search) + search.length() + 4);
				}
				file.closeEntry();
				ent = file.getNextEntry();
			}
			file.close();
		} catch (IOException e1) {
			Logger.logError(e1.getMessage(), e1);
			return "unknown";
		}
		return "unknown";
	}

	public boolean shouldUpdate(String requiredVersion, String jarFilePath) {
		if(Settings.getSettings().getForceUpdate()) {
			return true;
		}
		if(!ModPack.getSelectedPack().isUpToDate()){
			return false;
		}
		String version = getMinecraftVersion(jarFilePath);
		if(version.equals("unknown")) {
			return false;
		}
		return !version.equals(requiredVersion);
	}
}
