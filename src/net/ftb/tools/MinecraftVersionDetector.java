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

public class MinecraftVersionDetector {
	
	private URLClassLoader cl;
	/**
	 * Finds out using some clever tricks the current minecraft version version
	 * @param jarFilePath The .minecraft directory
	 * @return The version of the jar file
	 */
	@SuppressWarnings("resource")
	public String getMinecraftVersion(String jarFilePath) {
		String[] jarFiles = new String[] { "minecraft.jar", "lwjgl.jar" };

		URL[] urls = new URL[jarFiles.length];

		for (int i = 0; i < urls.length; i++) {
			try {
				File f = new File(new File(jarFilePath, "bin"), jarFiles[i]);
				urls[i] = f.toURI().toURL();
			} catch (MalformedURLException e) {
				return "unknown";
			}
		}

		cl = new URLClassLoader(urls,this.getClass().getClassLoader());
		
		JarFile file = null;
		try {
			file = new JarFile(new File(jarFilePath + "/bin", "minecraft.jar"));
		} catch (IOException e1) { return "unknown"; }
			
		Enumeration<JarEntry> ent = file.entries();
			
		while (ent.hasMoreElements()) {
			JarEntry entry = ent.nextElement();
			if (entry.getName().endsWith(".class")) {
				if (entry.getName().indexOf("/") == -1) { // it has to be in the root of the jar file
					Class<?> cls = null;
					try {
						cls = cl.loadClass(entry.getName().split("\\.")[0]);
					} catch (ClassNotFoundException e1) { continue; } // not a bad error just skip over this entry
					if (cls.getConstructors().length > 0 && cls.getConstructors()[0].getParameterTypes().length == 2) {
						Boolean string = false;
						Boolean thr = false;
						for (Class<?> item : cls.getConstructors()[0].getParameterTypes()) { // it needs to be roughly CrashReport(String arg1, Throwable arg2)
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
										meth.invoke(obj, testing); // invoke the getSectionsInStringBuilder method
										String search = "Minecraft Version: ";
										return testing.toString().substring(testing.indexOf(search) + search.length(), testing.indexOf("\n"));
											// Finally return the cut out version from the report
									}
								}
							} catch (IllegalArgumentException e) { return "unknown";
							} catch (SecurityException e) { return "unknown";
							} catch (InstantiationException e) { return "unknown";
							} catch (IllegalAccessException e) { return "unknown";
							} catch (InvocationTargetException e) { return "unknown";
							}
						}
					}
				}
			}
		}
		
		return "unknown";

	}
}
