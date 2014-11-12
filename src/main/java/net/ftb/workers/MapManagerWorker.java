package net.ftb.workers;

import net.ftb.data.Map;
import net.ftb.data.Settings;
import net.ftb.gui.dialogs.MapOverwriteDialog;
import net.ftb.log.Logger;
import net.ftb.util.DownloadUtils;
import net.ftb.util.FTBFileUtils;
import net.ftb.util.OSUtils;
import net.ftb.util.TrackerUtils;

import javax.swing.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import static net.ftb.download.Locations.MAPS;

public class MapManagerWorker extends SwingWorker<Boolean, Void> {
    private static boolean overwrite;
    private double downloadedPerc;

    public MapManagerWorker (Boolean overwrite) {
        MapManagerWorker.overwrite = overwrite;
    }

    @Override
    protected Boolean doInBackground () throws Exception {
        String installPath = Settings.getSettings().getInstallPath();
        Map map = Map.getSelectedMap();
        if (new File(installPath, map.getSelectedCompatible() + "/minecraft/saves/" + map.getMapName()).exists()) {
            MapOverwriteDialog dialog = new MapOverwriteDialog();
            dialog.setVisible(true);
            if (overwrite) {
                FTBFileUtils.delete(new File(installPath, map.getSelectedCompatible() + "/minecraft/saves/" + map.getMapName()));
            } else {
                Logger.logInfo("Canceled map installation.");
                return false;
            }
        }
        downloadMap(map.getUrl(), map.getMapName());
        return false;
    }

    public void downloadUrl (String filename, String urlString) throws IOException, NoSuchAlgorithmException {
        BufferedInputStream in = null;
        FileOutputStream fout = null;
        try {
            URL url_ = new URL(urlString);
            in = new BufferedInputStream(url_.openStream());
            fout = new FileOutputStream(filename);
            byte data[] = new byte[1024];
            int count, amount = 0, steps = 0, mapSize = url_.openConnection().getContentLength();
            SwingUtilities.invokeLater(new Runnable() {
                public void run () {
                    setProgressBarMaximum(10000);
                }
            });
            while ((count = in.read(data, 0, 1024)) != -1) {
                fout.write(data, 0, count);
                downloadedPerc += (count * 1.0 / mapSize) * 100;
                amount += count;
                steps++;
                if (steps > 100) {
                    steps = 0;
                    final String txt = (amount / 1024) + "Kb / " + (mapSize / 1024) + "Kb";
                    final int perc = (int) downloadedPerc * 100;
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run () {
                            setProgressBarValue(perc);
                            setLabelText(txt);
                        }
                    });
                }
            }
        } catch (Exception e) {
            Logger.logError(e.getMessage(), e);
        } finally {
            in.close();
            fout.flush();
            fout.close();
        }
    }

    protected void downloadMap (String mapName, String dir) throws IOException, NoSuchAlgorithmException {
        Logger.logInfo("Downloading Map");
        String installPath = OSUtils.getCacheStorageLocation();
        Map map = Map.getSelectedMap();
        new File(installPath + "/" + MAPS + dir + "/").mkdirs();
        new File(installPath + "/" + MAPS + dir + "/" + mapName).createNewFile();
        downloadUrl(installPath + "/" + MAPS + dir + "/" + mapName, DownloadUtils.getCreeperhostLink(MAPS + dir + "/" + map.getVersion().replace(".", "_") + "/" + mapName));
        FTBFileUtils.extractZipTo(installPath + "/" + MAPS + dir + "/" + mapName, installPath + "/" + MAPS + dir);
        installMap(mapName, dir);
    }

    protected void installMap (String mapName, String dir) throws IOException {
        Logger.logInfo("Installing Map");
        String installPath = Settings.getSettings().getInstallPath();
        String tempPath = OSUtils.getCacheStorageLocation();
        Map map = Map.getSelectedMap();
        new File(installPath, map.getSelectedCompatible() + "/minecraft/saves/" + dir).mkdirs();
        FTBFileUtils.copyFolder(new File(tempPath, MAPS + dir + "/" + dir), new File(installPath, map.getSelectedCompatible() + "/minecraft/saves/" + dir));
        FTBFileUtils.copyFile(new File(tempPath, MAPS + dir + "/" + "version"), new File(installPath, map.getSelectedCompatible() + "/minecraft/saves/" + dir + "/version"));
        TrackerUtils.sendPageView(map.getName() + " Install", "Maps / " + map.getName());
    }

    public void setLabelText (String s) {

    }

    public void setProgressBarMaximum (int i) {

    }

    public void setProgressBarValue (int i) {

    }
}
