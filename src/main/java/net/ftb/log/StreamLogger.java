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
    public void run () {
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
            Logger.logError("Error while reading log messages from external source(minecraft process)", e);
        }
    }

    public static void start (InputStream from, LogEntry logInfo) {
        logInfo.source(LogSource.EXTERNAL);
        StreamLogger processStreamRedirect = new StreamLogger(from, logInfo);
        processStreamRedirect.start();
    }
}
