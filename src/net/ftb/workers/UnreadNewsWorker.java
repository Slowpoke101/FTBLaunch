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
package net.ftb.workers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.swing.ImageIcon;
import javax.swing.SwingWorker;

import net.ftb.data.Settings;
import net.ftb.gui.ImageAndTextIcon;
import net.ftb.gui.LaunchFrame;
import net.ftb.log.Logger;



/**
 * SwingWorker that checks for unread news. Returns count of unread news
 * done() updates news icon
 */
public class UnreadNewsWorker extends SwingWorker<Integer, Void> {
    @Override
    protected Integer doInBackground () {
        //try{Thread.sleep(10000);}catch (Exception e){}
        int i = 0;
        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(new URL("http://launcher.feed-the-beast.com/newsupdate.php").openStream()));
            ArrayList<Long> timeStamps = new ArrayList<Long>();
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

        } catch (Exception e) {
            Logger.logError(e.getMessage(), e);
        }

        return i;
    }
    @Override
    protected void done() {
        try {
            int i = get();
            if (i > 0 && i < 100) {
                LaunchFrame.getInstance().tabbedPane.setIconAt(0, new ImageAndTextIcon(this.getClass().getResource("/image/tabs/news_unread_" + Integer.toString(i).length() + ".png"), Integer.toString(i)));
            } else {
                LaunchFrame.getInstance().tabbedPane.setIconAt(0, new ImageIcon(this.getClass().getResource("/image/tabs/news.png")));
            }
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
        }
    }
}
