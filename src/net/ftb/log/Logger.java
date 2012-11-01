package net.ftb.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


import net.ftb.util.OSUtils;
import net.ftb.util.PathUtils;

public class Logger {
	private static Logger instance = null;
	private final static String Logfile = "FTBLauncherLog.txt";
	
	private final static String StringInfo = "INFO";
	private final static String StringWarn = "WARN";
	private final static String StringError = "ERROR";
	
	
	private int infoCount = 0;
	private int errorCount = 0;
	private int warnCount = 0;
	
	private BufferedWriter fileoutwrite = null;
	
	private StringBuffer logbuffer;
	private StringBuffer logbufferExtensive;
	
	private List<ILogListener> listeners;
	
	
	public Logger() {
		logbuffer = new StringBuffer();
		logbufferExtensive = new StringBuffer();
		listeners = new ArrayList<ILogListener>();
		
		FileWriter fstream = null;
		try {
			fstream = new FileWriter(new File(PathUtils.combine(OSUtils.getDefInstallPath(),Logfile)));
			fileoutwrite = new BufferedWriter(fstream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String getDate() {
		SimpleDateFormat dateFormatGmt = new SimpleDateFormat("HH:mm:ss");
		dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
		return  dateFormatGmt.format(new Date());
	}
	private String getSource() {
		String source = "Unknown";
		
		Throwable t = new Throwable();
		for (StackTraceElement ste : t.getStackTrace()) {
			
			if (!ste.getClassName().equals(Logger.class.getName())  ) {
				source = ste.getClassName()+"."+ste.getMethodName()+":"+ste.getLineNumber();
				break;
			}
		}
		
		return source;
	}
	
	private void doLog(String message, String level, Throwable t) {

		if (level.equals(Logger.StringError))
			errorCount++;
		if (level.equals(Logger.StringInfo))
			infoCount++;
		if (level.equals(Logger.StringWarn))
			warnCount++;
		
		String date =  getDate();
		String source = getSource();
		
		String shortVersion = "["+level+"] "+message;
		String longVersion =  date+ " ["+level+"] "+source +" "+message;
		
		try {
			fileoutwrite.write(longVersion+"\r\n");
			fileoutwrite.flush();
		} catch (IOException e) {
			// for now disabled, will get stackoverlfow as long system.err is redirected by launchlog
		}
		
		logbuffer.append(shortVersion+"\n");
		logbufferExtensive.append(longVersion+"\n");

		
		if (t != null) {
			Writer trace = new StringWriter();
			PrintWriter printWriter = new PrintWriter(trace);
			t.printStackTrace(printWriter);
			logbufferExtensive.append(trace.toString()+"\n");
		}
		
		
		
		
		
		for (ILogListener listener : listeners)
			listener.onLogEvent(date, source, level, message);
	}
	
	/*
	 * Log with dynamic Log Level (only use INFO,WARN,ERROR!
	 */
	public static void log(String message, String level, Throwable t) {
		Logger.getInstance().doLog(message, level, t);
	}
	
	public static void logInfo(String message) {
		log(message,Logger.StringInfo,null);
	}
	public static  void logWarn(String message) {
		log(message,Logger.StringWarn,null);
	}
	public static  void logError(String message) {
		log(message,Logger.StringError,null);
	}

	public static void logInfo(String message, Throwable t) {
		log(message,Logger.StringInfo,t);
	}
	public static  void logWarn(String message, Throwable t) {
		log(message,Logger.StringWarn,t);
	}
	public static  void logError(String message, Throwable t) {
		log(message,Logger.StringError,t);
	}

	
	public static void addListener(ILogListener listener) {
		getInstance().listeners.add(listener);
	}
	public static void removeListener(ILogListener listener) {		
		getInstance().listeners.remove(listener);
	}
	
	public static Logger getInstance() {
		if (instance == null)
			instance = new Logger();
		return instance;
	}

	public StringBuffer getLogbuffer() {
		return logbuffer;
	}

	public StringBuffer getLogbufferExtensive() {
		return logbufferExtensive;
	}

	public int getInfoCount() {
		return infoCount;
	}

	public int getErrorCount() {
		return errorCount;
	}

	public int getWarnCount() {
		return warnCount;
	}
	
	
}
