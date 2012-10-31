package net.ftb.log;

public interface ILogListener {
	public void onLogEvent(String date, String source, String level, String msg);
}
