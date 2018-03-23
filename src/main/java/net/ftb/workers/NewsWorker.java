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
package net.ftb.workers;

import net.ftb.util.NewsUtils;

import javax.swing.*;

/**
 * SwingWorker to asynchronously fetch news for news pane
 * return String which can be used in news pane
 */
public class NewsWorker extends SwingWorker<String, Void> {
    @Override
    protected String doInBackground () {
        return NewsUtils.getNewsHTML();
    }
}
