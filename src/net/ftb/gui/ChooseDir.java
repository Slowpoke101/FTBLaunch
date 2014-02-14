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
package net.ftb.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import net.ftb.data.Settings;
import net.ftb.gui.dialogs.EditModPackDialog;
import net.ftb.gui.dialogs.InstallDirectoryDialog;
import net.ftb.gui.panes.OptionsPane;
import net.ftb.log.Logger;
import net.ftb.util.ErrorUtils;
import net.ftb.util.FileUtils;

public class ChooseDir extends JFrame implements ActionListener {
    private OptionsPane optionsPane;
    private EditModPackDialog editMPD;
    private InstallDirectoryDialog installDialog;
    private String choosertitle = "Please select an install location";

    public ChooseDir(OptionsPane optionsPane) {
        super();
        this.optionsPane = optionsPane;
        editMPD = null;
    }

    public ChooseDir(EditModPackDialog editMPD) {
        super();
        optionsPane = null;
        this.editMPD = editMPD;
    }

    public ChooseDir(InstallDirectoryDialog installDialog) {
        super();
        optionsPane = null;
        editMPD = null;
        this.installDialog = installDialog;
    }

    @Override
    public void actionPerformed (ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        if (optionsPane != null) {
            chooser.setCurrentDirectory(new File(Settings.getSettings().getInstallPath()));
            chooser.setDialogTitle(choosertitle);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                Logger.logInfo("getCurrentDirectory(): " + chooser.getCurrentDirectory());
                Logger.logInfo("getSelectedFile() : " + chooser.getSelectedFile());
                optionsPane.setInstallFolderText(chooser.getSelectedFile().getPath());
            }
            else {
                Logger.logWarn("No Selection.");
            }
        }
        else if (editMPD != null) {
            if (!Settings.getSettings().getLastAddPath().isEmpty()) {
                chooser.setCurrentDirectory(new File(Settings.getSettings().getLastAddPath()));
            }
            chooser.setDialogTitle("Please select the mod to install");
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setAcceptAllFileFilterUsed(true);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File destination = new File(editMPD.folder, chooser.getSelectedFile().getName());
                if (!destination.exists()) {
                    try {
                        FileUtils.copyFile(chooser.getSelectedFile(), destination);
                        Settings.getSettings().setLastAddPath(chooser.getSelectedFile().getPath());
                        LaunchFrame.getInstance().saveSettings();
                    }
                    catch (IOException e1) {
                        Logger.logError(e1.getMessage());
                    }
                    editMPD.updateLists();
                }
                else {
                    ErrorUtils.tossError("File already exists, cannot add mod!");
                }
            }
            else {
                Logger.logWarn("No Selection.");
            }
        }
        else {
            chooser.setCurrentDirectory(new File(Settings.getSettings().getInstallPath()));
            chooser.setDialogTitle(choosertitle);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                Logger.logInfo("getCurrentDirectory(): " + chooser.getCurrentDirectory());
                Logger.logInfo("getSelectedFile() : " + chooser.getSelectedFile());
                installDialog.setInstallFolderText(chooser.getSelectedFile().getPath());
            }
            else {
                Logger.logWarn("No Selection.");
            }
        }
    }

    @Override
    public Dimension getPreferredSize () {
        return new Dimension(200, 200);
    }
}