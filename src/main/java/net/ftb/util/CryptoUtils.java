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

import net.ftb.log.Logger;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtils {

    /**
     * Newer implementation available if possible use {@link #decrypt(String str)}
     * @param str string to decrypt
     * @param key decryption key
     * @return decrypted string or "" if fails
     */
    @Deprecated
    public static String decryptLegacy (String str, byte[] key) {
        BigInteger in = new BigInteger(str, 16).xor(new BigInteger(1, key));
        try {
            return new String(in.toByteArray(), "utf8");
        } catch (UnsupportedEncodingException e) {
            return "";
        } catch (NumberFormatException e) {
            Logger.logError("Error occurred during legacy decryption");
            return "";
        }
    }

    /**
     * Newer implementation available if possible use {@link #encrypt(String str)}
     * @param str string to decrypt
     * @param key decryption key
     * @return decrypted string or "" if fails
     */
    @Deprecated
    public static String encryptLegacy (String str, byte[] key) {
        BigInteger str2;
        try {
            str2 = new BigInteger(str.getBytes("utf8")).xor(new BigInteger(1, key));
        } catch (UnsupportedEncodingException e) {
            return "";
        }
        return String.format("%040x", str2);
    }

    /**
     * Method to AES decrypt string if fails, will attempt to use {@link #decryptLegacy(String str, byte[] key)}
     * @param str string to decrypt
     * @return decrypted string or "" if legacy fails
     */
    public static String decrypt (String str) {
        byte[] keyMac = OSUtils.getMacAddress();
        byte[] keyHardware = OSUtils.getHardwareID();
        String s;
        try {
            Cipher aes = Cipher.getInstance("AES");
            if (keyHardware != null && keyHardware.length > 0) {
                try {
                    aes.init(Cipher.DECRYPT_MODE, new SecretKeySpec(pad(keyHardware), "AES"));
                    s = new String(aes.doFinal(Base64.decodeBase64(str)), "utf8");
                    if (s.startsWith("FDT:") && s.length() > 4) {
                        return s.split(":", 2)[1];// it was decrypted with HW UUID
                    }
                } catch (Exception e) {
                    Logger.logDebug("foo", e);
                }
            }

            // did not open, try again with old mac based key
            aes.init(Cipher.DECRYPT_MODE, new SecretKeySpec(pad(keyMac), "AES"));
            s = new String(aes.doFinal(Base64.decodeBase64(str)), "utf8");
            if (s.startsWith("FDT:") && s.length() > 4) {
                return s.split(":", 2)[1];//we don't want the decryption test
            } else {
                return decryptLegacy(str, keyMac);
            }
        } catch (Exception e) {
            Logger.logError("Error Decrypting information, attempting legacy decryption", e);
            return decryptLegacy(str, keyMac);
        }
    }

    /**
     * Method to AES encrypt string if fails, will attempt to use {@link #encryptLegacy(String str, byte[] key)}
     * @param str string to encrypt
     * @return encrypted string or "" if legacy fails
     */
    public static String encrypt (String str) {
        byte[] keyMac = OSUtils.getMacAddress();
        byte[] keyHardware = OSUtils.getHardwareID();
        try {
            Cipher aes = Cipher.getInstance("AES");
            if (keyHardware != null && keyHardware.length > 0) {
                aes.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(pad(keyHardware), "AES"));
                return Base64.encodeBase64String(aes.doFinal(("FDT:" + str).getBytes("utf8")));
            }
            aes.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(pad(keyMac), "AES"));
            return Base64.encodeBase64String(aes.doFinal(("FDT:" + str).getBytes("utf8")));
        } catch (Exception e) {
            Logger.logError("Error Encrypting information, reverting to legacy format", e);
            return encryptLegacy(str, keyMac);
        }
    }

    /**
     * method to pad AES keys by using the sha1Hex hash on them
     * @param key key to pad
     * @return padded key
     */
    public static byte[] pad (byte[] key) {
        try {
            return Arrays.copyOf(DigestUtils.sha1Hex(key).getBytes("utf8"), 16);
        } catch (UnsupportedEncodingException e) {
            Logger.logError("error encoding padded key!", e);
            return Arrays.copyOf(DigestUtils.sha1Hex(key).getBytes(), 16);
        }
    }
}
