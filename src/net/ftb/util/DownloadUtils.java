package net.ftb.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.Scanner;

import net.ftb.data.Settings;
import net.ftb.log.Logger;

public class DownloadUtils {
	private static String currentmd5 = "";
	
	private static String[] names = new String[3];
	private static String[] sites = new String[3];
	private static String[] readStrings = new String[3];

	/**
	 * @param file - the name of the file, as saved to the repo (including extension)
	 * @return - the direct link
	 * @throws NoSuchAlgorithmException - see md5
	 */
	public static String getCreeperhostLink(String file) throws NoSuchAlgorithmException {
		if(currentmd5.isEmpty()) {
			currentmd5 = md5("mcepoch1" + getTime());
		}
		String resolved = "";
		if(Settings.getSettings().getDownloadServer().equalsIgnoreCase("automatic")) {
		resolved = "http://www.creeperrepo.net/direct/FTB2/" + currentmd5 + "/" + file;
		HttpURLConnection connection = null;
		try {
			int retries = 0;
			connection = (HttpURLConnection) new URL(resolved).openConnection();
			while(connection.getResponseCode() != 200 && retries < 3) {
				connection.disconnect();
				switch(retries) {
				case 0:
					resolved = "http://england1.creeperrepo.net/direct/FTB2/" + currentmd5 + "/" + file;
					break;
				case 1:
					resolved = "http://chicago1.creeperrepo.net/direct/FTB2/" + currentmd5 + "/" + file;
					break;
				case 2:
					resolved = "http://chicago2.creeperrepo.net/direct/FTB2/" + currentmd5 + "/" + file;
					break;
				}
				connection = (HttpURLConnection) new URL(resolved).openConnection();
				retries++;
			}
		} catch (IOException e) { }
		connection.disconnect();
		} else {
			resolved = "http://" + DownloadUtils.getServerSite(Settings.getSettings().getDownloadServer()) + "/direct/FTB2/" + currentmd5 + "/" + file;
		}
		Logger.logInfo(resolved);
		return resolved; 
	}

	/**
	 * @param file - the name of the file, as saved to the repo (including extension)
	 * @return - the direct link
	 */
	public static String getStaticCreeperhostLink(String file) {
		String resolved = "";
		if(Settings.getSettings().getDownloadServer().equalsIgnoreCase("automatic")) {
			resolved = "http://www.creeperrepo.net/static/FTB2/" + file;
			HttpURLConnection connection = null;
			try {
				int retries = 0;
				connection = (HttpURLConnection) new URL(resolved).openConnection();
				while(connection.getResponseCode() != 200 && retries < 3) {
					connection.disconnect();
					switch(retries) {
					case 0:
						resolved = "http://england1.creeperrepo.net/static/FTB2/" + file;
						break;
					case 1:
						resolved = "http://chicago1.creeperrepo.net/static/FTB2/" + file;
						break;
					case 2:
						resolved = "http://chicago2.creeperrepo.net/static/FTB2/" + file;
						break;
					}
					connection = (HttpURLConnection) new URL(resolved).openConnection();
					retries++;
				}
			} catch (IOException e) { }
			connection.disconnect();
		} else {
			resolved = "http://" + DownloadUtils.getServerSite(Settings.getSettings().getDownloadServer()) + "/static/FTB2/";
		}
		Logger.logInfo(resolved);
		return resolved;
	}

	/**
	 * @param input - String to hash
	 * @return - hashed string
	 * @throws NoSuchAlgorithmException - in case "MD5" isnt a correct input
	 */
	public static String md5(String input) throws NoSuchAlgorithmException {
		String result = input;
		if (input != null) {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(input.getBytes());
			BigInteger hash = new BigInteger(1, md.digest());
			result = hash.toString(16);
			while (result.length() < 32) {
				result = "0" + result;
			}
		}
		return result;
	}

	/**
	 * gets the time from the creeperhost servers
	 * @return - the time in the DDMMYY format
	 */
	public static String getTime() {
		String content = null;
		Scanner scanner = null;
		String resolved = "";
		HttpURLConnection connection = null;
		if(Settings.getSettings().getDownloadServer().equalsIgnoreCase("automatic")) {
			try {
				int retries = 0;
				resolved = "http://www.creeperrepo.net/getdate";
				connection = (HttpURLConnection) new URL(resolved).openConnection();
				while(connection.getResponseCode() != 200 && retries < 3) {
					connection.disconnect();
					switch(retries) {
					case 0:
						resolved = "http://england1.creeperrepo.net/getdate";
						break;
					case 1:
						resolved = "http://chicago1.creeperrepo.net/getdate";
						break;
					case 2:
						resolved = "http://chicago2.creeperrepo.net/getdate";
						break;
					}
					connection = (HttpURLConnection) new URL(resolved).openConnection();
					retries++;
				}
				scanner = new Scanner(connection.getInputStream());
				scanner.useDelimiter( "\\Z" );
				content = scanner.next();
				connection.disconnect();
			} catch (java.net.UnknownHostException uhe) {
			} catch (Exception ex) {
			} finally {
				if (scanner != null) {
					scanner.close();
				}
			}
		} else {
			resolved = "http://" + Settings.getSettings().getDownloadServer() + "/getdate";
			try {
				connection = (HttpURLConnection) new URL(resolved).openConnection();
				scanner = new Scanner(connection.getInputStream());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			scanner.useDelimiter( "\\Z" );
			content = scanner.next();
			connection.disconnect();
		}
		return content;
	}

	/**
	 * Downloads data from the given URL and saves it to the given file
	 * @param filename - String of destination
	 * @param urlString - http location of file to download
	 */
	public static void downloadToFile(String filename, String urlString) throws IOException {
		downloadToFile(new URL(urlString), new File(filename));
	}

	/**
	 * Downloads data from the given URL and saves it to the given file
	 * @param url The url to download from
	 * @param file The file to save to.
	 */
	public static void downloadToFile(URL url, File file) throws IOException {
		ReadableByteChannel rbc = Channels.newChannel(url.openStream());
		FileOutputStream fos = new FileOutputStream(file);
		fos.getChannel().transferFrom(rbc, 0, 1 << 24);
	}

	/**
	 * Checks the file for corruption.
	 * @param file - File to check
	 * @return boolean representing if it is valid
	 * @throws IOException 
	 */
	public static boolean isValid(File file, String url) throws IOException {
		String content = null;
		Scanner scanner = null;
		String resolved = "http://www.creeperrepo.net/md5/FTB2/" + url;
		HttpURLConnection connection = null;
		try {
			int retries = 0;
			connection = (HttpURLConnection) new URL(resolved).openConnection();
			while(connection.getResponseCode() != 200 && retries < 3) {
				connection.disconnect();
				switch(retries) {
				case 0:
					resolved = "http://england1.creeperrepo.net/md5/FTB2/" + url;
					break;
				case 1:
					resolved = "http://chicago1.creeperrepo.net/md5/FTB2/" + url;
					break;
				case 2:
					resolved = "http://chicago2.creeperrepo.net/md5/FTB2/" + url;
					break;
				}
				connection = (HttpURLConnection) new URL(resolved).openConnection();
				retries++;
			}
			scanner = new Scanner(connection.getInputStream());
			scanner.useDelimiter( "\\Z" );
			content = scanner.next();
			connection.disconnect();
		} catch (IOException e) { 
		} finally {
			if (scanner != null) {
				scanner.close();
			}
		}
		String result = fileMD5(file);
		Logger.logInfo("Local: " + result.toUpperCase());
		Logger.logInfo("Remote: " + content.toUpperCase());
		return content.equalsIgnoreCase(result);
	}

	/**
	 * Gets the md5 of the downloaded file
	 * @param file - File to check
	 * @return - string of file's md5
	 * @throws IOException 
	 */
	private static String fileMD5(File file) throws IOException {
		URL fileUrl = file.toURI().toURL();
		MessageDigest dgest = null;
		try {
			dgest = MessageDigest.getInstance("md5");
		} catch (NoSuchAlgorithmException e) { }
		InputStream str = fileUrl.openStream();
		byte[] buffer = new byte[65536];
		int readLen;
		while((readLen = str.read(buffer, 0, buffer.length)) != -1) {
			dgest.update(buffer, 0, readLen);
		}
		str.close();
		Formatter fmt = new Formatter();    
		for(byte b : dgest.digest()) { 
			fmt.format("%02X", b);    
		}
		return fmt.toString();
	}
	
	/**
	 * gets and stores all the servers that the downloads are hosted on
	 */
	public static void getDownloadSites() {
		URL creeperhost = null;
		BufferedReader creeperReader = null;
		try {
			creeperhost = new URL("http://www.creeperrepo.net/mirrors");
			creeperReader = new BufferedReader(new InputStreamReader(creeperhost.openStream()));
		} catch (IOException e) {
			Logger.logError(e.getMessage(), e);
		}
		
		for(int i = 0; i < readStrings.length; i++) {
			try {
				readStrings[i] = creeperReader.readLine();
			} catch (IOException e) {
				Logger.logError(e.getMessage(), e);
			}
		}
		
		for(int i = 0; i < readStrings.length; i++) {
			String[] temp = readStrings[i].split(",");
			
			names[i] = temp[0];
			sites[i] = temp[1];
		}
	}
	
	public static String[] getServerNames() {
		return names;
	}
	
	public static String[] getServerSite() {
		return sites;
	}
	
	public static String getServerSite(String name) {
		
		for(int i = 0; i < names.length; i++) {
			if(names[i].equalsIgnoreCase(name)) {
				return sites[i];
			}
		}
		return "";
	}
}
