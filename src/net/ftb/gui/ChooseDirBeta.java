package net.ftb.gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

import net.ftb.data.Settings;
import net.ftb.util.OSUtils;

public class ChooseDirBeta extends JFrame implements ActionListener {
	JButton go;

	JFileChooser chooser;
	String choosertitle;
	
	Settings settings = new Settings();

	public ChooseDirBeta() {
		setIconImage(Toolkit.getDefaultToolkit().getImage("res//logo.png"));
		go = new JButton("Do it");
		go.addActionListener(this);
		add(go);
	}

	public void actionPerformed(ActionEvent e) {
		int result;

		chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File(OptionsDialogBeta.installFolderTextField.getText()));
		chooser.setDialogTitle(choosertitle);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		//
		// disable the "All files" option.
		//
		chooser.setAcceptAllFileFilterUsed(false);
		//
		
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			System.out.println("getCurrentDirectory(): "
					+ chooser.getCurrentDirectory());
			System.out.println("getSelectedFile() : "
					+ chooser.getSelectedFile());
			
			OptionsDialogBeta.installFolderTextField.setText(chooser.getSelectedFile().getPath());
		} else {
			System.out.println("No Selection ");
		}
	}

	public Dimension getPreferredSize() {
		return new Dimension(200, 200);
	}

	public static void main(String s[]) {
		JFrame frame = new JFrame("");
		ChooseDirBeta panel = new ChooseDirBeta();
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		frame.getContentPane().add(panel, "Center");
		frame.setSize(panel.getPreferredSize());
		frame.setVisible(true);
	}
}