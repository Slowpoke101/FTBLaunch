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
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.Spring;
import javax.swing.SpringLayout;

import net.ftb.data.UserManager;
import net.ftb.gui.LaunchFrame;
import net.ftb.locale.I18N;
import net.ftb.util.ErrorUtils;

@SuppressWarnings("serial")
public class ProfileAdderDialog extends JDialog {
    private JLabel usernameLbl;
    private JTextField username;
    private JLabel passwordLbl;
    private JPasswordField password;
    private JButton add;

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


        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent event) {
                if (validate(username.getText(), username.getText())) {
                    UserManager.addUser(username.getText(), password.getText(), username.getText());
                    LaunchFrame.writeUsers(username.getText());
                        setVisible(false);
                    } else {
                        ErrorUtils.tossError(I18N.getLocaleString("PROFILADDER_ERROR"));
                    }
                }
        });
    }

    public void setUnlocalizedMessage (String editingName) {
    }

    private boolean validate (String name, String user, char[] pass) {
        if (!name.isEmpty() && !user.isEmpty() && pass.length > 1) {
                return true;
        }
        return false;
    }

    private boolean validate (String name, String user) {
        if (!name.isEmpty() && !user.isEmpty()) {
                return true;
        }
        return false;
    }

    private void setupGui () {
        setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
        setTitle(I18N.getLocaleString("PROFILEADDER_TITLE"));
        setResizable(false);

        Container panel = getContentPane();
        SpringLayout layout = new SpringLayout();
        panel.setLayout(layout);

        usernameLbl = new JLabel(I18N.getLocaleString("PROFILEADDER_USERNAME"));
        username = new JTextField(16);
        passwordLbl = new JLabel(I18N.getLocaleString("PROFILEADDER_PASSWORD"));
        password = new JPasswordField(16);
        add = new JButton(I18N.getLocaleString("MAIN_ADD"));

        usernameLbl.setLabelFor(username);
        passwordLbl.setLabelFor(password);

        panel.add(usernameLbl);
        panel.add(username);
        panel.add(passwordLbl);
        panel.add(password);
        panel.add(add);

        Spring hSpring;
        Spring columnWidth;

        hSpring = Spring.constant(10);

        layout.putConstraint(SpringLayout.WEST, usernameLbl, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, passwordLbl, hSpring, SpringLayout.WEST, panel);

        columnWidth = Spring.width(usernameLbl);
        columnWidth = Spring.max(columnWidth, Spring.width(passwordLbl));

        hSpring = Spring.sum(hSpring, columnWidth);
        hSpring = Spring.sum(hSpring, Spring.constant(10));

        layout.putConstraint(SpringLayout.WEST, username, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, password, hSpring, SpringLayout.WEST, panel);

        columnWidth = Spring.width(username);
        columnWidth = Spring.max(columnWidth, Spring.width(password));

        hSpring = Spring.sum(hSpring, columnWidth);
        hSpring = Spring.sum(hSpring, Spring.constant(10));

        layout.putConstraint(SpringLayout.EAST, panel, hSpring, SpringLayout.WEST, panel);

        layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, add, 0, SpringLayout.HORIZONTAL_CENTER, panel);

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

        vSpring = Spring.sum(vSpring, rowHeight);
        vSpring = Spring.sum(vSpring, Spring.constant(5));

        layout.putConstraint(SpringLayout.NORTH, add, vSpring, SpringLayout.NORTH, panel);

        vSpring = Spring.sum(vSpring, Spring.height(add));
        vSpring = Spring.sum(vSpring, Spring.constant(10));

        layout.putConstraint(SpringLayout.SOUTH, panel, vSpring, SpringLayout.NORTH, panel);

        pack();
        setLocationRelativeTo(getOwner());
    }
}
