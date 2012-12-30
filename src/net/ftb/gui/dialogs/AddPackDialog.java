package net.ftb.gui.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JTextField;

import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.gui.LaunchFrame;
import net.ftb.log.Logger;
import net.ftb.util.DownloadUtils;
import net.ftb.util.ErrorUtils;

public class AddPackDialog extends JDialog {
	private JTextField textField = new JTextField();
	private JButton btnRemove = new JButton("Remove");
	private JButton btnAdd = new JButton("Add");
	private JButton btnCancel = new JButton("Cancel");
	private JEditorPane editorPane = new JEditorPane();

	public AddPackDialog() {
		super(LaunchFrame.getInstance(), true);
		getContentPane().setLayout(null);
		setBounds(300, 300, 300, 250);
		setResizable(false);

		textField.setBounds(10, 137, 264, 30);
		textField.setColumns(10);
		add(textField);

		btnAdd.setBounds(10, 178, 82, 23);
		btnAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(DownloadUtils.staticFileExists(textField.getText() + ".xml") && !textField.getText().isEmpty()) {
					Logger.logInfo("Adding: " + textField.getText());
					ModPack.loadXml(textField.getText() + ".xml");
					Settings.getSettings().addPrivatePack(textField.getText());
					Settings.getSettings().save();
					setVisible(false);
				} else {
					ErrorUtils.tossError("Invalid Private Pack.");
				}
			}
		});
		add(btnAdd);
		getRootPane().setDefaultButton(btnAdd);

		btnCancel.setBounds(192, 178, 82, 23);
		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		add(btnCancel);

		editorPane.setBounds(10, 11, 264, 115);
		editorPane.setEditable(false);
		editorPane.setHighlighter(null);
		editorPane.setContentType("text/html");
		editorPane.setText("Type in the private code that you get from the private pack owner and click add, the pack will then be in your pack list. It will automatically load on startup. If you wish to remove a private pack, type the code again, and click remove. The removal will take effect on the next launcher load.");
		add(editorPane);

		btnRemove.setBounds(102, 178, 80, 23);
		btnRemove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ArrayList<String> codes = Settings.getSettings().getPrivatePacks();
				if(codes.contains(textField.getText())) {
					Settings.getSettings().removePrivatePack(textField.getText());
					Settings.getSettings().save();
					Logger.logInfo(textField.getText() + " successfully removed.");
					textField.setText("");
				} else {
					Logger.logInfo("Incorrect pack name.");
				}
			}
		});
		add(btnRemove);
	}
}
