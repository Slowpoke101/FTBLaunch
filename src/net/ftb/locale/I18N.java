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
package net.ftb.locale;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Properties;

import net.ftb.log.Logger;
import net.ftb.util.OSUtils;

public class I18N {
	private static Properties locales = new Properties();
	private static Properties fallback = new Properties();
	private static File dir = new File(OSUtils.getDynamicStorageLocation(), "locale");
	public static HashMap<String, String> localeFiles = new HashMap<String, String>();
	public final static HashMap<Integer, String> localeIndices = new HashMap<Integer, String>();
	public static Locale currentLocale = Locale.enUS;

	public enum Locale {
		cyGB,
		daDK,
		deDE,
		enUS,
		esES,
		fiFI,
		frFR,
		itIT,
		nlNL,
		noNO,
		maHU,
		ptBR,
		ptPT,
		ruRU,
		svSE
	}

	/**
	 * Gets the locale properties and stores loads it to locales
	 * @param file The locale file
	 */
	private static void getLocaleProperties(String file) {
		locales.clear();
		if (file.equalsIgnoreCase("enUS")) {
			try {
				locales.load(new InputStreamReader(I18N.class.getResource("/i18n/enUS").openStream(), "UTF8"));
			} catch (IOException e) {
				Logger.logError("[i18n] Could not load language file", e);
			}
		} else {
			try {
				locales.load(new InputStreamReader(new FileInputStream(dir.getAbsolutePath() + File.separator + file), "UTF8"));
			} catch (IOException e) {
				Logger.logWarn("[i18n] Could not load language file", e);
			}
		}
	}

	/**
	 * Set available locales and load fallback locale
	 */
	public static void setupLocale() {
		localeFiles.put("enUS", "English");
		synchronized (localeIndices) {
			localeIndices.put(0, "enUS");
		}
		try {
			new LocaleUpdater().start();
		} catch (Exception e) {
			Logger.logError(e.getMessage(), e);
		}
	}

	/**
	 * Add files from the locale directory
	 */
	public static void addFiles() {
		int i = 1;
		Properties tmp = new Properties();
		String[] list = dir.list();
		for (String file : list) {
			if (file.matches("^\\w{4}$")) {
				try {
					if(!file.equalsIgnoreCase("enUS")) {
						tmp.clear();
						tmp.load(new InputStreamReader(new FileInputStream(dir.getAbsolutePath() + File.separator + file), "UTF8"));
						localeFiles.put(file, tmp.getProperty("LOCALE_NAME", file));
						synchronized (localeIndices) {
							localeIndices.put(i, file);
						}
						i++;
					}
				} catch (IOException e) {
					Logger.logWarn("[i18n] Could not load language file", e);
				}
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
	public static void setLocale(String locale) {
	    if (locale == null) {
	        locale = "enUS";
	        currentLocale = Locale.enUS;
	    }
	    else {
	        try {
	        currentLocale = Locale.valueOf(locale);
	        } catch(IllegalArgumentException e) {
	            Logger.logWarn("[i18n] Unknown locale " + locale + ". Loaded enUs");
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
	private static String getFallbackString(String key) {
		return fallback.getProperty(key, key);
	}

	/**
	 * Gets the localized string for the field, if not defined, returns the key
	 * @param key The key for the string
	 * @return The localized string or fallback value
	 */
	public static String getLocaleString(String key) {
		return locales.getProperty(key, getFallbackString(key));
	}
}
