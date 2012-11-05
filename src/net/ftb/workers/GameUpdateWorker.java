package net.ftb.workers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.SwingWorker;

import net.ftb.log.Logger;
import net.ftb.util.OSUtils;
import net.ftb.util.OSUtils.OS;

/**
 * SwingWorker that downloads Minecraft. Returns true if successful, false if it
 * fails.
 * 
 * Right now this is pretty much just a port of MultiMC's GameUpdateTask, but
 * it will likely change in the future.
 */
public class GameUpdateWorker extends SwingWorker<Boolean, Void> {
	protected String status, reqVersion, mainGameURL;
	protected File binDir;
	protected boolean forceUpdate;

	protected URL[] jarURLs;

	public GameUpdateWorker(String packVersion, String mainGameURL, String binDir, boolean forceUpdate) {
		reqVersion = packVersion;
		this.mainGameURL = mainGameURL;
		this.binDir = new File(binDir);
		this.forceUpdate = forceUpdate;
		this.status = "";
	}

	@Override
	protected Boolean doInBackground() {
		setStatus("Determining packages to load...");
		if (!loadJarURLs()) {
			return false;
		}
		setProgress(5);

		if (!binDir.exists()) {
			binDir.mkdirs();
		}

		setProgress(90);
		setStatus("Downloading jars...");
		Logger.logInfo("Downloading Jars");
		if (!downloadJars()) {
			Logger.logError("Download Failed :(");
			return false;
		}
		setStatus("Extracting files...");
		Logger.logInfo("Extracting Files");
		if (!extractNatives()) {
			Logger.logError("Extraction Failed :(");
			return false;
		}
		return true;
	}

	protected boolean loadJarURLs() {
		Logger.logInfo("Loading Jar URLs");

		String[] jarList = { mainGameURL, "lwjgl.jar", "lwjgl_util.jar", "jinput.jar" };

		jarURLs = new URL[jarList.length + 1];
		try	{
			// Set the version to the required version of minecraft for the Mod Pack
			String ver = reqVersion.replace(".", "_");
			jarURLs[0] = new URL("http://assets.minecraft.net/" + ver + "/minecraft.jar");
			for (int i = 1; i < jarList.length; i++) {
				jarURLs[i] = new URL("http://s3.amazonaws.com/MinecraftDownload/" + jarList[i]);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return false;
		}

		String nativesFilename;
		if (OSUtils.getCurrentOS() == OS.WINDOWS) {
			nativesFilename = "windows_natives.jar";
		} else if (OSUtils.getCurrentOS() == OS.MACOSX) {
			nativesFilename = "macosx_natives.jar";
		} else if (OSUtils.getCurrentOS() == OS.UNIX) {
			nativesFilename = "linux_natives.jar";
		} else {
			return false;
		}

		try {
			jarURLs[jarURLs.length - 1] = new URL("http://s3.amazonaws.com/MinecraftDownload/" + nativesFilename);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	protected boolean downloadJars() {
		File md5sFile = new File(binDir, "md5s");
		Properties md5s = new Properties();

		try	{
			FileInputStream inputStream = new FileInputStream(md5sFile);
			md5s.load(inputStream);
			inputStream.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) { e.printStackTrace(); }

		int totalDownloadSize = 0;
		int[] fileSizes = new int[jarURLs.length];
		boolean[] skip = new boolean[jarURLs.length];
		URLConnection connection;

		// Compare MD5s and skip ones that match.
		for (int i = 0; i < jarURLs.length; i++) {
			try {
				connection = jarURLs[i].openConnection();
				connection.setDefaultUseCaches(false);
				skip[i] = false;
				if (connection instanceof HttpURLConnection) {
					((HttpURLConnection) connection).setRequestMethod("HEAD");

					String localMD5 = "\"" + md5s.getProperty(getFilename(jarURLs[i])) + "\"";
					if (!forceUpdate) {
						connection.setRequestProperty("If-None-Match", localMD5);
					}
					int response = ((HttpURLConnection) connection).getResponseCode();
					if (response == 300) {
						skip[i] = true;
					}
				}
				fileSizes[i] = connection.getContentLength();
				totalDownloadSize += fileSizes[i];
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}

		// TODO: Fix progress bar when updating minecraft
		int initialProgress = 10;
		setProgress(initialProgress);

		int totalDownloadedSize = 0;
		for (int i = 0; i < jarURLs.length; i++) {
			if (skip[i]) {
				setProgress(initialProgress + ((fileSizes[i] * 45) / totalDownloadSize));
				continue;
			}
			try	{
				FileOutputStream out = new FileOutputStream(md5sFile);
				md5s.remove(getFilename(jarURLs[i]));
				md5s.store(out, "md5 hashes for downloaded files");
				out.close();
			} catch (IOException e)	{
				e.printStackTrace();
			}
			int triesLeft = 0;
			boolean downloadSuccess = false;
			while (!downloadSuccess && (triesLeft < 5)) {
				try {
					triesLeft++;
					Logger.logInfo("Connecting.. Try " + triesLeft + " of 5");
					String etag = "";

					URLConnection dlConnection = jarURLs[i].openConnection();
					if (dlConnection instanceof HttpURLConnection) {
						dlConnection.setRequestProperty("Cache-Control", "no-cache");
						dlConnection.connect();

						etag = dlConnection.getHeaderField("ETag");
						etag = etag.substring(1, etag.length() - 1);
					}

					String jarFileName = getFilename(jarURLs[i]);
					InputStream dlStream = dlConnection.getInputStream();
					if(new File(binDir, jarFileName).exists()) {
						new File(binDir, jarFileName).delete();
					}
					FileOutputStream outStream = new FileOutputStream(new File(binDir, jarFileName));

					setStatus("Downloading " + jarFileName + "...");

					MessageDigest msgDigest = MessageDigest.getInstance("MD5");
					byte[] buffer = new byte[24000];
					int readLen;
					int currentDLSize = 0;
					while ((readLen = dlStream.read(buffer, 0, buffer.length)) != -1) {						
						outStream.write(buffer, 0, readLen);
						msgDigest.update(buffer, 0, readLen);

						currentDLSize += readLen;
						totalDownloadedSize += readLen;

						int prog = i + ((totalDownloadedSize * 45) / totalDownloadSize);
						if (prog > 100) {
							prog = 100;
						} else if (prog < 0){
							prog = 0;
						}
						setProgress(prog);
					}

					dlStream.close();
					outStream.close();

					String md5str = new BigInteger(1, msgDigest.digest()).toString(16);
					while (md5str.length() < 32) {
						md5str = "0" + md5str;
					}

					boolean md5Matches = true;
					if (etag != null) {
						md5Matches = md5str.equalsIgnoreCase(etag);
					}

					if (dlConnection instanceof HttpURLConnection) {
						if (md5Matches && ((currentDLSize == fileSizes[i]) || (fileSizes[i] <= 0)))	{
							downloadSuccess = true;
							try	{
								md5s.setProperty(getFilename(jarURLs[i]), etag);
								FileOutputStream out = new FileOutputStream(md5sFile);
								md5s.store(out, "md5 hashes for downloaded files");
								out.close();
							} catch (IOException e)	{ e.printStackTrace(); }
						}
					}
				} catch (Exception e) {
					downloadSuccess = false;
					Logger.logWarn("Connection failed, trying again");
					e.printStackTrace();
				}
			}
			if (!downloadSuccess) {
				return false;
			}
		}
		return true;
	}

	protected boolean extractNatives() {
		setStatus("Extracting natives...");
		File nativesJar = new File(binDir, getFilename(jarURLs[jarURLs.length - 1]));
		File nativesDir = new File(binDir, "natives");

		if (!nativesDir.isDirectory()) {
			nativesDir.mkdirs();
		}

		FileInputStream input;
		try	{
			input = new FileInputStream(nativesJar);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}

		ZipInputStream zipIn = new ZipInputStream(input); 
		try {
			ZipEntry currentEntry = zipIn.getNextEntry();
			while (currentEntry != null) {
				if (currentEntry.getName().contains("META-INF")) {
					currentEntry = zipIn.getNextEntry();
					continue;
				}

				setStatus("Extracting " + currentEntry + "...");
				FileOutputStream outStream = new FileOutputStream(new File(nativesDir, currentEntry.getName()));

				int readLen;
				byte[] buffer = new byte[1024];
				while ((readLen = zipIn.read(buffer, 0, buffer.length)) > 0) {
					outStream.write(buffer, 0, readLen);
				}
				outStream.close();

				currentEntry = zipIn.getNextEntry();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				zipIn.close();
				input.close();
			} catch (IOException e) { e.printStackTrace(); }
		}

		nativesJar.delete();
		return true;
	}

	protected String getFilename(URL url) {
		String string = url.getFile();
		if (string.contains("?")) {
			string = string.substring(0, string.indexOf('?'));
		}
		return string.substring(string.lastIndexOf('/') + 1);
	}

	protected void setStatus(String newStatus) {
		String oldStatus = status;
		status = newStatus;
		firePropertyChange("status", oldStatus, newStatus);
	}

	public String getStatus() {
		return status;
	}
}