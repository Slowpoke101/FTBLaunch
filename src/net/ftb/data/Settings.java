package net.ftb.data;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import net.ftb.log.Logger;
import net.ftb.util.OSUtils;

public class Settings extends Properties {
	private static final long serialVersionUID = 1L;
	private static Settings settings;
	private File configFile;
	private boolean forceUpdate = false;

	static {
		File cfgFile = new File(OSUtils.getDynamicStorageLocation(), "ftblaunch.cfg");
		try {
			settings = new Settings(cfgFile);
		} catch (IOException e) {
			Logger.logError("Failed to load settings", e);
		}
	}

	public static Settings getSettings() {
		return settings;
	}

	public Settings(File file) throws IOException {
		configFile = file;
		if (file.exists()) {
			load(new FileInputStream(file));
		}
	}

	public void save() {
		try {
			store(new FileOutputStream(configFile), "FTBLaunch Config File");
		} catch (IOException e) {
			Logger.logError("Failed to save settings", e);
		}
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
		configFile = path;
	}

	public String getLocale() {
		return getProperty("locale", "enUS");
	}

	public void setLocale(String locale) {
		setProperty("locale", locale);
	}

	public File getConfigFile() {
		return configFile;
	}

	public void setLastPack(String name) {
		setProperty("lastPack", name);
	}

	public String getLastPack() {
		return getProperty("lastPack", ModPack.getPack(0).getDir());
	}

	public void setDownloadServer(String server) {
		setProperty("downloadServer", server);
	}

	public String getDownloadServer() {
		return getProperty("downloadServer", "Automatic");
	}

	public void setConsoleActive(String console) {
		setProperty("consoleActive", console);
	}

	public String getConsoleActive() {
		return getProperty("consoleActive", "true");
	}

	public void setPackVer(String string) {
		setProperty(ModPack.getSelectedPack().getDir(), string);
	}

	public String getPackVer() {
		return getProperty(ModPack.getSelectedPack().getDir(), "Newest Version");
	}

	public String getLastAddPath() {
		return getProperty("lastAddPath", "");
	}

	public void setLastAddPath(String string) {
		setProperty("lastAddPath", string);
	}

	public void addPrivatePack(String code) {
		if(code == null || code.isEmpty()) {
			return;
		}
		if(getProperty("privatePacks") != null) {
			Set<String> packList = new HashSet<String>(Arrays.asList(getPrivatePacks()));
			if(!packList.contains(code.toLowerCase())) {
				packList.add(code);
				setPrivatePacks(packList.toArray(new String[packList.size()]));
			}
		} else {
			setProperty("privatePacks", code);
		}
	}
	
	public void removePrivatePack(String code) {
		ArrayList<String> codes = new ArrayList<String>(Arrays.asList(getPrivatePacks()));
		if(codes.contains(code)) {
			codes.remove(code);
			System.out.println("removed");
		}
		setPrivatePacks(codes.toArray(new String[]{}));
	}

	public void setPrivatePacks(String[] codes) {
		if(codes.length > 0) {
			String s = codes[0];
			for(int i = 1; i < codes.length; i++) {
				s += "," + codes[i];
			}
			setProperty("privatePacks", s);
		} else {
			setProperty("privatePacks", "");
		}
		save();
	}

	public String[] getPrivatePacks() {
		return getProperty("privatePacks", "").split(",");
	}

	public void setNewsDate() {
		setProperty("newsDate", Long.toString(Calendar.getInstance().getTime().getTime()));
	}

	public String getNewsDate() {
		return getProperty("newsDate", Long.toString(new Date(0).getTime()));
	}

	public void setLastExtendedState(int lastExtendedState) {
		setProperty("lastExtendedState", String.valueOf(lastExtendedState));
	}

	public int getLastExtendedState() {
		return Integer.valueOf(getProperty("lastExtendedState", String.valueOf(Frame.MAXIMIZED_BOTH)));
	}

	public void setLastPosition(Point lastPosition) {
		setObjectProperty("lastPosition", lastPosition);
	}

	public Point getLastPosition() {
		Point lastPosition = (Point) getObjectProperty("lastPosition");
		if (lastPosition == null) {
			lastPosition = new Point(300, 300);
		}
		return lastPosition;
	}

	public void setLastDimension(Dimension lastDimension) {
		setObjectProperty("lastDimension", lastDimension);
	}

	public Dimension getLastDimension() {
		Dimension lastDimension = (Dimension) getObjectProperty("lastDimension");
		if (lastDimension == null) {
			lastDimension = new Dimension(854, 480);
		}
		return lastDimension;
	}

	public void setObjectProperty(String propertyName, Serializable value) {
		setProperty(propertyName, objectToString(value));
	}

	public Object getObjectProperty(String propertyName) {
		return objectFromString(getProperty(propertyName, ""));
	}

	public static Object objectFromString(String s) {
		if (s == null || s.isEmpty()) {
			return null;
		}
		byte[] data = javax.xml.bind.DatatypeConverter.parseBase64Binary(s);
		try {
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
			try {
				return ois.readObject();
			} finally {
				ois.close();
			}
		} catch (Exception e) {
			Logger.logError("Failed to read object from string: " + s, e);
			return null;
		}
	}

	private static String objectToString(Serializable o) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			try {
				oos.writeObject(o);
				return javax.xml.bind.DatatypeConverter.printBase64Binary(baos.toByteArray());
			} finally {
				baos.close();
				oos.close();
			}
		} catch (Exception e) {
			Logger.logError("Failed to write object to string" + o, e);
			return null;
		}
	}
}
