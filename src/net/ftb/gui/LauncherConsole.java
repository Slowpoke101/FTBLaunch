package net.ftb.gui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Scrollbar;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URI;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import net.ftb.log.ILogListener;
import net.ftb.log.Logger;

public class LauncherConsole extends JDialog implements ILogListener {
	private static final long serialVersionUID = 1L;
	
	final JEditorPane displayArea;
	private HTMLEditorKit kit;
	private HTMLDocument doc;
	
	private JScrollPane scrollPane;
	private final JButton switchToExtendedBtn;
	private JButton ircButton;
	
	private boolean extendedLog = false;

	private class OutputOverride extends PrintStream {
		String type;
		public OutputOverride(OutputStream str, String type) throws FileNotFoundException {
			super(str);
			this.type = type;
		}
		
		@Override
		public void write(byte[] b) throws IOException {
			super.write(b);
			String text = new String(b).trim();
			if (!text.equals("") && !text.equals("\n")) {
				Logger.log("From Console: "+text,type,null);
			}
		}
		
		@Override
		public void write(byte[] buf, int off, int len) {
			super.write(buf, off, len);
			String text = new String(buf,off,len).trim();
			if (!text.equals("") && !text.equals("\n")) {
				Logger.log("From Console: "+text,type,null);
			}
		}
		
		@Override
		public void write(int b) {
			Logger.logWarn("Someone tried to use write(int b), that is not supported!");
		}
	}
	
	public LauncherConsole() {
		setTitle("FTB Launcher Console");
		this.setSize(new Dimension(800, 300));
		setResizable(false);
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		
		getContentPane().add(panel, BorderLayout.SOUTH);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		
		JButton btnNewButton = new JButton("Paste my log to pastebin.com for support requests");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane pane = new JOptionPane("The log will be copied to your clipboard and pastebin.com will be opened now");
				Object[] options = new String[] { "Yes do it", "Cancel" };
				pane.setOptions(options);
				JDialog dialog = pane.createDialog(new JFrame(), "Paste to pastebin.com");
				dialog.setVisible(true);
				Object obj = pane.getValue(); 
				int result = -1;
				for (int k = 0; k < options.length; k++) {
					if (options[k].equals(obj)) {
						result = k;
					}
				}
				if (result == 0) {
					StringSelection content = new StringSelection(Logger.getInstance().getLogbufferExtensive().toString());
					Toolkit.getDefaultToolkit().getSystemClipboard().setContents(content, null);
					if(Desktop.isDesktopSupported()) {
						Desktop desktop = Desktop.getDesktop();
						try {
							desktop.browse(new URI("http://www.pastebin.com/"));
						} catch(Exception exc) {
							Logger.logError("could not open url: "+exc.getMessage());
						}
					} else {
						Logger.logWarn("could not open url, not supported");
					}
				}
			}
		});
		panel.add(btnNewButton);

		switchToExtendedBtn = new JButton("Show extended Log");
		switchToExtendedBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				switchToExtendedBtn.setEnabled(false);
				switchToExtendedLog();
			}
		});
		panel.add(switchToExtendedBtn);

		ircButton = new JButton("Join support webchat");
		ircButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
		    	if(Desktop.isDesktopSupported()) {
					Desktop desktop = Desktop.getDesktop();
					try {
						desktop.browse(new URI("http://webchat.esper.net/?channels=FTB%2CFTBLauncher&prompt=0"));
					} catch(Exception exc) {
						Logger.logError("could not open url: "+exc.getMessage());
					}
				} else {
					Logger.logWarn("could not open url, not supported");
				}
			}
		});
		panel.add(ircButton);
		
		displayArea = new JEditorPane("text/html","test");
		kit = new HTMLEditorKit();
		doc = new HTMLDocument();
		displayArea.setEditorKit(kit);
		displayArea.setDocument(doc);
		displayArea.setEditable(false);
		
		
		scrollPane = new JScrollPane(displayArea);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		getContentPane().add(scrollPane);

		replay();
		Logger.addListener(this);
		
		try {
			System.setOut(new OutputOverride(System.out,"INFO"));
			System.setErr(new OutputOverride(System.err,"ERROR"));
		} catch (IOException e) {
			System.err.println("Error starting the Launcher Console: "+e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void switchToExtendedLog() {
		synchronized (doc) {
			extendedLog = true;
			doc = new HTMLDocument();
			displayArea.setDocument(doc);
			StringBuffer plogs = Logger.getInstance().getLogbufferExtensive();
			BufferedReader br = new BufferedReader(new StringReader(plogs.toString()));
			try {
				String line;
				while ((line = br.readLine()) != null) {
					String color = "white";
					if (line.contains("ERROR")) {
						color = "red";
					}
					if (line.contains("WARN")) {
						color = "yellow";
					}
					addText(line, color);
				}	
			} catch (IOException e) { }
		}
	}
	
	private void replay() {
		synchronized (doc) {
			doc = new HTMLDocument();
			displayArea.setDocument(doc);
			StringBuffer plogs = Logger.getInstance().getLogbuffer();
			BufferedReader br = new BufferedReader(new StringReader(plogs.toString()));
			try {
				String line;
				while ((line = br.readLine()) != null) {
					String color = "white";
					if (line.contains("ERROR")) {
						color = "red";
					}
					if (line.contains("WARN")) {
						color = "yellow";
					}
					addText(line, color);
				}
			} catch (IOException e) { }
		}
	}
	
	private  void addText(String text, String color) {
		String msg = "<font color=\""+color+"\">"+text+"</font><br/>";
		try {
			synchronized (doc) {
				kit.insertHTML(doc, doc.getLength(), msg, 0, 0, null);
			}
		} catch (BadLocationException e) {
		} catch (IOException e) { }
		scrollDown();
	}

	private void scrollDown() {
		 SwingUtilities.invokeLater(new Runnable() {
		      public void run() {
		        JScrollBar bar = scrollPane.getVerticalScrollBar();
		        bar.setValue(bar.getMaximum());
		      }
		    });
	}
	
	
	@Override
	public void onLogEvent(String date, String source, String level, String msg) {
		synchronized (doc) {
			String color = "white";
			if (level.equals("WARN")) {
				color = "yellow";
			} else if (level.equals("ERROR")) {
				color = "red";
				this.requestFocus();
			}
			if (extendedLog) {
				addText(date+" "+source+" ["+level+"] "+msg,color);
			} else {
				addText("["+level+"] "+msg,color);
			}
		}
		
	}
}