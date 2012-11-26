package net.ftb.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import net.ftb.data.Settings;

public class LogThread extends Thread {
	private final static String launcherLogFile = "FTBLauncherLog.txt";
	private final static String minecraftLogFile = "MinecraftLog.txt";
	private static BufferedWriter launcherLogWriter;
	private static BufferedWriter minecraftLogWriter;
	private static List<ILogListener> listeners;
	private LogEntry entry = null;

	public LogThread(LogEntry entry, List<ILogListener> listeners) {
		this.entry = entry;
		this.listeners = listeners;
	}

	public void run() {
		try {
			launcherLogWriter = new BufferedWriter(new FileWriter(new File(Settings.getSettings().getInstallPath(), launcherLogFile)));
			minecraftLogWriter = new BufferedWriter(new FileWriter(new File(Settings.getSettings().getInstallPath(), minecraftLogFile)));
		} catch (IOException e1) { }

		BufferedWriter logWriter = launcherLogWriter;
		if (entry.source == LogSource.EXTERNAL) {
			logWriter = minecraftLogWriter;
		}
		try {
			logWriter.write(entry.toString(LogType.EXTENDED) + System.getProperty("line.separator"));
			logWriter.flush();
		} catch (IOException e) { }
		for (ILogListener listener : listeners) {
			listener.onLogEvent(entry);
		}
	}
}