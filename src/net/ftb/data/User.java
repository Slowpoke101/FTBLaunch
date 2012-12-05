package net.ftb.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import net.ftb.util.CryptoUtils;
import net.ftb.util.OSUtils;

public class User implements Serializable {
	private static final long serialVersionUID = 1L;
	private String _username = "", _name = "", _encryptedPassword = "";
	private transient String _password = "";

	/**
	 * @param username - the username of the profile
	 * @param password - the password of the profile
	 * @param name - the name of the profile
	 */
	public User(String username, String password, String name) {
		setUsername(username);
		setPassword(password);
		setName(name);
	}

	/**
	 * @param input - text with username, password, name
	 */
	@Deprecated
	public User(String input) {
		String[] tokens = input.split(":");
		setName(tokens[0]);
		setUsername(tokens[1]);
		if(tokens.length == 3) {
			setPassword(tokens[2]);
		}
	}

	/**
	 * @return - profile username
	 */
	public String getUsername() {
		return _username;
	}

	/**
	 * @param username - set profile username
	 */
	public void setUsername(String username) {
		_username = username;
	}

	/**
	 * @return - profile password
	 */
	public String getPassword() {
		return _password;
	}

	/**
	 * @param password - set profile password
	 */
	public void setPassword(String password) {
		_password = password;
		if (_password.isEmpty()) {
			_encryptedPassword = "";
		} else {
			_encryptedPassword = CryptoUtils.encrypt(_password, OSUtils.getMacAddress());
		}
	}

	/**
	 * @return - profile name
	 */
	public String getName() {
		return _name;
	}

	/**
	 * @param name - set profile name
	 */
	public void setName(String name) {
		_name = name;
	}

	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		if (!_encryptedPassword.isEmpty()) {
			_password = CryptoUtils.decrypt(_encryptedPassword, OSUtils.getMacAddress());
		} else {
			_password = "";
		}
	}
}