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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LogThread extends Thread {
    private BlockingQueue<LogEntry> logQueue = new LinkedBlockingQueue<LogEntry>();
    private List<ILogListener> listeners;

    public LogThread(List<ILogListener> listeners) {
        this.listeners = listeners;
        this.setDaemon(true);
    }

    public void run () {
        LogEntry entry;
        setName("Log dispatcher");
        try {
            while ((entry = logQueue.take()) != null) {
                if (!listeners.isEmpty()) {
                    List<ILogListener> tempListeners = new ArrayList<ILogListener>();
                    tempListeners.addAll(listeners);
                    for (ILogListener listener : tempListeners) {
                        listener.onLogEvent(entry);
                    }
                }
            }
        } catch (InterruptedException ignored) { }
    }

    public void handleLog (LogEntry logEntry) {
        try {
            logQueue.put(logEntry);
        } catch (InterruptedException ignored) { }
    }
}
