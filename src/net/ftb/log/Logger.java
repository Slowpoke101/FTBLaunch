package net.ftb.log;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Logger {
	/**
	 * Listeners that will be notified on new log entries
	 */
	private static final List<ILogListener> listeners;
	private static final Vector<LogEntry> logEntries = new Vector<LogEntry>();

	/**
	 * Default constructor
	 * creates Buffers, opens launcherLogFile etc
	 */
	static {
		listeners = new ArrayList<ILogListener>();
	}

	public static void log(LogEntry entry) {
		logEntries.add(entry);
		LogThread logThread = new LogThread(entry, listeners);
		logThread.start();
	}

	public static void log(String message, LogLevel level, Throwable t) {
		log(new LogEntry().message(message).cause(t).level(level));
	}

	public static void logInfo(String message) {
		logInfo(message, null);
	}

	public static void logWarn(String message) {
		logWarn(message, null);
	}

	public static void logError(String message) {
		logError(message, null);
	}

	public static void logInfo(String message, Throwable t) {
		log(message, LogLevel.INFO, t);
	}

	public static void logWarn(String message, Throwable t) {
		log(message, LogLevel.WARN, t);
	}

	public static void logError(String message, Throwable t) {
		log(message, LogLevel.ERROR, t);
	}

	public static void addListener(ILogListener listener) {
		listeners.add(listener);
	}

	public static void removeListener(ILogListener listener) {
		listeners.remove(listener);
	}

	public static List<LogEntry> getLogEntries() {
		return new Vector<LogEntry>(logEntries);
	}

	public static String getLogs() {
		return getLogs(LogType.EXTENDED);
	}

	private static String getLogs(LogType type) {
		StringBuilder logStringBuilder = new StringBuilder();
		for (LogEntry entry : logEntries) {
			logStringBuilder.append(entry.toString(type)).append("\n");
		}
		return logStringBuilder.toString();
	}
}
