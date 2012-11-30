package net.ftb.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import net.ftb.data.Settings;

public class LogThread extends Thread {
	private final static String launcherLogFile = "FTBLauncherLog.txt";
	private final static String minecraftLogFile = "MinecraftLog.txt";

	private BlockingQueue<LogEntry> logQueue = new LinkedBlockingQueue<LogEntry>();
	private BufferedWriter launcherLogWriter;
	private BufferedWriter minecraftLogWriter;
	private List<ILogListener> listeners;

	public LogThread(List<ILogListener> listeners) {
		this.listeners = listeners;
		this.setDaemon(true);
	}

	public void run() {
		try {
			launcherLogWriter = new BufferedWriter(new FileWriter(new File(Settings.getSettings().getInstallPath(), launcherLogFile), true));
			minecraftLogWriter = new BufferedWriter(new FileWriter(new File(Settings.getSettings().getInstallPath(), minecraftLogFile), true));
		} catch (IOException e1) { }

		LogEntry entry;
		try {
			while((entry = logQueue.take()) != null) {
				BufferedWriter logWriter = entry.source == LogSource.EXTERNAL ? minecraftLogWriter : launcherLogWriter;
				try {
					logWriter.write(entry.toString(LogType.EXTENDED) + System.getProperty("line.separator"));
					logWriter.flush();
				} catch (IOException e) { }
				for (ILogListener listener : listeners) {
					listener.onLogEvent(entry);
				}
			}
		} catch (InterruptedException ignored) { }
	}

	public void handleLog(LogEntry logEntry) {
		try {
			logQueue.put(logEntry);
		} catch (InterruptedException ignored) { }
	}
}
