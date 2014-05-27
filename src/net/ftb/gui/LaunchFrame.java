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
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.google.common.eventbus.EventBus;
import lombok.Getter;
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
import net.ftb.download.Locations;
import net.ftb.download.info.DownloadInfo;
import net.ftb.download.workers.AssetDownloader;
import net.ftb.gui.dialogs.InstallDirectoryDialog;
import net.ftb.gui.dialogs.LauncherUpdateDialog;
import net.ftb.gui.dialogs.LoadingDialog;
import net.ftb.gui.dialogs.ModPackVersionChangeDialog;
import net.ftb.gui.dialogs.PasswordDialog;
import net.ftb.gui.dialogs.PlayOfflineDialog;
import net.ftb.gui.dialogs.ProfileAdderDialog;
import net.ftb.gui.dialogs.ProfileEditorDialog;
import net.ftb.gui.panes.*;
import net.ftb.locale.I18N;
import net.ftb.locale.I18N.Locale;
import net.ftb.log.LogEntry;
import net.ftb.log.LogLevel;
import net.ftb.log.LogSource;
import net.ftb.log.LogWriter;
import net.ftb.log.Logger;
import net.ftb.log.OutputOverride;
import net.ftb.log.StdOutLogger;
import net.ftb.log.StreamLogger;
import net.ftb.mclauncher.MinecraftLauncherNew;
import net.ftb.tools.MapManager;
import net.ftb.tools.ModManager;
import net.ftb.tools.ProcessMonitor;
import net.ftb.tools.TextureManager;
import net.ftb.tracking.AnalyticsConfigData;
import net.ftb.tracking.JGoogleAnalyticsTracker;
import net.ftb.tracking.JGoogleAnalyticsTracker.GoogleAnalyticsVersion;
import net.ftb.updater.UpdateChecker;
import net.ftb.util.*;
import net.ftb.util.OSUtils.OS;
import net.ftb.util.winreg.JavaInfo;
import net.ftb.workers.AuthlibDLWorker;
import net.ftb.workers.LoginWorker;
import net.ftb.workers.UnreadNewsWorker;

@SuppressWarnings("serial")
public class LaunchFrame extends JFrame {
    private LoginResponse RESPONSE;
    private NewsPane newsPane;
    public static JPanel panel;
    private JPanel footer = new JPanel();
    private JLabel footerLogo = new JLabel(new ImageIcon(this.getClass().getResource("/image/logo_ftb.png")));
    private JLabel footerCreeper = new JLabel(new ImageIcon(this.getClass().getResource("/image/logo_creeperHost.png")));
    private JLabel tpInstallLocLbl = new JLabel();
    @Getter
    private final JButton launch = new JButton(), edit = new JButton(), donate = new JButton(), serverbutton = new JButton(), mapInstall = new JButton(), serverMap = new JButton(),
            tpInstall = new JButton();

    private static String[] dropdown_ = { "Select Profile", "Create Profile" };
    private static JComboBox users, tpInstallLocation, mapInstallLocation;
    /**
     * @return - Outputs LaunchFrame instance
     */
    @Getter
    private static LaunchFrame instance = null;
    @Getter
    private static String version = "1.4.0";
    public static boolean canUseAuthlib;
    public static int minUsable = -1;
    public final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);

    protected static UserManager userManager;

    public FTBPacksPane modPacksPane;
    public ThirdPartyPane thirdPartyPane;
    public MapUtils mapsPane;
    public TexturepackPane tpPane;
    public OptionsPane optionsPane;

    /*
     * limit for version component is 99.
     */
    public static int buildNumber = 1 * 100 * 100 + 4 * 100 + 0 * 1;
    public static boolean noConfig = false;
    public static boolean allowVersionChange = false;
    public static boolean doVersionBackup = false;
    public static boolean MCRunning = false;
    public static boolean i18nLoaded = false;
    public static LauncherConsole con;
    public static String tempPass = "";
    public static Panes currentPane = Panes.MODPACK;
    public static AnalyticsConfigData AnalyticsConfigData = new AnalyticsConfigData("UA-37330489-2");
    public static JGoogleAnalyticsTracker tracker;
    public static LoadingDialog loader;

    public static final String FORGENAME = "MinecraftForge.zip";
    private final static String launcherLogFile = "FTBLauncherLog.txt";
    private final static String minecraftLogFile = "MinecraftLog.txt";

    @Getter
    private static ProcessMonitor procMonitor;
    /*
    * @return FTB Launcher event bus
    */
    @Getter
    private EventBus eventBus = new EventBus();


    public enum Panes {
        NEWS, OPTIONS, MODPACK, THIRDPARTY, TEXTURE
    }
    private boolean tpEnabled = true;

    /**
     * Launch the application.
     * @param args - CLI arguments
     */
    public static void main (String[] args) {
        /*
         *  Create dynamic storage location as soon as possible
         */
        OSUtils.createStorageLocations();

        // Use IPv4 when possible, only use IPv6 when connecting to IPv6 only addresses
        System.setProperty("java.net.preferIPv4Stack", "true");

        if(Settings.getSettings().getUseSystemProxy()) {
            // Use system default proxy settings
            System.setProperty("java.net.useSystemProxies", "true");
        }
        
        if (new File(Settings.getSettings().getInstallPath(), "FTBLauncherLog.txt").exists()) {
            new File(Settings.getSettings().getInstallPath(), "FTBLauncherLog.txt").delete();
        }

        if (new File(Settings.getSettings().getInstallPath(), "MinecraftLog.txt").exists()) {
            new File(Settings.getSettings().getInstallPath(), "MinecraftLog.txt").delete();
        }

        /*
         * Create new StdoutLogger as soon as possible
         */
        Logger.addListener(new StdOutLogger());
        /*
         * Setup System.out and System.err redirection as soon as possible
         */
        System.setOut(new OutputOverride(System.out, LogLevel.INFO));
        System.setErr(new OutputOverride(System.err, LogLevel.ERROR));

        /*
         * Setup LogWriters as soon as possible
         */
        try {
            Logger.addListener(new LogWriter(new File(Settings.getSettings().getInstallPath(), launcherLogFile), LogSource.LAUNCHER));
            Logger.addListener(new LogWriter(new File(Settings.getSettings().getInstallPath(), minecraftLogFile), LogSource.EXTERNAL));
        } catch (IOException e1) {
            Logger.logError(e1.getMessage(), e1);
        }

        /*
         *  Posts information about OS, JVM and launcher version into Google Analytics
         */
        AnalyticsConfigData.setUserAgent("Java/" + System.getProperty("java.version") + " (" + System.getProperty("os.name") + "; " + System.getProperty("os.arch") + ")");
        tracker = new JGoogleAnalyticsTracker(AnalyticsConfigData, GoogleAnalyticsVersion.V_4_7_2);
        tracker.setEnabled(true);
        TrackerUtils.sendPageView("net/ftb/gui/LaunchFrame.java", "Launcher Start v" + version);
        if (!new File(OSUtils.getDynamicStorageLocation(), "FTBOSSent" + version + ".txt").exists()) {
            TrackerUtils.sendPageView("net/ftb/gui/LaunchFrame.java", "Launcher " + version + " OS " + OSUtils.getOSString());
            try {
                new File(OSUtils.getDynamicStorageLocation(), "FTBOSSent" + version + ".txt").createNewFile();
            } catch (IOException e) {
                Logger.logError("Error creating os cache text file");
            }
        }

        LaunchFrameHelpers.printInfo();

        /*
         * Resolves servers in background thread
         */
        DownloadUtils thread = new DownloadUtils();
        thread.start();

        /*
         * Setup GUI style & create and show Splash screen in EDT
         * NEVER add code with Thread.sleep() or I/O blocking, including network usage in EDT
         *  => If this guideline is followed then GUI should work smoothly
         */
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
                loader = new LoadingDialog();
                loader.setModal(false);
                loader.setVisible(true);

                if (!noConfig) {
                    /*
                     * Setup locales,  set locale and check  for new locales
                     * in background thread. Download new locales.
                     * Assume we have locale files already downloaded
                     */
                    I18N.setupLocale();
                    I18N.setLocale(Settings.getSettings().getLocale());
                    // nothing will set i18nLoaded, but it's ready
                    i18nLoaded = true;
                    I18N.downloadLocale();

                } else {
                    /*
                     * First run
                     */
                    I18N.setupLocale();
                    I18N.setLocale(Settings.getSettings().getLocale());
                    i18nLoaded = true;
                    I18N.downloadLocale();
                }

                if (noConfig) {
                    InstallDirectoryDialog installDialog = new InstallDirectoryDialog();
                    installDialog.setVisible(true);
                }

                LoadingDialog.setProgress(120);

                File installDir = new File(Settings.getSettings().getInstallPath());
                if (!installDir.exists()) {
                    installDir.mkdirs();
                }

                LoadingDialog.setProgress(130);

                // Store this in the cache (local) storage, since it's machine specific.
                userManager = new UserManager(new File(OSUtils.getCacheStorageLocation(), "logindata"), new File(OSUtils.getDynamicStorageLocation(), "logindata"));

                LoadingDialog.setProgress(140);

                if (Settings.getSettings().getConsoleActive()) {
                    con = new LauncherConsole();
                    con.setVisible(true);
                    Logger.addListener(con);
                    con.scrollToBottom();
                }

                LaunchFrameHelpers.googleAnalytics();

                LoadingDialog.setProgress(160);

                /*  Delay startup until the i18n update thread completes it's work
                 *  and populates the localeIndices, allowing OptionsTab to load
                 *  correctly.
                 */
                AppUtils.waitForLock(i18nLoaded);

                LaunchFrame frame = new LaunchFrame(2);
                instance = frame;

                /*
                 * Execute AuthlibDLWorker swingworker. done() will enable launch button as soon as possible
                 */
                AuthlibDLWorker authworker = new AuthlibDLWorker(Settings.getSettings().getInstallPath() + File.separator + "authlib" + File.separator, "1.5.13") {
                    @Override
                    protected void done() {
                        LaunchFrame.getInstance().getLaunch().setEnabled(true);
                    }
                };
                authworker.execute();

                LoadingDialog.setProgress(170);

                Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException (Thread t, Throwable e) {
                        Logger.logError("Unhandled exception in " + t.toString(), e);
                    }
                });

                /*
                 * Show the main form but hide it behind any active windows until
                 * loading is complete to prevent display issues.
                 * 
                 * @TODO ModpacksPane has a display issue with packScroll if the  
                 * main form is not visible when constructed.
                 */
                instance.setVisible(true);
                instance.toBack();

                instance.eventBus.register(frame.thirdPartyPane);
                instance.eventBus.register(frame.modPacksPane);

                ModPack.loadXml(getXmls());

                Map.addListener(frame.mapsPane);
                //				Map.loadAll();

                TexturePack.addListener(frame.tpPane);
                //				TexturePack.loadAll();

                /*
                 * Run UpdateChecker swingworker. done() will open LauncherUpdateDialog if needed
                 */
                UpdateChecker updateChecker = new UpdateChecker(buildNumber, minUsable) {
                    @Override
                    protected void done() {
                        try {
                            if (get()) {
                                LauncherUpdateDialog p = new LauncherUpdateDialog(this, minUsable);
                                p.setVisible(true);
                            }
                        } catch (InterruptedException e) {
                        } catch (ExecutionException e) {
                        }
                    }
                };
                updateChecker.execute();
                LoadingDialog.setProgress(180);
            }
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
                OSUtils.browse(Locations.FTBSITE);
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

        ArrayList<String> var = UserManager.getNames();
        String[] dropdown = ObjectUtils.concatenateArrays(dropdown_, var.toArray(new String[var.size()]));
        users = new JComboBox(dropdown);
        if (Settings.getSettings().getLastUser() != null) {
            for (int i = 0; i < dropdown.length; i++) {
                if (dropdown[i].equalsIgnoreCase(Settings.getSettings().getLastUser())) {
                    users.setSelectedIndex(i);
                }
            }
        }

        donate.setText(I18N.getLocaleString("DONATE_BUTTON"));
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

        edit.setText(I18N.getLocaleString("EDIT_BUTTON"));
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
        launch.setEnabled(false);
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
            //TODO this needs to be sensitive to 2 panes!!!
            @Override
            public void actionPerformed (ActionEvent event) {
                if (!ModPack.getSelectedPack().getServerUrl().isEmpty()) {
                    if (users.getSelectedIndex() > 1 && modPacksPane.packPanels.size() > 0) {
                            String version = (Settings.getSettings().getPackVer().equalsIgnoreCase("recommended version") || Settings.getSettings().getPackVer().equalsIgnoreCase("newest version")) ? ModPack
                                    .getSelectedPack().getVersion().replace(".", "_")
                                    : Settings.getSettings().getPackVer().replace(".", "_");
                            if (ModPack.getSelectedPack().isPrivatePack()) {
                                OSUtils.browse(DownloadUtils.getCreeperhostLink("privatepacks/" + ModPack.getSelectedPack().getDir() + "/" + version + "/" + ModPack.getSelectedPack().getServerUrl()));
                            } else {
                                OSUtils.browse(DownloadUtils.getCreeperhostLink("modpacks/" + ModPack.getSelectedPack().getDir() + "/" + version + "/" + ModPack.getSelectedPack().getServerUrl()));
                            }
                            TrackerUtils.sendPageView(ModPack.getSelectedPack().getName() + " Server Download", ModPack.getSelectedPack().getName());
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
                        OSUtils.browse(DownloadUtils.getCreeperhostLink("maps%5E" + Map.getMap(LaunchFrame.getSelectedMapIndex()).getMapName() + "%5E"
                                + Map.getMap(LaunchFrame.getSelectedMapIndex()).getVersion() + "%5E" + Map.getMap(LaunchFrame.getSelectedMapIndex()).getUrl()));
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
        modPacksPane = new FTBPacksPane();
        thirdPartyPane = new ThirdPartyPane();
        mapsPane = new MapUtils();
        tpPane = new TexturepackPane();
        optionsPane = new OptionsPane(Settings.getSettings());

        getRootPane().setDefaultButton(launch);
        updateLocale();

        tabbedPane.add(newsPane, 0);
        tabbedPane.add(optionsPane, 1);
        tabbedPane.add(modPacksPane, 2);
        tabbedPane.add(thirdPartyPane, 3);
        tabbedPane.add(tpPane, 4);
        /*
         * TODO: This will block. Network.
         */
        setNewsIcon();
        try {
            tabbedPane.setIconAt(1, LauncherStyle.getCurrentStyle().filterHeaderIcon(this.getClass().getResource("/image/tabs/options.png")));
            tabbedPane.setIconAt(2,  LauncherStyle.getCurrentStyle().filterHeaderIcon(this.getClass().getResource("/image/tabs/ftbpacks.png")));
            tabbedPane.setIconAt(3, LauncherStyle.getCurrentStyle().filterHeaderIcon(this.getClass().getResource("/image/tabs/thirdpartypacks.png")));
            tabbedPane.setIconAt(4, LauncherStyle.getCurrentStyle().filterHeaderIcon(this.getClass().getResource("/image/tabs/mapstextures.png")));
            tabbedPane.setSelectedIndex(tab);
        }catch(Exception e1){
            Logger.logError("error changing colors", e1);
        }
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

    public static void checkDoneLoading () {
        SwingUtilities.invokeLater(new Runnable() {
            public void run () {
                if (FTBPacksPane.getInstance().loaded) {
                    LoadingDialog.setProgress(190);
                    if (MapUtils.loaded) {
                        LoadingDialog.setProgress(200);
                        if (TexturepackPane.loaded) {
                            loader.setVisible(false);
                            instance.setVisible(true);
                            instance.toFront();
                        }
                    }
                }
            }
        });
    }

    public void setNewsIcon () {
        /* Call unreadNews swingworker
         * done() will set news tab icon
         */
        UnreadNewsWorker unreadNews = new UnreadNewsWorker() {
            @Override
            protected void done() {
                try {
                    int i = get();
                    if (i > 0 && i < 100) {
                        ImageAndTextIcon iti = new ImageAndTextIcon(this.getClass().getResource("/image/tabs/news_unread_" + Integer.toString(i).length() + ".png"), Integer.toString(i));
                        iti.setImage(LauncherStyle.getCurrentStyle().filterHeaderIcon(this.getClass().getResource("/image/tabs/news_unread_" + Integer.toString(i).length() + ".png")).getImage());
                        LaunchFrame.getInstance().tabbedPane.setIconAt(0, iti);
                    } else {
                        LaunchFrame.getInstance().tabbedPane.setIconAt(0, LauncherStyle.getCurrentStyle().filterHeaderIcon(this.getClass().getResource("/image/tabs/news.png")));
                    }
                } catch (InterruptedException e) {
                } catch (ExecutionException e) {
                }
            }
        };
        unreadNews.execute();
    }

    /**
     * call this to login
     */
    private void doLogin (final String username, String password, String mojangData) {
        if ((mojangData == null || mojangData.isEmpty()) && password.isEmpty()) {
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
        tabbedPane.setIconAt(0, LauncherStyle.getCurrentStyle().filterHeaderIcon(this.getClass().getResource("/image/tabs/news.png")));
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

        LoginWorker loginWorker = new LoginWorker(username, password, mojangData) {
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
                        PlayOfflineDialog d = new PlayOfflineDialog("mcDown", username, UserManager.getUUID(username), getResp());
                        d.setVisible(true);
                    }
                    enableObjects();
                    return;
                }

                RESPONSE = getResp();
                String uuid = userManager.getUUID(username);
                if (responseStr.equals("good")) {
                    Logger.logInfo("Login complete.");
                    try {
                        userManager.write();
                    } catch (IOException e) {
                        Logger.logError("User data saving failed!");
                    }
                    runGameUpdater(RESPONSE);
                } else if (uuid != null && !uuid.isEmpty() && RESPONSE != null && responseStr.equals("offline")) {
                    PlayOfflineDialog d = new PlayOfflineDialog("mcDown", username, uuid, RESPONSE);
                    d.setVisible(true);
                } else {
                    enableObjects();
                    return;
                }//if user doesn't want offline mode
                enableObjects();
            }
        };
        loginWorker.execute();
    }

    private Boolean checkVersion (File verFile, ModPack pack) {
        String storedVersion = pack.getStoredVersion(verFile);
        String onlineVersion = (Settings.getSettings().getPackVer().equalsIgnoreCase("recommended version") || Settings.getSettings().getPackVer().equalsIgnoreCase("newest version")) ? pack
                .getVersion() : Settings.getSettings().getPackVer();

        if (storedVersion.isEmpty()) {
            // Always allow updates from a version that isn't installed at all
            allowVersionChange = true;
            return true;
        } else if (Integer.parseInt(storedVersion.replace(".", "")) != Integer.parseInt(onlineVersion.replace(".", ""))) {
            ModPackVersionChangeDialog verDialog = new ModPackVersionChangeDialog(this, true, storedVersion, onlineVersion);
            verDialog.setVisible(true);
        }
        return allowVersionChange & (!storedVersion.equals(onlineVersion));
    }

    /**
     * checks whether an update is needed, and then starts the update process off
     * @param response - the response from the minecraft servers
     */
    private void runGameUpdater (final LoginResponse response) {

        final String installPath = Settings.getSettings().getInstallPath();
        final ModPack pack = ModPack.getSelectedPack();

        Logger.logDebug("ForceUpdate: " + Settings.getSettings().isForceUpdateEnabled());
        Logger.logDebug("installPath: " + installPath);
        Logger.logDebug("pack dir: " + pack.getDir());
        Logger.logDebug("pack check path: " + pack.getDir() + File.separator + "version");

        File verFile = new File(installPath, pack.getDir() + File.separator + "version");

        if (Settings.getSettings().isForceUpdateEnabled() && verFile.exists()) {
            verFile.delete();
            Logger.logDebug("Pack found and delete attempted");
        }

        if (Settings.getSettings().isForceUpdateEnabled() || !verFile.exists() || checkVersion(verFile, pack)) {
            if (doVersionBackup) {
                try {
                    File destination = new File(OSUtils.getCacheStorageLocation(), "backups" + File.separator + pack.getDir() + File.separator + "config_backup");
                    if (destination.exists()) {
                        FileUtils.delete(destination);
                    }
                    FileUtils.copyFolder(new File(Settings.getSettings().getInstallPath(), pack.getDir() + File.separator + "minecraft" + File.separator + "config"), destination);
                } catch (IOException e) {
                    Logger.logError(e.getMessage(), e);
                }
            }

            if (!initializeMods()) {
                Logger.logDebug("initializeMods: Failed to Init mods! Aborting to menu.");
                enableObjects();
                return;
            }
        }

        try {
            TextureManager.updateTextures();
        } catch (Exception e1) {
        }
        boolean isLegacy = true;
        if (pack.getMcVersion().startsWith("1.6") || pack.getMcVersion().startsWith("1.7") || pack.getMcVersion().startsWith("1.8") || pack.getMcVersion().startsWith("14w")) {
            isLegacy = false;
        }
        setupNewStyle(installPath, pack, isLegacy);
    }

    private void setupNewStyle (final String installPath, final ModPack pack, final boolean isLegacy) {
        List<DownloadInfo> assets = gatherAssets(new File(installPath), pack.getMcVersion());

        if (assets != null && assets.size() > 0) {
            Logger.logInfo("Gathering " + assets.size() + " assets, this may take a while...");

            final ProgressMonitor prog = new ProgressMonitor(this, "Downloading Files...", "", 0, 100);
            prog.setMaximum(assets.size() * 100);

            final AssetDownloader downloader = new AssetDownloader(prog, assets) {
                @Override
                public void done () {
                    try {
                        prog.close();
                        if (get()) {
                            Logger.logInfo("Asset downloading complete");
                            launchMinecraftNew(installPath, pack, RESPONSE, isLegacy);
                        } else {
                            ErrorUtils.tossError("Error occurred during downloading the assets");
                        }
                    } catch (CancellationException e) {
                        Logger.logInfo("Asset download interrupted by user");
                    } catch (Exception e) {
                        ErrorUtils.tossError("Failed to download files.", e);
                    } finally {
                        enableObjects();
                    }
                }
            };

            downloader.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange (PropertyChangeEvent evt) {
                    if (prog.isCanceled()) {
                        downloader.cancel(false);
                        prog.close();
                    } else if (!downloader.isCancelled()) {
                        if ("ready".equals(evt.getPropertyName()))
                            prog.setProgress(downloader.getReady());
                        if ("status".equals(evt.getPropertyName()))
                            prog.setNote(downloader.getStatus());
                    }
                }
            });

            downloader.execute();
        } else {
            launchMinecraftNew(installPath, pack, RESPONSE,isLegacy);
        }
    }

    private List<DownloadInfo> gatherAssets (File root, String mcVersion) {
        try {
            List<DownloadInfo> list = new ArrayList<DownloadInfo>();
            Boolean forceUpdate = Settings.getSettings().isForceUpdateEnabled();

            /*
             * vanilla minecraft.jar
             */

            File local = new File(root, "versions/{MC_VER}/{MC_VER}.jar".replace("{MC_VER}", mcVersion));
            if (!local.exists() || forceUpdate) {
                list.add(new DownloadInfo(new URL(Locations.mc_dl + "versions/{MC_VER}/{MC_VER}.jar".replace("{MC_VER}", mcVersion)), local, local.getName()));
            }

            /*
             * <ftb installation location>/libraries/*
             */
            //check if our copy exists of the version json if not backup to mojang's copy
            URL url = new URL(DownloadUtils.getStaticCreeperhostLinkOrBackup("mcjsons/versions/{MC_VER}/{MC_VER}.json".replace("{MC_VER}", mcVersion), Locations.mc_dl
                    + "versions/{MC_VER}/{MC_VER}.json".replace("{MC_VER}", mcVersion)));
            File json = new File(root, "versions/{MC_VER}/{MC_VER}.json".replace("{MC_VER}", mcVersion));
            int attempt=0, attempts=3;
            boolean success = false;
            Exception reason = null;
            while ((attempt < attempts) && !success) {
                try {
                    success = true;
                    DownloadUtils.downloadToFile(url, json);
                } catch (Exception e) {
                    success = false;
                    reason = e;
                    attempt++;
                }
                if (attempt == attempts && !success) {
                    Logger.logError("JSON download failed", reason);
                    return null;
                }
            }
            Version version = JsonFactory.loadVersion(json);
            for (Library lib : version.getLibraries()) {
                if (lib.natives == null) {
                    local = new File(root, "libraries/" + lib.getPath());
                    if (!local.exists() || forceUpdate) {
                        if (!lib.getUrl().toLowerCase().contains(Locations.ftb_maven)) {
                            list.add(new DownloadInfo(new URL(lib.getUrl() + lib.getPath()), local, lib.getPath()));
                        } else {
                            list.add(new DownloadInfo(new URL(DownloadUtils.getCreeperhostLink(lib.getUrl() + lib.getPath())), local, lib.getPath(), true));
                        }
                    }
                } else {
                    local = new File(root, "libraries/" + lib.getPathNatives());
                    if (!local.exists() || forceUpdate) {
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

            /*
             * assets/*
             */
            url = new URL(Locations.mc_dl + "indexes/{INDEX}.json".replace("{INDEX}", version.getAssets()));
            json = new File(root, "assets/indexes/{INDEX}.json".replace("{INDEX}", version.getAssets()));
            attempt=0; attempts=3;
            success = false;
            while ((attempt < attempts) && !success) {
                try {
                    success = true;
                    DownloadUtils.downloadToFile(url, json);
                } catch (Exception e) {
                    success = false;
                    attempt++;
                    reason = e;
                }
                if (attempt == attempts && !success) {
                    Logger.logError("JSON download failed", reason);
                    return null;
                }
            }

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
                    list.add(new DownloadInfo(new URL(Locations.mc_res + path), local, name, asset.hash, "sha1"));
                }
            }
            return list;
        } catch (Exception e) {
            Logger.logError(e.getMessage(), e);
        }
        return null;
    }

    public void launchMinecraftNew (String installDir, ModPack pack, LoginResponse resp, boolean isLegacy) {
        try {
            File packDir = new File(installDir, pack.getDir());
            String gameFolder = installDir + File.separator + pack.getDir() + File.separator + "minecraft";
            File gameDir = new File(packDir, "minecraft");
            File assetDir = new File(installDir, "assets");
            File libDir = new File(installDir, "libraries");
            File natDir = new File(packDir, "natives");
            if (natDir.exists()) {
                natDir.delete();
            }
            natDir.mkdirs();
            if(isLegacy)
                extractLegacy();
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
                        ErrorUtils.tossError("Error extracting natives: " + e.getMessage());
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
            if (!pack.getDir().equals("mojang_vanilla")) {
                if(isLegacy){
                    extractLegacyJson(new File(gameDir, "pack.json"));
                }
                if (new File(gameDir, "pack.json").exists()) {
                    packjson = JsonFactory.loadVersion(new File(gameDir, "pack.json"));
                    for (Library lib : packjson.getLibraries()) {
                        //Logger.logError(new File(libDir, lib.getPath()).getAbsolutePath());
                        classpath.add(new File(libDir, lib.getPath()));
                    }
                }
            } else {
                packjson = base;
            }
            if(!isLegacy) //we copy the jar to a new location for legacy
                classpath.add(new File(installDir, "versions/{MC_VER}/{MC_VER}.jar".replace("{MC_VER}", pack.getMcVersion())));
            else {
                FileUtils.copyFile(new File(installDir, "versions/{MC_VER}/{MC_VER}.jar".replace("{MC_VER}", pack.getMcVersion())), new File(gameDir, "bin/minecraft.jar"));
                FileUtils.killMetaInf();
            }
            for (Library lib : base.getLibraries()) {
                classpath.add(new File(libDir, lib.getPath()));
            }
            //launchMinecraftNew(installPath, pack, RESPONSE.getUsername(), RESPONSE.getSessionID(), pack.getMaxPermSize(), RESPONSE.getUUID());

            Process minecraftProcess = MinecraftLauncherNew.launchMinecraft(Settings.getSettings().getJavaPath(), gameFolder, assetDir, natDir, classpath, packjson.mainClass != null ? packjson.mainClass
                    : base.mainClass, packjson.minecraftArguments != null ? packjson.minecraftArguments : base.minecraftArguments, packjson.assets != null ? packjson.assets : base.getAssets(),
                    Settings.getSettings().getRamMax(), pack.getMaxPermSize(), pack.getMcVersion(), resp.getAuth(), isLegacy);
            MCRunning = true;
            if(con != null) con.minecraftStarted();
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
                procMonitor = ProcessMonitor.create(minecraftProcess, new Runnable() {
                    @Override
                    public void run () {
                        if (!Settings.getSettings().getKeepLauncherOpen()) {
                            System.exit(0);
                        } else {
                            if(con != null) con.minecraftStopped();
                            LaunchFrame launchFrame = LaunchFrame.this;
                            launchFrame.setVisible(true);
                            launchFrame.enableObjects();
                            try {
                                Settings.getSettings().load(new FileInputStream(Settings.getSettings().getConfigFile()));
                                tabbedPane.remove(1);
                                optionsPane = new OptionsPane(Settings.getSettings());
                                tabbedPane.add(optionsPane, 1);
                                tabbedPane.setIconAt(1, LauncherStyle.getCurrentStyle().filterHeaderIcon(this.getClass().getResource("/image/tabs/options.png")));
                            } catch (Exception e1) {
                                Logger.logError("Failed to reload settings after launcher closed", e1);
                            }
                        }
                        MCRunning = false;
                    }
                });
            }
        } catch (Exception e) {
            Logger.logError(e.getMessage(), e);
        }
    }

    /**
     * @param modPackName - The pack to install (should already be downloaded)
     * @throws IOException
     */
    protected void installMods (String modPackName) throws IOException {
        String installpath = Settings.getSettings().getInstallPath();
        String temppath = OSUtils.getCacheStorageLocation();

        ModPack pack;
        if(LaunchFrame.currentPane == LaunchFrame.Panes.THIRDPARTY)
           pack = ModPack.getPack(thirdPartyPane.getSelectedThirdPartyModIndex());
        else
           pack = ModPack.getPack(modPacksPane.getSelectedFTBModIndex());

        String packDir = pack.getDir();

        Logger.logInfo("dirs mk'd");

        File source = new File(temppath, "ModPacks/" + packDir + "/.minecraft");
        if (!source.exists()) {
            source = new File(temppath, "ModPacks/" + packDir + "/minecraft");
        }

        Logger.logDebug("install path: " + installpath);
        Logger.logDebug("temp path: " + temppath);
        Logger.logDebug("source: " + source);
        Logger.logDebug("packDir: " + packDir);

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
        ArrayList<String> var = UserManager.getNames();
        String[] usernames = ObjectUtils.concatenateArrays(dropdown_, var.toArray(new String[var.size()]));
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
    public static void updateTpInstallLocs (List<String> locations) {
        tpInstallLocation.removeAllItems();
        for (String location : locations) {
            if (location != null && !location.isEmpty()) {
                tpInstallLocation.addItem(ModPack.getPack(location.trim()).getName());
            }
        }
        tpInstallLocation.setSelectedItem(ModPack.getSelectedPack(true).getName());
    }

    /**
     * updates the mapInstall to the available ones
     * @param locations - the available locations to install the map to
     */
    public static void updateMapInstallLocs (String[] locations) {
        mapInstallLocation.removeAllItems();
        for (String location : locations) {
            if (location != null && !location.isEmpty()) {
                mapInstallLocation.addItem(ModPack.getPack(location.trim()).getName());
            }
        }
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
        Logger.logDebug("pack dir...");
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
            Logger.logDebug("Exception: ", e);
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
        case TEXTURE:
            if(tpEnabled) {
            tpInstall.setVisible(true);
            tpInstallLocation.setVisible(true);
            disableMainButtons();
            disableMapButtons();
            } else {
                result = mapsPane.type.equals("Server");
                mapInstall.setVisible(!result);
                mapInstallLocation.setVisible(!result);
                serverMap.setVisible(result);
                disableMainButtons();
                disableTextureButtons();

            }
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
        optionsPane.updateLocale();
        modPacksPane.updateLocale();
        thirdPartyPane.updateLocale();
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
        s.add(1, "thirdparty.xml");
        return s;
    }

    public void doLaunch () {
        JavaInfo java = Settings.getSettings().getCurrentJava();
        int[] minSup = ModPack.getSelectedPack().getMinJRE();
        if (users.getSelectedIndex() > 1 && ModPack.getSelectedPack() != null) {
            if (minSup.length >= 2 && minSup[0] <= java.getMajor() && minSup[1] <= java.getMinor()) {
                Settings.getSettings().setLastFTBPack(ModPack.getSelectedPack(true).getDir());
                Settings.getSettings().setLastThirdPartyPack(ModPack.getSelectedPack(false).getDir());
                saveSettings();
                doLogin(UserManager.getUsername(users.getSelectedItem().toString()), UserManager.getPassword(users.getSelectedItem().toString()),
                        UserManager.getMojangData(users.getSelectedItem().toString()));
            } else {//user can't run pack-- JRE not high enough
                ErrorUtils.tossError("You must use at least java " + minSup[0] + "." + minSup[1] + " to play this pack! Please go to Options to get a link or Advanced Options enter a path.");
            }
        } else if (users.getSelectedIndex() <= 1) {
            ErrorUtils.tossError("Please select a profile!");
        }
    }
    public static void extractLegacy(){
       try {
           File f = new File(Settings.getSettings().getInstallPath() + File.separator + "libraries" + File.separator + "net.ftb.legacylaunch.FTBLegacyLaunch".replace(".", File.separator) + File.separator + "0.0.1" + File.separator + "FTBLegacyLaunch-0.0.1.jar");
           //Logger.logError("Extracting Legacy launch code to " + f.getAbsolutePath());
           if(!new File(f.getParent()).exists())
               new File(f.getParent()).mkdirs();
           if(f.exists())
               f.delete();//we want to have the current version always!!!
           URL u = LaunchFrame.class.getResource("/launch/FTBLegacyLaunch-0.0.1.jar");
           org.apache.commons.io.FileUtils.copyURLToFile(u,f);
       } catch (Exception e){
            Logger.logError("Error extracting legacy launch to maven directory");
       }
    }
    public static void extractLegacyJson(File newLoc){
        try {
            if(!new File(newLoc.getParent()).exists())
                new File(newLoc.getParent()).mkdirs();
            if(newLoc.exists())
                newLoc.delete();//we want to have the current version always!!!
            URL u = LaunchFrame.class.getResource("/launch/legacypack.json");
            org.apache.commons.io.FileUtils.copyURLToFile(u,newLoc);
        } catch (Exception e){
            Logger.logError("Error extracting legacy launch to maven directory");
        }
    }

    public void swapTabs(boolean toMaps){
        if(toMaps) {
            tabbedPane.remove(4);
            tabbedPane.add(mapsPane, 4);
            tabbedPane.setIconAt(4, LauncherStyle.getCurrentStyle().filterHeaderIcon(this.getClass().getResource("/image/tabs/mapstextures.png")));
            tabbedPane.setSelectedIndex(4);
            tpEnabled = false;
            updateFooter();
        } else {
            tabbedPane.remove(4);
            tabbedPane.add(tpPane, 4);
            tabbedPane.setIconAt(4, LauncherStyle.getCurrentStyle().filterHeaderIcon(this.getClass().getResource("/image/tabs/mapstextures.png")));
            tabbedPane.setSelectedIndex(4);
            tpEnabled = true;
            updateFooter();
        }
    }
}
