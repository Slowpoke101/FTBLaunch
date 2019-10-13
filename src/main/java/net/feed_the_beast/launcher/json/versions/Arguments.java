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
package net.feed_the_beast.launcher.json.versions;

import lombok.Data;

import java.util.List;

@Data
public class Arguments {
    public Arguments (List<Game> g, List<JVM> j) {
        this.game = g;
        this.jvm = j;
    }

    List<Game> game;
    List<JVM> jvm;

    public LaunchStrings getLaunchString () {
        LaunchStrings ls = new LaunchStrings();
        StringBuilder gm = new StringBuilder();
        StringBuilder j = new StringBuilder();
        if (jvm != null && jvm.size() > 0) {
            for (JVM java : jvm) {
                if (java.isUseText()) {
                    j.append(java.getText()).append(" ");
                } else if (java.applies()) {
                    for (String s : java.getValue()) {
                        j.append(s).append(" ");
                    }
                }
            }
        }
        if (game != null && game.size() > 0) {
            for (Game g1 : game) {
                if (g1.isUseText()) {
                    gm.append(g1.getText()).append(" ");
                } else if (g1.applies()) {
                    for (String s : g1.getValue()) {
                        gm.append(s).append(" ");
                    }
                }
            }
        }
        ls.arguments = gm.toString();
        ls.jvm = j.toString();
        return ls;
    }
}
