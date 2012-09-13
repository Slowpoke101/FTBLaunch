package net.ftb.gui;

import javax.swing.JDialog;
import javax.swing.JTextArea;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class LauncherConsole extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private JTextArea textArea;
	
	public LauncherConsole() throws IOException {
		setTitle("FTB Launcher Console");
		setVisible(true);
		this.setSize(new Dimension(451, 300));
		setResizable(false);
		getContentPane().setLayout(null);
		setIconImage(Toolkit.getDefaultToolkit().getImage("res//logo.png"));
		textArea = new JTextArea();
		textArea.setBounds(10, 11, 425, 250);
		getContentPane().add(textArea);
		textArea.setColumns(10);
	}
}
