package net.ftb.tracking.piwik;

import com.google.common.hash.Hashing;
import net.ftb.data.Constants;
import net.ftb.data.Settings;
import net.ftb.log.Logger;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import java.util.UUID;


public class PiwikTracker extends Thread {
    private final String thingToTrack, urlFrom;
    private static String extraParamaters = new String();
    public PiwikTracker(String thingToTrack, String urlFrom) {
        this.thingToTrack = thingToTrack;
        this.urlFrom = urlFrom;
    }

    public static void addExtraPair(String key, String value){
        extraParamaters += "&" + PiwikUtils.addPair(key,value);
    }
    public static void newSession() {
        extraParamaters += "&new_visit=1";
    }
    @Override
    public void run() {
        HttpURLConnection con = null;
        BufferedReader in = null;
        try {
            //TODO make this not dependent on having a headed server!!
            if(Settings.getSettings().getGeneratedID() == null || Settings.getSettings().getGeneratedID().isEmpty() ) {
                Settings.getSettings().setGeneratedID(Hashing.md5().hashUnencodedChars(UUID.randomUUID().toString()).toString().substring(0, 16));
            }//TODO this needs to put bits, and the OS version in the UA data properly!!
            if(thingToTrack.startsWith("Launcher Start v"))
                newSession();
            String s = "http://stats.feed-the-beast.com/piwik.php?action_name=" + PiwikUtils.urlEncode(thingToTrack) + extraParamaters +"&url=" + PiwikUtils.urlEncode(urlFrom) + "3%20&idsite=6&%20rand=" + new Random().nextInt(999999) + "&%20h=18&%20m=14&%20s=3%20&rec=1&%20apiv=1&%20cookie=%20&%20urlref=http://feed-the-beast.com%20&_id=" + Settings.getSettings().getGeneratedID() + "%20&res=" + (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() + "x" + (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() + "&_cvar={1:[\"Launcher_Version\",\"" + Constants.version + "\"]}&ua="+ "Java/" + PiwikUtils.urlEncode(System.getProperty("java.version")) + " (" + PiwikUtils.urlEncode(System.getProperty("os.name")) + "; " + PiwikUtils.urlEncode(System.getProperty("os.arch")) + ")" +"&";
            extraParamaters = "";
            con = (HttpURLConnection) new URL(s).openConnection();
            con.setRequestMethod("GET");
            in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        } catch(MalformedURLException e) {
            e.printStackTrace();
        } catch(HeadlessException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(in != null) {
                    in.close();
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
            if(con != null) {
                con.disconnect();
            }
        }
    }
}