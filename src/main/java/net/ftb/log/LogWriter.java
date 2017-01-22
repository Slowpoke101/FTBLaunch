/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2017, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class LogWriter implements ILogListener {
    private final BufferedWriter logWriter;
    private final LogSource source;

    public LogWriter (File logFile, LogSource source) throws IOException {
        this.source = source;
        this.logWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile), "UTF-8"));
        this.logWriter.write(logFile + ": written by FTB Launcher" + System.getProperty("line.separator"));
        this.logWriter.flush();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    logWriter.flush();
                } catch (IOException ignored) {
                    // ignored
                }
            }
        });
    }

    @Override
    public void onLogEvent (LogEntry entry) {
        if (entry.source == source) {
            try {
                logWriter.write(entry.toString(LogType.EXTENDED) + System.getProperty("line.separator"));
            } catch (IOException e) {
                // We probably do not want to trigger new errors
                // How can we notify user? Is notify needed?
                //Logger.logError("Error while writing logs", e);
            }
        }
    }
}
