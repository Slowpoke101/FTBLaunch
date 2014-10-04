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

import net.ftb.gui.LaunchFrame;
import net.ftb.locale.I18N;
import net.ftb.main.Main;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class LoadingDialog extends JDialog {
    private JProgressBar progress;
    public static LoadingDialog instance;

    public LoadingDialog() {
        super(LaunchFrame.getInstance(), true);
        instance = this;
        setupGui();
    }

    public void setProgress(int new_progress) {
        if(progress != null && progress.getValue() < new_progress) {
            progress.setValue(new_progress);
        }
    }

    public void releaseModal() {
        // Release modal from the loading screen, so the main thread can continue
        this.setVisible(false);
        this.setModal(false);
        this.setVisible(true);
        this.toFront();
        this.repaint();
    }

    private void setupGui () {
        setTitle(I18N.getLocaleString("Feed the Beast Launcher"));
        setSize(300, 260);
        setLayout(new GridBagLayout());
        setLocationRelativeTo(null);
        setResizable(false);
        setUndecorated(true);
        setAlwaysOnTop(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JLabel splashLbl = new JLabel(new ImageIcon(Main.img));
        JLabel loadStatusLbl = new JLabel("Loading...");
        loadStatusLbl.setHorizontalAlignment(SwingConstants.CENTER);
        progress = new JProgressBar(0, 200);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        this.add(splashLbl, gbc);
        gbc.gridy++;
        this.add(loadStatusLbl, gbc);
        gbc.gridy++;
        this.add(progress, gbc);
    }
}
