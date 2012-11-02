package net.ftb.log;



/**
 * This interfaces enables classes to receive log events 
 * 
 */

public interface ILogListener {
	/**
	 * Notifies once a Log entry has been added
	 * @param date the date of the log entry
	 * @param source Source of the Log (class and function)
	 * @param level	either INFO, WARN or ERROR
	 * @param msg the message text
	 */
	public void onLogEvent(String date, String source, String level, String msg);
}
