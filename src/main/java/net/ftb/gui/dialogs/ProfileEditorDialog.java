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

import net.ftb.data.UserManager;
import net.ftb.gui.GuiConstants;
import net.ftb.gui.LaunchFrame;
import net.ftb.locale.I18N;
import net.miginfocom.swing.MigLayout;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

@SuppressWarnings("serial")
public class ProfileEditorDialog extends JDialog {
    private JLabel usernameLbl;
    private JTextField username;
    private JLabel passwordLbl;
    private JPasswordField password;
    private JLabel nameLbl;
    private JTextField name;
    private JCheckBox savePassword, saveMojangData;
    private JButton update;
    private JButton remove;

    public ProfileEditorDialog (LaunchFrame instance, final String editingName, boolean modal) {
        super(instance, modal);

        setupGui();

        getRootPane().setDefaultButton(update);

        username.setText(UserManager.getUsername(editingName));
        name.setText(editingName);

        if (UserManager.getPassword(editingName).isEmpty()) {
            password.setEnabled(false);
            savePassword.setSelected(false);
        } else {
            password.setText(UserManager.getPassword(editingName));
            savePassword.setSelected(true);
        }

        saveMojangData.setSelected(UserManager.getSaveMojangData(editingName));

        username.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate (DocumentEvent arg0) {
                name.setText(username.getText());
            }

            @Override
            public void insertUpdate (DocumentEvent arg0) {
                name.setText(username.getText());
            }

            @Override
            public void changedUpdate (DocumentEvent e) {
            }
        });

        savePassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent event) {
                password.setEnabled(savePassword.isSelected());
            }
        });

        update.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                if (editingName.equals(name.getText()) || (!UserManager.getUsernames().contains(username.getText()) && !UserManager.getNames().contains(name.getText()))) {
                    if (savePassword.isSelected()) {
                        if (password.getPassword().length > 1) {
                            UserManager.updateUser(editingName, username.getText(), new String(password.getPassword()), name.getText());
                        }
                    } else {
                        UserManager.updateUser(editingName, username.getText(), "", name.getText());
                    }
                    UserManager.setSaveMojangData(editingName, saveMojangData.isSelected());
                    LaunchFrame.writeUsers(name.getText());
                    setVisible(false);
                }
            }
        });

        remove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                UserManager.removeUser(editingName);
                LaunchFrame.writeUsers(null);
                setVisible(false);
            }
        });
    }

    private void setupGui () {
        setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
        setTitle(I18N.getLocaleString("PROFILEDITOR_TITLE"));
        setResizable(true);

        Container panel = getContentPane();
        panel.setLayout(new MigLayout());

        usernameLbl = new JLabel(I18N.getLocaleString("PROFILEADDER_USERNAME"));
        username = new JTextField(16);
        passwordLbl = new JLabel(I18N.getLocaleString("PROFILEADDER_PASSWORD"));
        password = new JPasswordField(16);
        nameLbl = new JLabel(I18N.getLocaleString("PROFILEADDER_NAME"));
        name = new JTextField(16);
        savePassword = new JCheckBox(I18N.getLocaleString("PROFILEADDER_SAVEPASSWORD"));
        saveMojangData = new JCheckBox(I18N.getLocaleString("PROFILEADDER_SAVEMOJANGDATA"));
        update = new JButton(I18N.getLocaleString("PROFILEADDER_UPDATE"));
        remove = new JButton(I18N.getLocaleString("MAIN_REMOVE"));

        usernameLbl.setLabelFor(username);
        passwordLbl.setLabelFor(password);
        nameLbl.setLabelFor(name);

        panel.add(usernameLbl);
        panel.add(username, GuiConstants.WRAP);
        panel.add(passwordLbl);
        panel.add(password, GuiConstants.WRAP);
        panel.add(nameLbl);
        panel.add(name, GuiConstants.WRAP);
        panel.add(savePassword, GuiConstants.CENTER_SINGLE_LINE);
        panel.add(saveMojangData, GuiConstants.CENTER_SINGLE_LINE);
        panel.add(update, GuiConstants.CENTER_TWO);
        panel.add(remove);

        pack();
        setLocationRelativeTo(getOwner());
    }
}
