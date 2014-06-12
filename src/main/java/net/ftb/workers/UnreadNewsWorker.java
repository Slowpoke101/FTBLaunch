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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.SwingWorker;

import com.google.common.collect.Lists;
import net.ftb.data.Settings;
import net.ftb.download.Locations;
import net.ftb.log.Logger;
import net.ftb.util.Benchmark;


/**
 * SwingWorker that checks for unread news. Returns count of unread news
 * done() updates news icon
 */
public class UnreadNewsWorker extends SwingWorker<Integer, Void> {
    @Override
    protected Integer doInBackground () {
        Benchmark.start("UnreadNews");
        int i = 0;
        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(new URL(Locations.NEWSUPDATEPHP).openStream()));
            ArrayList<Long> timeStamps = Lists.newArrayList();
            String s = reader.readLine();
            s = s.trim();
            String[] str = s.split(",");
            for (String aStr : str) {
                if (!timeStamps.contains(Long.parseLong(aStr))) {
                    timeStamps.add(Long.parseLong(aStr));
                }
            }
            long l;
            if (Long.parseLong(Settings.getSettings().getNewsDate()) == 0) {
                l = Long.parseLong(Settings.getSettings().getNewsDate());
            } else {
                l = Long.parseLong(Settings.getSettings().getNewsDate().substring(0, 10));
            }
            for (Long timeStamp : timeStamps) {
                long time = timeStamp;
                if (time > l) {
                    i++;
                }
            }
            Benchmark.logBenchAs("UnreadNews", "UnreadNews Init");
        } catch (UnknownHostException e) {
            Logger.logWarn("Error while checking news: " + e.getMessage());
        } catch (Exception e) {
            Logger.logError("Error while checking news", e);
        }

        return i;
    }
}
