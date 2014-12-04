package net.ftb.util.winreg;

import lombok.Getter;
import net.ftb.util.ComparableVersion;

import java.util.Comparator;

public class JavaVersion {
    public ComparableVersion comparableVersion;
    public String origVersion;
    @Getter
    protected int major, minor, revision, update;

    public static JavaVersion createJavaVersion(String version) {
        try {
            return new JavaVersion(version, false);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     *
     * @param str String containing output of java -version command
     * @param full Boolean to indicate if str is full output or only version string
     * @throws Exception
     */
    protected JavaVersion (String str, boolean full) throws Exception {
        if (full) {
            String[] tokens = str.split("\"");
            if (tokens.length < 2) {
                throw new Exception("Input string unsupported format");
            } else {
                this.origVersion = tokens[1];
            }
        } else {
            this.origVersion = str;
        }
        parseVersionString();
        this.comparableVersion = new ComparableVersion(this.origVersion);
    }

    private void parseVersionString() {
        String[] s = this.origVersion.split("[._-]");
        this.major = Integer.parseInt(s[0]);
        this.minor = s.length > 1 ? Integer.parseInt(s[1]) : 0;
        this.revision = s.length > 2 ? Integer.parseInt(s[2]) : 0;
        this.update = s.length > 3 ? Integer.parseInt(s[3]) : 0;
    }

    /**
     * @return Human-readable contents of this JavaInfo instance
     ****************************************************************************/
    public String toString () {
        return "Java Version: " + origVersion + " sorted as: " + this.verToString();
    }

    public String verToString () {
        return major + "." + minor + "." + revision + "_" + update;
    }

    public static final Comparator<JavaVersion> PREFERRED_SORTING = new Comparator<JavaVersion>() {
        public int compare (JavaVersion j1, JavaVersion j2) {
            return j1.comparableVersion.compareTo(j2.comparableVersion);
        }
    };

    public boolean isOlder (JavaVersion  j1) {
        return this.comparableVersion.isOlder(j1.comparableVersion);
    }

    public boolean isOlder (String str) {
        return this.isOlder(JavaVersion.createJavaVersion(str));
    }

    public boolean isSameVersion (JavaVersion j1) {
        return this.comparableVersion.isSameVersion(j1.comparableVersion);
    }

    public boolean isSameVersion (String str) {
        return this.isSameVersion(JavaVersion.createJavaVersion(str));
    }
}