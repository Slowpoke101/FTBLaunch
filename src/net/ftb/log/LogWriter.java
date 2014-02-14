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
    public void onLogEvent (LogEntry entry) {
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
