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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JTextField;
import javax.swing.Spring;
import javax.swing.SpringLayout;

import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.gui.LaunchFrame;
import net.ftb.locale.I18N;
import net.ftb.log.Logger;
import net.ftb.util.DownloadUtils;
import net.ftb.util.ErrorUtils;
import net.ftb.util.FileUtils;
import net.ftb.util.OSUtils;

@SuppressWarnings("serial")
public class PrivatePackDialog extends JDialog {
	private JEditorPane editorPane;
	private JTextField modpackName;
	private JButton remove;
	private JButton add;
	private JButton cancel;

	public PrivatePackDialog() {
		super(LaunchFrame.getInstance(), true);

		setupGui();

		getRootPane().setDefaultButton(add);

		add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(DownloadUtils.staticFileExists(modpackName.getText() + ".xml") && !modpackName.getText().isEmpty()) {
					Logger.logInfo("Adding: " + modpackName.getText());
					ModPack.loadXml(modpackName.getText() + ".xml");
					Settings.getSettings().addPrivatePack(modpackName.getText());
					Settings.getSettings().save();
					setVisible(false);
				} else {
					ErrorUtils.tossError(I18N.getLocaleString("PRIVATEPACK_ERROR"));
				}
			}
		});

		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});

		remove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ArrayList<String> codes = Settings.getSettings().getPrivatePacks();
				if(codes.contains(modpackName.getText())) {
					Settings.getSettings().removePrivatePack(modpackName.getText());
					Settings.getSettings().save();
					try {
						for(ModPack pack : ModPack.getPackArray()) {
							if(pack.getParentXml().equalsIgnoreCase(modpackName.getText() + ".xml")) {
								FileUtils.delete(new File(OSUtils.getDynamicStorageLocation(), "ModPacks/" + pack.getDir()));
							}
						}
						ModPack.removePacks(modpackName.getText() + ".xml");
						FileUtils.delete(new File(OSUtils.getDynamicStorageLocation(), "ModPacks/" + modpackName.getText() + ".xml"));
						LaunchFrame.getInstance().modPacksPane.sortPacks();
					} catch (IOException e) {
						e.printStackTrace();
					}
					Logger.logInfo(modpackName.getText() + " " + I18N.getLocaleString("PRIVATEPACK_SECCESS"));
					modpackName.setText("");
					setVisible(false);
				} else {
					Logger.logInfo(I18N.getLocaleString("PRIVATEPACK_NOTEXISTS"));
				}
			}
		});
	}

	private void setupGui() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
		setTitle(I18N.getLocaleString("PRIVATEPACK_TITLE"));
		setResizable(false);

		Container panel = getContentPane();
		SpringLayout layout = new SpringLayout();
		panel.setLayout(layout);

		editorPane = new JEditorPane();
		modpackName = new JTextField(16);
		remove = new JButton(I18N.getLocaleString("MAIN_REMOVE"));
		add = new JButton(I18N.getLocaleString("MAIN_ADD"));
		cancel = new JButton(I18N.getLocaleString("MAIN_CANCEL"));

		modpackName.setColumns(10);

		editorPane.setEditable(false);
		editorPane.setHighlighter(null);
		editorPane.setContentType("text/html");
		editorPane.setText(I18N.getLocaleString("PRIVATEPACK_TEXT"));

		panel.add(modpackName);
		panel.add(add);
		panel.add(cancel);
		panel.add(editorPane);
		panel.add(remove);

		Spring hSpring;

		hSpring = Spring.constant(10);

		layout.putConstraint(SpringLayout.WEST, editorPane,  hSpring, SpringLayout.WEST, panel);

		layout.putConstraint(SpringLayout.WEST, modpackName, hSpring, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.WEST, add,         hSpring, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.WEST, remove,      hSpring, SpringLayout.EAST, add);
		layout.putConstraint(SpringLayout.WEST, cancel,      hSpring, SpringLayout.EAST, remove);

		hSpring = Spring.sum(hSpring, Spring.width(add));
		hSpring = Spring.sum(hSpring, Spring.constant(10));
		hSpring = Spring.sum(hSpring, Spring.width(remove));
		hSpring = Spring.sum(hSpring, Spring.constant(10));
		hSpring = Spring.sum(hSpring, Spring.width(cancel));
		hSpring = Spring.max(hSpring, Spring.width(modpackName));

		layout.putConstraint(SpringLayout.EAST, modpackName, hSpring, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.EAST, editorPane,  hSpring, SpringLayout.WEST, panel);

		hSpring = Spring.sum(hSpring, Spring.constant(10));

		layout.putConstraint(SpringLayout.EAST, panel, hSpring, SpringLayout.WEST, panel);

		// force the editorPane to wrap it's text.
		pack();

		Spring vSpring;
		Spring rowHeight;

		vSpring = Spring.constant(10);

		layout.putConstraint(SpringLayout.NORTH, editorPane, vSpring, SpringLayout.NORTH, panel);

		vSpring = Spring.sum(vSpring, Spring.height(editorPane));
		vSpring = Spring.sum(vSpring, Spring.constant(5));

		layout.putConstraint(SpringLayout.NORTH, modpackName, vSpring, SpringLayout.NORTH, panel);

		vSpring = Spring.sum(vSpring, Spring.height(modpackName));
		vSpring = Spring.sum(vSpring, Spring.constant(10));

		layout.putConstraint(SpringLayout.NORTH, add,    vSpring, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.NORTH, remove, vSpring, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.NORTH, cancel, vSpring, SpringLayout.NORTH, panel);

		rowHeight = Spring.height(add);
		rowHeight = Spring.max(rowHeight, Spring.height(remove));
		rowHeight = Spring.max(rowHeight, Spring.height(cancel));

		vSpring = Spring.sum(vSpring, rowHeight);
		vSpring = Spring.sum(vSpring, Spring.constant(10));

		layout.putConstraint(SpringLayout.SOUTH, panel, vSpring, SpringLayout.NORTH, panel);

		pack();
		setLocationRelativeTo(getOwner());
	}
}
