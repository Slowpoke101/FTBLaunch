package net.ftb.util.winreg;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
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
    private static Cache<String, JavaInfo> CACHE;
    static {
        CACHE = CacheBuilder.newBuilder().initialCapacity(10).build();
    }
    public String path; //! Full path to java.exe executable file
    public boolean is64bits; //! true for 64-bit javas, false for 32

    /**
     * Creates new JavaInfo using string got from java -version
     * @param version:
     ****************************************************************************/
    private JavaInfo (String version) throws Exception {
        super(version, true);
    }

    /**
     * Creates new JavaInfo or returns cached JavaInfo object
     *
     * @param javaPath: path to java binary
     * @return
     */
    public static JavaInfo getJavaInfo(String javaPath) {
        JavaInfo j = CACHE.getIfPresent(javaPath);
        // TODO: notation to mark that there will not be value for given key?
        if (j == null) {
            String output = RuntimeStreamer.execute(new String[] { javaPath, "-version" });
            try {
                j = new JavaInfo(output);
            } catch (Exception e) {
                return null;
            }
            j.path = javaPath;
            j.is64bits = output.toUpperCase().contains("64-");
            CACHE.put(javaPath, j);
        }
        return j;
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