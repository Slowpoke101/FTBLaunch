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
package net.ftb.main;

import java.lang.reflect.Method;
import java.net.URLClassLoader;

/**
 * This is required to ensure the loader is loaded via a URLClassLoader, LauncherClassLoader is used only to make addUrl public
 */
public class Bootstrap {

    private static final URLClassLoader CLASS_LOADER = new LauncherClassLoader();

    public static void main(String[] args) throws Exception  {
        // Before we have fun, make sure to protect ourselves ;)
        JndiPatch.patchJndi();

        Class<?> mainClass = Class.forName("net.ftb.main.Main", true, CLASS_LOADER);
        Method mainMethod = mainClass.getMethod("main", String[].class);
        mainMethod.invoke(null, (Object) args);
    }

}
