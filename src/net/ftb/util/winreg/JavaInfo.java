package net.ftb.util.winreg;

import net.ftb.log.Logger;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
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
    public String  path;        //! Full path to java.exe executable file
    public String  version;     //! Version string. "Unkown" if the java process returned non-standard version string
    public String  origVersion = new String();
    public boolean is64bits;    //! true for 64-bit javas, false for 32
    private int major, minor, revision, build;
    private static String regex = new String("[^\\d_.-]");
    /**
     * Calls 'javaPath -version' and parses the results
     * @param javaPath: path to a java.exe executable
     ****************************************************************************/
    public JavaInfo(String javaPath) {
        String versionInfo = RuntimeStreamer.execute( new String[] { javaPath, "-version" } );
        String[] tokens = versionInfo.split("\"");
        if (tokens.length < 2) this.version = "0.0.0_00";
        else this.version = tokens[1];
        this.origVersion = version;
        this.version = Pattern.compile(regex).matcher(this.version).replaceAll("0");
        /*if (version.contains("-"))
            version = version.replaceAll("-", ".");
        if (version.contains("_"))
            version = version.replaceAll("_", ".");*/
        this.is64bits = versionInfo.toUpperCase().contains("64-BIT");
        this.path     = javaPath;
        
        String[] s = this.version.split("[._-]");
        this.major = Integer.parseInt(s[0]);
        this.minor = s.length > 1 ? Integer.parseInt(s[1]) : 0;
        this.revision = s.length > 2 ? Integer.parseInt(s[2]) : 0;
        this.build = s.length > 3 ? Integer.parseInt(s[3]) : 0; 

    }

    /**
     * @return Human-readable contents of this JavaInfo instance
     ****************************************************************************/
    public String toString() {
        return "Java Version: " + origVersion + " sorted as: " + this.verToString() + " " + (this.is64bits ? "64" : "32") +" Bit Java at : " +this.path ;
    }
    public String verToString()
    {
        return major + "." + minor + "." + revision + "_" + build;
    }

    @Override
    public int compareTo (JavaInfo o)
    {
        if (o.major > major) return -1;
        if (o.major < major) return 1;
        if (o.minor > minor) return -1;
        if (o.minor < minor) return 1;
        if (o.revision > revision) return -1;
        if (o.revision < revision) return 1;
        if (o.build > build) return -1;
        if (o.build < build) return 1;
        return 0;
    }


}
