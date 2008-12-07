package net.roarsoftware.util;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import android.util.Log;

/**
 * Utilitiy class with methods to calculate an md5 hash and to encode URLs.
 * 
 * @author Janni Kovacs
 * @author Casey Link <unnamedrambler@gmail.com>
 */
public class StringUtilities {

	private static MessageDigest digest;
	private static Pattern MBID_PATTERN = Pattern
			.compile(
					"^[0-9a-f]{8}\\-[0-9a-f]{4}\\-[0-9a-f]{4}\\-[0-9a-f]{4}\\-[0-9a-f]{12}$",
					Pattern.CASE_INSENSITIVE);

	public static String md5(String s) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] messageDigest = md.digest(s.getBytes("UTF-8"));
			BigInteger number = new BigInteger(1, messageDigest);
			String md5 = number.toString(16);
			while (md5.length() < 32)
				md5 = "0" + md5;
			return md5;
		} catch (NoSuchAlgorithmException e) {
			Log.e("MD5", e.getMessage());
		} catch (UnsupportedEncodingException e) {
			Log.e("MD5", e.getMessage());
		}
		return null;
		
	}

	public static String encode(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// utf-8 always available
		}
		return null;
	}

	public static String decode(String s) {
		try {
			return URLDecoder.decode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// utf-8 always available
		}
		return null;
	}

	public static boolean isMbid(String artistOrMbid) {
		// example: bfcc6d75-a6a5-4bc6-8282-47aec8531818
		return artistOrMbid.length() == 36
				&& MBID_PATTERN.matcher(artistOrMbid).matches();
	}

	/**
	 * Creates a Map out of an array with Strings.
	 * 
	 * @param strings
	 *            input strings, key-value alternating
	 * @return a parameter map
	 */
	public static Map<String, String> map(String... strings) {
		if (strings.length % 2 != 0)
			throw new IllegalArgumentException("strings.length % 2 != 0");
		Map<String, String> mp = new HashMap<String, String>();
		for (int i = 0; i < strings.length; i += 2) {
			mp.put(strings[i], strings[i + 1]);
		}
		return mp;
	}
}
