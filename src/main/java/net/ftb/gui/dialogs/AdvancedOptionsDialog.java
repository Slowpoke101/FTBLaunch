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
package net.ftb.gui.dialogs;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import net.ftb.data.Settings;
import net.ftb.download.Locations;
import net.ftb.gui.GuiConstants;
import net.ftb.gui.LaunchFrame;
import net.ftb.locale.I18N;
import net.ftb.log.Logger;
import net.miginfocom.swing.MigLayout;

@SuppressWarnings("unchecked")
public class AdvancedOptionsDialog extends JDialog {
    private JButton exit;
    private JLabel downloadLocationLbl;
    private static JComboBox downloadLocation;
    private JLabel javaPathLbl;
    private JTextField javaPath;
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
    public AdvancedOptionsDialog() {
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
        javaPath.addFocusListener(settingsChangeListener);
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
        settings.setJavaPath(javaPath.getText());
        settings.setAdditionalJavaOptions(additionalJavaOptions.getText());
        settings.setSnooper(snooper.isSelected());
        settings.setDebugLauncher(debugLauncherVerbose.isSelected());
        settings.setBetaChannel(betaChannel.isSelected());
        settings.save();
        // invalidate current java information
        settings.setCurrentJava(null);
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
        javaPath = new JTextField();
        String javapath = settings.getJavaPath();
        if (javapath != null) {
            javaPath.setText(javapath);
            if (!new File(javapath).isFile())
                javaPath.setBackground(Color.RED);
        } else {
            // this should not happen ever
            javaPath.setBackground(Color.RED);
        }

        javaPath.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped (KeyEvent e) {
            }

            @Override
            public void keyPressed (KeyEvent e) {
            }

            @Override
            public void keyReleased (KeyEvent e) {
                if (!javaPath.getText().equals("") && !new File(javaPath.getText()).isFile())
                    javaPath.setBackground(Color.RED);
                else
                    javaPath.setBackground(new Color(40, 40, 40));
            }
        });
        additionalJavaOptionsLbl = new JLabel(I18N.getLocaleString("ADVANCED_OPTIONS_ADDJAVAOPTIONS"));
        additionalJavaOptions = new JTextField(settings.getAdditionalJavaOptions());
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
        add(javaPath, GuiConstants.WRAP);
        add(additionalJavaOptionsLbl);
        add(additionalJavaOptions,  GuiConstants.GROW + GuiConstants.SEP + GuiConstants.WRAP);
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
