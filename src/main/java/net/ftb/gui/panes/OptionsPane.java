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
package net.ftb.gui.panes;

import net.ftb.data.Settings;
import net.ftb.download.Locations;
import net.ftb.gui.ChooseDir;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.dialogs.AdvancedOptionsDialog;
import net.ftb.locale.I18N;
import net.ftb.locale.Locale;
import net.ftb.util.OSUtils;
import net.ftb.util.OSUtils.OS;
import net.ftb.util.winreg.JavaFinder;
import net.ftb.util.winreg.JavaInfo;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
public class OptionsPane extends JPanel implements ILauncherPane {
    private JToggleButton tglbtnForceUpdate;
    private JButton installBrowseBtn, advancedOptionsBtn, btnInstallJava = new JButton();
    private JLabel lblJavaVersion, lblInstallFolder, lblRamMaximum, lblLocale, currentRam, lbl32BitWarning = new JLabel();
    private JSlider ramMaximum;
    private JComboBox locale;
    private JTextField installFolderTextField;
    private JCheckBox chckbxShowConsole, keepLauncherOpen, optJavaArgs, useSystemProxy;
    private final Settings settings;

    private FocusListener settingsChangeListener = new FocusListener() {
        @Override
        public void focusLost (FocusEvent e) {
            saveSettingsInto(settings);
        }

        @Override
        public void focusGained (FocusEvent e) {
        }
    };

    public OptionsPane(Settings settings) {
        this.settings = settings;
        setBorder(new EmptyBorder(5, 5, 5, 5));

        installBrowseBtn = new JButton("...");
        installBrowseBtn.setBounds(786, 11, 49, 28);
        installBrowseBtn.addActionListener(new ChooseDir(this));
        setLayout(null);
        add(installBrowseBtn);

        lblInstallFolder = new JLabel(I18N.getLocaleString("INSTALL_FOLDER"));
        lblInstallFolder.setBounds(10, 11, 127, 28);
        add(lblInstallFolder);

        installFolderTextField = new JTextField();
        installFolderTextField.setBounds(147, 11, 629, 28);
        installFolderTextField.addFocusListener(settingsChangeListener);
        installFolderTextField.setColumns(10);
        installFolderTextField.setText(settings.getInstallPath());
        add(installFolderTextField);

        tglbtnForceUpdate = new JToggleButton(I18N.getLocaleString("FORCE_UPDATE"));
        tglbtnForceUpdate.setBounds(147, 48, 629, 29);
        tglbtnForceUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                saveSettingsInto(OptionsPane.this.settings);
            }
        });
        tglbtnForceUpdate.getModel().setPressed(settings.isForceUpdateEnabled());
        add(tglbtnForceUpdate);

        currentRam = new JLabel();
        currentRam.setBounds(427, 95, 85, 25);
        long ram = OSUtils.getOSTotalMemory();
        long freeram = OSUtils.getOSFreeMemory();

        ramMaximum = new JSlider();
        ramMaximum.setBounds(190, 95, 222, 25);
        ramMaximum.setSnapToTicks(true);
        ramMaximum.setMajorTickSpacing(256);
        ramMaximum.setMinorTickSpacing(256);
        ramMaximum.setMinimum(256);

        Boolean vm64Bits = Settings.getSettings().getCurrentJava().is64bits;
        if (vm64Bits != null) {
            if (vm64Bits) {
                ramMaximum.setMaximum((int) ram);
            } else {
                if (ram < 1024) {
                    ramMaximum.setMaximum((int) ram);
                } else {
                    if (freeram > 2046) {
                        ramMaximum.setMaximum(1536);
                    } else {
                        ramMaximum.setMaximum(1024);
                    }
                }
            }
        }
        int ramMax = (Integer.parseInt(settings.getRamMax()) > ramMaximum.getMaximum()) ? ramMaximum.getMaximum() : Integer.parseInt(settings.getRamMax());
        ramMaximum.setValue(ramMax);
        currentRam.setText(getAmount());
        ramMaximum.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged (ChangeEvent arg0) {
                currentRam.setText(getAmount());
            }
        });
        ramMaximum.addFocusListener(settingsChangeListener);

        lblRamMaximum = new JLabel(I18N.getLocaleString("RAM_MAX"));
        lblRamMaximum.setBounds(10, 95, 195, 25);
        add(lblRamMaximum);
        add(ramMaximum);
        add(currentRam);

        locale = new JComboBox<String>(I18N.available());
        locale.setBounds(190, 130, 222, 25);
        locale.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e) {
                I18N.setLocale(Locale.values()[locale.getSelectedIndex()].name());
                if (LaunchFrame.getInstance() != null) {
                    LaunchFrame.getInstance().updateLocale();
                }
                Settings.getSettings().setLocale(Locale.values()[locale.getSelectedIndex()].name());
            }
        });
        locale.addFocusListener(settingsChangeListener);
        locale.setSelectedItem(I18N.current());

        lblLocale = new JLabel(I18N.getLocaleString("LANGUAGE"));
        lblLocale.setBounds(10, 130, 195, 25);
        add(lblLocale);
        add(locale);

        updateJavaLabels();

        chckbxShowConsole = new JCheckBox(I18N.getLocaleString("SHOW_CONSOLE"));
        chckbxShowConsole.addFocusListener(settingsChangeListener);
        chckbxShowConsole.setSelected(settings.getConsoleActive());
        chckbxShowConsole.setBounds(540, 95, 183, 25);
        add(chckbxShowConsole);

        keepLauncherOpen = new JCheckBox(I18N.getLocaleString("REOPEN_LAUNCHER"));
        keepLauncherOpen.setBounds(540, 130, 300, 25);
        keepLauncherOpen.setSelected(settings.getKeepLauncherOpen());
        keepLauncherOpen.addFocusListener(settingsChangeListener);
        add(keepLauncherOpen);

        optJavaArgs = new JCheckBox(I18N.getLocaleString("OPT_JAVA_ARGS"));
        optJavaArgs.setBounds(540, 165, 300, 25);
        optJavaArgs.setSelected(settings.getOptJavaArgs());
        optJavaArgs.addFocusListener(settingsChangeListener);
        add(optJavaArgs);

        useSystemProxy = new JCheckBox(I18N.getLocaleString("USE_SYSTEM_PROXY"));
        useSystemProxy.setBounds(540, 200, 300, 25);
        useSystemProxy.setSelected(settings.getUseSystemProxy());
        useSystemProxy.addFocusListener(settingsChangeListener);
        add(useSystemProxy);

        advancedOptionsBtn = new JButton(I18N.getLocaleString("ADVANCED_OPTIONS"));
        advancedOptionsBtn.setBounds(147, 275, 629, 29);
        advancedOptionsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                AdvancedOptionsDialog aod = new AdvancedOptionsDialog();
                aod.setVisible(true);
            }
        });
        advancedOptionsBtn.getModel().setPressed(settings.isForceUpdateEnabled());
        add(advancedOptionsBtn);

        if ((OSUtils.getCurrentOS().equals(OS.WINDOWS) || OSUtils.getCurrentOS().equals(OS.MACOSX)) && JavaFinder.parseJavaVersion() != null && JavaFinder.parseJavaVersion().path != null) {
            lblJavaVersion = new JLabel("Java version: " + JavaFinder.parseJavaVersion().origVersion);
            lblJavaVersion.setBounds(15, 276, 250, 25);
            add(lblJavaVersion);
        }
    }

    public void setInstallFolderText (String text) {
        installFolderTextField.setText(text);
        saveSettingsInto(settings);
    }

    public void saveSettingsInto (Settings settings) {
        settings.setInstallPath(installFolderTextField.getText());
        settings.setForceUpdateEnabled(tglbtnForceUpdate.isSelected());
        settings.setRamMax(String.valueOf(ramMaximum.getValue()));
        settings.setLocale(Locale.values()[locale.getSelectedIndex()].name());
        settings.setConsoleActive(chckbxShowConsole.isSelected());
        settings.setOptJavaArgs(optJavaArgs.isSelected());
        settings.setKeepLauncherOpen(keepLauncherOpen.isSelected());
        settings.setUseSystemProxy(useSystemProxy.isSelected());
        settings.save();
    }

    public void updateLocale () {
        lblInstallFolder.setText(I18N.getLocaleString("INSTALL_FOLDER"));
        tglbtnForceUpdate.setText(I18N.getLocaleString("FORCE_UPDATE"));
        lblRamMaximum.setText(I18N.getLocaleString("RAM_MAX"));
        lblLocale.setText(I18N.getLocaleString("LANGUAGE"));
    }

    private String getAmount () {
        int ramMax = ramMaximum.getValue();
        return (ramMax >= 1024) ? Math.round((ramMax / 256) / 4) + "." + (((ramMax / 256) % 4) * 25) + " GB" : ramMax + " MB";
    }

    @Override
    public void onVisible () {
    }

    public void addUpdateJREButton(final String webLink, String unlocMessage){
        btnInstallJava.setText(I18N.getLocaleString(unlocMessage));
        btnInstallJava.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                OSUtils.browse(webLink);
            }
        });
        btnInstallJava.setBounds(345, 210, 150, 28);
        add(btnInstallJava);
    }

    public void addUpdateLabel(final String unlocMessage){
        lbl32BitWarning.setText(I18N.getLocaleString(unlocMessage));
        lbl32BitWarning.setBounds(147, 180, 600, 25);
        lbl32BitWarning.setForeground(Color.red);
        add(lbl32BitWarning);

    }

    public void updateJavaLabels() {
        remove(lbl32BitWarning);
        remove(btnInstallJava);
        // Dependant on vmType from earlier RAM calculations to detect 64 bit JVM
        JavaInfo java = Settings.getSettings().getCurrentJava();
        if(java.getMajor() < 1 || (java.getMajor() == 1 && java.getMinor() < 7)){
            if(OSUtils.getCurrentOS().equals(OS.MACOSX)){
                if(JavaFinder.java8Found) {//they need the jdk link
                    addUpdateJREButton(Locations.jdkMac, "DOWNLOAD_JAVAGOOD");
                    addUpdateLabel("JAVA_NEW_Warning");
                }else if(OSUtils.canRun7OnMac()){
                    addUpdateJREButton(Locations.jreMac, "DOWNLOAD_JAVAGOOD");
                    addUpdateLabel("JAVA_OLD_Warning");
                }else{
                    //TODO deal with old mac's that can't run java 7
                }
            }
            else if(OSUtils.is64BitOS()){
                if(OSUtils.getCurrentOS().equals(OS.WINDOWS)){
                    addUpdateJREButton(Locations.java64Win, "DOWNLOAD_JAVA64");
                    addUpdateLabel("JAVA_OLD_Warning");
                }
                else if(OSUtils.getCurrentOS().equals(OS.UNIX)){
                    addUpdateJREButton(Locations.java64Lin, "DOWNLOAD_JAVA64");
                    addUpdateLabel("JAVA_OLD_Warning");
                }
            }else{
                if(OSUtils.getCurrentOS().equals(OS.WINDOWS)){
                    addUpdateJREButton(Locations.java32Win, "DOWNLOAD_JAVA32");
                    addUpdateLabel("JAVA_OLD_Warning");
                }
                else if(OSUtils.getCurrentOS().equals(OS.UNIX)){
                    addUpdateJREButton(Locations.java32Lin, "DOWNLOAD_JAVA32");
                    addUpdateLabel("JAVA_OLD_Warning");
                }
            }
        }
        else if(OSUtils.getCurrentOS().equals(OS.MACOSX) && (java.getMajor() > 1 ||  java.getMinor() > 7)){
            addUpdateJREButton(Locations.jdkMac, "DOWNLOAD_JAVAGOOD");//they need the jdk link
            addUpdateLabel("JAVA_NEW_Warning");
        }else if (!Settings.getSettings().getCurrentJava().is64bits) {//needs to use proper bit's
            addUpdateLabel("JAVA_32BIT_WARNING");
            if (OSUtils.getCurrentOS().equals(OS.WINDOWS)) {
                if (OSUtils.is64BitWindows()) {
                    addUpdateJREButton(Locations.java64Win, "DOWNLOAD_JAVA64");
                }
            }
        }
        repaint();
    }

    public void updateShowConsole() {
        chckbxShowConsole.setSelected(settings.getConsoleActive());
    }
}