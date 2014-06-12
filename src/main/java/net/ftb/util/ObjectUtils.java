/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2014, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
 * FTB Launcher is licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ftb.util;

import lombok.NonNull;

import java.util.Arrays;

public class ObjectUtils {

    /**
     *
     * @param s - string to check
     * @param backup -if string is null or empty this will be returned
     * @return NonNull string
     */
    public static String preventNullString (String s,@NonNull String backup) {
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
