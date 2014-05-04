/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2013, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
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

import java.io.PrintStream;

import lombok.Setter;

import net.ftb.log.ILogListener;
import net.ftb.log.LogEntry;
import net.ftb.log.LogLevel;
import net.ftb.log.LogSource;
import net.ftb.log.LogType;

public class StdOutLogger implements ILogListener {
    // save real System.out and System.err
    // otherwise we'll got nasty loop
    private final static PrintStream realStderr = System.err;
    private final static PrintStream realStdout = System.out;
    
    // DEBUG, EXTENTED, MINIMAL
    // how to write. Debug is only needed if we want to
    // see source of the log message. Hardcoded to EXTENDED
    private LogType logType = LogType.EXTENDED;

    // ALL, LAUNCHER, EXTERNAL
    // which sources to write
    @Setter
    private LogSource logSource = LogSource.LAUNCHER;

    //INFO, WARN, ERROR, UNKNOWN
    // which severities to write. Not used in Console or LogWriter
    @Setter
    private LogLevel logLevel = LogLevel.UNKNOWN;

    public StdOutLogger() {
    }

    public StdOutLogger(LogSource logSource) {
        this.logSource = logSource;
    }

    @Override
    public void onLogEvent(LogEntry entry) {
        if (entry.source != logSource)
            return;

        if (entry.level == LogLevel.ERROR) {
            realStderr.println(entry.toString(logType));
        } else {
            realStdout.println(entry.toString(logType));
        }
    }
}
