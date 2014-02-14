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
import javax.swing.Spring;
import javax.swing.SpringLayout;

import net.ftb.gui.LaunchFrame;
import net.ftb.locale.I18N;

@SuppressWarnings("serial")
public class PasswordDialog extends JDialog {
    private JLabel passwordLbl;
    private JPasswordField password;
    private JButton login;

    public PasswordDialog(LaunchFrame instance, boolean modal) {
        super(instance, modal);
        setupGui();

        getRootPane().setDefaultButton(login);
        login.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent event) {
                if (!new String(password.getPassword()).isEmpty()) {
                    LaunchFrame.tempPass = new String(password.getPassword());
                    setVisible(false);
                }
            }
        });
    }

    private void setupGui () {
        setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
        setTitle(I18N.getLocaleString("PASSWORD_TITLE"));
        setResizable(false);

        Container panel = getContentPane();
        SpringLayout layout = new SpringLayout();
        panel.setLayout(layout);

        passwordLbl = new JLabel(I18N.getLocaleString("PASSWORD_PASSLABEL"));
        password = new JPasswordField(16);
        login = new JButton(I18N.getLocaleString("MAIN_SUBMIT"));

        panel.add(passwordLbl);
        panel.add(password);
        panel.add(login);

        Spring hSpring;

        hSpring = Spring.constant(10);

        layout.putConstraint(SpringLayout.WEST, passwordLbl, hSpring, SpringLayout.WEST, panel);

        hSpring = Spring.sum(hSpring, Spring.width(passwordLbl));
        hSpring = Spring.sum(hSpring, Spring.constant(10));

        layout.putConstraint(SpringLayout.WEST, password, hSpring, SpringLayout.WEST, panel);

        hSpring = Spring.sum(hSpring, Spring.width(password));
        hSpring = Spring.sum(hSpring, Spring.constant(10));

        layout.putConstraint(SpringLayout.EAST, panel, hSpring, SpringLayout.WEST, panel);

        layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, login, 0, SpringLayout.HORIZONTAL_CENTER, panel);

        Spring vSpring;
        Spring rowHeight;

        vSpring = Spring.constant(10);

        layout.putConstraint(SpringLayout.BASELINE, passwordLbl, 0, SpringLayout.BASELINE, password);
        layout.putConstraint(SpringLayout.NORTH, password, vSpring, SpringLayout.NORTH, panel);

        rowHeight = Spring.height(passwordLbl);
        rowHeight = Spring.max(rowHeight, Spring.height(password));

        vSpring = Spring.sum(vSpring, rowHeight);
        vSpring = Spring.sum(vSpring, Spring.constant(10));

        layout.putConstraint(SpringLayout.NORTH, login, vSpring, SpringLayout.NORTH, panel);

        vSpring = Spring.sum(vSpring, Spring.height(login));
        vSpring = Spring.sum(vSpring, Spring.constant(10));

        layout.putConstraint(SpringLayout.SOUTH, panel, vSpring, SpringLayout.NORTH, panel);

        pack();
        setLocationRelativeTo(getOwner());
    }
}
