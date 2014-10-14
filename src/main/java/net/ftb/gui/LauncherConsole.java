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

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.*;
import javax.swing.text.*;

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
    private final JTextPane displayArea;
    private final JComboBox logTypeComboBox;
    private LogType logType = LogType.MINIMAL;
    private final JComboBox logSourceComboBox;
    private LogSource logSource = LogSource.ALL;
    private LogLevel logLevel = LogLevel.INFO;
    private JButton killMCButton;
    private final Document displayAreaDoc;
    private final Font FONT = new Font("Monospaced", 0, 12);

    private SimpleAttributeSet RED = new SimpleAttributeSet();
    private SimpleAttributeSet YELLOW = new SimpleAttributeSet();

    public LauncherConsole() {
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

        // setup log area
        displayArea = new JTextPane(){
            @Override
            public boolean getScrollableTracksViewportWidth() {
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
            public void windowClosing(WindowEvent e) {
                Logger.removeListener(LaunchFrame.con);
                if (LaunchFrame.trayMenu != null) {
                    LaunchFrame.trayMenu.updateShowConsole(false);
                }
                dispose();
            }
        });

    }

    synchronized public void refreshLogs () {
        try {
            displayAreaDoc.remove(0, displayAreaDoc.getLength());
        } catch (Exception e) {
            // ignore
        }

        List<LogEntry> entries = Logger.getLogEntries();
        for (LogEntry entry : entries) {
            if ((logSource == LogSource.ALL || entry.source == logSource) && (logLevel == LogLevel.DEBUG || logLevel.includes(entry.level))) {
                addMessage(entry, this.displayAreaDoc);
            }
        }
        try {
            displayAreaDoc.remove(0, 1);
        } catch (Exception e) {
            //ignore
        }
    }

    public void scrollToBottom () {
        displayArea.setCaretPosition(displayArea.getDocument().getLength());
    }
//        String color = "#686868";
    synchronized private void addMessage(LogEntry entry, Document d) {
        SimpleAttributeSet color = null;
        switch (entry.level) {
        case ERROR:
            color = RED;
            break;
        case WARN:
            color = YELLOW;
        case INFO:
            break;
        case DEBUG:
            break;
        case UNKNOWN:
            break;
        default:
            break;
        }
        try {
            d.insertString(d.getLength(), "\n" + entry.toString(logType), color);
        } catch (Exception e) {
            //ignore
        }
    }

    public void minecraftStarted() {
        killMCButton.setEnabled(true);
    }
    
    public void minecraftStopped() {
        killMCButton.setEnabled(false);
    }
    
    @Override
    public void onLogEvent (final LogEntry entry) {
        // drop unneeded messages as soon as possible
        if ((logSource == LogSource.ALL || entry.source == logSource) && (logLevel == LogLevel.DEBUG || logLevel.includes(entry.level))){
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    addMessage(entry, LaunchFrame.con.displayAreaDoc);
                }
            });
        }
    }
}
