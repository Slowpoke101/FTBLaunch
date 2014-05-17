package net.ftb.util;

public class ObjectUtils {

    public static String preventNullString (String s, String backup) {
        if (s == null || s.isEmpty()) {
            return backup;
        }
        return s;
    }
}
