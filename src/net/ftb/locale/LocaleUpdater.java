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
import net.ftb.util.DownloadUtils;
import net.ftb.util.FileUtils;
import net.ftb.util.OSUtils;

public class LocaleUpdater extends Thread {
	private final String root = OSUtils.getDynamicStorageLocation();
	private File local = new File(root + File.separator + "locale" + File.separator + "version");
	private File archive = new File(root + File.separator + "locales.zip");
	private int remoteVer;

	public LocaleUpdater() {
		setName("Locale Updater");
		setPriority(MIN_PRIORITY);
	}

	private void updateFiles() {
		Logger.logInfo("[i18n] Downloading locale files ...");
		try {
			DownloadUtils.downloadToFile(new URL(DownloadUtils.getCreeperhostLink("locales.zip")), archive);
			Logger.logInfo("[i18n] Moving files into place ...");
			if(local.getParentFile().exists()) {
				FileUtils.delete(local.getParentFile());
			}
			FileUtils.extractZipTo(archive.getAbsolutePath(), local.getParentFile().getPath());
			Writer wr = new FileWriter(local);
			wr.write(String.valueOf(remoteVer));
			wr.close();
			cleanUpFiles();
		} catch (Exception e) {
			Logger.logWarn("[i18n] Update IOException", e);
		}
	}

	public void run() {
		Logger.logInfo("[i18n] Checking for updates ...");
		File dir = new File(root);
		File tmp = new File(dir, "locale");

		if (!dir.exists() || !tmp.exists()) {
			dir.mkdirs();
			tmp.mkdirs();
		}

		cleanUpFiles();

		try {
			URLConnection connection = new URL(DownloadUtils.getStaticCreeperhostLink("locales")).openConnection();
			Scanner scanner = new Scanner(connection.getInputStream());
			remoteVer = scanner.nextInt();
			Logger.logInfo("[i18n] remoteVer = " + remoteVer);
			scanner.close();
		} catch (MalformedURLException e1) {
			Logger.logError(e1.getMessage(), e1);
		} catch (IOException e1) {
			Logger.logInfo("[i18n] Could not retrieve version info, skipping update.", e1);
			return;
		}

		if (local.exists()) {
			try {
				int localVer;
				Scanner scanner = new Scanner(local);
				localVer = scanner.nextInt();
				Logger.logInfo("[i18n] localVer = " + localVer);
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

	private void cleanUpFiles() {
		if (archive.exists()) {
			archive.delete();
		}
	}
}
