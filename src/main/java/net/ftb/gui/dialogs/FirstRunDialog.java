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

import net.ftb.data.Settings;
import net.ftb.gui.ChooseDir;
import net.ftb.gui.LaunchFrame;
import net.ftb.locale.I18N;
import net.ftb.locale.Locale;
import net.ftb.util.CheckInstallPath;
import net.ftb.util.CheckInstallPath.Action;
import net.ftb.util.ErrorUtils;
import net.ftb.util.OSUtils;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class FirstRunDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final JPanel contentPanel = new JPanel();
    private JTextField installPath;

    private JLabel messageLbl;
    private JLabel installPathLbl;
    private JLabel languageLabel;
    private JButton installPathBrowse;
    private JButton applyButton;

    private JComboBox languageList;

    public FirstRunDialog() {
        super(LaunchFrame.getInstance(), true);

        setupGUI();

        getRootPane().setDefaultButton(applyButton);

        installPathBrowse.addActionListener(new ChooseDir(this));

        installPath.setText(OSUtils.getDefInstallPath());

        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                CheckInstallPath checkResult = new CheckInstallPath(installPath.getText());

                if (checkResult.action == Action.BLOCK) {
                    ErrorUtils.tossError(checkResult.message + "\nPlease select again", checkResult.localizedMessage + "\n" + I18N.getLocaleString("CIP_SELECTAGAIN"));
                } else if (checkResult.action == Action.WARN) {
                    ErrorUtils.tossError(checkResult.message + "\nPlease change your installation location under options tab", checkResult.localizedMessage +
                            "\n" + I18N.getLocaleString("CIP_PLEASECHANGE"));
                    setVisible(false);
                    Settings.getSettings().setInstallPath(installPath.getText());
                    Settings.getSettings().setLocale(Locale.values()[languageList.getSelectedIndex()].name());
                    Settings.getSettings().save();
                } else if (checkResult.action == Action.OK) {
                    setVisible(false);
                    Settings.getSettings().setInstallPath(installPath.getText());
                    Settings.getSettings().setLocale(Locale.values()[languageList.getSelectedIndex()].name());
                    Settings.getSettings().save();
                }
            }
        });
    }

    private void setupGUI() {
    	setTitle(I18N.getLocaleString("INSTALL_TITLE"));
        setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(750, 160);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(null);

        messageLbl = new JLabel(I18N.getLocaleString("INSTALL_FIRSTUSE"));
        messageLbl.setBounds(5, 5, 729, 20);
        messageLbl.setHorizontalAlignment(SwingConstants.CENTER);
        messageLbl.setFont(messageLbl.getFont().deriveFont(Font.BOLD, 16.0f));
        contentPanel.add(messageLbl);

        installPathLbl = new JLabel(I18N.getLocaleString("INSTALL_FOLDER"));
        installPathLbl.setBounds(5, 33, 154, 23);
        contentPanel.add(installPathLbl);

        installPathBrowse = new JButton("...");
        installPathBrowse.setBounds(679, 36, 55, 23);
        contentPanel.add(installPathBrowse);

        installPath = new JTextField();
        installPath.setBounds(169, 33, 500, 23);
        contentPanel.add(installPath);
        installPath.setColumns(10);

        languageLabel = new JLabel(I18N.getLocaleString("INSTALL_LANGUAGE"));
        languageLabel.setBounds(5, 67, 154, 14);
        contentPanel.add(languageLabel);

        applyButton = new JButton(I18N.getLocaleString("MAIN_APPLY"));
        applyButton.setBounds(319, 97, 89, 23);
        contentPanel.add(applyButton);

        String[] locales = new String[Locale.values().length];
        for(int i = 0; i < Locale.values().length; i++){
            locales[i] = I18N.lookup.get(Locale.values()[i]);
        }
        languageList = new JComboBox<String>(locales);
        languageList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e) {
                I18N.setLocale(Locale.values()[languageList.getSelectedIndex()].name());
                if (LaunchFrame.getInstance() != null) {
                    LaunchFrame.getInstance().updateLocale();
                }
                updateLocale();
            }
        });
        languageList.setSelectedItem(Locale.get(Settings.getSettings().getLocale()));
        languageList.setBounds(169, 63, 500, 23);
        contentPanel.add(languageList);
    }

    public void setInstallFolderText(String text) {
        installPath.setText(text);
    }

    private void updateLocale() {
        setTitle(I18N.getLocaleString("INSTALL_TITLE"));
        messageLbl.setText(I18N.getLocaleString("INSTALL_FIRSTUSE"));
        installPathLbl.setText(I18N.getLocaleString("INSTALL_FOLDER"));
        applyButton.setText(I18N.getLocaleString("MAIN_APPLY"));
        languageLabel.setText(I18N.getLocaleString("INSTALL_LANGUAGE"));
        repaint();
    }

}