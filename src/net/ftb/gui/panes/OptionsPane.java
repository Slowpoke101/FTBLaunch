/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2013, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
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

import java.awt.Color;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.net.URI;
import java.util.Map;

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

import net.ftb.data.Settings;
import net.ftb.download.Locations;
import net.ftb.gui.ChooseDir;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.dialogs.AdvancedOptionsDialog;
import net.ftb.locale.I18N;
import net.ftb.log.Logger;
import net.ftb.util.OSUtils;
import net.ftb.util.OSUtils.OS;
import net.ftb.util.winreg.JavaFinder;
import net.ftb.util.winreg.JavaInfo;

@SuppressWarnings("serial")
public class OptionsPane extends JPanel implements ILauncherPane {
    private JToggleButton tglbtnForceUpdate;
    private JButton installBrowseBtn, advancedOptionsBtn, btnInstallJava;
    private JLabel lblJavaVersion, lblInstallFolder, lblRamMaximum, lblLocale, currentRam, minecraftSize, lblX, lbl32BitWarning;
    private JSlider ramMaximum;
    private JComboBox locale;
    private JTextField installFolderTextField;
    private JCheckBox chckbxShowConsole, keepLauncherOpen;
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

        Boolean vm64Bits = OSUtils.is64BitVM();
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

        String[] locales;
        synchronized (I18N.localeIndices) {
            locales = new String[I18N.localeIndices.size()];
            for (Map.Entry<Integer, String> entry : I18N.localeIndices.entrySet()) {
                Logger.logInfo("[i18n] Added " + entry.getKey().toString() + " " + entry.getValue() + " to options pane");
                locales[entry.getKey()] = I18N.localeFiles.get(entry.getValue());
            }
        }
        locale = new JComboBox(locales);
        locale.setBounds(190, 130, 222, 25);
        locale.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e) {
                I18N.setLocale(I18N.localeIndices.get(locale.getSelectedIndex()));
                if (LaunchFrame.getInstance() != null) {
                    LaunchFrame.getInstance().updateLocale();
                }
            }
        });
        locale.addFocusListener(settingsChangeListener);
        locale.setSelectedItem(I18N.localeFiles.get(settings.getLocale()));

        lblLocale = new JLabel(I18N.getLocaleString("LANGUAGE"));
        lblLocale.setBounds(10, 130, 195, 25);
        add(lblLocale);
        add(locale);

        // Dependant on vmType from earlier RAM calculations to detect 64 bit JVM
        JavaInfo javaVersion = Settings.getSettings().getJavaVersion();
        if(javaVersion.getMajor() < 1 || (javaVersion.getMajor() == 1 && javaVersion.getMinor() < 7)){
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
        else if( OSUtils.getCurrentOS().equals(OS.MACOSX) && (javaVersion.getMajor() > 1 || (javaVersion.getMajor() == 1 ||  javaVersion.getMinor() > 7))){
            addUpdateJREButton(Locations.jdkMac, "DOWNLOAD_JAVAGOOD");//they need the jdk link
            addUpdateLabel("JAVA_NEW_Warning");
        }else if (!OSUtils.is64BitVM()) {//needs to use proper bit's
            addUpdateLabel("JAVA_32BIT_WARNING");
            if (OSUtils.getCurrentOS().equals(OS.WINDOWS)) {
                if (OSUtils.is64BitWindows()) {
                    addUpdateJREButton("http://javadl.sun.com/webapps/download/AutoDL?BundleId=81821", "DOWNLOAD_JAVA64");
                }
            }
        }

        chckbxShowConsole = new JCheckBox(I18N.getLocaleString("SHOW_CONSOLE"));
        chckbxShowConsole.addFocusListener(settingsChangeListener);
        chckbxShowConsole.setSelected(settings.getConsoleActive());
        chckbxShowConsole.setBounds(550, 95, 183, 25);
        add(chckbxShowConsole);

        keepLauncherOpen = new JCheckBox(I18N.getLocaleString("REOPEN_LAUNCHER"));
        keepLauncherOpen.setBounds(550, 130, 300, 25);
        keepLauncherOpen.setSelected(settings.getKeepLauncherOpen());
        keepLauncherOpen.addFocusListener(settingsChangeListener);
        add(keepLauncherOpen);

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

        if (OSUtils.getCurrentOS().equals(OS.WINDOWS) && JavaFinder.parseJavaVersion() != null && JavaFinder.parseJavaVersion().path != null) {
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
        settings.setLocale(I18N.localeIndices.get(locale.getSelectedIndex()));
        settings.setConsoleActive(chckbxShowConsole.isSelected());
        settings.setKeepLauncherOpen(keepLauncherOpen.isSelected());
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
        btnInstallJava = new JButton(I18N.getLocaleString(unlocMessage));
        btnInstallJava.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();
                    try {
                        desktop.browse(new URI(webLink));
                    } catch (Exception exc) {
                        Logger.logError("Could not open url: " + exc.getMessage());
                    }
                } else {
                    Logger.logWarn("Could not open Java Download url, not supported");
                }
            }
        });
        btnInstallJava.setBounds(345, 200, 150, 28);
        add(btnInstallJava);
    }
    public void addUpdateLabel(final String unlocMessage){
        lbl32BitWarning = new JLabel(I18N.getLocaleString(unlocMessage));
        lbl32BitWarning.setBounds(190, 170, 500, 25);
        lbl32BitWarning.setForeground(Color.red);
        add(lbl32BitWarning);

    }
}
