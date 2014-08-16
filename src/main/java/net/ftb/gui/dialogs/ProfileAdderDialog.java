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

import java.awt.Container;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.ftb.data.UserManager;
import net.ftb.gui.GuiConstants;
import net.ftb.gui.LaunchFrame;
import net.ftb.locale.I18N;
import net.ftb.util.ErrorUtils;
import net.ftb.util.SwingUtils;
import net.miginfocom.swing.MigLayout;

@SuppressWarnings("serial")
public class ProfileAdderDialog extends JDialog {
    private String updatecreds = "";
    private JLabel usernameLbl;
    private JTextField username;
    private JLabel passwordLbl;
    private JPasswordField password;
    private JLabel nameLbl;
    private JTextField name;
    private JCheckBox savePassword, saveMojangData;
    private JButton add;
    private JLabel messageLbl;

    public ProfileAdderDialog(LaunchFrame instance, String unlocalizedMessage, boolean modal) {
        super(instance, modal);
        setUnlocalizedMessage(unlocalizedMessage);
        preSetup();
    }

    public ProfileAdderDialog(LaunchFrame instance, boolean modal) {
        super(instance, modal);
        preSetup();

    }

    public void preSetup () {
        setupGui();

        getRootPane().setDefaultButton(add);

        savePassword.setSelected(true);
        saveMojangData.setSelected(true);

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

        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent event) {
                if (savePassword.isSelected()) {
                    if (validate(name.getText(), username.getText(), password.getPassword())) {
                        UserManager.addUser(username.getText(), new String(password.getPassword()), name.getText());
                        UserManager.setSaveMojangData(username.getText(), saveMojangData.isSelected());
                        LaunchFrame.writeUsers(name.getText());
                        setVisible(false);
                    } else {
                        ErrorUtils.tossError(I18N.getLocaleString("PROFILADDER_ERROR"));
                    }
                } else {
                    if (validate(name.getText(), username.getText())) {
                        UserManager.addUser(username.getText(), "", name.getText());
                        UserManager.setSaveMojangData(username.getText(), saveMojangData.isSelected());
                        LaunchFrame.writeUsers(name.getText());
                        setVisible(false);
                    } else {
                        ErrorUtils.tossError(I18N.getLocaleString("PROFILADDER_ERROR"));
                    }
                }
            }
        });
    }

    public void setUnlocalizedMessage (String editingName) {
        if (editingName.equals("CHANGEDUUID")) {
            updatecreds = I18N.getLocaleString(editingName);
            password.setEnabled(false);
            savePassword.setSelected(false);
        } else if (editingName.equals("OLDCREDS")) {
            updatecreds = I18N.getLocaleString(editingName);
            password.setEnabled(false);
            savePassword.setSelected(false);
        }

    }

    private boolean validate (String name, String user, char[] pass) {
        if (!name.isEmpty() && !user.isEmpty() && pass.length > 1) {
            if (!UserManager.getNames().contains(name) && !UserManager.getUsernames().contains(user)) {
                return true;
            }
        }
        return false;
    }

    private boolean validate (String name, String user) {
        if (!name.isEmpty() && !user.isEmpty()) {
            if (!UserManager.getNames().contains(name) && !UserManager.getUsernames().contains(user)) {
                return true;
            }
        }
        return false;
    }

    private void setupGui () {
        setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
        setTitle(I18N.getLocaleString("PROFILEADDER_TITLE"));
        setResizable(false);

        Container panel = getContentPane();
        MigLayout layout = new MigLayout();
        panel.setLayout(layout);

        usernameLbl = new JLabel(I18N.getLocaleString("PROFILEADDER_USERNAME"));
        username = new JTextField(16);
        passwordLbl = new JLabel(I18N.getLocaleString("PROFILEADDER_PASSWORD"));
        password = new JPasswordField(16);
        nameLbl = new JLabel(I18N.getLocaleString("PROFILEADDER_NAME"));
        name = new JTextField(16);
        savePassword = new JCheckBox(I18N.getLocaleString("PROFILEADDER_SAVEPASSWORD"));
        saveMojangData = new JCheckBox(I18N.getLocaleString("PROFILEADDER_SAVEMOJANGDATA"));
        add = new JButton(I18N.getLocaleString("MAIN_ADD"));

        usernameLbl.setLabelFor(username);
        passwordLbl.setLabelFor(password);
        nameLbl.setLabelFor(name);

        if (!updatecreds.equals("")) {
            messageLbl = new JLabel(updatecreds);
            panel.add(messageLbl);
        }
        panel.add(usernameLbl);
        panel.add(username, GuiConstants.WRAP);
        panel.add(passwordLbl);
        panel.add(password, GuiConstants.WRAP);
        panel.add(nameLbl);
        panel.add(name, GuiConstants.WRAP);
        panel.add(savePassword, GuiConstants.CENTER_SINGLE_LINE);
        panel.add(saveMojangData, GuiConstants.CENTER_SINGLE_LINE);
        panel.add(add, "center, wrap, span");

        pack();
        setLocationRelativeTo(getOwner());
    }
}
