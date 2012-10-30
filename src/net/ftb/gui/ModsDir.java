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

import net.ftb.data.Settings;

public class ModsDir extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;

	JButton go;

	JFileChooser chooser;
	String choosertitle;
	
	Settings settings = new Settings();

	public ModsDir() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
		go = new JButton("Do it");
		go.addActionListener(this);
		add(go);
	}

	public void actionPerformed(ActionEvent e) {

		chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File(LaunchFrame.modsFolderTextField.getText()));	
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
			LaunchFrame.modsFolderTextField.setText(chooser.getSelectedFile().getPath());		
		} else {
			System.out.println("No Selection ");
		}
	}

	public Dimension getPreferredSize() {
		return new Dimension(200, 200);
	}

	public static void main(String s[]) {
		JFrame frame = new JFrame("");
		ModsDir panel = new ModsDir();
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