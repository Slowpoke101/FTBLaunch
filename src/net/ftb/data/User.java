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
	
	public String getUsername() {
		return _username;
	}
	
	public String getPassword() {
		return _password;
	}
	
	public String getName() {
		return _name;
	}
}
