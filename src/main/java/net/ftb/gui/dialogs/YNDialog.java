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
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import net.ftb.gui.LauncherFrame;
import net.ftb.locale.I18N;
import net.ftb.util.SwingUtils;

public class YNDialog extends JDialog {
    private JLabel messageLbl;
    private JLabel overwriteLbl;
    public JButton overwrite;
    public JButton abort;
    private String message;
    private String confirmMsg;
    public boolean ready = false;
    public boolean ret = false;
    public YNDialog(String unlocMessage, String unlocConfirmMessage, String unlocTitle) {
        super(LauncherFrame.getInstance(), true);
        message = I18N.getLocaleString(unlocMessage);
        confirmMsg = I18N.getLocaleString(unlocConfirmMessage);
        this.setTitle(I18N.getLocaleString(unlocTitle));
        ret = false;
        ready = false;
        setupGui();
        overwrite.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent event) {
                ready = true;
                ret = true;
                setVisible(false);
            }
        });

        abort.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent event) {
                ret = false;
                ready = true;
                setVisible(false);
            }
        });

    }

    private void setupGui () {
        setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
        setResizable(false);

        Container panel = getContentPane();
        SpringLayout layout = new SpringLayout();
        panel.setLayout(layout);

        messageLbl = new JLabel(message);
        overwriteLbl = new JLabel(confirmMsg);
        overwrite = new JButton(I18N.getLocaleString("MAIN_YES"));
        abort = new JButton(I18N.getLocaleString("MAIN_NO"));

        messageLbl.setHorizontalAlignment(SwingConstants.CENTER);
        overwriteLbl.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(messageLbl);
        panel.add(overwriteLbl);
        panel.add(overwrite);
        panel.add(abort);

        Spring hSpring;
        Spring columnWidth;

        hSpring = Spring.constant(10);

        layout.putConstraint(SpringLayout.WEST, messageLbl, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, overwriteLbl, hSpring, SpringLayout.WEST, panel);

        columnWidth = Spring.max(Spring.width(messageLbl), Spring.width(overwriteLbl));

        hSpring = Spring.sum(hSpring, columnWidth);

        layout.putConstraint(SpringLayout.EAST, messageLbl, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, overwriteLbl, hSpring, SpringLayout.WEST, panel);

        hSpring = Spring.sum(hSpring, Spring.constant(10));

        layout.putConstraint(SpringLayout.EAST, panel, hSpring, SpringLayout.WEST, panel);

        layout.putConstraint(SpringLayout.EAST, overwrite, -5, SpringLayout.HORIZONTAL_CENTER, panel);
        layout.putConstraint(SpringLayout.WEST, abort, 5, SpringLayout.HORIZONTAL_CENTER, panel);

        Spring vSpring;
        Spring rowHeight;

        vSpring = Spring.constant(10);

        layout.putConstraint(SpringLayout.NORTH, messageLbl, vSpring, SpringLayout.NORTH, panel);

        vSpring = SwingUtils.springSum(vSpring, Spring.height(messageLbl), Spring.constant(5));

        layout.putConstraint(SpringLayout.NORTH, overwriteLbl, vSpring, SpringLayout.NORTH, panel);

        vSpring = SwingUtils.springSum(vSpring, Spring.height(overwriteLbl), Spring.constant(10));

        layout.putConstraint(SpringLayout.NORTH, overwrite, vSpring, SpringLayout.NORTH, panel);
        layout.putConstraint(SpringLayout.NORTH, abort, vSpring, SpringLayout.NORTH, panel);

        rowHeight = Spring.height(overwrite);
        rowHeight = Spring.max(rowHeight, Spring.height(abort));

        vSpring = SwingUtils.springSum(vSpring, rowHeight, Spring.constant(10));

        layout.putConstraint(SpringLayout.SOUTH, panel, vSpring, SpringLayout.NORTH, panel);

        pack();
        setLocationRelativeTo(getOwner());
    }
}
