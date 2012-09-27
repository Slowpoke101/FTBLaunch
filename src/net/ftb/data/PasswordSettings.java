package net.ftb.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordSettings {
	private File _filename;
	
	private String _username = "";
	private String _password = "";
	
	public PasswordSettings(File filename) {
		_filename = filename;
		if (_filename.exists()) {
			try {
				read();
			} catch (IOException e) { }
		}
	}
	
	public void flush() throws IOException{
		BufferedWriter wri = new BufferedWriter(new FileWriter(_filename));
		wri.write("");
		wri.close();
	}
	
	public String getHex(String str) {
		try {
			return String.format("%040x", new BigInteger(str.getBytes("utf8")));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	public String fromHex(String str) {
		try {
			return new String(new BigInteger(str, 16).toByteArray(), "utf8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	public String fromHexThing(String str) {
		BigInteger in;
		in = new BigInteger(str, 16).xor(new BigInteger(1, getSelfMD5()));
		try {
			return new String(in.toByteArray(), "utf8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	public String getHexThing(String str) {
		BigInteger str2;
		try {
			str2 = new BigInteger(str.getBytes("utf8")).xor(new BigInteger(1, getSelfMD5()));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
		};
		return String.format("%040x", str2);
	}
	
	public void write() throws IOException {
		BufferedWriter wri = new BufferedWriter(new FileWriter(_filename));
		wri.write(getHexThing(getHex(_username) + ":" + getHex(_password)));
		wri.close();
	}
	
	public void read() throws IOException {
		try {
			BufferedReader read = new BufferedReader(new FileReader(_filename));
			String str = fromHexThing(read.readLine());
			String[] tokens = str.split(":");
			_username = fromHex(tokens[0]);
			_password = fromHex(tokens[1]);
			read.close();
		} catch (Exception ex) {
			_username = "";
			_password = "";
			System.out.println("Error loading login data");
		}
	}
	
	public void storeUP(String username, String password) {
		try {
			_username = username;
			_password = password;
			write();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getUsername() {
		return _username;
		
	}
	
	public String getPassword() {
		return _password;
	}
	
	public static byte[] getFileMD5(URL string) throws Exception {
		MessageDigest dgest = null;
		try {
			dgest = MessageDigest.getInstance("md5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		InputStream str = string.openStream();
		
		byte[] buffer = new byte[65536];
		int readLen = 0;
		while ((readLen = str.read(buffer, 0, buffer.length)) != -1) {
			dgest.update(buffer, 0, readLen);
		}
		
		str.close();
		
		return dgest.digest();
	}
	
	public static byte[] getSelfMD5() {
		try {
			return getFileMD5(ClassLoader.getSystemClassLoader().getResource("net/ftb/gui/LauncherFrame.class"));
		} catch (Exception e) {
			return new byte[] {};
		}
	}
}
