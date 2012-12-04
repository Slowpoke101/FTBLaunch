package net.ftb.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class LogWriter implements ILogListener {
	private final BufferedWriter logWriter;
	private final LogSource source;

	public LogWriter(File logFile, LogSource source) throws IOException {
		this.source = source;
		this.logWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile), "UTF-8"));
	}

	@Override
	public void onLogEvent(LogEntry entry) {
		if (entry.source == source) {
			try {
				logWriter.write(entry.toString(LogType.EXTENDED) + System.getProperty("line.separator"));
				logWriter.flush();
			} catch (IOException e) {
				Logger.logError(e.getMessage(), e);
			}
		}
	}
}
