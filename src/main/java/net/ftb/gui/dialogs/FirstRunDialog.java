package net.ftb.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;

import net.ftb.data.Settings;
import net.ftb.gui.ChooseDir;
import net.ftb.gui.LaunchFrame;
import net.ftb.locale.I18N;
import net.ftb.log.Logger;
import net.ftb.util.CheckInstallPath;
import net.ftb.util.ErrorUtils;
import net.ftb.util.OSUtils;
import net.ftb.util.CheckInstallPath.Action;

import javax.swing.JTextField;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Map;

import javax.swing.JComboBox;

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

    private FocusListener updateLanguage = new FocusListener() {
        @Override
        public void focusLost (FocusEvent e) {
        	I18N.setLocale(I18N.localeIndices.get(languageList.getSelectedIndex()));
        	updateLocale();
        }

        @Override
        public void focusGained (FocusEvent e) {
        }
    };

    public FirstRunDialog() {
        super(LaunchFrame.getInstance(), true);

        setTitle(I18N.getLocaleString("INSTALL_TITLE"));
        setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(750, 160);
        setLocationRelativeTo(getOwner());

        setupGUI();

        getRootPane().setDefaultButton(applyButton);

        installPathBrowse.addActionListener(new ChooseDir(this));

        installPath.setText(OSUtils.getDefInstallPath());

        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                CheckInstallPath checkResult = new CheckInstallPath(installPath.getText());

                // No need to localize here. Only shown at first run and language is not yet selected
                if (checkResult.action == Action.BLOCK) {
                    ErrorUtils.tossError(checkResult.message + "\nPlease select again");
                } else if (checkResult.action == Action.WARN) {
                    ErrorUtils.tossError(checkResult.message + "\nPlease change your installation location under options tab");
                    setVisible(false);
                    Settings.getSettings().setInstallPath(installPath.getText());
                    Settings.getSettings().setLocale(I18N.localeIndices.get(languageList.getSelectedIndex()));
                    Settings.getSettings().save();
                } else if (checkResult.action == Action.OK) {
                    setVisible(false);
                    Settings.getSettings().setInstallPath(installPath.getText());
                    Settings.getSettings().setLocale(I18N.localeIndices.get(languageList.getSelectedIndex()));
                    Settings.getSettings().save();
                }
            }
        });
    }

    private void setupGUI() {
        getContentPane().setLayout(new BorderLayout());
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

        String[] locales;
        synchronized (I18N.localeIndices) {
            locales = new String[I18N.localeIndices.size()];
            for (Map.Entry<Integer, String> entry : I18N.localeIndices.entrySet()) {
                Logger.logInfo("[i18n] Added " + entry.getKey().toString() + " " + entry.getValue() + " to options pane");
                locales[entry.getKey()] = I18N.localeFiles.get(entry.getValue());
            }
        }
        languageList = new JComboBox(locales);
        languageList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e) {
                I18N.setLocale(I18N.localeIndices.get(languageList.getSelectedIndex()));
                if (LaunchFrame.getInstance() != null) {
                    LaunchFrame.getInstance().updateLocale();
                    updateLocale();
                }
            }
        });
        languageList.addFocusListener(updateLanguage);
        languageList.setSelectedItem(I18N.localeFiles.get(Settings.getSettings().getLocale()));
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
    }

}