package net.ftb.locale;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import net.ftb.data.Settings;
import net.ftb.log.Logger;
import net.ftb.util.FileUtils;

public class LocaleUpdater {
	private static final String host = "https://dl.dropbox.com/u/9031641/ftb-test/"; // TODO: update host :P
	private static final String root = Settings.getSettings().getInstallPath();
	private static File local = new File(root + File.separator + "i18n" + File.separator + "version");
	private static File remote = new File(root + File.separator + "temp" + File.separator + "i18n-version");
	private static File archive = new File(root + File.separator + "temp" + File.separator + "i18n-files.zip");

	private static void updateFiles() {
		Logger.logInfo("[i18n] Downloading locale files ...");
		try {
			FileUtils.downloadToFile(new URL(host + "locales.zip"), archive);
			Logger.logInfo("[i18n] Moving files into place ...");
			FileUtils.extractZipTo(archive.getAbsolutePath(), root + File.separator + "i18n");
			FileUtils.copyFile(remote, local);
		} catch (MalformedURLException e) {
		} catch (IOException e) {
			Logger.logWarn("[i18n] Update IOException", e);
		}
	}

	public static void checkForUpdates() {
		Logger.logInfo("[i18n] Checking for updates ...");
		File dir = new File(root + File.separator + "temp");
		File tmp = new File(root + File.separator + "i18n");

		if (!dir.exists() || !tmp.exists()) {
			dir.mkdirs();
			tmp.mkdirs();
		}

		try {
			FileUtils.downloadToFile(new URL(host + "locales"), remote);
		} catch (MalformedURLException e1) {
		} catch (IOException e1) {
			Logger.logWarn("[i18n] Could not download version file", e1);
		}

		if (local.exists()) {
			try {
				String localVer = new Scanner(local).nextLine();
				String remoteVer = new Scanner(remote).nextLine();

				if (!localVer.equalsIgnoreCase(remoteVer)) {
					updateFiles();
				} else {
					Logger.logInfo("[i18n] Files are up to date");
				}
			} catch (FileNotFoundException e) {
				Logger.logWarn("[i18n] Could not read version file", e);
			}
		} else {
			updateFiles();
		}
	}
}
