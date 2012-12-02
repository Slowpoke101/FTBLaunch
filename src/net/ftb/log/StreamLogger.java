package net.ftb.log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class StreamLogger extends Thread {
	private final InputStream is;
	private final LogEntry logInfo;

	private StreamLogger(InputStream from, LogEntry logInfo) {
		is = from;
		this.logInfo = logInfo;
	}

	@Override
	public void run() {
		byte buffer[] = new byte[4096];
		String logBuffer = "";
		int newLineIndex;
		int nullIndex;
		try {
			while (is.read(buffer) > 0) {
				logBuffer += new String(buffer).replace("\r\n", "\n");
				nullIndex = logBuffer.indexOf(0);
				if (nullIndex != -1) {
					logBuffer = logBuffer.substring(0, nullIndex);
				}
				while ((newLineIndex = logBuffer.indexOf("\n")) != -1) {
					Logger.log(new LogEntry().copyInformation(logInfo).message(logBuffer.substring(0, newLineIndex)));
					logBuffer = logBuffer.substring(newLineIndex + 1);
				}
				Arrays.fill(buffer, (byte) 0);
			}
		} catch (IOException e) {
			Logger.logError(e.getMessage(), e);
		}
	}

	public static void start(InputStream from, LogEntry logInfo) {
		logInfo.source(LogSource.EXTERNAL);
		StreamLogger processStreamRedirect = new StreamLogger(from, logInfo);
		processStreamRedirect.start();
	}
}