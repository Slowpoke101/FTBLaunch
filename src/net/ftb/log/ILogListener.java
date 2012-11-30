package net.ftb.log;

public interface ILogListener {
	/**
	 * Called when a log entry is added.
	 * Will be called from a Log Thread!
	 * @param logEntry the log entry
	 */
	public void onLogEvent(LogEntry logEntry);
}
