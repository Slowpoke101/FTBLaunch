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
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import net.ftb.data.Settings;
import net.ftb.gui.ChooseDir;
import net.ftb.gui.LaunchFrame;
import net.ftb.locale.I18N;
import net.ftb.util.ErrorUtils;
import net.ftb.util.OSUtils;
import net.ftb.util.OSUtils.OS;

@SuppressWarnings("serial")
public class InstallDirectoryDialog extends JDialog {
    private JLabel messageLbl;
    private JLabel installPathLbl;
    private JTextField installPath;
    private JButton installPathBrowse;
    private JButton apply;

    public InstallDirectoryDialog() {
        super(LaunchFrame.getInstance(), true);

        setupGui();

        getRootPane().setDefaultButton(apply);

        installPathBrowse.addActionListener(new ChooseDir(this));

        installPath.setText(OSUtils.getDefInstallPath());

        installPath.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost (FocusEvent e) {
                Settings.getSettings().setInstallPath(installPath.getText());
                Settings.getSettings().save();
            }
        });

        apply.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                File f = new File(installPath.getText());
                // TODO: add more tests! (Unicode test)\
                if (OSUtils.getCurrentOS()==OS.WINDOWS && System.getenv("ProgramFiles")!=null && installPath.getText().contains(System.getenv("ProgramFiles"))) {
                    ErrorUtils.tossError("Installing under C:\\Program Files\\ or similar is not supported. Please, select again.");
                }
                else if (OSUtils.getCurrentOS()==OS.WINDOWS && System.getenv("USERPROFILE")!=null && installPath.getText().contains(System.getenv("USERPROFILE"))) {
                    ErrorUtils.tossError("Installing under C:\\Users\\<username> is not recommended and can cause problems. You can change installation directory from  options tab.");
                    setVisible(false);
                }
                else if (f.isDirectory() && !f.canWrite()) {
                    ErrorUtils.tossError("No write access to selected directory. Please, select again");
                }
                else {
                    setVisible(false);
                }
            }
        });
    }

    public void setInstallFolderText (String text) {
        installPath.setText(text);
        Settings.getSettings().setInstallPath(installPath.getText());
        Settings.getSettings().save();
    }

    private void setupGui () {
        setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
        setTitle("Choose Install Directory");
        setResizable(false);

        Container panel = getContentPane();
        SpringLayout layout = new SpringLayout();
        panel.setLayout(layout);

        messageLbl = new JLabel(I18N.getLocaleString("INSTALL_FIRSTUSE"));
        installPathLbl = new JLabel(I18N.getLocaleString("INSTALL_FOLDER"));
        installPath = new JTextField(10);
        installPathBrowse = new JButton("...");
        apply = new JButton(I18N.getLocaleString("MAIN_APPLY"));

        messageLbl.setHorizontalAlignment(SwingConstants.CENTER);
        messageLbl.setFont(messageLbl.getFont().deriveFont(Font.BOLD, 16.0f));

        panel.add(messageLbl);
        panel.add(installPathBrowse);
        panel.add(installPathLbl);
        panel.add(installPath);
        panel.add(apply);

        Spring hSpring;
        Spring columnWidth;

        hSpring = Spring.constant(10);

        layout.putConstraint(SpringLayout.WEST, messageLbl, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, installPathLbl, hSpring, SpringLayout.WEST, panel);

        columnWidth = Spring.width(installPathLbl);
        columnWidth = Spring.sum(columnWidth, Spring.constant(5));

        layout.putConstraint(SpringLayout.WEST, installPath, Spring.sum(hSpring, columnWidth), SpringLayout.WEST, panel);

        columnWidth = Spring.sum(columnWidth, Spring.width(installPath));

        layout.putConstraint(SpringLayout.EAST, installPath, Spring.sum(hSpring, columnWidth), SpringLayout.WEST, panel);

        columnWidth = Spring.sum(columnWidth, Spring.constant(5));

        layout.putConstraint(SpringLayout.WEST, installPathBrowse, Spring.sum(hSpring, columnWidth), SpringLayout.WEST, panel);

        columnWidth = Spring.sum(columnWidth, Spring.width(installPathBrowse));
        columnWidth = Spring.max(columnWidth, Spring.width(messageLbl));

        hSpring = Spring.sum(hSpring, columnWidth);
        hSpring = Spring.sum(hSpring, Spring.constant(10));

        layout.putConstraint(SpringLayout.EAST, panel, hSpring, SpringLayout.WEST, panel);

        layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, apply, 0, SpringLayout.HORIZONTAL_CENTER, panel);

        Spring vSpring;
        Spring rowHeight;

        vSpring = Spring.constant(10);

        layout.putConstraint(SpringLayout.NORTH, messageLbl, vSpring, SpringLayout.NORTH, panel);

        vSpring = Spring.sum(vSpring, Spring.height(messageLbl));
        vSpring = Spring.sum(vSpring, Spring.constant(10));

        layout.putConstraint(SpringLayout.BASELINE, installPathLbl, 0, SpringLayout.BASELINE, installPath);
        layout.putConstraint(SpringLayout.BASELINE, installPathBrowse, 0, SpringLayout.BASELINE, installPath);
        layout.putConstraint(SpringLayout.NORTH, installPath, vSpring, SpringLayout.NORTH, panel);

        rowHeight = Spring.height(installPathLbl);
        rowHeight = Spring.max(rowHeight, Spring.height(installPath));
        rowHeight = Spring.max(rowHeight, Spring.height(installPathBrowse));

        vSpring = Spring.sum(vSpring, rowHeight);
        vSpring = Spring.sum(vSpring, Spring.constant(10));

        layout.putConstraint(SpringLayout.NORTH, apply, vSpring, SpringLayout.NORTH, panel);

        vSpring = Spring.sum(vSpring, Spring.height(apply));
        vSpring = Spring.sum(vSpring, Spring.constant(10));

        layout.putConstraint(SpringLayout.SOUTH, panel, vSpring, SpringLayout.NORTH, panel);

        pack();
        setLocationRelativeTo(getOwner());
    }
}
