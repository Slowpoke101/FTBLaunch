/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2017, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
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
package net.ftb.locale;

import com.google.common.collect.Maps;
import net.ftb.log.Logger;
import net.ftb.util.OSUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

public class I18N {
    private static Properties locales = new Properties();
    private static Properties fallback = new Properties();
    private static File dir = new File(OSUtils.getDynamicStorageLocation(), "locale");
    public static HashMap<String, String> localeFiles = Maps.newHashMap();
    public final static HashMap<Integer, String> localeIndices = Maps.newHashMap();
    public static Locale currentLocale = Locale.enUS;

    public enum Locale {
        cyGB, daDK, deDE, enGB, enUS, esES, fiFI, frCA, frFR, itIT, maHU, nlNL, noNO, plPL, ptBR, ptPT, ruRU, svSE, zhCN, zhTW
    }

    /**
     * Gets the locale properties and stores loads it to locales
     * @param file The locale file
     */
    private static void getLocaleProperties (String file) {
        locales.clear();
        try {
            locales.load(new InputStreamReader(I18N.class.getResource("/i18n/" + file).openStream(), "UTF8"));
            // clean empty entries
            for (Enumeration<Object> e = locales.keys(); e.hasMoreElements(); ) {
                String key = (String) e.nextElement();
                if (locales.get(key).equals("")) {
                    locales.remove(key);
                }
            }
        } catch (IOException e) {
            Logger.logError("[i18n] Could not load language file", e);
        }
    }

    /**
     * Set available locales and load fallback locale
     */
    public static void setupLocale () {
        localeFiles.put("enUS", "English (US)");
        synchronized (localeIndices) {
            localeIndices.put(0, "enUS");
        }
        addFiles();
    }

    /**
     * Add files from the locale directory
     */
    public static void addFiles () {
        int i = 1;
        Properties tmp = new Properties();
        for (Locale file_ : Locale.values()) {
            String file = file_.toString();
            try {
                tmp.clear();
                tmp.load(new InputStreamReader(I18N.class.getResource("/i18n/" + file).openStream(), "UTF8"));
                localeFiles.put(file, tmp.getProperty("LOCALE_NAME", file));
                synchronized (localeIndices) {
                    localeIndices.put(i, file);
                }
                i++;
            } catch (IOException e) {
                Logger.logWarn("[i18n] Could not load language file", e);
            }
        }

        try {
            fallback.clear();
            fallback.load(new InputStreamReader(I18N.class.getResource("/i18n/enUS").openStream(), "UTF8"));
            Logger.logInfo("[i18n] Fallback enUS loaded");
        } catch (IOException e) {
            Logger.logError("[i18n] Could not load fallback file", e);
        }
    }

    /**
     * Sets the locale for the launcher
     * @param locale the language file to be loaded
     */
    public static void setLocale (String locale) {
        if (locale == null) {
            locale = "enUS";
            currentLocale = Locale.enUS;
        } else {
            try {
                currentLocale = Locale.valueOf(locale);
            } catch (IllegalArgumentException e) {
                Logger.logWarn("[i18n] Unknown locale " + locale + ". Loaded enUS");
                currentLocale = Locale.enUS;
            }
        }
        getLocaleProperties(locale);
        Logger.logInfo("[i18n] " + locale + " " + locales.getProperty("LOCALE_LOADED", "loaded"));
    }

    /**
     * Gets the default translation for the key (enUS)
     * @param key The key for the string
     * @return the default string
     */
    public static String getFallbackString (String key) {
        return fallback.getProperty(key, key);
    }

    /**
     * Gets the localized string for the field, if not defined, returns the key
     * @param key The key for the string
     * @return The localized string or fallback value
     */
    public static String getLocaleString (String key) {
        return locales.getProperty(key, getFallbackString(key));
    }
}
