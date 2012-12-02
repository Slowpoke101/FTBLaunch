package net.ftb.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import net.ftb.gui.dialogs.EditModPackDialog;
import net.ftb.gui.panes.OptionsPane;
import net.ftb.log.Logger;
import net.ftb.util.ErrorUtils;
import net.ftb.util.FileUtils;

public class ChooseDir extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;

	private OptionsPane optionsPane;
	private EditModPackDialog editMPD;
	private String choosertitle = "Please select an install location";

	public ChooseDir(OptionsPane optionsPane) {
		super();
		this.optionsPane = optionsPane;
	}

	public ChooseDir(EditModPackDialog editMPD) {
		super();
		optionsPane = null;
		this.editMPD = editMPD;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser chooser = new JFileChooser();
		if(optionsPane != null) {
			chooser.setCurrentDirectory(new java.io.File(optionsPane.getInstallFolderText()));
			chooser.setDialogTitle(choosertitle);
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setAcceptAllFileFilterUsed(false);

			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				Logger.logInfo("getCurrentDirectory(): " + chooser.getCurrentDirectory());
				Logger.logInfo("getSelectedFile() : " + chooser.getSelectedFile());
				optionsPane.setInstallFolderText(chooser.getSelectedFile().getPath());
			} else {
				Logger.logInfo("No Selection ");
			}
		} else {
			chooser.setDialogTitle("Please select the mod to install");
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setAcceptAllFileFilterUsed(true);

			if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				File destination = new File(editMPD.folder, chooser.getSelectedFile().getName());
				if(!destination.exists()) {
					try {
						FileUtils.copyFile(chooser.getSelectedFile(), destination);
					} catch (IOException e1) {
						Logger.logError(e1.getMessage(), e1);
					}
					editMPD.updateLists();
				} else {
					Logger.logWarn("File already exists, cannot add mod!");
					ErrorUtils.tossError("File already exists, cannot add mod!");
				}
			} else {
				Logger.logInfo("No Selection.");
			}
		}
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(200, 200);
	}
}