package net.ftb.util.winreg;

import java.util.regex.Pattern;

import lombok.Getter;
import net.ftb.util.OSUtils;
import net.ftb.util.OSUtils.OS;

/**
 * Java Finder by petrucio@stackoverflow(828681) is licensed under a Creative Commons Attribution 3.0 Unported License.
 * Needs WinRegistry.java. Get it at: http://stackoverflow.com/questions/62289/read-write-to-windows-registry-using-java
 *
 * JavaFinder - Windows-specific classes to search for all installed versions of java on this system
 * Author: petrucio@stackoverflow (828681)
 *****************************************************************************/
/**
 * Helper struct to hold information about one installed java version
 ****************************************************************************/
public class JavaInfo implements Comparable<JavaInfo> {
    public String path; //! Full path to java.exe executable file
    public String version; //! Version string.
    public String origVersion = new String();
    public boolean supportedVersion = false;
    public boolean hasJava8;
    public boolean is64bits; //! true for 64-bit javas, false for 32
    @Getter
    private int major, minor, revision, build;
    private static String regex = new String("[^\\d_.-]");

    /**
     * Calls 'javaPath -version' and parses the results
     * @param javaPath: path to a java.exe executable
     ****************************************************************************/
    public JavaInfo(String javaPath) {
        String versionInfo = RuntimeStreamer.execute(new String[] { javaPath, "-version" });
        String[] tokens = versionInfo.split("\"");
        if (tokens.length < 2)
            this.version = "0.0.0_00";
        else
            this.version = tokens[1];
        this.origVersion = version;
        this.version = Pattern.compile(regex).matcher(this.version).replaceAll("0");
        this.is64bits = versionInfo.toUpperCase().contains("64-");
        this.path = javaPath;

        String[] s = this.version.split("[._-]");
        this.major = Integer.parseInt(s[0]);
        this.minor = s.length > 1 ? Integer.parseInt(s[1]) : 0;
        this.revision = s.length > 2 ? Integer.parseInt(s[2]) : 0;
        this.build = s.length > 3 ? Integer.parseInt(s[3]) : 0;

        if(OSUtils.getCurrentOS() == OS.MACOSX) {
            if (this.major == 1 && (this.minor == 7 || this.minor == 6))
                this.supportedVersion = true;
        } else {
            this.supportedVersion = true;
        }
    }

    public JavaInfo(int major, int minor) {
        this.path = null;
        this.major = major;
        this.minor = minor;
        this.revision = 0;
        this.build = 0;
    }

    public boolean isJava8() {
        return this.major == 1 && this.minor == 8;
    }

    /**
     * @return Human-readable contents of this JavaInfo instance
     ****************************************************************************/
    public String toString () {
        return "Java Version: " + origVersion + " sorted as: " + this.verToString() + " " + (this.is64bits ? "64" : "32") + " Bit Java at : " + this.path + (this.supportedVersion ? "" : " (UNSUPPORTED!)");
    }

    public String verToString () {
        return major + "." + minor + "." + revision + "_" + build;
    }

    @Override
    public int compareTo (JavaInfo o) {
        if (o.major > major)
            return -1;
        if (o.major < major)
            return 1;
        if (o.minor > minor)
            return -1;
        if (o.minor < minor)
            return 1;
        if (o.revision > revision)
            return -1;
        if (o.revision < revision)
            return 1;
        if (o.build > build)
            return -1;
        if (o.build < build)
            return 1;
        return 0;
    }

}
