package net.ftb.gui;

import javax.swing.JDialog;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class LauncherConsole extends JDialog {
	private static final long serialVersionUID = 1L;
	
	final JTextArea textArea;
	
	private class OutputOverride extends PrintStream {
		
		public OutputOverride(OutputStream str) throws FileNotFoundException {
			super(str);
		}
		
		@Override
		public void write(byte[] b) throws IOException {
			textArea.append(new String(b));
			// write it to the console
			super.write(b);
		}
		
		@Override
		public void write(byte[] buf, int off, int len) {
			textArea.append(new String(buf, off, len));
			// write it to the console
			super.write(buf, off, len);
		}
		
		@Override
		public void write(int b) {
			// write it to the console
			super.write(b);
		}
		
	}
	
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
		System.setOut(new OutputOverride(System.out));
	}
}
