package net.ftb.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Scanner;

import net.ftb.util.OSUtils;

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
	
	public void write() throws IOException {
		BufferedWriter wri = new BufferedWriter(new FileWriter(_filename));
		wri.write(getHex(getHex(_username) + ":" + getHex(_password)));
		wri.close();
	}
	
	public void read() throws IOException {
		BufferedReader read = new BufferedReader(new FileReader(_filename));
		String str = fromHex(read.readLine());
		String[] tokens = str.split(":");
		_username = fromHex(tokens[0]);
		_password = fromHex(tokens[1]);
		read.close();
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
}
