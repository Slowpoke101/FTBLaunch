package net.ftb.gui.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.JButton;

import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.util.DownloadUtils;
import net.ftb.util.ErrorUtils;

public class AddPack extends JDialog {
	public AddPack() {
		getContentPane().setLayout(null);
		this.setBounds(300, 300, 300, 120);
		
		textField = new JTextField();
		textField.setBounds(10, 11, 264, 30);
		getContentPane().add(textField);
		textField.setColumns(10);
		
		JButton btnAdd = new JButton("Add");
		btnAdd.setBounds(10, 52, 126, 23);
		btnAdd.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {

				ArrayList<String> codes = new ArrayList<String>(Arrays.asList(Settings.getSettings().getPrivatePacks()));
				
				if(DownloadUtils.staticFileExists(textField.getText() + ".xml") && !codes.contains(textField.getText() + ".xml")) {
					ModPack.loadXml(new String[]{textField.getText() + ".xml"});
					Settings.getSettings().addPrivatePack(textField.getText());
					setVisible(false);
				} else {
					ErrorUtils.tossError("Invalid Private Pack :( (or already added)");
				}
			}
		});
		getContentPane().add(btnAdd);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.setBounds(146, 52, 128, 23);
		btnCancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		getContentPane().add(btnCancel);
	}
	private static final long serialVersionUID = 1L;
	private JTextField textField;
}
