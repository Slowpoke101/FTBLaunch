/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2014, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
 * FTB Launcher is licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ftb.log;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
public class Logger {
    private static final List<ILogListener> listeners;
    private static final Vector<LogEntry> logEntries;
    private static LogThread logThread;

    /**
     * Default constructor
     * creates lists for listeners and log messages, creates and starts log dispather thread
     */
    static {
        listeners = new ArrayList<ILogListener>();
        logEntries = new Vector<LogEntry>();
        logThread = new LogThread(listeners);
        logThread.start();
    }

    public static void log (LogEntry entry) {
        logEntries.add(entry);
        logThread.handleLog(entry);
    }

    public static void log (String message, LogLevel level, Throwable t) {
        log(new LogEntry().level(level).message(message).cause(t));
    }

    public static void logDebug (String message) {
        logDebug(message, null);
    }

    public static void logInfo (String message) {
        logInfo(message, null);
    }

    public static void logWarn (String message) {
        logWarn(message, null);
    }

    public static void logError (String message) {
        logError(message, null);
    }

    public static void logDebug (String message, Throwable t) {
        log(message,  LogLevel.DEBUG, t);
    }

    public static void logInfo (String message, Throwable t) {
        log(message, LogLevel.INFO, t);
    }

    public static void logWarn (String message, Throwable t) {
        log(message, LogLevel.WARN, t);
    }

    public static void logError (String message, Throwable t) {
        log(message, LogLevel.ERROR, t);
    }

    public static void addListener (ILogListener listener) {
        listeners.add(listener);
    }

    public static void removeListener (ILogListener listener) {
        listeners.remove(listener);
    }

    public static List<LogEntry> getLogEntries () {
        return new Vector<LogEntry>(logEntries);
    }

    public static String getLogs () {
        return getLogs(LogType.EXTENDED);
    }

    private static String getLogs (LogType type) {
        StringBuilder logStringBuilder = new StringBuilder();
        for (LogEntry entry : logEntries) {
            logStringBuilder.append(entry.toString(type)).append("\n");
        }
        return logStringBuilder.toString();
    }
}
