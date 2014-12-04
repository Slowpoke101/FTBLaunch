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
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.*;

import net.ftb.locale.I18N;
import net.ftb.log.Logger;

@SuppressWarnings("serial")
public class LoadingDialog extends JDialog {
    private JLabel loadStatusLbl;
    private JLabel splashLbl;
    private JProgressBar progressBar;
    private static LoadingDialog instance;

    private static int BAR_SIZE = 200;
    private static int COUNT = 7;
    private static int INCREMENT = BAR_SIZE / COUNT;
    private static AtomicInteger progress = new AtomicInteger(0);

    public LoadingDialog () {
        super();
        setupGui();
        instance = this;
    }

    public static void advance (final String text) {
        Logger.logInfo(text);
        final int newValue = progress.getAndAdd(INCREMENT) + INCREMENT;
        if (instance != null) {
            if (SwingUtilities.isEventDispatchThread()) {
                doAdvance(text, newValue);
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run () {
                        doAdvance(text, newValue);
                    }
                });
            }
        }
    }

    private static void doAdvance (String text, int newValue) {
        instance.progressBar.setValue(newValue);
        instance.loadStatusLbl.setText(text);
        instance.repaint();
    }

    private void setupGui () {
        setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
        setTitle(I18N.getLocaleString("Feed the Beast Launcher"));
        setSize(300, 260);
        setLayout(null);
        setLocationRelativeTo(null);
        setResizable(false);
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        Container panel = getContentPane();

        splashLbl = new JLabel(new ImageIcon(this.getClass().getResource("/image/logo_ftb_large.png")));
        splashLbl.setBounds(0, 20, 300, 160);

        loadStatusLbl = new JLabel("Loading...");
        loadStatusLbl.setHorizontalAlignment(SwingConstants.CENTER);
        loadStatusLbl.setBounds(0, 200, 300, 20);

        progressBar = new JProgressBar(0, BAR_SIZE);
        progressBar.setBounds(10, 230, 280, 20);

        panel.add(splashLbl);
        panel.add(loadStatusLbl);
        panel.add(progressBar);
    }
}
