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
package net.ftb.events;

import lombok.Getter;
import lombok.Setter;

//Event for any changes that require partial or full UI re-setting up.
public class StyleUpdateEvent implements ILauncherEvent {
    /**
     *  tab = shared tab changes ex: map/TP swap
     *  style = changes to base colors, or fonts used by the launcher that require re-setting up parts of the UI
     */
    public enum TYPE {
        TAB, STYLE
    }

    @Getter
    @Setter
    private TYPE eventType;

    @Getter
    @Setter
    private String[] eventTarget;

    /**
     * constructor for events, must have a type so that listeners know what to refresh!
     * @param type type of style update
     */
    public StyleUpdateEvent (TYPE type) {
        this.eventType = type;
    }
}
