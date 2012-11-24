package net.ftb.log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LogEntry {
	private String message = "";
	public LogLevel level = LogLevel.INFO;
	public LogSource source = LogSource.LAUNCHER;
	private Throwable cause;
	private final String location;
	private final String dateString;
	private final Date date;
	private final Map<LogType, String> messageCache = new HashMap<LogType, String>();
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	public LogEntry() {
		this.date = new Date();
		this.dateString = dateFormat.format(date);
		this.location = getLocation(cause);
	}

	public LogEntry message(String message) {
		this.message = message;
		if (level == LogLevel.UNKNOWN) {

		}
		return this;
	}

	public LogEntry level(LogLevel level) {
		this.level = level;
		return this;
	}

	public LogEntry source(LogSource source) {
		this.source = source;
		return this;
	}

	public LogEntry cause(Throwable cause) {
		this.cause = cause;
		return this;
	}

	public LogEntry copyInformation(LogEntry entry) {
		this.message = entry.message;
		this.source = entry.source;
		this.level = entry.level;
		return this;
	}

	public String toString() {
		return toString(LogType.MINIMAL);
	}

	public String toString(LogType type) {
		if (messageCache.containsKey(type)) {
			return messageCache.get(type);
		}
		StringBuilder entryMessage = new StringBuilder();
		if (source != LogSource.EXTERNAL) {
			if (type == LogType.EXTENDED || type == LogType.DEBUG) {
				entryMessage.append("[").append(dateString).append("] ");
			}
			if (type == LogType.DEBUG) {
				entryMessage.append("in ").append(source).append(" ");
			}
			if (location != null && type == LogType.EXTENDED || type == LogType.DEBUG) {
				entryMessage.append(location).append(": ");
			}
		}
		entryMessage.append(message);
		String message = entryMessage.toString();
		messageCache.put(type, message);
		return message;
	}

	private static String getLocation(Throwable t) {
		String location = null;
		if (t == null) {
			t = new Throwable();
		}
		for (StackTraceElement ste : t.getStackTrace()) {
			if (!ste.getClassName().equals(Logger.class.getName()) && !ste.getClassName().equals(LogEntry.class.getName())) {
				location = ste.getClassName().substring(ste.getClassName().lastIndexOf('.') + 1) + "." + ste.getMethodName() + ":" + ste.getLineNumber();
				break;
			}
		}
		return location;
	}
}
