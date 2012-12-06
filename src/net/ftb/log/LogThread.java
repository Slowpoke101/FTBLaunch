package net.ftb.log;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LogThread extends Thread {
	private BlockingQueue<LogEntry> logQueue = new LinkedBlockingQueue<LogEntry>();
	private List<ILogListener> listeners;

	public LogThread(List<ILogListener> listeners) {
		this.listeners = listeners;
		this.setDaemon(true);
	}

	public void run() {
		LogEntry entry;
		try {
			while((entry = logQueue.take()) != null) {
				if (listeners.isEmpty()) {
					(entry.level == LogLevel.ERROR ? System.err : System.out).println(entry.toString(LogType.EXTENDED));
				} else {
					List<ILogListener> tempListeners = listeners;
					for (ILogListener listener : tempListeners) {
						listener.onLogEvent(entry);
					}
				}
			}
		} catch (InterruptedException ignored) {
			Logger.logError(ignored.getMessage(), ignored);
		}
	}

	public void handleLog(LogEntry logEntry) {
		try {
			logQueue.put(logEntry);
		} catch (InterruptedException ignored) {
			Logger.logError(ignored.getMessage(), ignored);
		}
	}
}
