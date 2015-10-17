package net.ftb.workers;

import net.feed_the_beast.launcher.json.JsonFactory;
import net.feed_the_beast.launcher.json.launcher.RetiredPacks;
import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.log.Logger;
import net.ftb.util.DownloadUtils;
import net.ftb.util.OSUtils;

import java.io.File;
import java.net.URL;

public class RetiredPacksLoader extends Thread {
    private URL url;
    private String jsonDir;
    private String packDir;

    public RetiredPacksLoader(URL url, String jsonDir, String packDir) {
        this.url = url;
        this.jsonDir = jsonDir;
        this.packDir = packDir;
    }

    @Override
    public void run() {
        File file = new File(jsonDir, File.separator + "hiddenpacks.json");
        RetiredPacks packs = null;
        boolean added = false;
        int added_packs = 0;

        try {
            DownloadUtils.downloadToFile(url, file);
        } catch (Exception e) {
            Logger.logWarn("hiddenpacks.json download failed: using cached file if available", e);
        }

        if (file.exists()) {
            try {
                packs = JsonFactory.getRetiredPacks(file);
            } catch (Exception e) {
                Logger.logDebug("failed", e);
                return;
            }
        } else {
            Logger.logWarn("hiddenpacks.json not found");
        }

        Logger.logDebug("Found " + packs.getMapping().size() + "packcodes");
        //TODO: add proper check instead of sleep
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            //ignored
        }

        for (String key: packs.getMapping().keySet()) {
            File f = new File(packDir, key);
            String val = packs.getMapping().get(key);
            if (f.exists() && f.isDirectory() && !packExists(val)) {
                Logger.logInfo("Found directory " + key + " in the disk... Adding \"" + val + "\" pack code. Pack will be found at end of the pack list" );
                ModPack.loadXml(val + ".xml");
                Settings.getSettings().addPrivatePack(val);
                added = true;
                added_packs++;
            }
        }

        if (added) {
            Logger.logDebug("Added " + added_packs + " packcodes from hiddenpacks.josn");
            Settings.getSettings().save();
        }
    }

    private boolean packExists (String name) {
        for (String p : Settings.getSettings().getPrivatePacks()) {
            if (p.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}
