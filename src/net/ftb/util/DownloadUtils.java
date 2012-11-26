package net.ftb.util;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import net.ftb.log.Logger;

public class DownloadUtils {
	private static String currentmd5 = "";
	
	/**
	 * @param file - the name of the file, as saved to the repo (including extension)
	 * @return - the direct link
	 * @throws NoSuchAlgorithmException - see md5
	 */
	public static String getCreeperhostLink(String file) throws NoSuchAlgorithmException {
		if(currentmd5.isEmpty()) {
			currentmd5 = md5("mcepoch1" + getTime());
		}
		String resolved = "http://repo.creeperhost.net/direct/FTB2/" + currentmd5 + "/" + file;
		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) new URL(resolved).openConnection();
		} catch (MalformedURLException e1) {
		} catch (IOException e1) { }
		try {
			int retries = 1;
			while(connection.getResponseCode() != 200 && retries <= 3) {
				connection.disconnect();
				resolved = "http://repo" + retries + ".creeperhost.net/direct/FTB2/" + currentmd5 + "/" + file;
				retries++;
				try {
					connection = (HttpURLConnection) new URL(resolved).openConnection();
				} catch (MalformedURLException e) {
				} catch (IOException e) { }
			}
		} catch (IOException e) { }
		connection.disconnect();
		Logger.logInfo(resolved);
		return resolved; 
	}
	
	/**
	 * @param file - the name of the file, as saved to the repo (including extension)
	 * @return - the direct link
	 */
	public static String getStaticCreeperhostLink(String file) {
		String resolved = "http://repo.creeperhost.net/static/FTB2/" + file;
		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) new URL(resolved).openConnection();
		} catch (MalformedURLException e1) {
		} catch (IOException e1) { }
		try {
			int retries = 1;
			while(connection.getResponseCode() != 200 && retries <= 3) {
				connection.disconnect();
				resolved = "http://repo" + retries + ".creeperhost.net/static/FTB2/" + file;
				retries++;
				try {
					connection = (HttpURLConnection) new URL(resolved).openConnection();
				} catch (MalformedURLException e) {
				} catch (IOException e) { }
			}
		} catch (IOException e) { }
		connection.disconnect();
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
		int retries = 1;
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL("http://repo.creeperhost.net/getdate").openConnection();
			while(connection.getResponseCode() != 200 && retries <= 3) {
				connection.disconnect();
				connection = (HttpURLConnection) new URL("http://repo" + retries + ".creeperhost.net/getdate").openConnection();
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
		return content;
	}
	
	/**
	 * Downloads the file to the destination
	 * @param filename - String of destination
	 * @param urlString - http location of file to download
	 */
	public static void downloadUrl(String filename, String urlString) throws IOException {
		BufferedInputStream in = null;
		FileOutputStream fout = null;
		try {
			in = new BufferedInputStream(new URL(urlString).openStream());
			fout = new FileOutputStream(filename);
			byte data[] = new byte[1024];
			int count;
			while ((count = in.read(data, 0, 1024)) != -1) {
				fout.write(data, 0, count);
			}
		} finally {
			if (in != null) {
				in.close();
			}
			if (fout != null) {
				fout.flush();
				fout.close();
			}	
		}
	}
}
