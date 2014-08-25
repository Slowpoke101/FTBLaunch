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

import com.google.common.collect.Maps;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LogEntry {
    private String message = "";
    public LogLevel level = LogLevel.UNKNOWN;
    public LogSource source = LogSource.LAUNCHER;
    private Throwable cause;
    private String location;
    private final String dateString;
    private final Map<LogType, String> messageCache = Maps.newHashMap();
    private static final String dateFormatString = "HH:mm:ss";

    public LogEntry() {
        Date date = new Date();
        this.dateString = new SimpleDateFormat(dateFormatString).format(date);
        this.location = getLocation(cause);
    }

    public LogEntry message (String message) {
        this.message = message;
        if (level == LogLevel.UNKNOWN) {
            message = message.toLowerCase();
            if (message.contains("[severe]") || message.contains("[stderr]") || message.contains("[error]")) {
                level = LogLevel.ERROR;
            } else if (message.contains("[info]")) {
                level = LogLevel.INFO;
            } else if (message.contains("[warning]") || message.contains("[warn]")) {
                level = LogLevel.WARN;
            } else if (message.contains("error") || message.contains("severe")) {
                level = LogLevel.ERROR;
            } else if (message.contains("warn")) {
                level = LogLevel.WARN;
            } else {
                level = LogLevel.INFO;
            }
        }
        return this;
    }

    public LogEntry level (LogLevel level) {
        this.level = level;
        return this;
    }

    public LogEntry source (LogSource source) {
        this.source = source;
        return this;
    }

    public LogEntry cause (Throwable cause) {
        if (cause != this.cause) {
            this.location = getLocation(cause);
        }
        this.cause = cause;
        return this;
    }

    public LogEntry copyInformation (LogEntry entry) {
        this.message = entry.message;
        this.source = entry.source;
        this.level = entry.level;
        return this;
    }

    public String toString () {
        return toString(LogType.MINIMAL);
    }

    public String toString (LogType type) {
        if (messageCache.containsKey(type)) {
            return messageCache.get(type);
        }
        StringBuilder entryMessage = new StringBuilder();
        if (source != LogSource.EXTERNAL) {
            if (type.includes(LogType.EXTENDED)) {
                entryMessage.append("[").append(dateString).append("] ");
            }
            if (type.includes(LogType.EXTENDED)) {
                entryMessage.append("[").append(level).append("] ");
            }
            if (type.includes(LogType.DEBUG)) {
                entryMessage.append("in ").append(source).append(" ");
            }
            if (location != null && type.includes(LogType.EXTENDED)) {
                entryMessage.append(location).append(": ");
            }
        }
        entryMessage.append(message);
        if (cause != null) {
            entryMessage.append(": ").append(cause.toString());
            if (type.includes(LogType.EXTENDED)) {
                for (StackTraceElement stackTraceElement : cause.getStackTrace()) {
                    entryMessage.append("\n").append(stackTraceElement.toString());
                }
            }
        }
        String message = entryMessage.toString();
        messageCache.put(type, message);
        return message;
    }

    private static String getLocation (Throwable t) {
        String location = "";
        if (t != null) {
            location += getLocation(t.getStackTrace()) + "->";
        }
        location += getLocation(new Throwable().getStackTrace());
        return location;
    }

    private static String getLocation (StackTraceElement[] stackTraceElements) {
        for (StackTraceElement ste : stackTraceElements) {
            if (!ste.getClassName().equals(Logger.class.getName()) && !ste.getClassName().equals(LogEntry.class.getName())) {
                return ste.getClassName().substring(ste.getClassName().lastIndexOf('.') + 1) + "." + ste.getMethodName() + ":" + ste.getLineNumber();
            }
        }
        return "unknown location";
    }
}
