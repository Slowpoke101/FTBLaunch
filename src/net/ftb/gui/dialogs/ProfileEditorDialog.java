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
import net.ftb.gui.LaunchFrame;
import net.ftb.locale.I18N;

@SuppressWarnings("serial")
public class ProfileEditorDialog extends JDialog {
    private JLabel usernameLbl;
    private JTextField username;
    private JLabel passwordLbl;
    private JPasswordField password;
    private JLabel nameLbl;
    private JTextField name;
    private JCheckBox savePassword;
    private JButton update;
    private JButton remove;

    public ProfileEditorDialog(LaunchFrame instance, final String editingName, boolean modal) {
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
        setResizable(false);

        Container panel = getContentPane();
        SpringLayout layout = new SpringLayout();
        panel.setLayout(layout);

        usernameLbl = new JLabel(I18N.getLocaleString("PROFILEADDER_USERNAME"));
        username = new JTextField(16);
        passwordLbl = new JLabel(I18N.getLocaleString("PROFILEADDER_PASSWORD"));
        password = new JPasswordField(16);
        nameLbl = new JLabel(I18N.getLocaleString("PROFILEADDER_NAME"));
        name = new JTextField(16);
        savePassword = new JCheckBox(I18N.getLocaleString("PROFILEADDER_SAVEPASSWORD"));
        update = new JButton(I18N.getLocaleString("PROFILEADDER_UPDATE"));
        remove = new JButton(I18N.getLocaleString("MAIN_REMOVE"));

        usernameLbl.setLabelFor(username);
        passwordLbl.setLabelFor(password);
        nameLbl.setLabelFor(name);

        panel.add(usernameLbl);
        panel.add(username);
        panel.add(passwordLbl);
        panel.add(password);
        panel.add(nameLbl);
        panel.add(name);
        panel.add(savePassword);
        panel.add(update);
        panel.add(remove);

        Spring hSpring;
        Spring columnWidth;

        hSpring = Spring.constant(10);

        layout.putConstraint(SpringLayout.WEST, usernameLbl, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, passwordLbl, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, nameLbl, hSpring, SpringLayout.WEST, panel);

        columnWidth = Spring.width(usernameLbl);
        columnWidth = Spring.max(columnWidth, Spring.width(passwordLbl));
        columnWidth = Spring.max(columnWidth, Spring.width(nameLbl));

        hSpring = Spring.sum(hSpring, columnWidth);
        hSpring = Spring.sum(hSpring, Spring.constant(10));

        layout.putConstraint(SpringLayout.WEST, username, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, password, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, name, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, savePassword, hSpring, SpringLayout.WEST, panel);

        columnWidth = Spring.width(username);
        columnWidth = Spring.max(columnWidth, Spring.width(password));
        columnWidth = Spring.max(columnWidth, Spring.width(name));
        columnWidth = Spring.max(columnWidth, Spring.width(savePassword));

        hSpring = Spring.sum(hSpring, columnWidth);
        hSpring = Spring.sum(hSpring, Spring.constant(10));

        layout.putConstraint(SpringLayout.EAST, panel, hSpring, SpringLayout.WEST, panel);

        layout.putConstraint(SpringLayout.EAST, update, -5, SpringLayout.HORIZONTAL_CENTER, panel);
        layout.putConstraint(SpringLayout.WEST, remove, 5, SpringLayout.HORIZONTAL_CENTER, panel);

        Spring vSpring;
        Spring rowHeight;

        vSpring = Spring.constant(10);

        layout.putConstraint(SpringLayout.BASELINE, usernameLbl, 0, SpringLayout.BASELINE, username);
        layout.putConstraint(SpringLayout.NORTH, username, vSpring, SpringLayout.NORTH, panel);

        rowHeight = Spring.height(usernameLbl);
        rowHeight = Spring.max(rowHeight, Spring.height(username));

        vSpring = Spring.sum(vSpring, rowHeight);
        vSpring = Spring.sum(vSpring, Spring.constant(5));

        layout.putConstraint(SpringLayout.BASELINE, passwordLbl, 0, SpringLayout.BASELINE, password);
        layout.putConstraint(SpringLayout.NORTH, password, vSpring, SpringLayout.NORTH, panel);

        rowHeight = Spring.height(passwordLbl);
        rowHeight = Spring.max(rowHeight, Spring.height(password));

        vSpring = Spring.sum(vSpring, rowHeight);
        vSpring = Spring.sum(vSpring, Spring.constant(5));

        layout.putConstraint(SpringLayout.BASELINE, nameLbl, 0, SpringLayout.BASELINE, name);
        layout.putConstraint(SpringLayout.NORTH, name, vSpring, SpringLayout.NORTH, panel);

        rowHeight = Spring.height(nameLbl);
        rowHeight = Spring.max(rowHeight, Spring.height(name));

        vSpring = Spring.sum(vSpring, rowHeight);
        vSpring = Spring.sum(vSpring, Spring.constant(5));

        layout.putConstraint(SpringLayout.NORTH, savePassword, vSpring, SpringLayout.NORTH, panel);

        vSpring = Spring.sum(vSpring, Spring.height(savePassword));
        vSpring = Spring.sum(vSpring, Spring.constant(10));

        layout.putConstraint(SpringLayout.NORTH, update, vSpring, SpringLayout.NORTH, panel);
        layout.putConstraint(SpringLayout.NORTH, remove, vSpring, SpringLayout.NORTH, panel);

        rowHeight = Spring.height(update);
        rowHeight = Spring.max(rowHeight, Spring.height(remove));

        vSpring = Spring.sum(vSpring, rowHeight);
        vSpring = Spring.sum(vSpring, Spring.constant(10));

        layout.putConstraint(SpringLayout.SOUTH, panel, vSpring, SpringLayout.NORTH, panel);

        pack();
        setLocationRelativeTo(getOwner());
    }
}
