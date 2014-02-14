/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2013, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
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

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

public class CryptoUtils {
    public static String decrypt (String str, byte[] key) {
        BigInteger in = new BigInteger(str, 16).xor(new BigInteger(1, key));
        try {
            return new String(in.toByteArray(), "utf8");
        }
        catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    public static String encrypt (String str, byte[] key) {
        BigInteger str2;
        try {
            str2 = new BigInteger(str.getBytes("utf8")).xor(new BigInteger(1, key));
        }
        catch (UnsupportedEncodingException e) {
            return "";
        }
        return String.format("%040x", str2);
    }
}