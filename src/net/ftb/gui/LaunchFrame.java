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

import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.feed_the_beast.launcher.json.JsonFactory;
import net.feed_the_beast.launcher.json.assets.AssetIndex;
import net.feed_the_beast.launcher.json.assets.AssetIndex.Asset;
import net.feed_the_beast.launcher.json.versions.Library;
import net.feed_the_beast.launcher.json.versions.Version;
import net.ftb.data.LauncherStyle;
import net.ftb.data.LoginResponse;
import net.ftb.data.Map;
import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.data.TexturePack;
import net.ftb.data.UserManager;
import net.ftb.gui.dialogs.InstallDirectoryDialog;
import net.ftb.gui.dialogs.LauncherUpdateDialog;
import net.ftb.gui.dialogs.ModPackVersionChangeDialog;
import net.ftb.gui.dialogs.PasswordDialog;
import net.ftb.gui.dialogs.PlayOfflineDialog;
import net.ftb.gui.dialogs.ProfileAdderDialog;
import net.ftb.gui.dialogs.ProfileEditorDialog;
import net.ftb.gui.panes.ILauncherPane;
import net.ftb.gui.panes.MapsPane;
import net.ftb.gui.panes.ModpacksPane;
import net.ftb.gui.panes.NewsPane;
import net.ftb.gui.panes.OptionsPane;
import net.ftb.gui.panes.TexturepackPane;
import net.ftb.locale.I18N;
import net.ftb.locale.I18N.Locale;
import net.ftb.log.LogEntry;
import net.ftb.log.LogLevel;
import net.ftb.log.Logger;
import net.ftb.log.StreamLogger;
import net.ftb.mclauncher.MinecraftLauncher;
import net.ftb.mclauncher.MinecraftLauncherNew;
import net.ftb.tools.MapManager;
import net.ftb.tools.MinecraftVersionDetector;
import net.ftb.tools.ModManager;
import net.ftb.tools.ProcessMonitor;
import net.ftb.tools.TextureManager;
import net.ftb.tracking.AnalyticsConfigData;
import net.ftb.tracking.JGoogleAnalyticsTracker;
import net.ftb.tracking.JGoogleAnalyticsTracker.GoogleAnalyticsVersion;
import net.ftb.updater.UpdateChecker;
import net.ftb.util.DownloadUtils;
import net.ftb.util.ErrorUtils;
import net.ftb.util.FileUtils;
import net.ftb.util.OSUtils;
import net.ftb.util.OSUtils.OS;
import net.ftb.util.StyleUtil;
import net.ftb.util.TrackerUtils;
import net.ftb.workers.AuthlibDLWorker;
import net.ftb.workers.GameUpdateWorker;
import net.ftb.workers.LoginWorker;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@SuppressWarnings("serial")
public class LaunchFrame extends JFrame {
    private LoginResponse RESPONSE;
    private NewsPane newsPane;
    public static JPanel panel;
    private JPanel footer = new JPanel();
    private JLabel footerLogo = new JLabel(new ImageIcon(this.getClass().getResource("/image/logo_ftb.png")));
    private JLabel footerCreeper = new JLabel(new ImageIcon(this.getClass().getResource("/image/logo_creeperHost.png")));
    private JLabel tpInstallLocLbl = new JLabel();
    private JButton launch = new JButton(), edit = new JButton(), donate = new JButton(), serverbutton = new JButton(), mapInstall = new JButton(), serverMap = new JButton(),
            tpInstall = new JButton();

    private static String[] dropdown_ = { "Select Profile", "Create Profile" };
    private static JComboBox users, tpInstallLocation, mapInstallLocation;
    private static LaunchFrame instance = null;
    private static String version = "1.3.5";
    public static boolean canUseAuthlib;

    public final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);

    protected static UserManager userManager;

    public static ModpacksPane modPacksPane;
    public MapsPane mapsPane;
    public TexturepackPane tpPane;
    public OptionsPane optionsPane;

    public static int buildNumber = 135;
    public static boolean noConfig = false;
    public static boolean allowVersionChange = false;
    public static boolean doVersionBackup = false;
    public static LauncherConsole con;
    public static String tempPass = "";
    public static Panes currentPane = Panes.MODPACK;
    public static JGoogleAnalyticsTracker tracker = new JGoogleAnalyticsTracker(new AnalyticsConfigData("UA-37330489-2"), GoogleAnalyticsVersion.V_4_7_2);

    public static final String FORGENAME = "MinecraftForge.zip";

    protected enum Panes {
        NEWS, OPTIONS, MODPACK, MAPS, TEXTURE
    }

    /**
     * Launch the application.
     * @param args - CLI arguments
     */
    public static void main (String[] args) {
        tracker.setEnabled(true);
        TrackerUtils.sendPageView("net/ftb/gui/LaunchFrame.java", "Launcher Start v" + version);

        if (new File(Settings.getSettings().getInstallPath(), "FTBLauncherLog.txt").exists()) {
            new File(Settings.getSettings().getInstallPath(), "FTBLauncherLog.txt").delete();
        }

        if (new File(Settings.getSettings().getInstallPath(), "MinecraftLog.txt").exists()) {
            new File(Settings.getSettings().getInstallPath(), "MinecraftLog.txt").delete();
        }

        DownloadUtils thread = new DownloadUtils();
        thread.start();

        Logger.logInfo("FTBLaunch starting up (version " + version + ")");
        Logger.logInfo("Java version: " + System.getProperty("java.version"));
        Logger.logInfo("Java vendor: " + System.getProperty("java.vendor"));
        Logger.logInfo("Java home: " + System.getProperty("java.home"));
        Logger.logInfo("Java specification: " + System.getProperty("java.vm.specification.name") + " version: " + System.getProperty("java.vm.specification.version") + " by "
                + System.getProperty("java.vm.specification.vendor"));
        Logger.logInfo("Java vm: " + System.getProperty("java.vm.name") + " version: " + System.getProperty("java.vm.version") + " by " + System.getProperty("java.vm.vendor"));
        Logger.logInfo("OS: " + System.getProperty("os.arch") + " " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        Logger.logInfo("Launcher Install Dir: " + Settings.getSettings().getInstallPath());
        Logger.logInfo("System memory: " + OSUtils.getOSFreeMemory() + "M free, " + OSUtils.getOSTotalMemory() + "M total");

        // Use IPv4 when possible, only use IPv6 when connecting to IPv6 only addresses
        System.setProperty("java.net.preferIPv4Stack", "true");

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run () {
                StyleUtil.loadUiStyles();
                try {
                    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                        if ("Nimbus".equals(info.getName())) {
                            UIManager.setLookAndFeel(info.getClassName());
                            break;
                        }
                    }
                } catch (Exception e) {
                    try {
                        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                    } catch (Exception e1) {
                    }
                }
                I18N.setupLocale();
                I18N.setLocale(Settings.getSettings().getLocale());

                if (noConfig) {
                    InstallDirectoryDialog installDialog = new InstallDirectoryDialog();
                    installDialog.setVisible(true);
                }

                File installDir = new File(Settings.getSettings().getInstallPath());
                if (!installDir.exists()) {
                    installDir.mkdirs();
                }
                File dynamicDir = new File(OSUtils.getDynamicStorageLocation());
                if (!dynamicDir.exists()) {
                    dynamicDir.mkdirs();
                }

                userManager = new UserManager(new File(OSUtils.getDynamicStorageLocation(), "logindata"));
                con = new LauncherConsole();
                if (Settings.getSettings().getConsoleActive()) {
                    con.setVisible(true);
                }

                File credits = new File(OSUtils.getDynamicStorageLocation(), "credits.txt");

                try {
                    if (!credits.exists()) {
                        FileOutputStream fos = new FileOutputStream(credits);
                        OutputStreamWriter osw = new OutputStreamWriter(fos);

                        osw.write("FTB Launcher and Modpack Credits " + System.getProperty("line.separator"));
                        osw.write("-------------------------------" + System.getProperty("line.separator"));
                        osw.write("Launcher Developers:" + System.getProperty("line.separator"));
                        osw.write("jjw123" + System.getProperty("line.separator"));
                        osw.write("unv_annihilator" + System.getProperty("line.separator"));
                        osw.write("ProgWML6" + System.getProperty("line.separator"));
                        osw.write("Major Launcher Dev Contributors" + System.getProperty("line.separator"));
                        osw.write("LexManos" + System.getProperty("line.separator"));
                        osw.write("Viper-7" + System.getProperty("line.separator") + System.getProperty("line.separator"));
                        osw.write("Vbitz" + System.getProperty("line.separator") + System.getProperty("line.separator"));
                        osw.write("Web Developers:" + System.getProperty("line.separator"));
                        osw.write("Captainnana" + System.getProperty("line.separator"));
                        osw.write("Rob" + System.getProperty("line.separator") + System.getProperty("line.separator"));
                        osw.write("Modpack Team:" + System.getProperty("line.separator"));
                        osw.write("Lathanael" + System.getProperty("line.separator"));
                        osw.write("Watchful11" + System.getProperty("line.separator"));
                        osw.write("Jadedcat" + System.getProperty("line.separator"));
                        osw.write("Eyamaz" + System.getProperty("line.separator"));

                        osw.flush();

                        TrackerUtils.sendPageView("net/ftb/gui/LaunchFrame.java", "Unique User (Credits)");
                    }

                    if (!Settings.getSettings().getLoaded() && !Settings.getSettings().getSnooper()) {
                        TrackerUtils.sendPageView("net/ftb/gui/LaunchFrame.java", "Unique User (Settings)");
                        Settings.getSettings().setLoaded(true);
                    }

                } catch (FileNotFoundException e1) {
                    Logger.logError(e1.getMessage());
                } catch (IOException e1) {
                    Logger.logError(e1.getMessage());
                }

                LaunchFrame frame = new LaunchFrame(2);
                instance = frame;
                frame.setVisible(true);

                AuthlibDLWorker authworker = new AuthlibDLWorker(Settings.getSettings().getInstallPath() + File.separator + "authlib" + File.separator, "1.4.2") {
                };

                Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException (Thread t, Throwable e) {
                        Logger.logError("Unhandled exception in " + t.toString(), e);
                    }
                });

                ModPack.addListener(frame.modPacksPane);
                ModPack.loadXml(getXmls());

                Map.addListener(frame.mapsPane);
                //				Map.loadAll();

                TexturePack.addListener(frame.tpPane);
                //				TexturePack.loadAll();

                UpdateChecker updateChecker = new UpdateChecker(buildNumber);
                if (updateChecker.shouldUpdate()) {
                    LauncherUpdateDialog p = new LauncherUpdateDialog(updateChecker);
                    p.setVisible(true);
                }

            };
        });
    }

    /**
     * Create the frame.
     */
    public LaunchFrame(final int tab) {
        setFont(new Font("a_FuturaOrto", Font.PLAIN, 12));
        setResizable(false);
        setTitle("Feed the Beast Launcher v" + version);
        setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));

        panel = new JPanel();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        if (OSUtils.getCurrentOS() == OS.WINDOWS) {
            setBounds(100, 100, 842, 480);
        } else {
            setBounds(100, 100, 850, 480);
        }
        panel.setBounds(0, 0, 850, 480);
        panel.setLayout(null);
        footer.setBounds(0, 380, 850, 100);
        footer.setLayout(null);
        footer.setBackground(LauncherStyle.getCurrentStyle().footerColor);
        tabbedPane.setBounds(0, 0, 850, 380);
        panel.add(tabbedPane);
        panel.add(footer);
        setContentPane(panel);

        //Footer
        footerLogo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        footerLogo.setBounds(20, 20, 42, 42);
        footerLogo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked (MouseEvent event) {
                OSUtils.browse("http://www.feed-the-beast.com");
            }
        });

        footerCreeper.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        footerCreeper.setBounds(72, 20, 132, 42);
        footerCreeper.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked (MouseEvent event) {
                OSUtils.browse("http://www.creeperhost.net/aff.php?aff=293");
            }
        });

        dropdown_[0] = I18N.getLocaleString("PROFILE_SELECT");
        dropdown_[1] = I18N.getLocaleString("PROFILE_CREATE");

        String[] dropdown = concatenateArrays(dropdown_, UserManager.getNames().toArray(new String[] {}));
        users = new JComboBox(dropdown);
        if (Settings.getSettings().getLastUser() != null) {
            for (int i = 0; i < dropdown.length; i++) {
                if (dropdown[i].equalsIgnoreCase(Settings.getSettings().getLastUser())) {
                    users.setSelectedIndex(i);
                }
            }
        }

        donate = new JButton(I18N.getLocaleString("DONATE_BUTTON"));
        donate.setBounds(390, 20, 80, 30);
        donate.setEnabled(false);
        donate.setToolTipText("Coming Soon...");
        donate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e) {
            }
        });

        users.setBounds(550, 20, 150, 30);
        users.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e) {
                if (users.getSelectedIndex() == 1) {
                    ProfileAdderDialog p = new ProfileAdderDialog(getInstance(), true);
                    users.setSelectedIndex(0);
                    p.setVisible(true);
                }
                edit.setEnabled(users.getSelectedIndex() > 1);
            }
        });

        edit = new JButton(I18N.getLocaleString("EDIT_BUTTON"));
        edit.setBounds(480, 20, 60, 30);
        edit.setVisible(true);
        edit.setEnabled(users.getSelectedIndex() > 1);
        edit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent event) {
                if (users.getSelectedIndex() > 1) {
                    ProfileEditorDialog p = new ProfileEditorDialog(getInstance(), (String) users.getSelectedItem(), true);
                    users.setSelectedIndex(0);
                    p.setVisible(true);
                }
                edit.setEnabled(users.getSelectedIndex() > 1);
            }
        });

        launch.setText(I18N.getLocaleString("LAUNCH_BUTTON"));
        launch.setBounds(711, 20, 100, 30);
        launch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                doLaunch();
            }
        });

        serverbutton.setBounds(480, 20, 330, 30);
        serverbutton.setText(I18N.getLocaleString("DOWNLOAD_SERVER_PACK"));
        serverbutton.setVisible(false);
        serverbutton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent event) {
                if (!ModPack.getSelectedPack().getServerUrl().isEmpty()) {
                    if (users.getSelectedIndex() > 1 && modPacksPane.packPanels.size() > 0) {
                        try {
                            String version = (Settings.getSettings().getPackVer().equalsIgnoreCase("recommended version") || Settings.getSettings().getPackVer().equalsIgnoreCase("newest version")) ? ModPack
                                    .getSelectedPack().getVersion().replace(".", "_")
                                    : Settings.getSettings().getPackVer().replace(".", "_");
                            if (ModPack.getSelectedPack().isPrivatePack()) {
                                OSUtils.browse(DownloadUtils.getCreeperhostLink("privatepacks/" + ModPack.getSelectedPack().getDir() + "/" + version + "/" + ModPack.getSelectedPack().getServerUrl()));
                            } else {
                                OSUtils.browse(DownloadUtils.getCreeperhostLink("modpacks/" + ModPack.getSelectedPack().getDir() + "/" + version + "/" + ModPack.getSelectedPack().getServerUrl()));
                            }
                            TrackerUtils.sendPageView(ModPack.getSelectedPack().getName() + " Server Download", ModPack.getSelectedPack().getName());
                        } catch (NoSuchAlgorithmException e) {
                        }
                    }
                }
            }
        });

        mapInstall.setBounds(650, 20, 160, 30);
        mapInstall.setText(I18N.getLocaleString("INSTALL_MAP"));
        mapInstall.setVisible(false);
        mapInstall.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                if (mapsPane.mapPanels.size() > 0 && getSelectedMapIndex() >= 0) {
                    MapManager man = new MapManager(new JFrame(), true);
                    man.setVisible(true);
                    MapManager.cleanUp();
                }
            }
        });

        mapInstallLocation = new JComboBox();
        mapInstallLocation.setBounds(480, 20, 160, 30);
        mapInstallLocation.setToolTipText("Install to...");
        mapInstallLocation.setVisible(false);

        serverMap.setBounds(480, 20, 330, 30);
        serverMap.setText(I18N.getLocaleString("DOWNLOAD_MAP_SERVER"));
        serverMap.setVisible(false);
        serverMap.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent event) {
                if (mapsPane.mapPanels.size() > 0 && getSelectedMapIndex() >= 0) {
                    try {
                        OSUtils.browse(DownloadUtils.getCreeperhostLink("maps%5E" + Map.getMap(LaunchFrame.getSelectedMapIndex()).getMapName() + "%5E"
                                + Map.getMap(LaunchFrame.getSelectedMapIndex()).getVersion() + "%5E" + Map.getMap(LaunchFrame.getSelectedMapIndex()).getUrl()));
                    } catch (NoSuchAlgorithmException e) {
                    }
                }
            }
        });

        tpInstall.setBounds(650, 20, 160, 30);
        tpInstall.setText(I18N.getLocaleString("INSTALL_TEXTUREPACK"));
        tpInstall.setVisible(false);
        tpInstall.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                if (tpPane.texturePackPanels.size() > 0 && getSelectedTexturePackIndex() >= 0) {
                    TextureManager man = new TextureManager(new JFrame(), true);
                    man.setVisible(true);
                }
            }
        });

        tpInstallLocation = new JComboBox();
        tpInstallLocation.setBounds(480, 20, 160, 30);
        tpInstallLocation.setToolTipText("Install to...");
        tpInstallLocation.setVisible(false);

        tpInstallLocLbl.setText("Install to...");
        tpInstallLocLbl.setBounds(480, 20, 80, 30);
        tpInstallLocLbl.setVisible(false);

        footer.add(edit);
        footer.add(users);
        footer.add(footerLogo);
        footer.add(footerCreeper);
        footer.add(launch);
        footer.add(donate);
        footer.add(serverbutton);
        footer.add(mapInstall);
        footer.add(mapInstallLocation);
        footer.add(serverMap);
        footer.add(tpInstall);
        footer.add(tpInstallLocation);

        newsPane = new NewsPane();
        modPacksPane = new ModpacksPane();
        mapsPane = new MapsPane();
        tpPane = new TexturepackPane();
        optionsPane = new OptionsPane(Settings.getSettings());

        getRootPane().setDefaultButton(launch);
        updateLocale();

        tabbedPane.add(newsPane, 0);
        tabbedPane.add(optionsPane, 1);
        tabbedPane.add(modPacksPane, 2);
        tabbedPane.add(mapsPane, 3);
        tabbedPane.add(tpPane, 4);
        setNewsIcon();
        tabbedPane.setIconAt(1, new ImageIcon(this.getClass().getResource("/image/tabs/options.png")));
        tabbedPane.setIconAt(2, new ImageIcon(this.getClass().getResource("/image/tabs/modpacks.png")));
        tabbedPane.setIconAt(3, new ImageIcon(this.getClass().getResource("/image/tabs/maps.png")));
        tabbedPane.setIconAt(4, new ImageIcon(this.getClass().getResource("/image/tabs/texturepacks.png")));
        tabbedPane.setSelectedIndex(tab);

        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged (ChangeEvent event) {
                if (tabbedPane.getSelectedComponent() instanceof ILauncherPane) {
                    ((ILauncherPane) tabbedPane.getSelectedComponent()).onVisible();
                    currentPane = Panes.values()[tabbedPane.getSelectedIndex()];
                    updateFooter();
                }
            }
        });
    }

    public void setNewsIcon () {
        int i = getUnreadNews();
        if (i > 0 && i < 100) {
            tabbedPane.setIconAt(0, new ImageAndTextIcon(this.getClass().getResource("/image/tabs/news_unread_" + Integer.toString(i).length() + ".png"), Integer.toString(i)));
        } else {
            tabbedPane.setIconAt(0, new ImageIcon(this.getClass().getResource("/image/tabs/news.png")));
        }
    }

    /**
     * call this to login
     */
    private void doLogin (final String username, String password) {
        if (password.isEmpty()) {
            PasswordDialog p = new PasswordDialog(this, true);
            p.setVisible(true);
            if (tempPass.isEmpty()) {
                enableObjects();
                return;
            }
            password = tempPass;
        }

        Logger.logInfo("Logging in...");

        tabbedPane.setEnabledAt(0, false);
        tabbedPane.setIconAt(0, new ImageIcon(this.getClass().getResource("/image/tabs/news.png")));
        tabbedPane.setEnabledAt(1, false);
        tabbedPane.setEnabledAt(2, false);
        tabbedPane.setEnabledAt(3, false);
        tabbedPane.setEnabledAt(4, false);
        tabbedPane.getSelectedComponent().setEnabled(false);

        launch.setEnabled(false);
        users.setEnabled(false);
        edit.setEnabled(false);
        serverbutton.setEnabled(false);
        mapInstall.setEnabled(false);
        mapInstallLocation.setEnabled(false);
        serverMap.setEnabled(false);
        tpInstall.setEnabled(false);
        tpInstallLocation.setEnabled(false);

        LoginWorker loginWorker = new LoginWorker(username, password) {
            @Override
            public void done () {
                String responseStr;
                try {
                    responseStr = get();
                } catch (InterruptedException err) {
                    Logger.logError(err.getMessage(), err);
                    enableObjects();
                    return;
                } catch (ExecutionException err) {
                    if (err.getCause() instanceof IOException || err.getCause() instanceof MalformedURLException) {
                        Logger.logError(err.getMessage(), err);
                        PlayOfflineDialog d = new PlayOfflineDialog("mcDown", username);
                        d.setVisible(true);
                    }
                    enableObjects();
                    return;
                }

                try {
                    RESPONSE = new LoginResponse(responseStr);
                } catch (IllegalArgumentException e) {
                    if (responseStr.contains(":")) {
                        Logger.logError("Received invalid response from server.");
                    } else {
                        if (responseStr.equalsIgnoreCase("bad login")) {
                            ErrorUtils.tossError("Invalid username or password.");
                        } else if (responseStr.equalsIgnoreCase("old version")) {
                            ErrorUtils.tossError("Outdated launcher.");
                        } else {
                            ErrorUtils.tossError("Login failed: " + responseStr);
                            PlayOfflineDialog d = new PlayOfflineDialog("mcDown", username);
                            d.setVisible(true);
                        }
                    }
                    enableObjects();
                    return;
                }
                Logger.logInfo("Login complete.");
                runGameUpdater(RESPONSE);
            }
        };
        loginWorker.execute();
    }

    private Boolean checkVersion (File verFile, ModPack pack) {
        String storedVersion = pack.getStoredVersion(verFile);
        String onlineVersion = (Settings.getSettings().getPackVer().equalsIgnoreCase("recommended version") || Settings.getSettings().getPackVer().equalsIgnoreCase("newest version")) ? pack
                .getVersion() : Settings.getSettings().getPackVer();

        if (storedVersion == "") {
            // Always allow updates from a version that isn't installed at all
            allowVersionChange = true;
            return true;
        } else if (Integer.parseInt(storedVersion.replace(".", "")) != Integer.parseInt(onlineVersion.replace(".", ""))) {
            ModPackVersionChangeDialog verDialog = new ModPackVersionChangeDialog(this, true, storedVersion, onlineVersion);
            verDialog.setVisible(true);
        }
        return allowVersionChange & (storedVersion != onlineVersion);
    }

    /**
     * checks whether an update is needed, and then starts the update process off
     * @param response - the response from the minecraft servers
     */
    private void runGameUpdater (final LoginResponse response) {
        final String installPath = Settings.getSettings().getInstallPath();
        final ModPack pack = ModPack.getSelectedPack();
        boolean debugVerbose = Settings.getSettings().getDebugLauncher();
        final String debugTag = "DEBUG: runGameUpdater: ";

        if (debugVerbose) {
            Logger.logInfo(debugTag + "ForceUpdate: " + Settings.getSettings().getForceUpdate());
            Logger.logInfo(debugTag + "installPath: " + installPath);
            Logger.logInfo(debugTag + "pack dir: " + pack.getDir());
            Logger.logInfo(debugTag + "pack check path: " + pack.getDir() + File.separator + "version");
        }

        File verFile = new File(installPath, pack.getDir() + File.separator + "version");

        if (Settings.getSettings().getForceUpdate() && verFile.exists()) {
            verFile.delete();
            if (debugVerbose) {
                Logger.logInfo(debugTag + "Pack found and delete attempted");
            }
        }

        if (Settings.getSettings().getForceUpdate() || !verFile.exists() || checkVersion(verFile, pack)) {
            if (doVersionBackup) {
                try {
                    File destination = new File(OSUtils.getDynamicStorageLocation(), "backups" + File.separator + pack.getDir() + File.separator + "config_backup");
                    if (destination.exists()) {
                        FileUtils.delete(destination);
                    }
                    FileUtils.copyFolder(new File(Settings.getSettings().getInstallPath(), pack.getDir() + File.separator + "minecraft" + File.separator + "config"), destination);
                } catch (IOException e) {
                    Logger.logError(e.getMessage(), e);
                }
            }

            if (!initializeMods()) {
                if (debugVerbose) {
                    Logger.logInfo(debugTag + "initializeMods: Failed to Init mods! Aborting to menu.");
                }
                enableObjects();
                return;
            }
        }

        try {
            TextureManager.updateTextures();
        } catch (Exception e1) {
        }

        if (pack.getMcVersion().startsWith("1.6") || pack.getMcVersion().startsWith("1.7")) {
            setupNewStyle(installPath, pack);
            return;
        }

        MinecraftVersionDetector mvd = new MinecraftVersionDetector();

        // I know it's wordy, but it's correct; why is this not using File.separator ? http://stackoverflow.com/questions/2417485/file-separator-vs-slash-in-paths

        if (!new File(installPath, pack.getDir() + "/minecraft/bin/minecraft.jar").exists() || mvd.shouldUpdate(installPath + "/" + pack.getDir() + "/minecraft")) {
            final ProgressMonitor progMonitor = new ProgressMonitor(this, "Downloading minecraft...", "", 0, 100);
            final GameUpdateWorker updater = new GameUpdateWorker(pack.getMcVersion(), new File(installPath, pack.getDir() + "/minecraft/bin").getPath()) {
                @Override
                public void done () {
                    progMonitor.close();
                    try {
                        if (get()) {
                            Logger.logInfo("Game update complete");
                            FileUtils.killMetaInf();
                            launchMinecraft(installPath + "/" + pack.getDir() + "/minecraft", RESPONSE.getUsername(), RESPONSE.getSessionID(), pack.getMaxPermSize());
                        } else {
                            ErrorUtils.tossError("Error occurred during downloading the game");
                        }
                    } catch (CancellationException e) {
                        ErrorUtils.tossError("Game update canceled.");
                    } catch (InterruptedException e) {
                        ErrorUtils.tossError("Game update interrupted.");
                    } catch (ExecutionException e) {
                        ErrorUtils.tossError("Failed to download game.");
                    } finally {
                        enableObjects();
                    }
                }
            };

            updater.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange (PropertyChangeEvent evt) {
                    if (progMonitor.isCanceled()) {
                        updater.cancel(false);
                    }
                    if (!updater.isDone()) {
                        int prog = updater.getProgress();
                        if (prog < 0) {
                            prog = 0;
                        } else if (prog > 100) {
                            prog = 100;
                        }
                        progMonitor.setProgress(prog);
                        progMonitor.setNote(updater.getStatus());
                    }
                }
            });
            updater.execute();
        } else {
            launchMinecraft(installPath + "/" + pack.getDir() + "/minecraft", RESPONSE.getUsername(), RESPONSE.getSessionID(), pack.getMaxPermSize());
        }
    }

    private void setupNewStyle (final String installPath, final ModPack pack) {
        List<DownloadInfo> assets = gatherAssets(new File(installPath), pack.getMcVersion());

        if (assets.size() > 0) {
            Logger.logInfo("Gathering " + assets.size() + " assets, this may take a while...");

            final ProgressMonitor prog = new ProgressMonitor(this, "Downloading Files...", "", 0, 100); //Not sure why this isnt showing...
            final AssetDownloader downloader = new AssetDownloader(prog, assets) {
                @Override
                public void done () {
                    try {
                        prog.close();
                        if (get()) {
                            Logger.logInfo("Asset downloading complete");
                            launchMinecraftNew(installPath, pack, RESPONSE.getUsername(), RESPONSE.getSessionID(), pack.getMaxPermSize(), RESPONSE.getUUID());
                        } else {
                            ErrorUtils.tossError("Error occurred during downloading the assets");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        ErrorUtils.tossError("Failed to download files.");
                    } finally {
                        enableObjects();
                    }
                }
            };

            downloader.execute();
        } else {
            launchMinecraftNew(installPath, pack, RESPONSE.getUsername(), RESPONSE.getSessionID(), pack.getMaxPermSize(), RESPONSE.getUUID());
        }
    }

    private static class DownloadInfo {
        public URL url;
        public File local;
        public String name;
        public long size = 0;
        public String hash;
        public String hashType;

        public DownloadInfo() {
        }

        public DownloadInfo(URL url, File local, String name) {
            this(url, local, name, null, "md5");
        }

        public DownloadInfo(URL url, File local, String name, String hash, String hashType) {
            this.url = url;
            this.local = local;
            this.name = name;
            this.hash = hash;
            this.hashType = hashType;
        }
    }

    private static final class AssetInfo extends DownloadInfo {
        public final String etag;

        private AssetInfo(File root, Element node) throws MalformedURLException {
            url = new URL("http://resources.download.minecraft.net/" + getText(node, "Key", null));
            name = getText(node, "Key", "");
            etag = getText(node, "ETag", "").replace("\"", "");
            size = Long.parseLong(getText(node, "Size", "0"));
            local = new File(root, name);
        }

        private String getText (Element node, String name, String def) {
            NodeList lst = node.getElementsByTagName(name);
            if (lst == null)
                return def;
            return lst.item(0).getChildNodes().item(0).getNodeValue();
        }

        public String toString () {
            return etag + " " + name + " " + size;
        }
    }

    private static class AssetDownloader extends SwingWorker<Boolean, Void> {
        private List<DownloadInfo> downloads;
        private final ProgressMonitor monitor;
        private String status;
        private int progressIndex = 0;

        private AssetDownloader(final ProgressMonitor monitor, List<DownloadInfo> downloads) {
            this.downloads = downloads;
            this.monitor = monitor;

            monitor.setMaximum(downloads.size() * 100);

            addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange (PropertyChangeEvent evt) {
                    if (monitor.isCanceled())
                        AssetDownloader.this.cancel(false);
                }
            });
        }

        @Override
        protected Boolean doInBackground () throws Exception {
            boolean allDownloaded = true;

            byte[] buffer = new byte[24000];
            for (int x = 0; x < downloads.size(); x++) {
                DownloadInfo asset = downloads.get(x);
                int attempt = 0;
                final int attempts = 5;
                boolean downloadSuccess = false;
                while (!downloadSuccess && (attempt < attempts)) {
                    try {
                        if (attempt++ > 0) {
                            Logger.logInfo("Connecting.. Try " + attempt + " of " + attempts + " for: " + asset.url);
                        }
                        URLConnection con = asset.url.openConnection();
                        if (con instanceof HttpURLConnection) {
                            con.setRequestProperty("Cache-Control", "no-cache");
                            con.connect();
                        }
                        this.status = "Downloading " + asset.name + "...";
                        asset.local.getParentFile().mkdirs();
                        InputStream input = con.getInputStream();
                        FileOutputStream output = new FileOutputStream(asset.local);
                        int readLen;
                        int currentSize = 0;
                        int size = Integer.parseInt(con.getHeaderField("Content-Length"));
                        setProgress(0);
                        while ((readLen = input.read(buffer, 0, buffer.length)) != -1) {
                            output.write(buffer, 0, readLen);
                            currentSize += readLen;
                            int prog = (int) ((currentSize / size) * 100);
                            if (prog > 100)
                                prog = 100;
                            if (prog < 0)
                                prog = 0;

                            setProgress(prog);

                            prog = (progressIndex * 100) + prog;

                            monitor.setProgress(prog);
                            monitor.setNote(this.status);
                        }
                        input.close();
                        output.close();
                        String hash = DownloadUtils.fileHash(asset.local, asset.hashType).toLowerCase();
                        if (con instanceof HttpURLConnection && (currentSize == asset.size || asset.size <= 0)) {
                            if (asset.hash != null && !asset.hash.toLowerCase().equals(hash)) {
                                asset.local.delete();
                            } else {
                                downloadSuccess = true;
                            }
                        }
                        progressIndex += 1;
                    } catch (Exception e) {
                        e.printStackTrace();
                        downloadSuccess = false;
                        Logger.logWarn("Connection failed, trying again");
                    }
                }
                if (!downloadSuccess) {
                    allDownloaded = false;
                }
            }
            status = allDownloaded ? "Success" : "Downloads failed";
            return allDownloaded;
        }
    }

    private List<DownloadInfo> gatherAssets (File root, String mcVersion) {
        try {
            List<DownloadInfo> list = new ArrayList<DownloadInfo>();
            /*
            String baseUrl = "http://resources.download.minecraft.net/";
            Document doc = DocumentBuilderFactory.newInstance()
                           .newDocumentBuilder()
                           .parse(new URL(baseUrl).openConnection().getInputStream());

            File assetsDir = new File(root, "assets");
            NodeList nodes = doc.getElementsByTagName("Contents");
            for (int x = 0; x < nodes.getLength(); x++)
            {
                if (nodes.item(x).getNodeType() == Node.ELEMENT_NODE)
                {
                    AssetInfo asset = new AssetInfo(assetsDir, (Element)nodes.item(x));
                    if (!asset.name.isEmpty() && !asset.name.endsWith("/"))
                    {
                        File local = new File(assetsDir, asset.name);
                        if (!local.exists())
                        {
                            list.add(asset);
                        }
                        else if (!asset.etag.isEmpty() && !DownloadUtils.fileMD5(local).equalsIgnoreCase(asset.etag))
                        {
                            local.delete();
                            list.add(asset);
                        }
                        else if (asset.etag.isEmpty())
                        {
                            list.add(asset);
                        }
                    }
                }
            }
            */

            File local = new File(root, "versions/{MC_VER}/{MC_VER}.jar".replace("{MC_VER}", mcVersion));
            if (!local.exists()) {
                list.add(new DownloadInfo(new URL("https://s3.amazonaws.com/Minecraft.Download/versions/{MC_VER}/{MC_VER}.jar".replace("{MC_VER}", mcVersion)), local, local.getName()));
            }

            URL url = new URL("https://s3.amazonaws.com/Minecraft.Download/versions/{MC_VER}/{MC_VER}.json".replace("{MC_VER}", mcVersion));
            File json = new File(root, "versions/{MC_VER}/{MC_VER}.json".replace("{MC_VER}", mcVersion));
            DownloadUtils.downloadToFile(url, json);
            Version version = JsonFactory.loadVersion(json);
            for (Library lib : version.getLibraries()) {
                if (lib.natives == null) {
                    local = new File(root, "libraries/" + lib.getPath());
                    if (!local.exists()) {
                        list.add(new DownloadInfo(new URL(lib.getUrl() + lib.getPath()), local, lib.getPath()));
                    }
                } else {
                    local = new File(root, "libraries/" + lib.getPathNatives());
                    if (!local.exists()) {
                        list.add(new DownloadInfo(new URL(lib.getUrl() + lib.getPathNatives()), local, lib.getPathNatives()));
                    }

                }
            }

            // Move the old format to the new:
            File test = new File(root, "assets/READ_ME_I_AM_VERY_IMPORTANT.txt");
            if (test.exists()) {
                File assets = new File(root, "assets");
                Set<File> old = FileUtils.listFiles(assets);
                File objects = new File(assets, "objects");
                String[] skip = new String[] { objects.getAbsolutePath(), new File(assets, "indexes").getAbsolutePath(), new File(assets, "virtual").getAbsolutePath() };

                for (File f : old) {
                    String path = f.getAbsolutePath();
                    boolean move = true;
                    for (String prefix : skip) {
                        if (path.startsWith(prefix))
                            move = false;
                    }
                    if (move) {
                        String hash = DownloadUtils.fileSHA(f);
                        File cache = new File(objects, hash.substring(0, 2) + "/" + hash);
                        Logger.logInfo("Caching Asset: " + hash + " - " + f.getAbsolutePath().replace(assets.getAbsolutePath(), ""));
                        if (!cache.exists()) {
                            cache.getParentFile().mkdirs();
                            f.renameTo(cache);
                        }
                        f.delete();
                    }
                }

                List<File> dirs = FileUtils.listDirs(assets);
                for (File dir : dirs) {
                    if (dir.listFiles().length == 0) {
                        dir.delete();
                    }
                }
            }

            url = new URL("https://s3.amazonaws.com/Minecraft.Download/indexes/{INDEX}.json".replace("{INDEX}", version.getAssets()));
            json = new File(root, "assets/indexes/{INDEX}.json".replace("{INDEX}", version.getAssets()));
            DownloadUtils.downloadToFile(url, json);
            AssetIndex index = JsonFactory.loadAssetIndex(json);

            for (Entry<String, Asset> e : index.objects.entrySet()) {
                String name = e.getKey();
                Asset asset = e.getValue();
                String path = asset.hash.substring(0, 2) + "/" + asset.hash;
                local = new File(root, "assets/objects/" + path);

                if (local.exists() && !asset.hash.equals(DownloadUtils.fileSHA(local))) {
                    local.delete();
                }

                if (!local.exists()) {
                    list.add(new DownloadInfo(new URL("http://resources.download.minecraft.net/" + path), local, name, asset.hash, "sha1"));
                }
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * launch the game with the mods in the classpath
     * @param workingDir - install path
     * @param username - the MC username
     * @param password - the MC password
     */
    public void launchMinecraft (String workingDir, String username, String password, String maxPermSize) {
        try {
            Process minecraftProcess = MinecraftLauncher.launchMinecraft( Settings.getSettings().getJavaPath(), workingDir, username, password, FORGENAME, Settings.getSettings().getRamMax(), maxPermSize);
            StreamLogger.start(minecraftProcess.getInputStream(), new LogEntry().level(LogLevel.UNKNOWN));
            TrackerUtils.sendPageView(ModPack.getSelectedPack().getName() + " Launched", ModPack.getSelectedPack().getName());
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
            }
            try {
                minecraftProcess.exitValue();
            } catch (IllegalThreadStateException e) {
                this.setVisible(false);
                ProcessMonitor.create(minecraftProcess, new Runnable() {
                    @Override
                    public void run () {
                        if (!Settings.getSettings().getKeepLauncherOpen()) {
                            System.exit(0);
                        } else {
                            LaunchFrame launchFrame = LaunchFrame.this;
                            launchFrame.setVisible(true);
                            launchFrame.enableObjects();
                            try {
                                Settings.getSettings().load(new FileInputStream(Settings.getSettings().getConfigFile()));
                                tabbedPane.remove(1);
                                optionsPane = new OptionsPane(Settings.getSettings());
                                tabbedPane.add(optionsPane, 1);
                                tabbedPane.setIconAt(1, new ImageIcon(this.getClass().getResource("/image/tabs/options.png")));
                            } catch (Exception e1) {
                                Logger.logError("Failed to reload settings after launcher closed", e1);
                            }
                        }
                    }
                });
            }
        } catch (Exception e) {
        }
    }

    public void launchMinecraftNew (String installDir, ModPack pack, String username, String password, String maxPermSize, String UUID) {
        try {
            File packDir = new File(installDir, pack.getDir());
            File gameDir = new File(packDir, "minecraft");
            File assetDir = new File(installDir, "assets");
            File libDir = new File(installDir, "libraries");
            File natDir = new File(packDir, "natives");

            if (natDir.exists()) {
                natDir.delete();
            }
            natDir.mkdirs();

            Version base = JsonFactory.loadVersion(new File(installDir, "versions/{MC_VER}/{MC_VER}.json".replace("{MC_VER}", pack.getMcVersion())));
            byte[] buf = new byte[1024];
            for (Library lib : base.getLibraries()) {
                if (lib.natives != null) {
                    File local = new File(libDir, lib.getPathNatives());
                    ZipInputStream input = null;
                    try {
                        input = new ZipInputStream(new FileInputStream(local));
                        ZipEntry entry = input.getNextEntry();
                        while (entry != null) {
                            String name = entry.getName();
                            int n;
                            if (lib.extract == null || !lib.extract.exclude(name)) {
                                File output = new File(natDir, name);
                                output.getParentFile().mkdirs();
                                FileOutputStream out = new FileOutputStream(output);
                                while ((n = input.read(buf, 0, 1024)) > -1) {
                                    out.write(buf, 0, n);
                                }
                                out.close();
                            }
                            input.closeEntry();
                            entry = input.getNextEntry();
                        }
                    } catch (Exception e) {
                        Logger.logError(e.getMessage(), e);
                        ErrorUtils.tossError("Error extracitng natives: " + e.getMessage());
                    } finally {
                        try {
                            input.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }
            List<File> classpath = new ArrayList<File>();
            Version packjson = new Version();
            if (new File(gameDir, "pack.json").exists()) {
                packjson = JsonFactory.loadVersion(new File(gameDir, "pack.json"));
                for (Library lib : packjson.getLibraries()) {
                    classpath.add(new File(libDir, lib.getPath()));
                }
            }
            classpath.add(new File(installDir, "versions/{MC_VER}/{MC_VER}.jar".replace("{MC_VER}", pack.getMcVersion())));
            for (Library lib : base.getLibraries()) {
                classpath.add(new File(libDir, lib.getPath()));
            }

            Process minecraftProcess = MinecraftLauncherNew.launchMinecraft(Settings.getSettings().getJavaPath(), gameDir, assetDir, natDir, classpath, username, password, packjson.mainClass != null ? packjson.mainClass : base.mainClass,
                    packjson.minecraftArguments != null ? packjson.minecraftArguments : base.minecraftArguments, packjson.assets != null ? packjson.assets : base.getAssets(), Settings.getSettings()
                            .getRamMax(), maxPermSize, pack.getMcVersion(), UUID);

            StreamLogger.start(minecraftProcess.getInputStream(), new LogEntry().level(LogLevel.UNKNOWN));
            TrackerUtils.sendPageView(ModPack.getSelectedPack().getName() + " Launched", ModPack.getSelectedPack().getName());
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
            }
            try {
                minecraftProcess.exitValue();
            } catch (IllegalThreadStateException e) {
                this.setVisible(false);
                ProcessMonitor.create(minecraftProcess, new Runnable() {
                    @Override
                    public void run () {
                        if (!Settings.getSettings().getKeepLauncherOpen()) {
                            System.exit(0);
                        } else {
                            LaunchFrame launchFrame = LaunchFrame.this;
                            launchFrame.setVisible(true);
                            launchFrame.enableObjects();
                            try {
                                Settings.getSettings().load(new FileInputStream(Settings.getSettings().getConfigFile()));
                                tabbedPane.remove(1);
                                optionsPane = new OptionsPane(Settings.getSettings());
                                tabbedPane.add(optionsPane, 1);
                                tabbedPane.setIconAt(1, new ImageIcon(this.getClass().getResource("/image/tabs/options.png")));
                            } catch (Exception e1) {
                                Logger.logError("Failed to reload settings after launcher closed", e1);
                            }
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param modPackName - The pack to install (should already be downloaded)
     * @throws IOException
     */
    protected void installMods (String modPackName) throws IOException {
        String installpath = Settings.getSettings().getInstallPath();
        String temppath = OSUtils.getDynamicStorageLocation();

        ModPack pack = ModPack.getPack(modPacksPane.getSelectedModIndex());

        String packDir = pack.getDir();

        Logger.logInfo("dirs mk'd");

        File source = new File(temppath, "ModPacks/" + packDir + "/.minecraft");
        if (!source.exists()) {
            source = new File(temppath, "ModPacks/" + packDir + "/minecraft");
        }

        if (Settings.getSettings().getDebugLauncher()) {
            final String debugTag = "debug: installMods: ";
            Logger.logInfo(debugTag + "install path: " + installpath);
            Logger.logInfo(debugTag + "temp path: " + temppath);
            Logger.logInfo(debugTag + "source: " + source);
            Logger.logInfo(debugTag + "packDir: " + packDir);
        }

        FileUtils.copyFolder(source, new File(installpath, packDir + "/minecraft/"));
        FileUtils.copyFolder(new File(temppath, "ModPacks/" + packDir + "/instMods/"), new File(installpath, packDir + "/instMods/"));
        FileUtils.copyFolder(new File(temppath, "ModPacks/" + packDir + "/libraries/"), new File(installpath, "/libraries/"), false);
    }

    /**
     * "Saves" the settings from the GUI controls into the settings class.
     */
    public void saveSettings () {
        Settings.getSettings().setLastUser(String.valueOf(users.getSelectedItem()));
        instance.optionsPane.saveSettingsInto(Settings.getSettings());
    }

    /**
     * @param user - user added/edited
     */
    public static void writeUsers (String user) {
        try {
            userManager.write();
        } catch (IOException e) {
        }
        String[] usernames = concatenateArrays(dropdown_, UserManager.getNames().toArray(new String[] {}));
        users.removeAllItems();
        for (int i = 0; i < usernames.length; i++) {
            users.addItem(usernames[i]);
            if (usernames[i].equals(user)) {
                users.setSelectedIndex(i);
            }
        }
    }

    /**
     * updates the tpInstall to the available ones
     * @param locations - the available locations to install the tp to
     */
    public static void updateTpInstallLocs (String[] locations) {
        tpInstallLocation.removeAllItems();
        for (String location : locations) {
            if (!location.isEmpty()) {
                tpInstallLocation.addItem(ModPack.getPack(location.trim()).getName());
            }
        }
        tpInstallLocation.setSelectedItem(ModPack.getSelectedPack().getName());
    }

    /**
     * updates the mapInstall to the available ones
     * @param locations - the available locations to install the map to
     */
    public static void updateMapInstallLocs (String[] locations) {
        mapInstallLocation.removeAllItems();
        for (String location : locations) {
            if (!location.isEmpty()) {
                mapInstallLocation.addItem(ModPack.getPack(location.trim()).getName());
            }
        }
    }

    /**
     * @param first - First array
     * @param rest - Rest of the arrays
     * @return - Outputs concatenated arrays
     */
    public static <T> T[] concatenateArrays (T[] first, T[]... rest) {
        int totalLength = first.length;
        for (T[] array : rest) {
            totalLength += array.length;
        }
        T[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (T[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    /**
     * @return - Outputs selected modpack index
     */
    public static int getSelectedModIndex () {
        return instance.modPacksPane.getSelectedModIndex();
    }

    /**
     * @return - Outputs selected map index
     */
    public static int getSelectedMapIndex () {
        return instance.mapsPane.getSelectedMapIndex();
    }

    /**
     * @return - Outputs selected texturepack index
     */
    public static int getSelectedTexturePackIndex () {
        return instance.tpPane.getSelectedTexturePackIndex();
    }

    /**
     * @return - Outputs selected map install index
     */
    public static int getSelectedMapInstallIndex () {
        return instance.mapInstallLocation.getSelectedIndex();
    }

    /**
     * @return - Outputs selected texturepack install index
     */
    public static int getSelectedTPInstallIndex () {
        return instance.tpInstallLocation.getSelectedIndex();
    }

    /**
     * @return - Outputs LaunchFrame instance
     */
    public static LaunchFrame getInstance () {
        return instance;
    }

    /**
     * Enables all items that are disabled upon launching
     */
    private void enableObjects () {
        tabbedPane.setEnabledAt(0, true);
        setNewsIcon();
        tabbedPane.setEnabledAt(1, true);
        tabbedPane.setEnabledAt(2, true);
        tabbedPane.setEnabledAt(3, true);
        tabbedPane.setEnabledAt(4, true);
        tabbedPane.getSelectedComponent().setEnabled(true);
        updateFooter();
        mapInstall.setEnabled(true);
        mapInstallLocation.setEnabled(true);
        serverMap.setEnabled(true);
        tpInstall.setEnabled(true);
        launch.setEnabled(true);
        users.setEnabled(true);
        serverbutton.setEnabled(true);
        tpInstallLocation.setEnabled(true);
        TextureManager.updating = false;
    }

    /**
     * Download and install mods
     * @return boolean - represents whether it was successful in initializing mods
     */
    private boolean initializeMods () {
        boolean debugVerbose = Settings.getSettings().getDebugLauncher();
        final String debugTag = "debug: initializeMods: ";

        if (debugVerbose) {
            Logger.logInfo(debugTag + "pack dir...");
        }
        Logger.logInfo(ModPack.getSelectedPack().getDir());
        ModManager man = new ModManager(new JFrame(), true);
        man.setVisible(true);
        while (man == null) {
        }
        while (!man.worker.isDone()) {
        }
        if (man.erroneous) {
            return false;
        }
        try {
            installMods(ModPack.getSelectedPack().getDir());
            man.cleanUp();
        } catch (IOException e) {
            if (debugVerbose) {
                Logger.logInfo(debugTag + "Exception: " + e);
            }
        }
        return true;
    }

    /**
     * disables the buttons that are usually active on the footer
     */
    public void disableMainButtons () {
        serverbutton.setVisible(false);
        launch.setVisible(false);
        edit.setVisible(false);
        users.setVisible(false);
    }

    /**
     * disables the footer buttons active when the modpack tab is selected
     */
    public void disableMapButtons () {
        mapInstall.setVisible(false);
        mapInstallLocation.setVisible(false);
        serverMap.setVisible(false);
    }

    /**
     * disables the footer buttons active when the texture pack tab is selected
     */
    public void disableTextureButtons () {
        tpInstall.setVisible(false);
        tpInstallLocation.setVisible(false);
    }

    /**
     * update the footer to the correct buttons for active tab
     */
    public void updateFooter () {
        boolean result;
        switch (currentPane) {
        case MAPS:
            result = mapsPane.type.equals("Server");
            mapInstall.setVisible(!result);
            mapInstallLocation.setVisible(!result);
            serverMap.setVisible(result);
            disableMainButtons();
            disableTextureButtons();
            break;
        case TEXTURE:
            tpInstall.setVisible(true);
            tpInstallLocation.setVisible(true);
            disableMainButtons();
            disableMapButtons();
            break;
        default:
            launch.setVisible(true);
            edit.setEnabled(users.getSelectedIndex() > 1);
            edit.setVisible(true);
            users.setVisible(true);
            serverbutton.setVisible(false);
            disableMapButtons();
            disableTextureButtons();
            break;
        }
    }

    // TODO: Make buttons dynamically sized.
    /**
     * updates the buttons/text to language specific
     */
    public void updateLocale () {
        if (I18N.currentLocale == Locale.deDE) {
            edit.setBounds(420, 20, 120, 30);
            donate.setBounds(330, 20, 80, 30);
            mapInstall.setBounds(620, 20, 190, 30);
            mapInstallLocation.setBounds(420, 20, 190, 30);
            serverbutton.setBounds(420, 20, 390, 30);
            tpInstallLocation.setBounds(420, 20, 190, 30);
            tpInstall.setBounds(620, 20, 190, 30);
        } else {
            edit.setBounds(480, 20, 60, 30);
            donate.setBounds(390, 20, 80, 30);
            mapInstall.setBounds(650, 20, 160, 30);
            mapInstallLocation.setBounds(480, 20, 160, 30);
            serverbutton.setBounds(480, 20, 330, 30);
            tpInstallLocation.setBounds(480, 20, 160, 30);
            tpInstall.setBounds(650, 20, 160, 30);
        }
        launch.setText(I18N.getLocaleString("LAUNCH_BUTTON"));
        edit.setText(I18N.getLocaleString("EDIT_BUTTON"));
        serverbutton.setText(I18N.getLocaleString("DOWNLOAD_SERVER_PACK"));
        mapInstall.setText(I18N.getLocaleString("INSTALL_MAP"));
        serverMap.setText(I18N.getLocaleString("DOWNLOAD_MAP_SERVER"));
        tpInstall.setText(I18N.getLocaleString("INSTALL_TEXTUREPACK"));
        donate.setText(I18N.getLocaleString("DONATE_BUTTON"));
        dropdown_[0] = I18N.getLocaleString("PROFILE_SELECT");
        dropdown_[1] = I18N.getLocaleString("PROFILE_CREATE");
        writeUsers((String) users.getSelectedItem());
        optionsPane.updateLocale();
        modPacksPane.updateLocale();
        mapsPane.updateLocale();
        tpPane.updateLocale();
    }

    private static ArrayList<String> getXmls () {
        ArrayList<String> s = Settings.getSettings().getPrivatePacks();
        if (s == null) {
            s = new ArrayList<String>();
        }
        for (int i = 0; i < s.size(); i++) {
            if (s.get(i).isEmpty()) {
                s.remove(i);
                i--;
            } else {
                String temp = s.get(i);
                if (!temp.endsWith(".xml")) {
                    s.remove(i);
                    s.add(i, temp + ".xml");
                }
            }
        }
        s.add(0, "modpacks.xml");
        return s;
    }

    public int getUnreadNews () {
        int i = 0;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new URL("http://launcher.feed-the-beast.com/newsupdate.php").openStream()));
            ArrayList<Long> timeStamps = new ArrayList<Long>();
            String s = reader.readLine();
            s = s.trim();
            String[] str = s.split(",");
            for (String aStr : str) {
                if (!timeStamps.contains(Long.parseLong(aStr))) {
                    timeStamps.add(Long.parseLong(aStr));
                }
            }
            long l;
            if (Long.parseLong(Settings.getSettings().getNewsDate()) == 0) {
                l = Long.parseLong(Settings.getSettings().getNewsDate());
            } else {
                l = Long.parseLong(Settings.getSettings().getNewsDate().substring(0, 10));
            }
            for (Long timeStamp : timeStamps) {
                long time = timeStamp;
                if (time > l) {
                    i++;
                }
            }

        } catch (Exception e) {
            Logger.logError(e.getMessage(), e);
        }

        return i;
    }

    public void doLaunch () {
        if (users.getSelectedIndex() > 1 && ModPack.getSelectedPack() != null) {
            Settings.getSettings().setLastPack(ModPack.getSelectedPack().getDir());
            saveSettings();
            doLogin(UserManager.getUsername(users.getSelectedItem().toString()), UserManager.getPassword(users.getSelectedItem().toString()));
        } else if (users.getSelectedIndex() <= 1) {
            ErrorUtils.tossError("Please select a profile!");
        }
    }
}
