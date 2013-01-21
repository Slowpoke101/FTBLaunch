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

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.ftb.gui.LaunchFrame;
import net.ftb.gui.panes.MapsPane;
import net.ftb.gui.panes.ModpacksPane;
import net.ftb.gui.panes.TexturepackPane;

public class SearchDialog extends JDialog {
	public static String lastPackSearch = "", lastMapSearch = "", lastTextureSearch = "";
	public JTextField query = new JTextField();

	public SearchDialog(final ModpacksPane instance) {
		super(LaunchFrame.getInstance(), true);
		setUpGui();
		query.setText((lastPackSearch == null) ? "" : lastPackSearch);
		query.getDocument().addDocumentListener(new DocumentListener() {
			@Override public void removeUpdate(DocumentEvent arg0) {
				lastPackSearch = query.getText();
				instance.sortPacks();
			}
			@Override public void insertUpdate(DocumentEvent arg0) {
				lastPackSearch = query.getText();
				instance.sortPacks();
			}
			@Override public void changedUpdate(DocumentEvent arg0) { }
		});
		query.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				lastPackSearch = query.getText();
				instance.sortPacks();
				setVisible(false);
			}
		});
	}

	public SearchDialog(final MapsPane instance) {
		super(LaunchFrame.getInstance(), true);
		setUpGui();
		query.setText((lastMapSearch == null) ? "" : lastMapSearch);
		query.getDocument().addDocumentListener(new DocumentListener() {
			@Override public void removeUpdate(DocumentEvent arg0) {
				lastMapSearch = query.getText();
				instance.sortMaps();
			}
			@Override public void insertUpdate(DocumentEvent arg0) {
				lastMapSearch = query.getText();
				instance.sortMaps();
			}
			@Override public void changedUpdate(DocumentEvent arg0) { }
		});
		query.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				lastPackSearch = query.getText();
				instance.sortMaps();
				setVisible(false);
			}
		});
	}

	public SearchDialog(final TexturepackPane instance) {
		super(LaunchFrame.getInstance(), true);
		setUpGui();
		query.setText((lastTextureSearch == null) ? "" : lastTextureSearch);
		query.getDocument().addDocumentListener(new DocumentListener() {
			@Override public void removeUpdate(DocumentEvent arg0) {
				lastTextureSearch = query.getText();
				instance.sortTexturePacks();
			}
			@Override public void insertUpdate(DocumentEvent arg0) {
				lastTextureSearch = query.getText();
				instance.sortTexturePacks();
			}
			@Override public void changedUpdate(DocumentEvent arg0) { }
		});
		query.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				lastPackSearch = query.getText();
				instance.sortTexturePacks();
				setVisible(false);
			}
		});
	}

	private void setUpGui() {
		setTitle("Text Search Filter");
		setBounds(300, 300, 220, 90);
		setResizable(false);
		getContentPane().setLayout(null);
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
		query.setBounds(10, 10, 200, 30);
		getContentPane().add(query);
	}
}
