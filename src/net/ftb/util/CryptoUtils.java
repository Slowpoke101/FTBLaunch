package net.ftb.util;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

public class CryptoUtils {
	public static String decrypt(String str, byte[] key) {
		BigInteger in = new BigInteger(str, 16).xor(new BigInteger(1, key));
		try {
			return new String(in.toByteArray(), "utf8");
		} catch (UnsupportedEncodingException e) {
			return "";
		}
	}

	public static String encrypt(String str, byte[] key) {
		BigInteger str2;
		try {
			str2 = new BigInteger(str.getBytes("utf8")).xor(new BigInteger(1, key));
		} catch (UnsupportedEncodingException e) {
			return "";
		}
		return String.format("%040x", str2);
	}
}
