package net.ftb.locale;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

import net.ftb.log.Logger;
import net.ftb.util.FileUtils;
import net.ftb.util.OSUtils;

public class LocaleUpdater {
	private static final String host = "https://dl.dropbox.com/u/9031641/ftb-test/"; // TODO: update host :P
	private static final String root = OSUtils.getDynamicStorageLocation();
	private static File local = new File(root + File.separator + "temp" + File.separator + "i18n" + File.separator + "version");
	private static File archive = new File(root + File.separator + "temp" + File.separator + "locales.zip");
	private static int remoteVer;

	private static void updateFiles() {
		Logger.logInfo("[i18n] Downloading locale files ...");
		try {
			FileUtils.downloadToFile(new URL(host + "locales.zip"), archive);
			Logger.logInfo("[i18n] Moving files into place ...");
			if(local.getParentFile().exists()) {
				FileUtils.delete(local.getParentFile());
			}
			FileUtils.extractZipTo(archive.getAbsolutePath(), local.getParentFile().getPath());
			Writer wr = new FileWriter(local);
			wr.write(String.valueOf(remoteVer));
			wr.close();
			cleanUpFiles();
		} catch (MalformedURLException e) {
		} catch (IOException e) {
			Logger.logWarn("[i18n] Update IOException", e);
		}
	}

	public static void checkForUpdates() {
		Logger.logInfo("[i18n] Checking for updates ...");
		File dir = new File(root + File.separator + "temp");
		File tmp = new File(dir, "i18n");

		if (!dir.exists() || !tmp.exists()) {
			dir.mkdirs();
			tmp.mkdirs();
		}

		cleanUpFiles();

		try {
			URLConnection connection = new URL(host + "locales").openConnection();
			Scanner scanner = new Scanner(connection.getInputStream());
			remoteVer = scanner.nextInt();
			Logger.logInfo("[i18n] remoteVer = " + remoteVer);
			scanner.close();
		} catch (MalformedURLException e1) {
		} catch (IOException e1) {
			Logger.logInfo("[i18n] Could not retrieve version info", e1);
		}

		if (local.exists()) {
			try {
				int localVer;
				Scanner scanner = new Scanner(local);
				localVer = scanner.nextInt();
				Logger.logInfo("[i18n] localVar = " + localVer);
				scanner.close();

				if (localVer < remoteVer) {
					updateFiles();
				} else {
					Logger.logInfo("[i18n] Files are up to date");
				}
			} catch (FileNotFoundException e1) {
				Logger.logInfo("[i18n] Could not read version file", e1);
			}
		} else {
			updateFiles();
		}
	}

	private static void cleanUpFiles() {
		if (archive.exists()) {
			archive.delete();
		}
	}
}
