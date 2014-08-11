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
package net.ftb.tools;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import net.ftb.data.Map;
import net.ftb.data.Settings;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.dialogs.MapOverwriteDialog;
import net.ftb.log.Logger;
import net.ftb.util.DownloadUtils;
import net.ftb.util.FileUtils;
import net.ftb.util.OSUtils;
import net.ftb.util.TrackerUtils;
import net.ftb.workers.MapManagerWorker;

import static net.ftb.download.Locations.MAPS;
@SuppressWarnings("serial")
public class MapManager extends JDialog {
    private JPanel contentPane;
    private final JProgressBar progressBar;
    private final JLabel label;
    public static boolean overwrite = false;
    private static String sep = File.separator;


    public MapManager(JFrame owner, Boolean model) {
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

        JLabel lblDownloadingMap = new JLabel("<html><body><center>Downloading map...<br/>Please Wait</center></body></html>");
        lblDownloadingMap.setHorizontalAlignment(SwingConstants.CENTER);
        lblDownloadingMap.setBounds(0, 5, 313, 30);
        contentPane.add(lblDownloadingMap);

        label = new JLabel("");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBounds(0, 42, 313, 14);
        contentPane.add(label);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened (WindowEvent arg0) {
                MapManagerWorker worker = new MapManagerWorker(overwrite) {
                    @Override
                    protected void done () {
                        setVisible(false);
                        super.done();
                    }
                    @Override
                    public void setLabelText(String s) {
                        label.setText(s);
                    }
                    @Override
                    public void setProgressBarMaximum (int i){
                        progressBar.setMaximum(i);
                    }
                    @Override
                    public void setProgressBarValue (int i){
                        progressBar.setValue(i);
                    }

                };
                worker.execute();
            }
        });
    }

    public static void cleanUp () {
        Map map = Map.getMap(LaunchFrame.getSelectedMapIndex());
        File tempFolder = new File(OSUtils.getCacheStorageLocation(),  MAPS.replace("/",sep) + map.getMapName() + sep);
        for (String file : tempFolder.list()) {
            if (!file.equals(map.getLogoName()) && !file.equals(map.getImageName()) && !file.equalsIgnoreCase("version")) {
                try {
                    FileUtils.delete(new File(tempFolder, file));
                } catch (IOException e) {
                    Logger.logError(e.getMessage(), e);
                }
            }
        }
    }
}
