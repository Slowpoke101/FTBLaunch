package net.ftb.gui.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextField;

import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.util.DownloadUtils;
import net.ftb.util.ErrorUtils;
import javax.swing.JEditorPane;

public class AddPack extends JDialog {
	public AddPack() {
		getContentPane().setLayout(null);
		setBounds(300, 300, 300, 250);

		textField = new JTextField();
		textField.setBounds(10, 137, 264, 30);
		getContentPane().add(textField);
		textField.setColumns(10);

		JButton btnAdd = new JButton("Add");
		btnAdd.setBounds(10, 178, 82, 23);
		btnAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(DownloadUtils.staticFileExists(textField.getText() + ".xml") && !textField.getText().isEmpty()) {
					System.out.println("Adding: " + textField.getText());
					ModPack.loadXml(new String[]{textField.getText() + ".xml"});
					Settings.getSettings().addPrivatePack(textField.getText());
					setVisible(false);
				} else {
					ErrorUtils.tossError("Invalid Private Pack.");
				}
			}
		});
		getContentPane().add(btnAdd);
		JButton btnCancel = new JButton("Cancel");
		btnCancel.setBounds(192, 178, 82, 23);
		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		getContentPane().add(btnCancel);
		getRootPane().setDefaultButton(btnAdd);
		JEditorPane editorPane = new JEditorPane();
		editorPane.setBounds(10, 11, 264, 115);
		editorPane.setEditable(false);
		editorPane.setContentType("text/html");
		editorPane.setText("Type in the private code that you get from the private pack owner and click add, the pack will then be in your pack list. It will automatically load on startup. If you wish to remove a private pack, type the code again, and click remove. The removal will take effect on the next launcher load.");
		getContentPane().add(editorPane);
		
		btnRemove = new JButton("Remove");
		btnRemove.setBounds(102, 178, 80, 23);
		btnRemove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ArrayList<String> codes = new ArrayList<String>(Arrays.asList(Settings.getSettings().getPrivatePacks()));
				if(codes.contains(textField.getText())) {
					Settings.getSettings().removePrivatePack(textField.getText());
				}
			}
		});
		getContentPane().add(btnRemove);
	}
	private static final long serialVersionUID = 1L;
	private JTextField textField;
	JButton btnRemove;
}
