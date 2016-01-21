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

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArrayList;

public class Logger {
    private static final List<ILogListener> listeners = new CopyOnWriteArrayList<ILogListener>();
    private static final ConcurrentIterable<LogEntry> logEntries = new ConcurrentIterable<LogEntry>();
    private static LogThread logThread = new LogThread(listeners);
    private static PrintStream standardErrorPrintStream = new PrintStream(new FileOutputStream(FileDescriptor.err));

    /**
     * Default constructor
     * creates lists for listeners and log messages, creates and starts log dispather thread
     */
    static {
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
        log(message, LogLevel.DEBUG, t);
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

    /**
     * Used to log an error which occurs while logging. Logs directly to standard error
     * Use only in cases where logging properly could cause recursive errors, for example
     * an error writing to a log file -> log -> tries to write to log file
     */
    public static void logLoggingError(String error, Throwable t) {
        if (error != null) {
            standardErrorPrintStream.append(error).append('\n');
        }
        if (t != null) {
            t.printStackTrace(standardErrorPrintStream);
        }
        standardErrorPrintStream.flush();
    }

    public static void addListener (ILogListener listener) {
        listeners.add(listener);
    }

    public static void removeListener (ILogListener listener) {
        listeners.remove(listener);
    }

    public static Iterable<LogEntry> getLogEntries () {
        return logEntries;
    }

    public static String getLogs () {
        return getLogs(LogType.EXTENDED);
    }

    private static String getLogs (LogType type) {
        StringBuilder logStringBuilder = new StringBuilder();
        for (LogEntry entry : getLogEntries()) {
            logStringBuilder.append(entry.toString(type)).append("\n");
        }
        return logStringBuilder.toString();
    }

    /**
     * Simple iterable data structure which can be iterated while being modified,
     * and is backed by an array. More compact than a ConcurrentLinkedQueue
     */
    private static class ConcurrentIterable<T> implements Iterable<T> {
        int length = 0;
        private Object[] entries = new Object[0];

        @Override
        public Iterator<T> iterator () {
            return new Iterator<T>() {
                int position = 0;
                Object next = getNext();

                @Override
                public boolean hasNext () {
                    return next != null;
                }

                @Override
                public T next () {
                    Object current = next;

                    if (current == null) {
                        throw new NoSuchElementException();
                    }

                    next = getNext();
                    return (T) current;
                }

                @Override
                public void remove () {
                    throw new UnsupportedOperationException("remove");
                }

                private Object getNext () {
                    Object[] currentEntries = entries;
                    int currentLength = length;

                    if (position >= currentLength || position >= currentEntries.length) {
                        return null;
                    }

                    return currentEntries[position++];
                }
            };
        }

        public void add (T entry) {
            synchronized (this) {
                if (entries.length == length) {
                    entries = Arrays.copyOf(entries, entries.length == 0 ? 64 : (entries.length + (entries.length >> 1)));
                }
                entries[length++] = entry;
            }
        }
    }
}
