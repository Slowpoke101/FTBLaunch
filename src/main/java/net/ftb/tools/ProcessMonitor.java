/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2018, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
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
package net.ftb.tools;

import lombok.Getter;
import lombok.Setter;
import net.ftb.log.Logger;
import net.ftb.util.OSUtils;

public class ProcessMonitor implements Runnable {

    private final Process proc;
    private final Runnable onComplete;
    @Getter
    @Setter
    long pid;

    private volatile boolean complete = false;

    private ProcessMonitor (Process proc, Runnable onComplete) {
        this.proc = proc;
        this.onComplete = onComplete;
        this.pid = OSUtils.getPID(proc);
    }

    public void run () {
        try {
            proc.waitFor();
            Logger.logInfo("MC process exited. return value: " + proc.exitValue());
        } catch (InterruptedException e) {
            Logger.logError("ProcessMonitor interrupted", e);
        }
        complete = true;
        onComplete.run();
    }

    public static ProcessMonitor create (Process proc, Runnable onComplete) {
        ProcessMonitor processMonitor = new ProcessMonitor(proc, onComplete);
        Thread monitorThread = new Thread(processMonitor);
        monitorThread.start();
        return processMonitor;
    }

    public void stop () {
        if (proc != null) {
            proc.destroy();
        }
    }

}
