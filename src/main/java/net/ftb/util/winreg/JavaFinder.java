package net.ftb.util.winreg;

/**
 * Java Finder by petrucio@stackoverflow(828681) is licensed under a Creative Commons Attribution 3.0 Unported License.
 * Needs WinRegistry.java. Get it at: http://stackoverflow.com/questions/62289/read-write-to-windows-registry-using-java
 *
 * JavaFinder - Windows-specific classes to search for all installed versions of java on this system
 * Author: petrucio@stackoverflow (828681)
 *****************************************************************************/

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import net.ftb.log.Logger;
import net.ftb.util.OSUtils;
import net.ftb.util.OSUtils.OS;

/**
 * Windows-specific java versions finder
 *****************************************************************************/
public class JavaFinder {
    public static boolean java8Found = false;
    /**
     * @return: A list of javaExec paths found under this registry key (rooted at HKEY_LOCAL_MACHINE)
     * @param wow64  0 for standard registry access (32-bits for 32-bit app, 64-bits for 64-bits app)
     *               or WinRegistry.KEY_WOW64_32KEY to force access to 32-bit registry view,
     *               or WinRegistry.KEY_WOW64_64KEY to force access to 64-bit registry view
     * @param previous: Insert all entries from this list at the beggining of the results
     *************************************************************************/
    private static List<String> searchRegistry (String key, int wow64, final List<String> previous) {
        List<String> result = previous;
        try {
            List<String> entries = WinRegistry.readStringSubKeys(WinRegistry.HKEY_LOCAL_MACHINE, key, wow64);
            for (int i = 0; entries != null && i < entries.size(); i++) {
                String val = WinRegistry.readString(WinRegistry.HKEY_LOCAL_MACHINE, key + "\\" + entries.get(i), "JavaHome", wow64);
                if (!result.contains(val + "\\bin\\java.exe")) {
                    result.add(val + "\\bin\\java.exe");
                }
            }
        } catch (Throwable t) {
            Logger.logError("Error Searching windows registry for java versions", t);
        }
        return result;
    }

    /**
     * @return: A list of JavaInfo with informations about all javas installed on this machine
     * Searches and returns results in this order:
     *   HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Runtime Environment (32-bits view)
     *   HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Runtime Environment (64-bits view)
     *   HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Development Kit     (32-bits view)
     *   HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Development Kit     (64-bits view)
     *   WINDIR\system32
     *   WINDIR\SysWOW64
     ****************************************************************************/
    public static List<JavaInfo> findJavas () {
        if(OSUtils.getCurrentOS() == OS.MACOSX)
            return findMacJavas();
        
        if(OSUtils.getCurrentOS() == OS.WINDOWS)
            return findWinJavas();
        
        return Lists.newArrayList();
    }
    
    protected static List<JavaInfo> findWinJavas() {
        List<String> javaExecs = Lists.newArrayList();
        
        javaExecs = JavaFinder.searchRegistry("SOFTWARE\\JavaSoft\\Java Runtime Environment", WinRegistry.KEY_WOW64_32KEY, javaExecs);
        javaExecs = JavaFinder.searchRegistry("SOFTWARE\\JavaSoft\\Java Runtime Environment", WinRegistry.KEY_WOW64_64KEY, javaExecs);
        javaExecs = JavaFinder.searchRegistry("SOFTWARE\\JavaSoft\\Java Development Kit", WinRegistry.KEY_WOW64_32KEY, javaExecs);
        javaExecs = JavaFinder.searchRegistry("SOFTWARE\\JavaSoft\\Java Development Kit", WinRegistry.KEY_WOW64_64KEY, javaExecs);

        javaExecs.add(System.getenv("WINDIR") + "\\system32\\java.exe");
        javaExecs.add(System.getenv("WINDIR") + "\\SysWOW64\\java.exe");
        javaExecs.add(System.getProperty("java.home") + "\\bin\\java.exe");

        List<JavaInfo> result = Lists.newArrayList();
        for (String javaPath : javaExecs) {
            if (!(new File(javaPath).exists()))
                continue;
            try {
                result.add(new JavaInfo(javaPath));
            } catch (Exception e) {
                Logger.logDebug("Error while creating JavaInfo");
            }
        }
        return result;
    }

    protected static String getMacJavaPath(String javaVersion) {
        String versionInfo;
        
        versionInfo = RuntimeStreamer.execute(new String[] { "/usr/libexec/java_home", "-v " + javaVersion });

        // Unable to find any JVMs matching version "1.7"
        if(versionInfo.contains("version \"" + javaVersion + "\"")) {    
            return null;
        }
        
        return versionInfo.trim();
    }
    
    protected static List<JavaInfo> findMacJavas() {
        List<String> javaExecs = Lists.newArrayList();
        String javaVersion;
        
        javaVersion = getMacJavaPath("1.6");
        if(javaVersion != null)
            javaExecs.add(javaVersion + "/bin/java");
        
        javaVersion = getMacJavaPath("1.7");
        if(javaVersion != null)
            javaExecs.add(javaVersion + "/bin/java");

        javaVersion = getMacJavaPath("1.8");
        if(javaVersion != null)
            javaExecs.add(javaVersion + "/bin/java");

        javaExecs.add("/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home/bin/java");
        javaExecs.add(System.getProperty("java.home") + "/bin/java");

        List<JavaInfo> result = Lists.newArrayList();
        for (String javaPath : javaExecs) {
            File javaFile = new File(javaPath); 
            if (!javaFile.exists() || !javaFile.canExecute())
                continue;

            try {
                result.add(new JavaInfo(javaPath));
            } catch (Exception e) {
                Logger.logDebug("Error while creating JavaInfo", e);
            }
        }
        return result;
    }

    /**
     * @return: The path to a java.exe that has the same bitness as the OS
     * (or null if no matching java is found)
     ****************************************************************************/
    public static String getOSBitnessJava () {
        boolean isOS64 = OSUtils.is64BitWindows();

        List<JavaInfo> javas = JavaFinder.findJavas();
        for (JavaInfo java : javas) {
            if (java.is64bits == isOS64)
                return java.path;
        }
        return null;
    }

    private static JavaInfo preferred;

    /**
     * Standalone testing - lists all Javas in the system
     ****************************************************************************/
    public static JavaInfo parseJavaVersion () {
        if (preferred == null) {
            List<JavaInfo> javas = JavaFinder.findJavas();
            List<JavaInfo> java32 = Lists.newArrayList();
            List<JavaInfo> java64 = Lists.newArrayList();

            Logger.logInfo("The FTB Launcher has found the following Java versions installed:");
            for (JavaInfo java : javas) {
                Logger.logInfo(java.toString());
                if (java.isJava8()) {
                    java8Found = true;
                }
                if (java.supportedVersion) {
                    if (preferred == null && java != null)
                        preferred = java;
                    if (java.is64bits)
                        java64.add(java);
                    else
                        java32.add(java);
                }
            }

            if (java64.size() > 0) {
                for (JavaInfo aJava64 : java64) {
                    if (!preferred.is64bits || aJava64.compareTo(preferred) == 1)
                        preferred = aJava64;
                }
            }
            if (java32.size() > 0) {
                for (JavaInfo aJava32 : java32) {
                    if (!preferred.is64bits && aJava32.compareTo(preferred) == 1)
                        preferred = aJava32;
                }
            }
            Logger.logInfo("Preferred: " + String.valueOf(preferred));
        }

        if (preferred != null) {
            return preferred;
        } else {
            Logger.logError("No Java versions found!");
            return null;
        }
    }
}
