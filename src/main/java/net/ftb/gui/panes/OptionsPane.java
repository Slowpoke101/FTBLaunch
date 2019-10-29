/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2018, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
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

import net.feed_the_beast.launcher.json.JsonFactory;
import net.feed_the_beast.launcher.json.app.Export;
import net.ftb.data.Constants;
import net.ftb.data.ModPack;
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
import net.ftb.util.winreg.JavaVersion;
import org.apache.commons.io.FileUtils;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.swing.*;
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

    private JPanel fitterPane;

    private FocusListener settingsChangeListener = new FocusListener() {
        @Override
        public void focusLost (FocusEvent e) {
            saveSettingsInto(settings);
        }

        @Override
        public void focusGained (FocusEvent e) {
        }
    };

    public OptionsPane (Settings settings) {
        this.settings = settings;

        fitterPane = new JPanel();
        fitterPane.setMinimumSize(new Dimension(840, 340));
        fitterPane.setMaximumSize(new Dimension(840, 340));
        fitterPane.setLayout(null);

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(fitterPane);

        installBrowseBtn = new JButton("...");
        installBrowseBtn.setBounds(786, 11, 49, 28);
        installBrowseBtn.addActionListener(new ChooseDir(this));

        fitterPane.add(installBrowseBtn);

        lblInstallFolder = new JLabel(I18N.getLocaleString("INSTALL_FOLDER"));
        lblInstallFolder.setBounds(10, 11, 127, 28);
        fitterPane.add(lblInstallFolder);

        installFolderTextField = new JTextField();
        installFolderTextField.setBounds(147, 11, 629, 28);
        installFolderTextField.addFocusListener(settingsChangeListener);
        installFolderTextField.setColumns(10);
        installFolderTextField.setText(settings.getInstallPath());
        fitterPane.add(installFolderTextField);

        tglbtnForceUpdate = new JToggleButton(I18N.getLocaleString("FORCE_UPDATE"));
        tglbtnForceUpdate.setBounds(147, 48, 629, 29);
        tglbtnForceUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                saveSettingsInto(OptionsPane.this.settings);
            }
        });
        tglbtnForceUpdate.getModel().setPressed(settings.isForceUpdateEnabled());
        fitterPane.add(tglbtnForceUpdate);

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
        fitterPane.add(lblRamMaximum);
        fitterPane.add(ramMaximum);
        fitterPane.add(currentRam);

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
        fitterPane.add(lblLocale);
        fitterPane.add(locale);

        updateJavaLabels();

        chckbxShowConsole = new JCheckBox(I18N.getLocaleString("SHOW_CONSOLE"));
        chckbxShowConsole.addFocusListener(settingsChangeListener);
        chckbxShowConsole.setSelected(settings.getConsoleActive());
        chckbxShowConsole.setBounds(540, 95, 183, 25);
        fitterPane.add(chckbxShowConsole);

        keepLauncherOpen = new JCheckBox(I18N.getLocaleString("REOPEN_LAUNCHER"));
        keepLauncherOpen.setBounds(540, 130, 300, 25);
        keepLauncherOpen.setSelected(settings.getKeepLauncherOpen());
        keepLauncherOpen.addFocusListener(settingsChangeListener);
        fitterPane.add(keepLauncherOpen);

        optJavaArgs = new JCheckBox(I18N.getLocaleString("OPT_JAVA_ARGS"));
        optJavaArgs.setBounds(540, 165, 300, 25);
        optJavaArgs.setSelected(settings.getOptJavaArgs());
        optJavaArgs.addFocusListener(settingsChangeListener);
        fitterPane.add(optJavaArgs);

        useSystemProxy = new JCheckBox(I18N.getLocaleString("USE_SYSTEM_PROXY"));
        useSystemProxy.setBounds(540, 200, 300, 25);
        useSystemProxy.setSelected(settings.getUseSystemProxy());
        useSystemProxy.addFocusListener(settingsChangeListener);
        fitterPane.add(useSystemProxy);

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
        fitterPane.add(advancedOptionsBtn);

        if ((OSUtils.getCurrentOS().equals(OS.WINDOWS) || OSUtils.getCurrentOS().equals(OS.MACOSX)) && JavaFinder.parseJavaVersion() != null && JavaFinder.parseJavaVersion().path != null) {
            lblJavaVersion = new JLabel("Java version: " + JavaFinder.parseJavaVersion().origVersion);
            lblJavaVersion.setBounds(15, 276, 250, 25);
            fitterPane.add(lblJavaVersion);
        }
    }

    public static void exportData () {
        Logger.logDebug("exporting data");
        Export export = new Export(ModPack.getPackArray(), Constants.version, new File(Settings.getSettings().getInstallPath() + File.separator + "libraries/").getAbsolutePath(),
                Settings.getSettings().getInstallPath());
        String json = JsonFactory.exportToApp(export);
        //TODO dump to disk
        String base = OSUtils.getCacheStorageLocation();
        try {
            FileUtils.writeStringToFile(new File(base + File.separator + "migrationdata.json"), json);
        } catch (IOException e) {
            Logger.logError("Migration Data json write failed", e);
        }

        Logger.logDebug("done exporting data");
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

    public void addUpdateJREButton (final String webLink, String unlocMessage) {
        btnInstallJava.setText(I18N.getLocaleString(unlocMessage));
        btnInstallJava.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                OSUtils.browse(webLink);
            }
        });
        btnInstallJava.setBounds(345, 210, 150, 28);
        fitterPane.add(btnInstallJava);
    }

    public void addUpdateLabel (final String unlocMessage) {
        lbl32BitWarning.setText(I18N.getLocaleString(unlocMessage));
        lbl32BitWarning.setBounds(147, 180, 600, 25);
        lbl32BitWarning.setForeground(Color.red);
        fitterPane.add(lbl32BitWarning);

    }

    public void offerJava7 (String reason) {
        if (OSUtils.getCurrentOS().equals(OS.MACOSX)) {
            if (OSUtils.canRun7OnMac()) {
                addUpdateJREButton(Locations.jreMac, "DOWNLOAD_JAVAGOOD");
                addUpdateLabel(reason);
            } else {
                //TODO deal with old mac's that can't run java 7
            }
        } else if (OSUtils.is64BitOS()) {
            if (OSUtils.getCurrentOS().equals(OS.WINDOWS)) {
                addUpdateJREButton(Locations.java64Win, "DOWNLOAD_JAVA64");
                addUpdateLabel(reason);
            } else if (OSUtils.getCurrentOS().equals(OS.UNIX)) {
                addUpdateJREButton(Locations.java64Lin, "DOWNLOAD_JAVA64");
                addUpdateLabel(reason);
            }
        } else {
            if (OSUtils.getCurrentOS().equals(OS.WINDOWS)) {
                addUpdateJREButton(Locations.java32Win, "DOWNLOAD_JAVA32");
                addUpdateLabel(reason);
            } else if (OSUtils.getCurrentOS().equals(OS.UNIX)) {
                addUpdateJREButton(Locations.java32Lin, "DOWNLOAD_JAVA32");
                addUpdateLabel(reason);
            }
        }
    }

    public void updateJavaLabels () {
        remove(lbl32BitWarning);
        if (btnInstallJava != null) {
            remove(btnInstallJava);
        }
        // Dependant on vmType from earlier RAM calculations to detect 64 bit JVM
        JavaInfo java = Settings.getSettings().getCurrentJava();
        JavaVersion java7 = JavaVersion.createJavaVersion("1.7.0");
        // offer java 7 if java 6 or older is detected
        if (java.isOlder(java7)) {
            offerJava7("JAVA_OLD_Warning");
        }

        // offer 64-bit java if 32-bit java detected in 64-bit OS
        else if (!Settings.getSettings().getCurrentJava().is64bits) {//needs to use proper bit's
            addUpdateLabel("JAVA_32BIT_WARNING");
            if (OSUtils.getCurrentOS().equals(OS.WINDOWS)) {
                if (OSUtils.is64BitWindows()) {
                    addUpdateJREButton(Locations.java64Win, "DOWNLOAD_JAVA64");
                }
            }
        }
        repaint();
    }

    public void updateShowConsole () {
        chckbxShowConsole.setSelected(settings.getConsoleActive());
    }
}
