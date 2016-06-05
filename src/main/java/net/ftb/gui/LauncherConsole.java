/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2016, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
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

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

@SuppressWarnings("serial")
public class LauncherConsole extends JFrame implements ILogListener {
    private static final Font FONT = new Font("Monospaced", 0, 12);
    // Process at most LOG_CHUNK_SIZE log records at once so that console doesn't freeze for a long time
    // when lots of logs show up simultaneously
    private static final int LOG_CHUNK_SIZE = 25000;
    private final JTextPane displayArea;
    private final JComboBox logTypeComboBox;
    private LogType logType = LogType.MINIMAL;
    private final JComboBox logSourceComboBox;
    private LogSource logSource = LogSource.ALL;
    private LogLevel logLevel = LogLevel.INFO;
    private JButton killMCButton;
    private JButton threadDumpButton;
    private Document displayAreaDoc;
    private final AtomicBoolean queuedRecordsInProgress = new AtomicBoolean();
    private final Queue<LogRecord> logRecords = new ConcurrentLinkedQueue<LogRecord>();

    private SimpleAttributeSet RED = new SimpleAttributeSet();
    private SimpleAttributeSet YELLOW = new SimpleAttributeSet();

    public LauncherConsole () {
        setTitle(Constants.name + " " + I18N.getLocaleString("CONSOLE_TITLE"));
        setMinimumSize(new Dimension(800, 400));
        setPreferredSize(new Dimension(800, 400));
        setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
        getContentPane().setLayout(new BorderLayout(0, 0));

        StyleConstants.setForeground(RED, Color.RED);
        StyleConstants.setForeground(YELLOW, Color.YELLOW);

        // setup buttons
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
                    logLevel = LogLevel.INFO;
                    break;
                case EXTENDED:
                    logLevel = LogLevel.INFO;
                    break;
                case DEBUG:
                    logLevel = LogLevel.DEBUG;
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

        threadDumpButton = new JButton(I18N.getLocaleString("TD_MC"));
        threadDumpButton.setEnabled(false);
        threadDumpButton.setVisible(true);
        threadDumpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                GameUtils.threadDumpMC();
            }
        });
        panel.add(threadDumpButton);

        // setup log area
        displayArea = new JTextPane() {
            @Override
            public boolean getScrollableTracksViewportWidth () {
                return true;
            }
        };

        displayArea.setFont(FONT);
        displayArea.setEditable(false);
        displayAreaDoc = this.displayArea.getDocument();
        displayArea.setMargin(null);

        JScrollPane scrollPane = new JScrollPane(displayArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Use third party library to implement autoscroll
        new SmartScroller(scrollPane);

        getContentPane().add(scrollPane);
        pack();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing (WindowEvent e) {
                Logger.removeListener(LaunchFrame.con);
                if (LaunchFrame.trayMenu != null) {
                    LaunchFrame.trayMenu.updateShowConsole(false);
                }
                dispose();
            }
        });

    }

    synchronized public void refreshLogs () {
        // Write messages to new blank document which is not being displayed
        displayAreaDoc = new DefaultStyledDocument();

        // Add all log entries to list and display them
        Queue<LogRecord> records = new LinkedList<LogRecord>();
        for (LogEntry entry : Logger.getLogEntries()) {
            if (shouldProcess(entry)) {
                records.add(getLogRecord(entry));
            }
        }
        displayMessages(records, -1);

        // Remove newline from start
        if (displayAreaDoc.getLength() != 0) {
            try {
                displayAreaDoc.remove(0, 1);
            } catch (BadLocationException ignored) {
                // ignore
            }
        }

        // Swap to displaying new document
        displayArea.setDocument(displayAreaDoc);
    }

    public void scrollToBottom () {
        displayArea.setCaretPosition(displayArea.getDocument().getLength());
    }

    synchronized private void displayMessage (String message, SimpleAttributeSet attributes, Document d) {
        try {
            d.insertString(d.getLength(), message, attributes);
        } catch (Exception e) {
            Logger.logLoggingError(null, e);
        }
    }

    private synchronized void displayMessages (Queue<LogRecord> logRecords, int limit) {
        StringBuilder b = new StringBuilder();

        SimpleAttributeSet lastAttributes = null;
        while (true) {
            LogRecord r = --limit == 0 ? null : logRecords.poll();

            if (r == null || r.attributes != lastAttributes) {
                if (b.length() != 0) {
                    displayMessage(b.toString(), lastAttributes, displayAreaDoc);
                    b.setLength(0);
                }

                if (r == null) {
                    if (limit == 0) {
                        runLogQueue();
                    }
                    return;
                }

                lastAttributes = r.attributes;
            }

            b.append('\n').append(r.message);
        }
    }

    private LogRecord getLogRecord (LogEntry entry) {
        SimpleAttributeSet color = null;
        switch (entry.level) {
            case ERROR:
                color = RED;
                break;
            case WARN:
                color = YELLOW;
                break;
            case INFO:
            case DEBUG:
            case UNKNOWN:
            default:
                break;
        }
        return new LogRecord(entry.toString(logType), color);
    }

    private static class LogRecord {
        public final String message;
        public final SimpleAttributeSet attributes;

        private LogRecord (String message, SimpleAttributeSet attributes) {
            this.message = message;
            this.attributes = attributes;
        }
    }

    public void minecraftStarted () {
        killMCButton.setEnabled(true);
        threadDumpButton.setEnabled(true);
    }

    public void minecraftStopped () {
        killMCButton.setEnabled(false);
        threadDumpButton.setEnabled(false);
    }

    private boolean shouldProcess(LogEntry entry) {
        return (logSource == LogSource.ALL || entry.source == logSource) && (logLevel == LogLevel.DEBUG || logLevel.includes(entry.level));
    }

    @Override
    public void onLogEvent (final LogEntry entry) {
        if (!shouldProcess(entry)) {
            return;// drop unneeded messages as soon as possible
        }

        logRecords.add(getLogRecord(entry));

        runLogQueue();
    }

    private void runLogQueue() {
        if (!queuedRecordsInProgress.compareAndSet(false, true)) {
            return;// Already queued message display with invokeLater, no need to do it again
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    if (!queuedRecordsInProgress.compareAndSet(true, false)) {
                        throw new IllegalStateException("Unexpected queuedRecords value: false");
                    }
                    displayMessages(logRecords, LOG_CHUNK_SIZE);
                } catch (Throwable t) {
                    Logger.logLoggingError(null, t);
                }
            }
        });
    }
}
