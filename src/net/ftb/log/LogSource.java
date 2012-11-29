package net.ftb.log;

public enum LogSource {
	ALL,
	LAUNCHER,
	EXTERNAL("Minecraft");
	private String humanReadableName;

	private LogSource() {
		this(null);
	}

	private LogSource(String humanReadableName) {
		this.humanReadableName = humanReadableName;
	}

	public String toString() {
		return (humanReadableName == null) ? name().substring(0, 1) + name().substring(1).toLowerCase() : humanReadableName;
	}
}
