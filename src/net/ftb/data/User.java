package net.ftb.data;

public class User {
	private String _username = "";
	private String _password = "";
	private String _name = "";

	public User(String username, String password, String name) {
		_username = username;
		_password = password;
		_name = name;
	}
	
	public User(String input) {
		String[] tokens = input.split(":");
		_name = tokens[0];
		_username = tokens[1];
		_password = tokens[2];
	}
	
	public String getUsername() {
		return _username;
	}
	
	public void setUsername(String username) {
		_username = username;
	}
	
	public String getPassword() {
		return _password;
	}
	
	public void setPassword(String password) {
		_password = password;
	}
	
	public String getName() {
		return _name;
	}
	
	public void setName(String name) {
		_name = name;
	}
	
	@Override
	public String toString() {
		return _name + ":" + _username + ":" + _password; 
	}
}
