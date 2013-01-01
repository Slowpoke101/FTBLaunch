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
	public static HashMap<Integer, String> localeIndices = new HashMap<Integer, String>();
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
		localeIndices.put(0, "enUS");
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
						localeIndices.put(i, file);
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
		if (locale.equalsIgnoreCase("daDK")) {
			currentLocale = Locale.daDK;
		} else if (locale.equalsIgnoreCase("deDE")) {
			currentLocale = Locale.deDE;
		} else if(locale.equalsIgnoreCase("nlNL")) {
			currentLocale = Locale.nlNL;
		} else if(locale.equalsIgnoreCase("ptBR")) {
			currentLocale = Locale.ptBR;
		} else if(locale.equalsIgnoreCase("ptPT")) {
			currentLocale = Locale.ptPT;
		} else if(locale.equalsIgnoreCase("ruRU")) {
			currentLocale = Locale.ruRU;
		} else if(locale.equalsIgnoreCase("svSE")) {
			currentLocale = Locale.svSE;
		} else if(locale.equalsIgnoreCase("esES")) {
			currentLocale = Locale.esES;
		} else if(locale.equalsIgnoreCase("itIT")) {
			currentLocale = Locale.itIT;
		} else if(locale.equalsIgnoreCase("maHU")) {
			currentLocale = Locale.maHU;
		} else if(locale.equalsIgnoreCase("frFR")) {
			currentLocale = Locale.frFR;
		} else if(locale.equalsIgnoreCase("cyGB")) {
			currentLocale = Locale.cyGB;
		} else if(locale.equalsIgnoreCase("fiFI")) {
			currentLocale = Locale.fiFI;
		} else {
			currentLocale = Locale.enUS;
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
