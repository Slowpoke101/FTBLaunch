package net.ftb.gui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import net.ftb.log.ILogListener;
import net.ftb.log.LogEntry;
import net.ftb.log.LogLevel;
import net.ftb.log.LogSource;
import net.ftb.log.LogType;
import net.ftb.log.Logger;

public class LauncherConsole extends JFrame implements ILogListener {
	private static final long serialVersionUID = 1L;
	private final JEditorPane displayArea;
	private final HTMLEditorKit kit;
	private HTMLDocument doc;
	private final JComboBox logTypeComboBox;
	private LogType logType = LogType.MINIMAL;
	private final JComboBox logSourceComboBox;
	private LogSource logSource = LogSource.ALL;

	private class OutputOverride extends PrintStream {
		final LogLevel level;

		public OutputOverride(OutputStream str, LogLevel type) {
			super(str);
			this.level = type;
		}

		@Override
		public void write(byte[] b) throws IOException {
			super.write(b);
			String text = new String(b).trim();
			if (!text.equals("") && !text.equals("\n")) {
				Logger.log("From Console: " + text, level, null);
			}
		}

		@Override
		public void write(byte[] buf, int off, int len) {
			super.write(buf, off, len);
			String text = new String(buf, off, len).trim();
			if (!text.equals("") && !text.equals("\n")) {
				Logger.log("From Console: " + text, level, null);
			}
		}

		@Override
		public void write(int b) {
			throw new UnsupportedOperationException("write(int) is not supported by OutputOverride.");
		}
	}

	public LauncherConsole() {
		setTitle("FTB Launcher Console");
		this.setSize(new Dimension(800, 400));
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
		getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();

		getContentPane().add(panel, BorderLayout.SOUTH);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

		JButton btnNewButton = new JButton("Paste my log to pastebin.com");
		btnNewButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane pane = new JOptionPane("The log will be copied to your clipboard and pastebin.com will be opened now");
				Object[] options = new String[] { "Yes do it", "Cancel" };
				pane.setOptions(options);
				JDialog dialog = pane.createDialog(new JFrame(), "Paste to pastebin.com");
				dialog.setVisible(true);
				Object obj = pane.getValue();
				int result = -1;
				for (int i = 0; i < options.length; i++) {
					if (options[i].equals(obj)) {
						result = i;
					}
				}
				if (result == 0) {
					StringSelection content = new StringSelection(Logger.getLogs());
					Toolkit.getDefaultToolkit().getSystemClipboard().setContents(content, null);
					if (Desktop.isDesktopSupported()) {
						Desktop desktop = Desktop.getDesktop();
						try {
							desktop.browse(new URI("http://www.pastebin.com/"));
						} catch (Exception exc) {
							Logger.logError("Could not open url: " + exc.getMessage());
						}
					} else {
						Logger.logWarn("Could not open url, not supported");
					}
				}
			}
		});
		panel.add(btnNewButton);

		logTypeComboBox = new JComboBox(LogType.values());
		logTypeComboBox.setSelectedItem(logType);
		logTypeComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				logType = (LogType) logTypeComboBox.getSelectedItem();
				refreshLogs();
			}
		});
		panel.add(logTypeComboBox);

		logSourceComboBox = new JComboBox(LogSource.values());
		logSourceComboBox.setSelectedItem(logSource);
		logSourceComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				logSource = (LogSource) logSourceComboBox.getSelectedItem();
				refreshLogs();
			}
		});
		panel.add(logSourceComboBox);

		JButton ircButton = new JButton("Join support webchat");
		ircButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (Desktop.isDesktopSupported()) {
					Desktop desktop = Desktop.getDesktop();
					try {
						desktop.browse(new URI("http://webchat.esper.net/?channels=FTB%2CFTBLauncher&prompt=0"));
					} catch (Exception exc) {
						Logger.logError("Could not open url: " + exc.getMessage());
					}
				} else {
					Logger.logWarn("Could not open url, not supported");
				}
			}
		});
		panel.add(ircButton);

		displayArea = new JEditorPane("text/html", "");
		displayArea.setEditable(false);
		kit = new HTMLEditorKit();
		displayArea.setEditorKit(kit);

		JScrollPane scrollPane = new JScrollPane(displayArea);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		getContentPane().add(scrollPane);

		refreshLogs();
		Logger.addListener(this);

		System.setOut(new OutputOverride(System.out, LogLevel.INFO));
		System.setErr(new OutputOverride(System.err, LogLevel.ERROR));
	}

	synchronized private void refreshLogs() {
		doc = new HTMLDocument();
		displayArea.setDocument(doc);
		List<LogEntry> entries = Logger.getLogEntries();
		StringBuilder logHTML = new StringBuilder();
		for(LogEntry entry : entries) {
			if(logSource == LogSource.ALL || entry.source == logSource) {
				logHTML.append(getMessage(entry));
			}
		}
		addHTML(logHTML.toString());
	}

	private void addHTML(String html) {
		synchronized (kit) {
			try {
				kit.insertHTML(doc, doc.getLength(), html, 0, 0, null);
			} catch (BadLocationException ignored) {
			} catch (IOException ignored) { }
			displayArea.setCaretPosition(displayArea.getDocument().getLength());
		}
	}

	private String getMessage(LogEntry entry) {
		String color = "white";
		switch(entry.level) {
		case ERROR:
			color = "#FF7070";
			break;
		case WARN:
			color = "yellow";
		}
		return "<font color=\"" + color + "\">" + (entry.toString(logType).replace("<", "&lt;").replace(">", "&gt;").trim().replace("\r\n","\n").replace("\n","<br/>")) + "</font><br/>";
	}

	@Override
	public void onLogEvent(LogEntry entry) {
		if(logSource == LogSource.ALL || entry.source == logSource) {
			addHTML(getMessage(entry));
		}
	}
}
