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

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import net.ftb.gui.LaunchFrame;
import net.ftb.locale.I18N;

@SuppressWarnings("serial")
public class LoadingDialog extends JDialog {
    private JLabel loadStatusLbl;
    private JLabel splashLbl;
    private static JProgressBar progress;
    public static JDialog instance;
    
    public LoadingDialog() {
        super(LaunchFrame.getInstance(), true);
        instance = this;
        
        setupGui();
    }
    
    public static void setProgress(int new_progress) {
        if(progress != null && progress.getValue() < new_progress) {
            progress.setValue(new_progress);
            
            instance.repaint();
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
        
        progress = new JProgressBar(0, 200);
        progress.setBounds(10, 230, 280, 20);
        
        panel.add(splashLbl);
        panel.add(loadStatusLbl);
        panel.add(progress);
    }
}
