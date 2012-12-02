package net.ftb.updater;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.ftb.log.Logger;
import net.ftb.util.FileUtils;
import net.ftb.util.OSUtils;

public class SelfUpdate {
	public static void runUpdate(String currentPath, String temporaryUpdatePath) {
		List<String> arguments = new ArrayList<String>();

		String separator = System.getProperty("file.separator");
		String path = System.getProperty("java.home") + separator + "bin" + separator + "java";
		arguments.add(path);
		arguments.add("-cp");
		arguments.add(temporaryUpdatePath);
		arguments.add(SelfUpdate.class.getCanonicalName());
		arguments.add(currentPath);
		arguments.add(temporaryUpdatePath);

		Logger.logInfo("Would update with: " + arguments);
		Logger.logInfo("c: " + currentPath);
		Logger.logInfo("n: " + temporaryUpdatePath);
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command(arguments);
		try {
			processBuilder.start();
		} catch (IOException e) { Logger.logError("Failed to start self-update process", e); }
		System.exit(0);
	}

	public static void main(String[] args) {
		try {
			if (OSUtils.getCurrentOS() != OSUtils.OS.UNIX) {
				Thread.sleep(4000);
			}
		} catch (InterruptedException ignored) {
			Logger.logError(ignored.getMessage(), ignored);
		}
		String launcherPath = args[0];
		String temporaryUpdatePath = args[1];
		File launcher = new File(launcherPath);
		File temporaryUpdate = new File(temporaryUpdatePath);
		try {
			FileUtils.delete(launcher);
			FileUtils.copyFile(temporaryUpdate, launcher);
		} catch (IOException e) {
			Logger.logError("Auto Updating Failed", e);
		}

		List<String> arguments = new ArrayList<String>();

		String separator = System.getProperty("file.separator");
		String path = System.getProperty("java.home") + separator + "bin" + separator + "java";
		arguments.add(path);
		arguments.add("-jar");
		arguments.add(launcherPath);

		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command(arguments);
		try {
			processBuilder.start();
		} catch (IOException e) {
			Logger.logError("Failed to start launcher process after updating", e);
		}
	}
}
