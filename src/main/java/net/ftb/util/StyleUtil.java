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
package net.ftb.util;

import net.ftb.data.LauncherStyle;

import javax.swing.*;

public final class StyleUtil {

    private StyleUtil () {
    }

    public static void loadUiStyles () {
        final LauncherStyle style = LauncherStyle.getCurrentStyle();
        UIManager.put("control", style.control);
        UIManager.put("text", style.text);
        UIManager.put("nimbusBase", style.nimbusBase);
        UIManager.put("nimbusFocus", style.nimbusFocus);
        UIManager.put("nimbusBorder", style.nimbusBorder);
        UIManager.put("nimbusLightBackground", style.nimbusLightBackground);
        UIManager.put("info", style.info);
        UIManager.put("nimbusSelectionBackground", style.nimbusSelectionBackground);
    }

}
