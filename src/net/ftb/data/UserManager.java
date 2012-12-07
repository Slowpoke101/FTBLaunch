package net.ftb.data;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import net.ftb.log.Logger;
import net.ftb.util.CryptoUtils;
import net.ftb.util.OSUtils;

public class UserManager {
	public final static ArrayList<User> _users = new ArrayList<User>();
	private File _file;

	public UserManager(File file) {
		_file = file;
		read();
	}

	public void write() throws IOException {
		FileOutputStream fileOutputStream = new FileOutputStream(_file);
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
		try {
			for (User user : _users) {
				objectOutputStream.writeObject(user);
			}
		} finally {
			objectOutputStream.close();
			fileOutputStream.close();
		}
	}

	public void read() {
		if (!_file.exists()) {
			return;
		}
		_users.clear();
		try {
			FileInputStream fileInputStream = new FileInputStream(_file);
			ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
			try {
				Object obj;
				while ((obj = objectInputStream.readObject()) != null) {
					if (obj instanceof User) {
						_users.add((User) obj);
					}
				}
			} catch (EOFException ignored) {
			} finally {
				objectInputStream.close();
				fileInputStream.close();
			}
		} catch (Exception e) {
			Logger.logError("Failed to decode logindata", e);
		}

		// TODO: Remove this in a while once people are unlikely to have old format saved logindata
		if (_users.isEmpty()) {
			try {
				BufferedReader read = new BufferedReader(new FileReader(_file));
				String str;
				while((str = read.readLine()) != null) {
					str = CryptoUtils.decrypt(str, OSUtils.getMacAddress());
					_users.add(new User(str));
				}
				read.close();
			} catch (Exception ex) {
				Logger.logError(ex.getMessage(), ex);
			}
		}
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