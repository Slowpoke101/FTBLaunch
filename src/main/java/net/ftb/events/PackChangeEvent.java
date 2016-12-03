/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2016, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
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
package net.ftb.events;

import lombok.Getter;
import net.ftb.data.ModPack;

import java.util.ArrayList;
import java.util.List;

public class PackChangeEvent implements ILauncherEvent {
    public enum TYPE {
        ADD, CHANGE, REMOVE, FILTER
    }

    @Getter
    private TYPE type;

    @Getter
    private boolean xml;
    @Getter
    private String[] names;
    /**
     * make sure to null check when using getter !!!!
     */
    @Getter
    private List<ModPack> packs;

    /**
     *
     * @param type type of pack change event such as add or remove
     * @param xml are the names XML file names in the repo(true) or pack names(false)?
     * @param name name of pack involved
     */
    public PackChangeEvent (TYPE type, boolean xml, String... name) {
        this.type = type;
        this.names = name;
        this.xml = xml;
    }

    /**
     *
     * @param type type of pack change event such as add or remove
     * @param packs Mod Packs being added/removed/changed
     */
    public PackChangeEvent (TYPE type, ArrayList<ModPack> packs) {
        this.type = type;
        this.packs = packs;
        this.xml = false;
        names = new String[packs.size()];
        int cnt = 0;
        for (ModPack pack : packs) {
            names[cnt] = (pack.getName());
            cnt++;
        }
    }
}
