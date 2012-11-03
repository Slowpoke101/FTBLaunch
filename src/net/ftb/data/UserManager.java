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
import java.util.ArrayList;

public class UserManager {
	public static ArrayList<User> _users = new ArrayList<User>();

	private File _filename;

	public UserManager(File filename) {
		_filename = filename;
		if (_filename.exists()) {
			read();
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
		}
		return String.format("%040x", str2);
	}

	public void write() throws IOException {
		BufferedWriter wri = new BufferedWriter(new FileWriter(_filename));
		for (int i = 0; i < _users.size(); i++) {
			String str = _users.get(i).toString();
			wri.write(getHexThing(str));
			if((i+1) != _users.size()) {
				wri.newLine();
			}
		}
		wri.close();
	}

	public void read() {
		_users.clear();
		if(_filename.exists()) {
			try {
				BufferedReader read = new BufferedReader(new FileReader(_filename));
				String str;
				while((str = read.readLine()) != null) {
					str = fromHexThing(str);
					_users.add(new User(str));
				}
				read.close();
			} catch (Exception ex) {
				ex.printStackTrace();
				System.out.println("Error loading login data");
			}
		}
	}


	public static byte[] getFileMD5(URL string) throws IOException {
		MessageDigest dgest = null;
		try {
			dgest = MessageDigest.getInstance("md5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		InputStream str = string.openStream();

		byte[] buffer = new byte[65536];
		int readLen;
		while ((readLen = str.read(buffer, 0, buffer.length)) != -1) {
			dgest.update(buffer, 0, readLen);
		}
		str.close();

		return dgest.digest();
	}

	public static byte[] getSelfMD5() {
		try {
			return getFileMD5(ClassLoader.getSystemClassLoader().getResource(
					"net/ftb/gui/LauncherFrame.class"));
		} catch (Exception e) {	return new byte[] {}; }
	}

	public static void addUser(String username, String password, String name) {
		_users.add(new User(username, password, name));
	}

	public static ArrayList<String> getUsernames() {
		ArrayList<String> ret = new ArrayList<String>();
		for (User user : _users) {
			ret.add(user.getName());
		}
		return ret;
	}

	public static ArrayList<String> getNames() {
		ArrayList<String> ret = new ArrayList<String>();
		for (User user : _users) {
			ret.add(user.getName());
		}
		return ret;
	}

	public static String getUsername(String name) {
		for (User user : _users) {
			if (user.getName().equals(name)) {
				return user.getUsername();
			}
		}
		return "";
	}

	public static String getPassword(String name) {
		for (User user : _users) {
			if (user.getName().equals(name)) {
				return user.getPassword();
			}
		}
		return "";
	}

	private static User findUser(String name) {
		for (User user : _users) {
			if (user.getName().equals(name)) {
				return user;
			}
		}
		return null;
	}

	public static void removePass(String username) {
		for(User user : _users) {
			if(user.getUsername().equals(username)) {
				user.setPassword("");
				return;
			}
		}
	}

	public static void removeUser(String name) {
		User temp = findUser(name);
		if (temp != null) {
			_users.remove(_users.indexOf(temp));
		}
	}

	public static void updateUser(String oldName, String username, String password, String name) {
		User temp = findUser(oldName);
		if (temp != null) {
			_users.get(_users.indexOf(temp)).setUsername(username);
			_users.get(_users.indexOf(temp)).setPassword(password);
			_users.get(_users.indexOf(temp)).setName(name);
		}
	}
}
