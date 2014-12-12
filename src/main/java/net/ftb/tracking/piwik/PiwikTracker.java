package net.ftb.tracking.piwik;

import com.google.common.hash.Hashing;
import net.ftb.data.Constants;
import net.ftb.data.Settings;
import net.ftb.download.Locations;
import net.ftb.log.Logger;

import java.awt.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Random;
import java.util.UUID;

public class PiwikTracker extends Thread {
    private final String thingToTrack, urlFrom;
    private String extraParamaters = new String();

    public PiwikTracker (String thingToTrack, String urlFrom) {
        this.thingToTrack = thingToTrack;
        this.urlFrom = urlFrom;
        this.extraParamaters = "";
    }

    public void addExtraPair (String key, String value) {
        extraParamaters += "&" + PiwikUtils.addPair(key, value);
    }

    public void newSession () {
        extraParamaters += "&new_visit=1";
    }

    @Override
    public void run () {
        HttpURLConnection con = null;
        try {
            //TODO make this not dependent on having a headed server!!
            if (Settings.getSettings().getGeneratedID() == null || Settings.getSettings().getGeneratedID().isEmpty()) {
                Settings.getSettings().setGeneratedID(Hashing.md5().hashUnencodedChars(UUID.randomUUID().toString()).toString().substring(0, 16));
            }//TODO this needs to put bits, and the OS version in the UA data properly!!
            if (thingToTrack.startsWith("Launcher Start v")) {
                newSession();
            }
            Calendar time = Calendar.getInstance();

            String s = Locations.PIWIK + "/piwik.php?action_name="
                    + PiwikUtils.urlEncode(thingToTrack)
                    + extraParamaters
                    + "&url=" + PiwikUtils.urlEncode(urlFrom)
                    + "&idsite=6&%20rand=" + new Random().nextInt(999999)
                    + "&%20h=" + time.get(Calendar.HOUR_OF_DAY) + " &%20m=" + time.get(Calendar.MINUTE) + "&%20s=" + time.get(Calendar.SECOND) + "%20"
                    + "&rec=1&%20apiv=1&%20cookie=%20&%20urlref=http://feed-the-beast.com%20"
                    + "&_id=" + Settings.getSettings().getGeneratedID()
                    + "%20&res=" + (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() + "x" + (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight()
                    + "&_cvar={1:[\"Launcher_Version\",\"" + Constants.version + "\"]}&ua=" + "Java/" + PiwikUtils.urlEncode(System.getProperty("java.version")) + " (" + PiwikUtils
                    .urlEncode(System.getProperty("os.name")) + "; " + PiwikUtils.urlEncode(System.getProperty("os.arch")) + ")" + "&";
            extraParamaters = "";
            con = (HttpURLConnection) new URL(s).openConnection();
            con.setRequestMethod("GET");
            int result = con.getResponseCode();
            if (result != 200) {
                Logger.logDebug("Tracker request failed. Return code: " + result);
            }
        } catch (MalformedURLException e) {
            Logger.logDebug("Malformed Tracker URL", e);
        } catch (HeadlessException e) {
            Logger.logDebug("Headless Exception from Piwik", e);
        } catch (IOException e) {
            Logger.logDebug("Error Contacting tracking server", e);
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }
}