package net.ftb.data;

public class User {
	private String _username = "", _password = "", _name = "";

	/**
	 * @param username - the username of the profile
	 * @param password - the password of the profile
	 * @param name - the name of the profile
	 */
	public User(String username, String password, String name) {
		_username = username;
		_password = password;
		_name = name;
	}

	/**
	 * @param input - text with username, password, name
	 */
	public User(String input) {
		String[] tokens = input.split(":");
		if(tokens.length == 3) {
			_name = tokens[0];
			_username = tokens[1];
			_password = tokens[2];
		} else if(tokens.length == 2) {
			_name = tokens[0];
			_username = tokens[1];
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

	/**
	 * @return - a string with username, password, name in
	 */
	@Override
	public String toString() {
		return _name + ":" + _username + ":" + _password; 
	}
}
