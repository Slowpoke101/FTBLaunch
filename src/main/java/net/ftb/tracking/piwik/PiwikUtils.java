package net.ftb.tracking.piwik;

import net.ftb.log.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class PiwikUtils {

    public static String urlEncode (String in) {
        String output;
        try {
            output = URLEncoder.encode(in, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Logger.logError("URL Encoding Error", e);
            output = in;
        }
        return output;
    }

    public static String addPair (String key, int value) {
        return addPair(key, String.valueOf(value));
    }

    public static String addPair (String key, String value) {
        return key + "=" + urlEncode(value);
    }
}
