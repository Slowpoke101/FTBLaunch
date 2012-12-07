package net.ftb.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.log.Logger;

public class MinecraftVersionDetector {
	public MinecraftVersionDetector() { }

	/**
	 * Finds out using some clever tricks the current minecraft version version
	 * @param jarFilePath The .minecraft directory
	 * @return The version of the jar file
	 */
	public String getMinecraftVersion(String jarFilePath) {
		try {
			ZipInputStream file = new ZipInputStream(new FileInputStream(new File(jarFilePath, "bin/" + "minecraft.jar")));
			ZipEntry ent;
			ent = file.getNextEntry();
			while(ent != null) {
				if(ent.getName().contains("Minecraft.class")) {
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

	public boolean shouldUpdate(String jarFilePath) {
		String requiredVersion = ModPack.getSelectedPack().getMcVersion();
		if(Settings.getSettings().getForceUpdate()) {
			return true;
		}
		String version = getMinecraftVersion(jarFilePath);
		if(version.equals("unknown")) {
			return false;
		}
		File mcVersion = new File(jarFilePath, "bin/version");
		if(mcVersion.exists()) {
			BufferedReader in;
			try {
				in = new BufferedReader(new FileReader(mcVersion));
				requiredVersion = in.readLine();
				in.close();
			} catch (IOException e) { }
		}
		Logger.logInfo("Current: " + version);
		Logger.logInfo("Required: " + requiredVersion);
		ModPack.getSelectedPack().setMcVersion(requiredVersion);
		return !version.equals(requiredVersion);
	}
}
