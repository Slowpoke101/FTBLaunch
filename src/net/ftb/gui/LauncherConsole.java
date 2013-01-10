/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2013, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
 * FTB Launcher is licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ftb.gui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
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

import net.ftb.data.Settings;
import net.ftb.log.ILogListener;
import net.ftb.log.LogEntry;
import net.ftb.log.LogLevel;
import net.ftb.log.LogSource;
import net.ftb.log.LogType;
import net.ftb.log.LogWriter;
import net.ftb.log.Logger;
import net.ftb.tools.PastebinPoster;

public class LauncherConsole extends JFrame implements ILogListener {
	private final static String launcherLogFile = "FTBLauncherLog.txt";
	private final static String minecraftLogFile = "MinecraftLog.txt";
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
			throw new UnsupportedOperationException("Write(int) is not supported by OutputOverride.");
		}
	}

	public LauncherConsole() {
		setTitle("Console");
		setMinimumSize(new Dimension(800, 400));
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
		getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();

		getContentPane().add(panel, BorderLayout.SOUTH);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

		JButton pastebin = new JButton("Paste my log to pastebin.com");
		pastebin.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane pane = new JOptionPane("The log will be copied to your clipboard and pastebin.com will be opened now");
				Object[] options = new String[] { "Yes do it", "Cancel" };
				pane.setOptions(options);
				JDialog dialog = pane.createDialog(new JFrame(), "Paste to pastebin.com");
				dialog.setVisible(true);
				Object obj = pane.getValue();
				int result = -1;
				for(int i = 0; i < options.length; i++) {
					if (options[i].equals(obj)) {
						result = i;
					}
				}
				if(result == 0) {
					PastebinPoster thread = new PastebinPoster();
					thread.start();
				}
			}
		});
		panel.add(pastebin);

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

		JButton ircButton = new JButton("Need support? Click me!");
		ircButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (Desktop.isDesktopSupported()) {
					Desktop desktop = Desktop.getDesktop();
					try {
						desktop.browse(new URI("http://support.feed-the-beast.com/?qa=hot"));
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
		try {
			Logger.addListener(new LogWriter(new File(Settings.getSettings().getInstallPath(), launcherLogFile), LogSource.LAUNCHER));
			Logger.addListener(new LogWriter(new File(Settings.getSettings().getInstallPath(), minecraftLogFile), LogSource.EXTERNAL));
		} catch (IOException e1) {
			Logger.logError(e1.getMessage(), e1);
		}
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
				Logger.logError(ignored.getMessage(), ignored);
			} catch (IOException ignored) {
				Logger.logError(ignored.getMessage(), ignored);
			}
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
