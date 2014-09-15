/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2014, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import net.ftb.data.Constants;
import net.ftb.download.Locations;
import net.ftb.locale.I18N;
import net.ftb.log.ILogListener;
import net.ftb.log.LogEntry;
import net.ftb.log.LogLevel;
import net.ftb.log.LogSource;
import net.ftb.log.LogType;
import net.ftb.log.Logger;
import net.ftb.tools.PastebinPoster;
import net.ftb.util.GameUtils;
import net.ftb.util.OSUtils;

@SuppressWarnings("serial")
public class LauncherConsole extends JFrame implements ILogListener {
    private final JEditorPane displayArea;
    private final HTMLEditorKit kit;
    private HTMLDocument doc;
    private final JComboBox logTypeComboBox;
    private LogType logType = LogType.MINIMAL;
    private final JComboBox logSourceComboBox;
    private LogSource logSource = LogSource.ALL;
    private LogLevel logLevel = LogLevel.INFO;
    private JButton killMCButton;

    public LauncherConsole() {
        setTitle(Constants.name + " " + I18N.getLocaleString("CONSOLE_TITLE"));
        setMinimumSize(new Dimension(800, 400));
        setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
        getContentPane().setLayout(new BorderLayout(0, 0));

        JPanel panel = new JPanel();

        getContentPane().add(panel, BorderLayout.SOUTH);
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

        JButton paste = new JButton(I18N.getLocaleString("CONSOLE_PASTE"));
        paste.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                JOptionPane pane = new JOptionPane("The log will be sent to the FTB paste site and opened in your browser");
                Object[] options = new String[] { "Yes do it", "Cancel" };
                pane.setOptions(options);
                JDialog dialog = pane.createDialog(new JFrame(), I18N.getLocaleString("CONSOLE_PASTE"));
                dialog.setVisible(true);
                Object obj = pane.getValue();
                int result = -1;
                for (int i = 0; i < options.length; i++) {
                    if (options[i].equals(obj)) {
                        result = i;
                    }
                }
                if (result == 0) {
                    PastebinPoster thread = new PastebinPoster();
                    thread.start();
                }
            }
        });
        panel.add(paste);

        JButton clipboard = new JButton(I18N.getLocaleString("CONSOLE_COPYCLIP"));
        clipboard.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                JOptionPane pane = new JOptionPane(I18N.getLocaleString("CONSOLE_CLIP_CONFIRM"));
                Object[] options = new String[] { I18N.getLocaleString("MAIN_YES"), I18N.getLocaleString("MAIN_CANCEL") };
                pane.setOptions(options);
                JDialog dialog = pane.createDialog(new JFrame(), I18N.getLocaleString("CONSOLE_COPYCLIP"));
                dialog.setVisible(true);
                Object obj = pane.getValue();
                int result = -1;
                for (int i = 0; i < options.length; i++) {
                    if (options[i].equals(obj)) {
                        result = i;
                    }
                }
                if (result == 0) {
                    StringSelection stringSelection = new StringSelection("FTB Launcher logs:\n" + Logger.getLogs()
                            + "[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "]" + " Logs copied to clipboard");
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(stringSelection, null);
                }
            }
        });
        panel.add(clipboard);

        logTypeComboBox = new JComboBox(LogType.values());
        logTypeComboBox.setSelectedItem(logType);
        logTypeComboBox.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent arg0) {
                logType = (LogType) logTypeComboBox.getSelectedItem();

                // setup loglevel. If DEBUG selected show also DEBUG messages
                switch (logType) {
                case MINIMAL:
                    logLevel=LogLevel.INFO;
                    break;
                case EXTENDED:
                    logLevel=LogLevel.INFO;
                    break;
                case DEBUG:
                    logLevel=LogLevel.DEBUG;
                    break;
                }

                refreshLogs();
            }
        });
        panel.add(logTypeComboBox);

        logSourceComboBox = new JComboBox(LogSource.values());
        logSourceComboBox.setSelectedItem(logSource);
        logSourceComboBox.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent arg0) {
                logSource = (LogSource) logSourceComboBox.getSelectedItem();
                refreshLogs();
            }
        });
        panel.add(logSourceComboBox);

        JButton ircButton = new JButton(I18N.getLocaleString("CONSOLE_SUPPORT"));
        ircButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                OSUtils.browse(Locations.SUPPORTSITE);
            }
        });
        panel.add(ircButton);

        killMCButton = new JButton(I18N.getLocaleString("KILL_MC"));
        killMCButton.setEnabled(false);
        killMCButton.setVisible(true);
        killMCButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                GameUtils.killMC();
            }
        });
        panel.add(killMCButton);

        displayArea = new JEditorPane("text/html", "");
        displayArea.setEditable(false);
        kit = new HTMLEditorKit();
        displayArea.setEditorKit(kit);

        DefaultCaret caret = (DefaultCaret) displayArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane scrollPane = new JScrollPane(displayArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        getContentPane().add(scrollPane);
        pack();

        refreshLogs();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Logger.removeListener(LaunchFrame.con);
                if (LaunchFrame.trayMenu != null) {
                    LaunchFrame.trayMenu.updateShowConsole(false);
                }
                dispose();  
            }
        });

    }

    synchronized private void refreshLogs () {
        doc = new HTMLDocument();
        displayArea.setDocument(doc);
        List<LogEntry> entries = Logger.getLogEntries();
        StringBuilder logHTML = new StringBuilder();
        for (LogEntry entry : entries) {
            // select only messages we want
            if ((logSource == LogSource.ALL || entry.source == logSource) && (logLevel == LogLevel.DEBUG || logLevel.includes(entry.level))) {
                logHTML.append(getMessage(entry));
            }
        }
        addHTML(logHTML.toString());
    }

    private void addHTML (String html) {
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

    public void scrollToBottom () {
        displayArea.setCaretPosition(displayArea.getDocument().getLength());
    }

    private String getMessage (LogEntry entry) {
        String color = "white";
        switch (entry.level) {
        case ERROR:
            color = "#FF7070";
            break;
        case WARN:
            color = "yellow";
        case INFO:
            break;
        case DEBUG:
            break;
        case UNKNOWN:
            break;
        default:
            break;
        }
        return "<font color=\"" + color + "\">" + (entry.toString(logType).replace("<", "&lt;").replace(">", "&gt;").trim().replace("\r\n", "\n").replace("\n", "<br/>")) + "</font><br/>";
    }

    public void minecraftStarted() {
        killMCButton.setEnabled(true);
    }
    
    public void minecraftStopped() {
        killMCButton.setEnabled(false);
    }
    
    @Override
    public void onLogEvent (LogEntry entry) {
        // drop unneeded messages as soon as possible
        if ((logSource == LogSource.ALL || entry.source == logSource) && (logLevel == LogLevel.DEBUG || logLevel.includes(entry.level))){
            final LogEntry entry_ = entry;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    addHTML(getMessage(entry_));
                }
            });
        }
    }
}
