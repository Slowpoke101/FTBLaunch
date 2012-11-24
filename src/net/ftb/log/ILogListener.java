package net.ftb.log;

public interface ILogListener {
	/**
	 * Called when a log entry is added.
	 * May be called from any thread!
	 * @param logEntry the date of the log entry
	 */
	public void onLogEvent(LogEntry logEntry);
}
