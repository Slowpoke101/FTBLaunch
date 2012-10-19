package net.ftb.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class LauncherConsole extends JDialog {
	private static final long serialVersionUID = 1L;
	
	final JTextArea textArea;
	
	private JScrollPane scrollPane;
	
	private static Boolean logToConsole = true;
	
	private class OutputOverride extends PrintStream {
		
		public OutputOverride(OutputStream str) throws FileNotFoundException {
			super(str);
		}
		
		@Override
		public void write(byte[] b) throws IOException {
			if (logToConsole) {
				textArea.append(new String(b));
			}
			// write it to the console
			super.write(b);
		}
		
		@Override
		public void write(byte[] buf, int off, int len) {
			if (logToConsole) {
				textArea.append(new String(buf, off, len));
			}
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
		this.setSize(new Dimension(451, 300));
		setResizable(false);
		getContentPane().setLayout(new FlowLayout());
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo.png")));
		textArea = new JTextArea(10, 50);
		//textArea.setBounds(10, 11, 425, 250);
		
		//getContentPane().add(textArea);
		
		scrollPane = new JScrollPane(textArea);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		getContentPane().add(scrollPane);
		
		pack();
		
		System.setOut(new OutputOverride(System.out));
		System.setErr(new OutputOverride(System.err));
	}
	
	public static void setLogToConsole(Boolean enable) {
		logToConsole = enable;
	}
}