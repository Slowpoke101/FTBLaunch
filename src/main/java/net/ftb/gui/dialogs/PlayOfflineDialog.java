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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextArea;

import net.ftb.data.LoginResponse;
import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.gui.GuiConstants;
import net.ftb.gui.LaunchFrame;
import net.ftb.locale.I18N;
import net.ftb.minecraft.MCInstaller;
import net.miginfocom.swing.MigLayout;

@SuppressWarnings("serial")
public class PlayOfflineDialog extends JDialog {
    private JTextArea text;
    private JButton play;
    private JButton abort;

    public PlayOfflineDialog(String cause, final String username, final String uuid, final LoginResponse resp) {
        super(LaunchFrame.getInstance(), true);
        setupGui();

        play.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                ModPack pack = ModPack.getSelectedPack();
                boolean legacy = false;
                if (!pack.getMcVersion().startsWith("14w") && Integer.parseInt(pack.getMcVersion().replaceAll("[^\\d]", "")) < 162)
                    legacy = true;
                if (pack.getDir().equalsIgnoreCase("mojang_vanilla"))
                    legacy = false;//vanilla goes direct!!
                setVisible(false);
                String unique;
                if (uuid == null || uuid.isEmpty())
                    unique = "1234567890";
                else
                    unique = uuid;
                MCInstaller.launchMinecraft(Settings.getSettings().getInstallPath(), ModPack.getSelectedPack(), new LoginResponse("1", "token", username, "offlinemods", unique, resp.getAuth()),
                        legacy);

            }
        });

        abort.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e) {
                setVisible(false);
            }
        });
    }

    public void setupGui () {
        setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
        setTitle("Could not log in");
        setResizable(true);

        Container panel = getContentPane();
        panel.setLayout(new MigLayout());

        text = new JTextArea(I18N.getLocaleString("PLAYOFFLINE_WANNA"));
        text.setEditable(false);
        text.setHighlighter(null);
        text.setBorder(BorderFactory.createEmptyBorder());
        play = new JButton(I18N.getLocaleString("MAIN_YES"));
        abort = new JButton(I18N.getLocaleString("MAIN_NO"));

        panel.add(text, GuiConstants.WRAP);
        panel.add(abort, GuiConstants.FILL_TWO);
        panel.add(play, GuiConstants.GROW);

        pack();
        setLocationRelativeTo(getOwner());
    }
}
