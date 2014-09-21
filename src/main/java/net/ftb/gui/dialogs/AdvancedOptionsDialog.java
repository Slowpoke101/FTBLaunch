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
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.Spring;
import javax.swing.SpringLayout;

import net.ftb.data.Settings;
import net.ftb.download.Locations;
import net.ftb.gui.LaunchFrame;
import net.ftb.locale.I18N;
import net.ftb.log.Logger;
import net.ftb.util.OSUtils;
import net.ftb.util.SwingUtils;
import net.ftb.util.winreg.JavaFinder;
import net.ftb.util.winreg.JavaInfo;

@SuppressWarnings("unchecked")
public class AdvancedOptionsDialog extends JDialog {
    private JButton exit;
    private JLabel downloadLocationLbl;
    private static JComboBox downloadLocation;
    private JLabel javaPathLbl;
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
        if (javaPath.getSelectedIndex() >= 1){
            settings.setJavaPath(javapaths[javaPath.getSelectedIndex() - 1]);
        }
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
        SpringLayout layout = new SpringLayout();
        getContentPane().setLayout(layout);

        downloadLocationLbl = new JLabel(I18N.getLocaleString("ADVANCED_OPTIONS_DLLOCATION"));
        downloadLocation = new JComboBox(getDownloadServerNames());
        javaPathLbl = new JLabel(I18N.getLocaleString("ADVANCED_OPTIONS_JAVA_PATH"));
        List<JavaInfo> javas = JavaFinder.findJavas();
        String[] javaslist = new String[javas.size() + 2];
        javapaths = new String[javas.size() + 1];
        int i = 0;
        for (JavaInfo java : javas) {
            javaslist[i + 1] = java.version;
            if(java.is64bits){
                javaslist[i + 1] = javaslist[i + 1] + " 64bit";
            }
            javapaths[i] = java.path;
            i++;
        }
        javaslist[i + 1] = "Default";
        javapaths[i] = "";
        javaPath = new JComboBox(javaslist);
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
        add(downloadLocation);
        add(javaPathLbl);
        add(javaPath);
        add(additionalJavaOptionsLbl);
        add(additionalJavaOptions);
        add(mcWindowSizeLbl);
        add(mcWindowSizeWidth);
        add(mcWindowSizeSepLbl);
        add(mcWindowSizeHeight);
        add(mcWindowPosLbl);
        add(mcWindowPosX);
        add(mcWindowPosSepLbl);
        add(mcWindowPosY);
        add(autoMaxCheck);
        add(snooper);
        add(debugLauncherVerbose);
        add(betaChannel);
        add(exit);

        Spring hSpring;
        Spring columnWidth;

        hSpring = Spring.constant(10);

        layout.putConstraint(SpringLayout.WEST, downloadLocationLbl, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, javaPathLbl, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, additionalJavaOptionsLbl, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, mcWindowSizeLbl, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, mcWindowPosLbl, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, autoMaxCheck, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, snooper, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, debugLauncherVerbose, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, betaChannel, hSpring, SpringLayout.WEST, panel);

        columnWidth = SwingUtils.springMax(Spring.width(downloadLocationLbl), Spring.width(javaPathLbl), Spring.width(additionalJavaOptionsLbl), Spring.width(mcWindowSizeLbl),
                Spring.width(mcWindowPosLbl));

        hSpring = SwingUtils.springSum(hSpring, columnWidth, Spring.constant(10));

        layout.putConstraint(SpringLayout.WEST, downloadLocation, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, javaPath, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, additionalJavaOptions, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, mcWindowSizeWidth, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, mcWindowPosX, hSpring, SpringLayout.WEST, panel);

        columnWidth = Spring.width(mcWindowSizeWidth);
        columnWidth = Spring.max(columnWidth, Spring.width(mcWindowPosX));

        hSpring = Spring.sum(hSpring, columnWidth);

        layout.putConstraint(SpringLayout.EAST, mcWindowSizeWidth, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, mcWindowPosX, hSpring, SpringLayout.WEST, panel);

        hSpring = Spring.sum(hSpring, Spring.constant(5));

        layout.putConstraint(SpringLayout.WEST, mcWindowSizeSepLbl, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, mcWindowPosSepLbl, hSpring, SpringLayout.WEST, panel);

        columnWidth = Spring.width(mcWindowSizeSepLbl);
        columnWidth = Spring.max(columnWidth, Spring.width(mcWindowPosSepLbl));

        hSpring = Spring.sum(hSpring, columnWidth);
        hSpring = Spring.sum(hSpring, Spring.constant(5));

        layout.putConstraint(SpringLayout.WEST, mcWindowSizeHeight, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, mcWindowPosY, hSpring, SpringLayout.WEST, panel);

        columnWidth = Spring.width(mcWindowSizeHeight);
        columnWidth = Spring.max(columnWidth, Spring.width(mcWindowPosY));

        hSpring = Spring.sum(hSpring, columnWidth);

        layout.putConstraint(SpringLayout.EAST, downloadLocation, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, javaPath, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, additionalJavaOptions, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, mcWindowSizeHeight, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, mcWindowPosY, hSpring, SpringLayout.WEST, panel);

        hSpring = Spring.sum(hSpring, Spring.constant(10));

        layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, exit, 0, SpringLayout.HORIZONTAL_CENTER, panel);

        layout.putConstraint(SpringLayout.EAST, panel, hSpring, SpringLayout.WEST, panel);

        Spring vSpring;
        Spring rowHeight;

        vSpring = Spring.constant(10);

        layout.putConstraint(SpringLayout.BASELINE, downloadLocationLbl, 0, SpringLayout.BASELINE, downloadLocation);
        layout.putConstraint(SpringLayout.NORTH, downloadLocation, vSpring, SpringLayout.NORTH, panel);
        rowHeight = Spring.height(downloadLocationLbl);
        rowHeight = Spring.max(rowHeight, Spring.height(downloadLocation));

        vSpring = SwingUtils.springSum(vSpring, rowHeight, Spring.constant(10));

        layout.putConstraint(SpringLayout.BASELINE, javaPathLbl, 0, SpringLayout.BASELINE, javaPath);
        layout.putConstraint(SpringLayout.NORTH, javaPath, vSpring, SpringLayout.NORTH, panel);

        rowHeight = Spring.height(javaPathLbl);
        rowHeight = Spring.max(rowHeight, Spring.height(javaPath));

        vSpring = SwingUtils.springSum(vSpring, rowHeight, Spring.constant(10));

        layout.putConstraint(SpringLayout.BASELINE, additionalJavaOptionsLbl, 0, SpringLayout.BASELINE, additionalJavaOptions);
        layout.putConstraint(SpringLayout.NORTH, additionalJavaOptions, vSpring, SpringLayout.NORTH, panel);

        rowHeight = Spring.height(additionalJavaOptionsLbl);
        rowHeight = Spring.max(rowHeight, Spring.height(additionalJavaOptions));

        vSpring = SwingUtils.springSum(vSpring, rowHeight, Spring.constant(10));

        layout.putConstraint(SpringLayout.BASELINE, mcWindowSizeLbl, 0, SpringLayout.BASELINE, mcWindowSizeWidth);
        layout.putConstraint(SpringLayout.NORTH, mcWindowSizeWidth, vSpring, SpringLayout.NORTH, panel);
        layout.putConstraint(SpringLayout.BASELINE, mcWindowSizeSepLbl, 0, SpringLayout.BASELINE, mcWindowSizeWidth);
        layout.putConstraint(SpringLayout.NORTH, mcWindowSizeHeight, vSpring, SpringLayout.NORTH, panel);

        rowHeight = SwingUtils.springMax(Spring.height(mcWindowSizeLbl), Spring.height(mcWindowSizeWidth), Spring.height(mcWindowSizeSepLbl), Spring.height(mcWindowSizeHeight));

        vSpring = SwingUtils.springSum(vSpring, rowHeight, Spring.constant(10));

        layout.putConstraint(SpringLayout.BASELINE, mcWindowPosLbl, 0, SpringLayout.BASELINE, mcWindowPosX);
        layout.putConstraint(SpringLayout.NORTH, mcWindowPosX, vSpring, SpringLayout.NORTH, panel);
        layout.putConstraint(SpringLayout.BASELINE, mcWindowPosSepLbl, 0, SpringLayout.BASELINE, mcWindowPosX);
        layout.putConstraint(SpringLayout.NORTH, mcWindowPosY, vSpring, SpringLayout.NORTH, panel);

        rowHeight = SwingUtils.springMax(Spring.height(mcWindowPosLbl), Spring.height(mcWindowPosX), Spring.height(mcWindowPosSepLbl), Spring.height(mcWindowPosY));

        vSpring = SwingUtils.springSum(vSpring, rowHeight, Spring.constant(10));

        layout.putConstraint(SpringLayout.NORTH, autoMaxCheck, vSpring, SpringLayout.NORTH, panel);

        vSpring = SwingUtils.springSum(vSpring, Spring.height(autoMaxCheck), Spring.constant(10));

        layout.putConstraint(SpringLayout.NORTH, snooper, vSpring, SpringLayout.NORTH, panel);

        vSpring = SwingUtils.springSum(vSpring, Spring.height(snooper), Spring.constant(10));

        layout.putConstraint(SpringLayout.NORTH, debugLauncherVerbose, vSpring, SpringLayout.NORTH, panel);

        vSpring = SwingUtils.springSum(vSpring, Spring.height(debugLauncherVerbose), Spring.constant(10));

        layout.putConstraint(SpringLayout.NORTH, betaChannel, vSpring, SpringLayout.NORTH, panel);

        vSpring = SwingUtils.springSum(vSpring, Spring.height(betaChannel), Spring.constant(10));

        layout.putConstraint(SpringLayout.NORTH, exit, vSpring, SpringLayout.NORTH, panel);

        vSpring = SwingUtils.springSum(vSpring, Spring.height(exit), Spring.constant(10));

        layout.putConstraint(SpringLayout.SOUTH, panel, vSpring, SpringLayout.NORTH, panel);

        pack();
        setLocationRelativeTo(getOwner());
    }
}
