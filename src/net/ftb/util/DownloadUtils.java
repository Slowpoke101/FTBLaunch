package net.ftb.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
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

	/**
	 * Checks the file for corruption.
	 * @param file - File to check
	 * @return - boolean representing if it is valid
	 * @throws IOException 
	 */
	public static boolean isValid(File file) throws IOException {
		String content = null;
		Scanner scanner = null;
		int retries = 1;
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL("http://repo.creeperhost.net/md5/FTB2/" + file.getName()).openConnection();
			while(connection.getResponseCode() != 200 && retries <= 3) {
				connection.disconnect();
				connection = (HttpURLConnection) new URL("http://repo" + retries + ".creeperhost.net/md5/FTB2/" + file.getName()).openConnection();
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
		String result = fileMD5(file);
		Logger.logInfo(result);
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
		} catch (NoSuchAlgorithmException e) { e.printStackTrace(); }

		InputStream str = fileUrl.openStream();
		byte[] buffer = new byte[65536];
		int readLen;
		while ((readLen = str.read(buffer, 0, buffer.length)) != -1) {
			dgest.update(buffer, 0, readLen);
		}
		str.close();
		Formatter fmt = new Formatter();    
		for (byte b : dgest.digest()) { 
		  fmt.format("%02X", b);    
		}
		return fmt.toString();
	}
}
