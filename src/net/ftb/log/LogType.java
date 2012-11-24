package net.ftb.log;

public enum LogType {
	DEBUG,
	EXTENDED,
	MINIMAL;

	public String toString() {
		return name().substring(0, 1) + name().substring(1).toLowerCase();
	}
}
