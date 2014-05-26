package net.ftb.util;

import java.util.Arrays;

public class ObjectUtils {

    public static String preventNullString (String s, String backup) {
        if (s == null || s.isEmpty()) {
            return backup;
        }
        return s;
    }
    /**
     * @param first - First array
     * @param rest - Rest of the arrays
     * @return - Outputs concatenated arrays
     */
    public static <T> T[] concatenateArrays (T[] first, T[]... rest) {
        int totalLength = first.length;
        for (T[] array : rest) {
            totalLength += array.length;
        }
        T[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (T[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }
}
