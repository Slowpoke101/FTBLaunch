package net.ftb.util.winreg;

import lombok.Getter;
import net.ftb.util.ComparableVersion;

import java.util.Comparator;

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
public class JavaInfo extends JavaVersion {
    public String path; //! Full path to java.exe executable file
    public boolean is64bits; //! true for 64-bit javas, false for 32

    /**
     * Calls 'javaPath -version' and parses the results
     * @param javaPath: path to a java.exe executable
     ****************************************************************************/
    public JavaInfo (String javaPath) throws Exception {
        super(RuntimeStreamer.execute(new String[] { javaPath, "-version" }), true);
        this.path = javaPath;
        this.is64bits = origStr.toUpperCase().contains("64-");
    }

    public boolean samePath (JavaInfo j) {
        return this.path.equals(j.path);
    }
    public boolean sameBitness (JavaInfo j) {
        return this.is64bits == j.is64bits;
    }

    /**
     * Tests if JavaInfo are identical: same version, same bitness but not always same path
     *
     * Used to manually remove duplicate JavaInfos from Collections
     * @param j other JavaInfo objct to test
     * @return
     */
    public boolean isIdentical (JavaInfo j) {
        return (this.sameVersion(j) && sameBitness(j));
    }

    /**
     * @return Human-readable contents of this JavaInfo instance
     ****************************************************************************/
    public String toString () {
        return "Java Version: " + origVersion + " sorted as: " + this.verToString() + " " + (this.is64bits ? "64" : "32") + " Bit Java at : " + this.path;
    }

    public String verToString () {
        return major + "." + minor + "." + revision + "_" + update;
    }

    // PREFERRED sorting compares first bitness then java version
    public static final Comparator<JavaInfo> PREFERRED_SORTING = new Comparator<JavaInfo>() {
        public int compare (JavaInfo j1, JavaInfo j2) {
            if (!j1.is64bits && j2.is64bits) {
                return -1;
            } else if (j1.is64bits && !j2.is64bits) {
                return 1;
            } else {
                return j1.comparableVersion.compareTo(j2.comparableVersion);
            }
        }
    };
}