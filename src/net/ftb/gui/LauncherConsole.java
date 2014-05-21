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
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import net.ftb.data.Settings;
import net.ftb.gui.dialogs.YNDialog;
import net.ftb.locale.I18N;
import net.ftb.log.ILogListener;
import net.ftb.log.LogEntry;
import net.ftb.log.LogLevel;
import net.ftb.log.LogSource;
import net.ftb.log.LogType;
import net.ftb.log.Logger;
import net.ftb.tools.PastebinPoster;

@SuppressWarnings("serial")
public class LauncherConsole extends JFrame implements ILogListener {
    private final JEditorPane displayArea;
    private final HTMLEditorKit kit;
    private HTMLDocument doc;
    private final JComboBox logTypeComboBox;
    private LogType logType = LogType.MINIMAL;
    private final JComboBox logSourceComboBox;
    private LogSource logSource = LogSource.ALL;
    private YNDialog yn;
    private JButton killMCButton;

    public LauncherConsole() {
        setTitle(I18N.getLocaleString("CONSOLE_TITLE"));
        setMinimumSize(new Dimension(800, 400));
        setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
        getContentPane().setLayout(new BorderLayout(0, 0));

        JPanel panel = new JPanel();

        getContentPane().add(panel, BorderLayout.SOUTH);
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

        JButton pastebin = new JButton(I18N.getLocaleString("CONSOLE_PASTEBIN"));
        pastebin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
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
                    PastebinPoster thread = new PastebinPoster();
                    thread.start();
                }
            }
        });
        panel.add(pastebin);

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
                    StringSelection stringSelection = new StringSelection(Logger.getLogs());
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
                if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();
                    try {
                        desktop.browse(new URI("http://support.feed-the-beast.com/"));
                    } catch (Exception exc) {
                        Logger.logError("Could not open url: " + exc.getMessage());
                    }
                } else {
                    Logger.logWarn("Could not open url, not supported");
                }
            }
        });
        panel.add(ircButton);

        killMCButton = new JButton(I18N.getLocaleString("KILL_MC"));
        killMCButton.setEnabled(false);
        killMCButton.setVisible(false);
        killMCButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                //if Mc is running
                if (LaunchFrame.getInstance().MCRunning) {
                    //open confirm dialog for closing MC
                    yn = new YNDialog("KILL_MC_MESSAGE", "KILL_MC_CONFIRM", "KILL_MC_TITLE");
                    yn.setVisible(true);
                    yn.toFront();
                    if (yn.ready && yn.ret && LaunchFrame.getInstance().MCRunning && LaunchFrame.getInstance() != null && LaunchFrame.getInstance().getProcMonitor() != null) {
                        Logger.logWarn("MC Killed by the user!");
                        LaunchFrame.getInstance().getProcMonitor().stop();
                    }
                    yn.setVisible(false);

                } else {
                    Logger.logInfo("no Minecraft Process currently running to kill");
                }
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
                Logger.removeListener(LaunchFrame.getInstance().con);
            }
        });

    }

    synchronized private void refreshLogs () {
        doc = new HTMLDocument();
        displayArea.setDocument(doc);
        List<LogEntry> entries = Logger.getLogEntries();
        StringBuilder logHTML = new StringBuilder();
        for (LogEntry entry : entries) {
            if (logSource == LogSource.ALL || entry.source == logSource) {
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
        case UNKNOWN:
            break;
        default:
            break;
        }
        return "<font color=\"" + color + "\">" + (entry.toString(logType).replace("<", "&lt;").replace(">", "&gt;").trim().replace("\r\n", "\n").replace("\n", "<br/>")) + "</font><br/>";
    }

    public void minecraftStarted() {
        killMCButton.setEnabled(true);
        killMCButton.setVisible(true);
    }
    
    public void minecraftStopped() {
        killMCButton.setEnabled(false);
    }
    
    @Override
    public void onLogEvent (LogEntry entry) {
        if (logSource == LogSource.ALL || entry.source == logSource) {
            final LogEntry entry_ = entry;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    addHTML(getMessage(entry_));
                }
            });
        }
    }
}
