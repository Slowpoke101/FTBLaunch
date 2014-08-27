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
package net.ftb.locale;

import net.ftb.data.Settings;
import net.ftb.log.Logger;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class I18N{
    private static final Map<Locale, Properties> langs = new HashMap<Locale, Properties>();
    public static final Map<Locale, String> lookup = new HashMap<Locale, String>();
    public volatile static Locale current = Locale.enUS;

    public static void load(){
        for(Locale locale : Locale.values()){
            try{
                Properties props = new Properties();
                props.load(new InputStreamReader(I18N.class.getResourceAsStream("/i18n/" + locale.name()), "UTF-8"));
                langs.put(locale, props);
                lookup.put(locale, props.getProperty("LOCALE_NAME", locale.name()));
                Logger.logInfo("[I18N] Loaded Locale " + locale);
            } catch(Exception ex){
                Logger.logWarn("[I18N] Couldn't load language file", ex);
            }
        }
    }

    public static String[] available(){
        String[] ret = new String[Locale.values().length];
        for(int i = 0; i < Locale.values().length; i++){
            ret[i] = lookup.get(Locale.values()[i]);
        }
        return ret;
    }

    public static String current(){
        return lookup.get(Locale.get(Settings.getSettings().getLocale()));
    }

    public static synchronized void setLocale(String locale){
        if(locale == null){
            current = Locale.enUS;
        } else{
            current = Locale.get(locale);
        }
    }

    public static String getLocaleString(String key){
        if(langs.get(current).containsKey(key) && !langs.get(current).getProperty(key).isEmpty()){
            return langs.get(current).getProperty(key);
        } else{
            return langs.get(Locale.enUS).getProperty(key, key);
        }
    }
}
