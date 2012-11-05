package net.ftb.tools;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import net.ftb.util.FileUtils;

public class MinecraftVersionDetector {
	public MinecraftVersionDetector() { }

	/**
	 * Finds out using some clever tricks the current minecraft version version
	 * @param jarFilePath The .minecraft directory
	 * @return The version of the jar file
	 */
	private String getMinecraftVersion(String jarFilePath) {
		String[] jarFiles = new String[] { "bckminecraft.jar", "bcklwjgl.jar" };

		if(new File(jarFilePath + "/bin/bckminecraft.jar").exists()) {
			new File(jarFilePath + "/bin/bckminecraft.jar").delete();
		}
		try {
			FileUtils.copyFile(new File(jarFilePath + "/bin/minecraft.jar"), new File(jarFilePath + "/bin/bckminecraft.jar"));
		} catch (IOException e2) { e2.printStackTrace(); }

		if(new File(jarFilePath + "/bin/bcklwjgl.jar").exists()) {
			new File(jarFilePath + "/bin/bcklwjgl.jar").delete();
		}
		try {
			FileUtils.copyFile(new File(jarFilePath + "/bin/lwjgl.jar"), new File(jarFilePath + "/bin/bcklwjgl.jar"));
		} catch (IOException e2) { e2.printStackTrace(); }

		URL[] urls = new URL[jarFiles.length];

		for (int i = 0; i < urls.length; i++) {
			try {
				File f = new File(new File(jarFilePath, "bin"), jarFiles[i]);
				urls[i] = f.toURI().toURL();
			} catch (MalformedURLException e) {
				return "unknown";
			}
		}
		URLClassLoader cl = new URLClassLoader(urls,this.getClass().getClassLoader());

		JarFile file;
		try {
			file = new JarFile(new File(jarFilePath + "/bin", "minecraft.jar"));
		} catch (IOException e1) { return "unknown"; }

		Enumeration<JarEntry> ent = file.entries();

		while (ent.hasMoreElements()) {
			JarEntry entry = ent.nextElement();
			if (entry.getName().endsWith(".class")) {
				if (!entry.getName().contains("/")) { 
					Class<?> cls;
					try {
						cls = cl.loadClass(entry.getName().split("\\.")[0]);
					} catch (ClassNotFoundException e1) { continue; } 
					if (cls.getConstructors().length > 0 && cls.getConstructors()[0].getParameterTypes().length == 2) {
						Boolean string = false;
						Boolean thr = false;
						for (Class<?> item : cls.getConstructors()[0].getParameterTypes()) { 
							if (item == String.class) {
								string = true;
							} else if (item == Throwable.class) {
								thr = true;
							}
						}
						if (string && thr) {
							try {
								Object obj = cls.getConstructors()[0].newInstance("", new Throwable("Not a Real Crash")); // create the report
								for (Method meth : cls.getMethods()) {
									if (meth.getParameterTypes().length > 0 && meth.getParameterTypes()[0] == StringBuilder.class) {
										StringBuilder testing = new StringBuilder();
										meth.invoke(obj, testing);
										String search = "Minecraft Version: ";
										return testing.toString().substring(testing.indexOf(search) + search.length(), testing.indexOf("\n"));
									}
								}
							} catch (IllegalArgumentException e) { return "unknown";
							} catch (SecurityException e) { return "unknown";
							} catch (InstantiationException e) { return "unknown";
							} catch (IllegalAccessException e) { return "unknown";
							} catch (InvocationTargetException e) { return "unknown"; }
						}
					}
				}
			}
		}
		return "unknown";
	}

	public boolean shouldUpdate(String requiredVersion, String jarFilePath) {
		return !getMinecraftVersion(jarFilePath).equals(requiredVersion);
	}
}
