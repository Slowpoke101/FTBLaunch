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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JTextField;

import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.gui.GuiConstants;
import net.ftb.gui.LaunchFrame;
import net.ftb.locale.I18N;
import net.ftb.log.Logger;
import net.ftb.util.*;
import net.miginfocom.swing.MigLayout;

@SuppressWarnings("serial")
public class PrivatePackDialog extends JDialog {
    private JEditorPane editorPane;
    private JTextField modpackName;
    private JButton remove;
    private JButton add;
    private JButton cancel;

    public PrivatePackDialog() {
        super(LaunchFrame.getInstance(), false);

        setupGui();

        getRootPane().setDefaultButton(add);

        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e) {
                if (!modpackName.getText().isEmpty() && DownloadUtils.staticFileExists(modpackName.getText() + ".xml")) {
                    if (!packExists(modpackName.getText())) {
                        Logger.logInfo("Adding: " + modpackName.getText());
                        ModPack.loadXml(modpackName.getText() + ".xml");
                        Settings.getSettings().addPrivatePack(modpackName.getText());
                        Settings.getSettings().save();
                        setVisible(false);
                    } else {
                        ErrorUtils.tossError(I18N.getLocaleString("PRIVATEPACK_ALREADY_ADDED"));
                    }
                } else {
                    ErrorUtils.tossError(I18N.getLocaleString("PRIVATEPACK_ERROR"));
                }
            }
        });

        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e) {
                setVisible(false);
            }
        });

        remove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                ArrayList<String> codes = Settings.getSettings().getPrivatePacks();
                String toRemove = "";
                for (String s : codes) {
                    if (s.equalsIgnoreCase(modpackName.getText()))
                        toRemove = s;
                }
                if (!toRemove.isEmpty()) {
                    Settings.getSettings().removePrivatePack(toRemove);
                    Settings.getSettings().save();
                    try {
                        for (ModPack pack : ModPack.getPackArray()) {
                            if (pack.getParentXml().equalsIgnoreCase(toRemove + ".xml")) {
                                FTBFileUtils.delete(new File(OSUtils.getCacheStorageLocation(), "ModPacks/" + pack.getDir()));
                                break;
                            }
                        }
                        ModPack.removePacks(toRemove + ".xml");
                        FTBFileUtils.delete(new File(OSUtils.getCacheStorageLocation(), "ModPacks/" + toRemove + ".xml"));
                        LaunchFrame.getInstance().modPacksPane.filterPacks();
                    } catch (IOException e) {
                        Logger.logError("Error while deleting private modpack", e);
                    }
                    Logger.logInfo(modpackName.getText() + " " + I18N.getLocaleString("PRIVATEPACK_SECCESS"));
                    modpackName.setText("");
                    setVisible(false);
                } else {
                    Logger.logInfo(I18N.getLocaleString("PRIVATEPACK_NOTEXISTS"));
                }
            }
        });
    }

    private void setupGui () {
        setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
        setTitle(I18N.getLocaleString("PRIVATEPACK_TITLE"));
        setResizable(true);
        setPreferredSize(new Dimension(300, 200));

        Container panel = getContentPane();
        panel.setLayout(new MigLayout());

        editorPane = new JEditorPane();
        modpackName = new JTextField(16);
        remove = new JButton(I18N.getLocaleString("MAIN_REMOVE"));
        add = new JButton(I18N.getLocaleString("MAIN_ADD"));
        cancel = new JButton(I18N.getLocaleString("MAIN_CANCEL"));

        modpackName.setColumns(10);

        editorPane.setEditable(false);
        editorPane.setHighlighter(null);
        editorPane.setContentType("text/html");
        editorPane.setText(I18N.getLocaleString("PRIVATEPACK_TEXT"));

        panel.add(editorPane, GuiConstants.FILL_SINGLE_LINE);
        panel.add(modpackName, GuiConstants.FILL_SINGLE_LINE);
        panel.add(add, GuiConstants.FILL_THREE);
        panel.add(remove, GuiConstants.GROW);
        panel.add(cancel, GuiConstants.GROW);

        pack();
        modpackName.requestFocusInWindow();
        setLocationRelativeTo(getOwner());
    }

    private boolean packExists (String name) {
        for (String p : Settings.getSettings().getPrivatePacks()) {
            if (p.equalsIgnoreCase(name))
                return true;
        }
        return false;
    }
}
