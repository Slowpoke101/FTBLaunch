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

import net.ftb.gui.panes.OptionsPane;

public class ChooseDir extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	
	private OptionsPane optionsPane;
	private JButton go;

	private JFileChooser chooser;
	private String choosertitle;
	

	public ChooseDir(OptionsPane optionsPane) {
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
		go = new JButton("Do it");
		go.addActionListener(this);
		add(go);
	}

	public void actionPerformed(ActionEvent e) {

		chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File(optionsPane.getInstallFolderText()));
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
			
			optionsPane.setInstallFolderText(chooser.getSelectedFile().getPath());
		} else {
			System.out.println("No Selection ");
		}
	}

	public Dimension getPreferredSize() {
		return new Dimension(200, 200);
	}

	public static void main(String s[]) {
		JFrame frame = new JFrame("");
		ChooseDir panel = new ChooseDir(new OptionsPane());
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