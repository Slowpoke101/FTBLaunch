package net.ftb.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import net.ftb.util.OSUtils;

public class Settings extends Properties {
	private static final long serialVersionUID = 1L;
	private static Settings settings;
	private File configPath;
	private boolean forceUpdate = false;

	public static void initSettings() throws IOException {
		File cfgFile = new File(OSUtils.getDynamicStorageLocation(), "ftblaunch.cfg");
		if (cfgFile.exists()) {
			loadSettings(cfgFile);
			return;
		}
		settings = new Settings();
		settings.setConfigFile(cfgFile);
	}

	public static void loadSettings(File file) throws FileNotFoundException, IOException {
		settings = new Settings(file);
	}

	public static Settings getSettings() {
		return settings;
	}

	public Settings() { }

	public Settings(File file) throws IOException {
		configPath = file;
		load(new FileInputStream(file));
	}

	public void save() throws IOException {
		store(new FileOutputStream(configPath), "FTBLaunch Config File");
	}

	public String getRamMax() {
		return getProperty("ramMax", Integer.toString(1024));
	}

	public void setRamMax(String max) {
		setProperty("ramMax", max);
	}

	public String getLastUser()	{
		return getProperty("lastUser", null);
	}

	public void setLastUser(String user) {
		setProperty("lastUser", user);
	}

	public String getInstallPath() {
		return getProperty("installPath", OSUtils.getDefInstallPath());
	}

	public void setInstallPath(String path) {
		setProperty("installPath", path);
	}

	public boolean getForceUpdate() {
		return forceUpdate;
	}

	public void setForceUpdate(boolean force) {
		forceUpdate = force;
	}

	public void setConfigFile(File path) {
		configPath = path;
	}

	public String getLocale() {
		return getProperty("locale", "enUS");
	}

	public void setLocale(String locale) {
		setProperty("locale", locale);
	}

	public File getConfigFile() {
		return configPath;
	}

	public void setLastPack(String name) {
		setProperty("lastPack", name);
	}

	public String getLastPack() {
		return getProperty("lastPack", ModPack.getPack(0).getDir());
	}

	public void setMinecraftX(String x) {
		setProperty("minecraftX", x);
	}

	public String getMinecraftX() {
		return getProperty("minecraftX", "854");
	}

	public void setMinecraftY(String y) {
		setProperty("minecraftY", y);
	}

	public String getMinecraftY() {
		return getProperty("minecraftY", "480");
	}

	public void setMinecraftXPos(String x) {
		setProperty("minecraftXPos", x);
	}

	public String getMinecraftXPos() {
		return getProperty("minecraftXPos", "300");
	}

	public void setMinecraftYPos(String y) {
		setProperty("minecraftYPos", y);
	}

	public String getMinecraftYPos() {
		return getProperty("minecraftYPos", "300");
	}

	public void setDownlaodServer(String server) {
		setProperty("downloadServer", server);
	}

	public String getDownloadServer() {
		return getProperty("downloadServer", "0");
	}
	
	public void setConsoleActive(String console) {
		setProperty("consoleActive", console);
	}
	
	public String getConsoleActive() {
		return getProperty("consoleActive", "true");
	}
	
	public void setAutoMaximize(String autoMax) {
		setProperty("autoMaximize", autoMax);
	}
	
	public String getAutoMaximize() {
		return getProperty("autoMaximize", "true");
	}

	public void setCenterWindow(String string) {
		setProperty("centerWindow", string);
	}

	public String getCenterWindow() {
		return getProperty("centerWindow", "false");
	}
	
	public void setPackVer(String string) {
		setProperty(ModPack.getSelectedPack().getDir(), string);
	}
	
	public String getPackVer() {
		return getProperty(ModPack.getSelectedPack().getDir(), "newest");
	}
}
