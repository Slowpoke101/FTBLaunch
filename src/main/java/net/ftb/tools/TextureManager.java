/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2017, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
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
package net.ftb.tools;

import static net.ftb.download.Locations.TEXTUREPACKS;

import com.google.common.collect.Maps;
import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.data.TexturePack;
import net.ftb.log.Logger;
import net.ftb.util.DownloadUtils;
import net.ftb.util.ErrorUtils;
import net.ftb.util.TrackerUtils;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

@SuppressWarnings("serial")
public class TextureManager extends JDialog {
    private static TexturePack updateTexture;
    private static ModPack updateModPack;
    private JPanel contentPane;
    private double downloadedPerc;
    private final JProgressBar progressBar;
    private final JLabel label;
    public static boolean updating = false;
    private static String sep = File.separator;
    private static HashMap<String, String> installedTextures;

    private class TexturePackManagerWorker extends SwingWorker<Boolean, Void> {
        @Override
        protected Boolean doInBackground () throws Exception {
            String installPath = Settings.getSettings().getInstallPath();
            TexturePack texturePack = TexturePack.getSelectedTexturePack();
            String compDir = texturePack.getSelectedCompatible();
            ModPack compPack = ModPack.getPack(compDir);
            int mcversion = Integer.parseInt(compPack.getMcVersion().replace(".", ""));
            if (updating) {
                texturePack = updateTexture;
                compDir = updateModPack.getDir();
                compPack = updateModPack;
            }
            if (compPack == null) {
                ErrorUtils.tossError("Error: Invalid Mod Pack destination selected.");
                return false;
            }

            String packVer = (Settings.getSettings().getPackVer(compDir).equalsIgnoreCase("Recommended Version") ? compPack.getVersion() : Settings.getSettings().getPackVer(compDir))
                    .replace(".", "_");
            String packStr;
            if (mcversion > 160) {
                packStr = "";
            } else {
                packStr = "/" + packVer;
            }
            if (!compPack.hasCustomTP()
                    && DownloadUtils.fileExists(TEXTUREPACKS + texturePack.getName().replace(" ", "_") + "/" + "master_" + compPack.getMcVersion().replace(".", "_") + packStr + "/"
                    + texturePack.getUrl())) {
                populateInstalledTextures(compPack);
                File oldFile = new File(installPath, texturePack.getSelectedCompatible() + sep + "minecraft" + sep + getTPDirectory(mcversion) + sep + texturePack.getUrl());
                if (oldFile.exists()) {
                    oldFile.delete();
                }
                return downloadTexturePack(texturePack.getUrl(), texturePack.getName(), "master_" + compPack.getMcVersion().replace(".", "_"), compDir, packVer);
            } else if (DownloadUtils.fileExists(TEXTUREPACKS + texturePack.getName().replace(" ", "_") + "/" + compDir + packStr + "/" + texturePack.getUrl())) {
                populateInstalledTextures(compPack);
                File oldFile = new File(installPath, texturePack.getSelectedCompatible() + sep + "minecraft" + sep + getTPDirectory(mcversion) + sep + texturePack.getUrl());
                if (oldFile.exists()) {
                    oldFile.delete();
                }
                return downloadTexturePack(texturePack.getUrl(), texturePack.getName(), compDir, compDir, packVer);
            } else {
                ErrorUtils.tossError("Error: Texture Pack not found for selected mod pack's version!");
                return false;
            }
        }

        public boolean downloadUrl (String filename, String urlString) {
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
                        progressBar.setMaximum(10000);
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
                                progressBar.setValue(perc);
                                label.setText(txt);
                            }
                        });
                    }
                }
            } catch (MalformedURLException e) {
                Logger.logError("Texture Download Error", e);
                return false;
            } catch (IOException e) {
                Logger.logError("Texture Download Error", e);
                return false;
            } finally {
                try {
                    in.close();
                    fout.flush();
                    fout.close();
                } catch (IOException e) {
                    Logger.logWarn("Error while downloading texture pack", e);
                }
            }
            return true;
        }

        protected boolean downloadTexturePack (String texturePackName, String dir, String remoteName, String compDir, String packVer) throws IOException, NoSuchAlgorithmException {
            Logger.logInfo("Downloading Texture Pack");
            String installPath = Settings.getSettings().getInstallPath();
            ModPack pack = ModPack.getPack(compDir);
            int mcversion = Integer.parseInt(pack.getMcVersion().replace(".", ""));
            new File(installPath, compDir + sep + "minecraft" + sep + getTPDirectory(mcversion) + sep).mkdirs();
            new File(installPath, compDir + sep + "minecraft" + sep + getTPDirectory(mcversion) + sep + texturePackName).createNewFile();
            String packStr;
            if (mcversion > 160) {
                packStr = "";
            } else {
                packStr = "/" + packVer;
            }
            if (downloadUrl(installPath + sep + compDir + sep + "minecraft" + sep + getTPDirectory(mcversion) + sep + texturePackName,
                    DownloadUtils.getCreeperhostLink(TEXTUREPACKS + dir.replace(" ", "_") + "/" + remoteName + packStr + "/" + texturePackName))) {
                File versionFile = new File(installPath, compDir + sep + "minecraft" + sep + getTPDirectory(mcversion) + sep + "textureVersions");
                installedTextures.put(dir.toLowerCase(), packVer);
                BufferedWriter out = new BufferedWriter(new FileWriter(versionFile));
                for (int i = 0; i < installedTextures.size(); i++) {
                    out.write(installedTextures.keySet().toArray()[i] + ":" + installedTextures.values().toArray()[i]);
                    out.newLine();
                }
                out.flush();
                out.close();
                TrackerUtils.sendPageView(dir + " Install", "Texture Packs / " + dir + " / " + compDir + " / " + packVer);
                return true;
            }
            return false;
        }
    }

    public static String getTPDirectory (int MCVersion) {
        if (MCVersion < 160) {
            return "texturepacks";
        } else {
            return "resourcepacks";
        }
    }

    public TextureManager (JFrame owner, Boolean model) {
        super(owner, model);
        setResizable(false);
        setTitle("Downloading...");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setBounds(100, 100, 313, 138);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        progressBar = new JProgressBar();
        progressBar.setBounds(10, 63, 278, 22);
        contentPane.add(progressBar);

        JLabel lblDownloadingTexture = new JLabel("<html><body><center>Downloading texture pack...<br/>Please Wait</center></body></html>");
        lblDownloadingTexture.setHorizontalAlignment(SwingConstants.CENTER);
        lblDownloadingTexture.setBounds(0, 5, 313, 30);
        contentPane.add(lblDownloadingTexture);

        label = new JLabel("");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBounds(0, 42, 313, 14);
        contentPane.add(label);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened (WindowEvent arg0) {
                TexturePackManagerWorker worker = new TexturePackManagerWorker() {
                    @Override
                    protected void done () {
                        try {
                            get();
                        } catch (InterruptedException e) {
                            Logger.logDebug("Swingworker Exception", e);
                        } catch (ExecutionException e) {
                            Logger.logDebug("Swingworker Exception", e.getCause());
                        }
                        setVisible(false);
                        super.done();
                    }
                };
                worker.execute();
            }
        });
    }

    public static void updateTextures () throws NoSuchAlgorithmException, IOException {
        boolean removed = false;
        ModPack pack = ModPack.getSelectedPack();
        int mcversion = Integer.parseInt(pack.getMcVersion().replace(".", ""));
        String installDir = Settings.getSettings().getInstallPath();
        File textureVersionFile = new File(installDir, pack.getDir() + sep + "minecraft" + sep + getTPDirectory(mcversion) + sep + "textureVersions");
        if (textureVersionFile.exists()) {
            populateInstalledTextures(pack);
            if (installedTextures.size() > 0) {
                for (TexturePack tp : TexturePack.getTexturePackArray()) {
                    if (installedTextures.containsKey(tp.getName().toLowerCase()) && tp.isCompatible(pack.getDir())) {
                        File texturePackFile = new File(installDir, pack.getDir() + sep + "minecraft" + sep + getTPDirectory(mcversion) + sep + tp.getUrl());
                        if (texturePackFile.exists()) {
                            String version = (Settings.getSettings().getPackVer().equalsIgnoreCase("Recommended Version") ? pack.getVersion() : Settings.getSettings().getPackVer()).replace(".", "_");
                            if (!installedTextures.get(tp.getName().toLowerCase()).equalsIgnoreCase(version)) {
                                String packStr;
                                if (mcversion > 160) {
                                    packStr = "";
                                } else {
                                    packStr = "/" + version;
                                }
                                if (DownloadUtils.fileExists(TEXTUREPACKS + tp.getName().replace(" ", "_") + "/" + pack.getDir() + "/" + packStr + "/" + tp.getUrl())) {
                                    updating = true;
                                    TextureManager man = new TextureManager(new JFrame(), true);
                                    TextureManager.updateTexture = tp;
                                    TextureManager.updateModPack = pack;
                                    man.setVisible(true);
                                }
                            }
                        } else {
                            installedTextures.remove(tp.getName().toLowerCase());
                            removed = true;
                        }
                    }
                }
                if (removed) {
                    BufferedWriter out = new BufferedWriter(new FileWriter(textureVersionFile));
                    for (int i = 0; i < installedTextures.size(); i++) {
                        out.write(installedTextures.keySet().toArray()[i] + ":" + installedTextures.values().toArray()[i]);
                        out.newLine();
                    }
                    out.flush();
                    out.close();
                }
            }
        }
    }

    private static void populateInstalledTextures (ModPack pack) {

        int mcversion = Integer.parseInt(pack.getMcVersion().replace(".", ""));
        File textureVersionFile = new File(Settings.getSettings().getInstallPath(), pack.getDir() + sep + "minecraft" + sep + getTPDirectory(mcversion) + sep + "textureVersions");
        if (installedTextures != null) {
            installedTextures.clear();
        } else {
            installedTextures = Maps.newHashMap();
        }
        if (textureVersionFile.exists()) {
            try {
                BufferedReader in = new BufferedReader(new FileReader(textureVersionFile));
                String line;
                while ((line = in.readLine()) != null) {
                    String[] split = line.toLowerCase().split(":");
                    if (split.length == 2) {
                        installedTextures.put(split[0], split[1]);
                    }
                }
                in.close();
            } catch (Exception e) {
                Logger.logError("Error populating installed textures.", e);
            }
        }
    }
}
