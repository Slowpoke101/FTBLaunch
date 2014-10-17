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
import javax.swing.SwingConstants;

import net.ftb.gui.GuiConstants;
import net.ftb.gui.LaunchFrame;
import net.ftb.locale.I18N;
import net.miginfocom.swing.MigLayout;

public class YNDialog extends JDialog
{
    private JLabel messageLbl;
    private JLabel overwriteLbl;
    public JButton overwrite;
    public JButton abort;
    private String message;
    private String confirmMsg;
    public boolean ready = false;
    public boolean ret = false;

    public YNDialog(String unlocMessage, String unlocConfirmMessage, String unlocTitle)
    {
        super(LaunchFrame.getInstance(), true);
        message = I18N.getLocaleString(unlocMessage);
        confirmMsg = I18N.getLocaleString(unlocConfirmMessage);
        this.setTitle(I18N.getLocaleString(unlocTitle));
        ret = false;
        ready = false;
        setupGui();
        overwrite.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed (ActionEvent event)
            {
                ready = true;
                ret = true;
                setVisible(false);
            }
        });

        abort.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed (ActionEvent event)
            {
                ret = false;
                ready = true;
                setVisible(false);
            }
        });

    }

    private void setupGui ()
    {
        setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
        setResizable(false);

        Container panel = getContentPane();
        panel.setLayout(new MigLayout());

        messageLbl = new JLabel(message);
        overwriteLbl = new JLabel(confirmMsg);
        overwrite = new JButton(I18N.getLocaleString("MAIN_YES"));
        abort = new JButton(I18N.getLocaleString("MAIN_NO"));

        messageLbl.setHorizontalAlignment(SwingConstants.CENTER);
        overwriteLbl.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(messageLbl, GuiConstants.CENTER_SINGLE_LINE);
        panel.add(overwriteLbl, GuiConstants.CENTER_SINGLE_LINE);
        panel.add(overwrite, GuiConstants.FILL_TWO);
        panel.add(abort, GuiConstants.GROW);

        pack();
        setLocationRelativeTo(getOwner());
    }
}
