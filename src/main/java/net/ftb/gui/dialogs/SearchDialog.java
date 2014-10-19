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

import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.ftb.gui.GuiConstants;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.panes.*;
import net.miginfocom.swing.MigLayout;

@SuppressWarnings("serial")
public class SearchDialog extends JDialog {
    public static String lastPackSearch = "", lastMapSearch = "", lastTextureSearch = "";
    public JTextField query = new JTextField(20);

    public SearchDialog (final AbstractModPackPane instance) {
        super(LaunchFrame.getInstance(), true);
        setupGui();
        query.setText((lastPackSearch == null) ? "" : lastPackSearch);
        query.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate (DocumentEvent arg0) {
                lastPackSearch = query.getText();
                instance.filterPacks();
            }

            @Override
            public void insertUpdate (DocumentEvent arg0) {
                lastPackSearch = query.getText();
                instance.filterPacks();
            }

            @Override
            public void changedUpdate (DocumentEvent arg0) {
            }
        });
        query.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent event) {
                lastPackSearch = query.getText();
                instance.filterPacks();
                setVisible(false);
            }
        });
    }

    public SearchDialog (final MapUtils instance) {
        super(LaunchFrame.getInstance(), true);
        setupGui();
        query.setText((lastMapSearch == null) ? "" : lastMapSearch);
        query.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate (DocumentEvent arg0) {
                lastMapSearch = query.getText();
                instance.sortMaps();
            }

            @Override
            public void insertUpdate (DocumentEvent arg0) {
                lastMapSearch = query.getText();
                instance.sortMaps();
            }

            @Override
            public void changedUpdate (DocumentEvent arg0) {
            }
        });
        query.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent event) {
                lastPackSearch = query.getText();
                instance.sortMaps();
                setVisible(false);
            }
        });
    }

    public SearchDialog (final TexturepackPane instance) {
        super(LaunchFrame.getInstance(), true);
        setupGui();
        query.setText((lastTextureSearch == null) ? "" : lastTextureSearch);
        query.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate (DocumentEvent arg0) {
                lastTextureSearch = query.getText();
                instance.sortTexturePacks();
            }

            @Override
            public void insertUpdate (DocumentEvent arg0) {
                lastTextureSearch = query.getText();
                instance.sortTexturePacks();
            }

            @Override
            public void changedUpdate (DocumentEvent arg0) {
            }
        });
        query.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent event) {
                lastPackSearch = query.getText();
                instance.sortTexturePacks();
                setVisible(false);
            }
        });
    }

    private void setupGui () {
        setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
        setTitle("Text Search Filter");
        setResizable(true);

        Container panel = getContentPane();
        panel.setLayout(new MigLayout());

        panel.add(query, GuiConstants.FILL_SINGLE_LINE);

        pack();
        setLocationRelativeTo(getOwner());
    }
}
