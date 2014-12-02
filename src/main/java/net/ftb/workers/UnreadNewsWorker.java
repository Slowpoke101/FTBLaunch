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
package net.ftb.workers;

import java.util.ArrayList;

import javax.swing.SwingWorker;

import net.ftb.data.Settings;
import net.ftb.util.Benchmark;
import net.ftb.util.NewsUtils;

/**
 * SwingWorker that checks for unread news. Returns count of unread news
 * done() updates news icon
 */
public class UnreadNewsWorker extends SwingWorker<Integer, Void> {
    @Override
    protected Integer doInBackground () {
        Benchmark.start("UnreadNews");
        int i = 0;
        ArrayList<String> dates = NewsUtils.getPubDates();
        Long lastRead = Long.parseLong(Settings.getSettings().getNewsDate());
        int read = 0;
        for (String s : dates) {
            long l = Long.parseLong(s);
            if (!(read == 1)) {
                if (l > lastRead) {
                    i++;
                } else {
                    read = 1;
                }
            }
        }
        Benchmark.logBenchAs("UnreadNews", "UnreadNews Init");
        return i;

    }
}
