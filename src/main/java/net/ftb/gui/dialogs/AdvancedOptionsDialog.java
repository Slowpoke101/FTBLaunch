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
package net.ftb.gui.dialogs;

import net.ftb.data.Settings;
import net.ftb.download.Locations;
import net.ftb.gui.GuiConstants;
import net.ftb.gui.LaunchFrame;
import net.ftb.locale.I18N;
import net.ftb.log.Logger;
import net.ftb.util.OSUtils;
import net.ftb.util.winreg.JavaFinder;
import net.ftb.util.winreg.JavaInfo;
import net.miginfocom.swing.MigLayout;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.Collections;
import java.util.List;

import javax.swing.*;

@SuppressWarnings("unchecked")
public class AdvancedOptionsDialog extends JDialog {
    private JButton exit;
    private JLabel downloadLocationLbl;
    private static JComboBox downloadLocation;
    private JLabel javaPathLbl;
    private JTextField javaPathText;
    private static JComboBox javaPath;
    private String[] javapaths;
    private JLabel additionalJavaOptionsLbl;
    private JTextField additionalJavaOptions;
    private JLabel mcWindowSizeLbl;
    private JTextField mcWindowSizeWidth;
    private JLabel mcWindowSizeSepLbl;
    private JTextField mcWindowSizeHeight;
    private JLabel mcWindowPosLbl;
    private JTextField mcWindowPosX;
    private JLabel mcWindowPosSepLbl;
    private JTextField mcWindowPosY;
    private JCheckBox autoMaxCheck;
    private JCheckBox snooper;
    private JCheckBox debugLauncherVerbose;
    private JCheckBox betaChannel;

    private final Settings settings = Settings.getSettings();

    //TODO add a UI adjustment tab here?
    public AdvancedOptionsDialog () {
        super(LaunchFrame.getInstance(), true);
        setupGui();

        if (Locations.serversLoaded) {
            if (Locations.downloadServers.containsKey(settings.getDownloadServer())) {
                downloadLocation.setSelectedItem(settings.getDownloadServer());
            }
        }

        mcWindowSizeWidth.setText(Integer.toString(settings.getLastDimension().width));
        mcWindowSizeHeight.setText(Integer.toString(settings.getLastDimension().height));
        mcWindowPosX.setText(Integer.toString(settings.getLastPosition().x));
        mcWindowPosY.setText(Integer.toString(settings.getLastPosition().y));
        autoMaxCheck.setSelected((settings.getLastExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH);
        snooper.setSelected(settings.getSnooper());
        debugLauncherVerbose.setSelected(settings.getDebugLauncher());
        betaChannel.setSelected(settings.isBetaChannel());

        FocusAdapter settingsChangeListener = new FocusAdapter() {
            @Override
            public void focusLost (FocusEvent e) {
                saveSettingsInto(settings);
            }
        };

        downloadLocation.addFocusListener(settingsChangeListener);
        if (javaPathText != null) {
            javaPathText.addFocusListener(settingsChangeListener);
        }
        if (javaPath != null) {
            javaPath.addFocusListener(settingsChangeListener);
        }
        additionalJavaOptions.addFocusListener(settingsChangeListener);
        mcWindowSizeWidth.addFocusListener(settingsChangeListener);
        mcWindowSizeHeight.addFocusListener(settingsChangeListener);
        mcWindowPosX.addFocusListener(settingsChangeListener);
        mcWindowPosY.addFocusListener(settingsChangeListener);
        autoMaxCheck.addFocusListener(settingsChangeListener);
        snooper.addFocusListener(settingsChangeListener);
        debugLauncherVerbose.addFocusListener(settingsChangeListener);
        betaChannel.addFocusListener(settingsChangeListener);
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e) {
                setVisible(false);
            }
        });
    }

    public static void setDownloadServers () {
        String downloadserver = Settings.getSettings().getDownloadServer();
        downloadLocation.removeAllItems();
        for (String server : Locations.downloadServers.keySet()) {
            downloadLocation.addItem(server);
        }
        if (Locations.downloadServers.containsKey(downloadserver)) {
            downloadLocation.setSelectedItem(downloadserver);
        }
    }

    public String[] getDownloadServerNames () {
        if (!Locations.serversLoaded) {
            Logger.logWarn("Servers not loaded yet.");
            return new String[] { "Automatic" };
        } else {
            String[] out = new String[Locations.downloadServers.size()];
            for (int i = 0; i < out.length; i++) {
                out[i] = String.valueOf(Locations.downloadServers.keySet().toArray()[i]);
            }
            return out;
        }
    }

    public void saveSettingsInto (Settings settings) {
        settings.setDownloadServer(String.valueOf(downloadLocation.getItemAt(downloadLocation.getSelectedIndex())));
        settings.setLastDimension(new Dimension(Integer.parseInt(mcWindowSizeWidth.getText()), Integer.parseInt(mcWindowSizeHeight.getText())));
        int lastExtendedState = settings.getLastExtendedState();
        settings.setLastExtendedState(autoMaxCheck.isSelected() ? (lastExtendedState | JFrame.MAXIMIZED_BOTH) : (lastExtendedState & ~JFrame.MAXIMIZED_BOTH));
        settings.setLastPosition(new Point(Integer.parseInt(mcWindowPosX.getText()), Integer.parseInt(mcWindowPosY.getText())));
        if (OSUtils.getCurrentOS() == OSUtils.OS.UNIX) {
            settings.setJavaPath(javaPathText.getText());
        } else {
            if (javaPath.getSelectedIndex() >= 0) {
                settings.setJavaPath(javapaths[javaPath.getSelectedIndex()]);
            }
        }
        settings.setAdditionalJavaOptions(additionalJavaOptions.getText());
        settings.setSnooper(snooper.isSelected());
        settings.setDebugLauncher(debugLauncherVerbose.isSelected());
        settings.setBetaChannel(betaChannel.isSelected());
        settings.save();
        // invalidate current java information
        Settings.setCurrentJava(null);
        //update options pane
        LaunchFrame.getInstance().optionsPane.updateJavaLabels();
    }

    private void setupGui () {
        setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
        setTitle(I18N.getLocaleString("ADVANCED_OPTIONS_TITLE"));
        setResizable(true); // false

        Container panel = getContentPane();
        getContentPane().setLayout(new MigLayout());

        downloadLocationLbl = new JLabel(I18N.getLocaleString("ADVANCED_OPTIONS_DLLOCATION"));
        downloadLocation = new JComboBox(getDownloadServerNames());
        javaPathLbl = new JLabel(I18N.getLocaleString("ADVANCED_OPTIONS_JAVA_PATH"));
        if (OSUtils.getCurrentOS() == OSUtils.OS.UNIX) {
            javaPathText = new JTextField();
            String javapath = settings.getJavaPath();
            if (javapath != null) {
                javaPathText.setText(javapath);
                if (!new File(javapath).isFile()) {
                    javaPathText.setBackground(Color.RED);
                }
            } else {
                // this should not happen ever
                javaPathText.setBackground(Color.RED);
            }

            javaPathText.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped (KeyEvent e) {
                }

                @Override
                public void keyPressed (KeyEvent e) {
                }

                @Override
                public void keyReleased (KeyEvent e) {
                    if (!javaPathText.getText().equals("") && !new File(javaPathText.getText()).isFile()) {
                        javaPathText.setBackground(Color.RED);
                    } else {
                        javaPathText.setBackground(new Color(40, 40, 40));
                    }
                }
            });
        } else {
            List<JavaInfo> javas = JavaFinder.findJavas();
            Collections.sort(javas, JavaInfo.PREFERRED_SORTING);
            // if we are going to remove duplicates here => what are duplicates?
            // * same version + bitness?
            // * same path?
            String[] javaslist = new String[javas.size() + 1];
            javapaths = new String[javas.size() + 1];
            int i = -1;
            for (JavaInfo java : javas) {
                i++;
                javaslist[i] = java.origVersion;
                if (java.is64bits) {
                    javaslist[i] = javaslist[i] + " 64bit";
                }
                javapaths[i] = java.path;
            }
            javaslist[i + 1] = "Default";
            javapaths[i + 1] = "";
            javaPath = new JComboBox(javaslist);

            //TODO: set current selected java
            String selectedJavaPath = Settings.getSettings().getJavaPath();
            if (selectedJavaPath.equals(Settings.getSettings().getDefaultJavaPath())) {
                javaPath.setSelectedIndex(i + 1);
            } else {
                i = 0;
                for (JavaInfo java : javas) {
                    if (java.path.equals(selectedJavaPath)) {
                        javaPath.setSelectedIndex(i);
                    }
                    i++;
                }
            }
        }
        additionalJavaOptionsLbl = new JLabel(I18N.getLocaleString("ADVANCED_OPTIONS_ADDJAVAOPTIONS"));
        additionalJavaOptions = new JTextField(settings.getAdditionalJavaOptions(), 30);
        mcWindowSizeLbl = new JLabel(I18N.getLocaleString("ADVANCED_OPTIONS_MCWINDOW_SIZE"));
        mcWindowSizeWidth = new JTextField(4);
        mcWindowSizeSepLbl = new JLabel("x");
        mcWindowSizeHeight = new JTextField(4);
        mcWindowPosLbl = new JLabel(I18N.getLocaleString("ADVANCED_OPTIONS_MCWINDOW_POS"));
        mcWindowPosX = new JTextField(4);
        mcWindowPosSepLbl = new JLabel("x");
        mcWindowPosY = new JTextField(4);
        autoMaxCheck = new JCheckBox(I18N.getLocaleString("ADVANCED_OPTIONS_MCWINDOW_AUTOMAXCHECK"));
        snooper = new JCheckBox(I18N.getLocaleString("ADVANCED_OPTIONS_DISABLEGOOGLEANALYTICS"));
        debugLauncherVerbose = new JCheckBox(I18N.getLocaleString("ADVANCED_OPTIONS_DEBUGLAUNCHERVERBOSE"));
        betaChannel = new JCheckBox(I18N.getLocaleString("ADVANCED_OPTIONS_BETA"));
        exit = new JButton(I18N.getLocaleString("MAIN_EXIT"));

        downloadLocationLbl.setLabelFor(downloadLocation);

        add(downloadLocationLbl);
        add(downloadLocation, GuiConstants.WRAP);
        add(javaPathLbl);
        if (javaPathText != null) {
            add(javaPathText, GuiConstants.WRAP);
        }
        if (javaPath != null) {
            add(javaPath, GuiConstants.WRAP);
        }
        add(additionalJavaOptionsLbl);
        add(additionalJavaOptions, GuiConstants.GROW + GuiConstants.SEP + GuiConstants.WRAP);
        add(mcWindowSizeLbl, GuiConstants.FILL_FOUR);
        add(mcWindowSizeWidth);
        add(mcWindowSizeSepLbl);
        add(mcWindowSizeHeight, GuiConstants.WRAP);
        add(mcWindowPosLbl, GuiConstants.FILL_FOUR);
        add(mcWindowPosX);
        add(mcWindowPosSepLbl);
        add(mcWindowPosY, GuiConstants.WRAP);
        add(autoMaxCheck, GuiConstants.WRAP);
        add(snooper, GuiConstants.WRAP);
        add(debugLauncherVerbose, GuiConstants.WRAP);
        add(betaChannel, GuiConstants.WRAP);
        add(exit, GuiConstants.CENTER_SINGLE_LINE);

        pack();
        setLocationRelativeTo(getOwner());
    }
}
