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

import com.google.common.eventbus.Subscribe;
import lombok.Getter;
import lombok.Setter;
import net.ftb.data.*;
import net.ftb.download.Locations;
import net.ftb.events.EnableObjectsEvent;
import net.ftb.gui.dialogs.LoadingDialog;
import net.ftb.gui.dialogs.ModPackVersionChangeDialog;
import net.ftb.gui.dialogs.PasswordDialog;
import net.ftb.gui.dialogs.PlayOfflineDialog;
import net.ftb.gui.dialogs.ProfileAdderDialog;
import net.ftb.gui.dialogs.ProfileEditorDialog;
import net.ftb.gui.panes.FTBPacksPane;
import net.ftb.gui.panes.ILauncherPane;
import net.ftb.gui.panes.MapUtils;
import net.ftb.gui.panes.NewsPane;
import net.ftb.gui.panes.OptionsPane;
import net.ftb.gui.panes.TexturepackPane;
import net.ftb.gui.panes.ThirdPartyPane;
import net.ftb.locale.I18N;
import net.ftb.log.Logger;
import net.ftb.main.Main;
import net.ftb.minecraft.MCInstaller;
import net.ftb.tools.MapManager;
import net.ftb.tools.ModManager;
import net.ftb.tools.ProcessMonitor;
import net.ftb.tools.TextureManager;
import net.ftb.util.Benchmark;
import net.ftb.util.DownloadUtils;
import net.ftb.util.ErrorUtils;
import net.ftb.util.FTBFileUtils;
import net.ftb.util.OSUtils;
import net.ftb.util.ObjectUtils;
import net.ftb.util.TrackerUtils;
import net.ftb.util.winreg.JavaInfo;
import net.ftb.util.winreg.JavaVersion;
import net.ftb.workers.LoginWorker;
import net.ftb.workers.NewsWorker;
import net.ftb.workers.UnreadNewsWorker;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
public class LaunchFrame extends JFrame {
    private LoginResponse RESPONSE;
    private NewsPane newsPane;
    public static JPanel panel;
    private JPanel footer = new JPanel();
    private JLabel footerLogo = new JLabel(new ImageIcon(this.getClass().getResource(Locations.FTBLOGO)));
    private JLabel footerCreeper = new JLabel(new ImageIcon(this.getClass().getResource(Locations.CHLOGO)));
    private JLabel footerCurse = new JLabel(new ImageIcon(this.getClass().getResource(Locations.CURSELOGO)));
    private JLabel tpInstallLocLbl = new JLabel();
    @Getter
    private final JButton launch = new JButton(), edit = new JButton(), serverbutton = new JButton(), mapInstall = new JButton(), serverMap = new JButton(),
            tpInstall = new JButton();

    private static String[] dropdown_ = { "Select Profile", "Create Profile" };
    private static JComboBox users, tpInstallLocation, mapInstallLocation;
    private static AtomicInteger checkDoneLoadingCallCount = new AtomicInteger(0);
    /**
     * @return - Outputs LaunchFrame instance
     */
    @Getter
    @Setter
    private static LaunchFrame instance = null;

    public static int minUsable = -1;
    public final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);

    public FTBPacksPane modPacksPane;
    public ThirdPartyPane thirdPartyPane;
    public MapUtils mapsPane;
    public TexturepackPane tpPane;
    public OptionsPane optionsPane;
    public int tab;

    public static TrayMenu trayMenu;

    public static boolean allowVersionChange = false;
    public static boolean doVersionBackup = false;
    public static boolean MCRunning = false;
    public static LauncherConsole con;
    public static String tempPass = "";
    public static Panes currentPane = Panes.MODPACK;
    public static LoadingDialog loader;

    @Getter
    @Setter
    private static ProcessMonitor procMonitor;

    public enum Panes {
        NEWS, OPTIONS, MODPACK, THIRDPARTY, TEXTURE
    }

    private boolean tpEnabled = true;

    /**
     * Create the frame.
     */
    public LaunchFrame (final int tab) {
        this.tab = tab;
        setFont(new Font("a_FuturaOrto", Font.PLAIN, 12));
        setResizable(true);
        setTitle(Constants.name + " v" + Constants.version);
        setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));

        panel = new JPanel(new BorderLayout());

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        int prefWidth = 850;
        int prefHeight = 480;
        this.setMinimumSize(new Dimension(prefWidth, prefHeight));

        // Determine how much space is used by window decoration, resize accordingly
        this.pack();
        Dimension fullWindowSize = this.getContentPane().getSize();
        this.setMinimumSize(new Dimension(prefWidth + (prefWidth - fullWindowSize.width), prefHeight + (prefHeight - fullWindowSize.height)));

        // Center on screen
        this.setLocationRelativeTo(null);

        footer.setMinimumSize(new Dimension(850, 100));
        footer.setLayout(new BorderLayout());
        footer.setBackground(LauncherStyle.getCurrentStyle().footerColor);

        tabbedPane.setMinimumSize(new Dimension(850, 380));

        panel.add(tabbedPane, BorderLayout.CENTER);
        panel.add(footer, BorderLayout.PAGE_END);
        setContentPane(panel);

        //Footer
        footerLogo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        footerLogo.setMinimumSize(new Dimension(42, 42));
        footerLogo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked (MouseEvent event) {
                OSUtils.browse(Locations.FTBSITE);
            }
        });

        footerCreeper.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        footerCreeper.setMinimumSize(new Dimension(132, 42));
        footerCreeper.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked (MouseEvent event) {
                OSUtils.browse("http://billing.creeperhost.net/link.php?id=2");
            }
        });

        footerCurse.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        footerCurse.setMinimumSize(new Dimension(118, 29));
        footerCurse.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked (MouseEvent event) {
                OSUtils.browse(Locations.CURSEVOICE);
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

        users.setMinimumSize(new Dimension(150, 30));
        users.setMaximumSize(new Dimension(150, 30));
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
        edit.setMinimumSize(new Dimension(60, 30));
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
        //TODO: move this or make sure doLaunch() enables it. Only visual bug.
        launch.setEnabled(false);
        launch.setMinimumSize(new Dimension(100, 30));
        launch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                doLaunch();
            }
        });

        serverbutton.setMinimumSize(new Dimension(330, 30));
        serverbutton.setText(I18N.getLocaleString("DOWNLOAD_SERVER_PACK"));
        serverbutton.setVisible(false);
        serverbutton.addActionListener(new ActionListener() {
            //TODO this needs to be sensitive to 2 panes!!!
            @Override
            public void actionPerformed (ActionEvent event) {
                if (!ModPack.getSelectedPack().getServerUrl().isEmpty()) {
                    if (users.getSelectedIndex() > 1 && modPacksPane.packPanels.size() > 0) {
                        String version = (Settings.getSettings().getPackVer().equalsIgnoreCase("recommended version") || Settings.getSettings().getPackVer().equalsIgnoreCase("newest version"))
                                ? ModPack
                                .getSelectedPack().getVersion().replace(".", "_")
                                : Settings.getSettings().getPackVer().replace(".", "_");
                        if (ModPack.getSelectedPack().isPrivatePack()) {
                            OSUtils.browse(DownloadUtils.getCreeperhostLink("privatepacks/" + ModPack.getSelectedPack().getDir() + "/" + version + "/" + ModPack.getSelectedPack().getServerUrl()));
                        } else {
                            OSUtils.browse(DownloadUtils.getCreeperhostLink("modpacks/" + ModPack.getSelectedPack().getDir() + "/" + version + "/" + ModPack.getSelectedPack().getServerUrl()));
                        }
                        TrackerUtils.sendPageView(ModPack.getSelectedPack().getName() + "Server Download",
                                "Server Download / " + ModPack.getSelectedPack().getName() + " / " + ModPack.getSelectedPack().getVersion());
                    }
                }
            }
        });

        mapInstall.setMinimumSize(new Dimension(160, 30));
        mapInstall.setText(I18N.getLocaleString("INSTALL_MAP"));
        mapInstall.setVisible(false);
        mapInstall.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                if (MapUtils.mapPanels.size() > 0 && getSelectedMapIndex() >= 0) {
                    MapManager man = new MapManager(new JFrame(), true);
                    man.setVisible(true);
                    MapManager.cleanUp();
                }
            }
        });

        mapInstallLocation = new JComboBox();
        mapInstallLocation.setMinimumSize(new Dimension(160, 30));
        mapInstallLocation.setToolTipText("Install to...");
        mapInstallLocation.setVisible(false);

        serverMap.setMinimumSize(new Dimension(330, 30));
        serverMap.setText(I18N.getLocaleString("DOWNLOAD_MAP_SERVER"));
        serverMap.setVisible(false);
        serverMap.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent event) {
                if (MapUtils.mapPanels.size() > 0 && getSelectedMapIndex() >= 0) {
                    OSUtils.browse(DownloadUtils.getCreeperhostLink("maps/" + Map.getMap(LaunchFrame.getSelectedMapIndex()).getMapName() + "/"
                            + Map.getMap(LaunchFrame.getSelectedMapIndex()).getVersion() + "/" + Map.getMap(LaunchFrame.getSelectedMapIndex()).getUrl()));
                }
            }
        });

        tpInstall.setMinimumSize(new Dimension(160, 30));
        tpInstall.setText(I18N.getLocaleString("INSTALL_TEXTUREPACK"));
        tpInstall.setVisible(false);
        tpInstall.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                if (TexturepackPane.texturePackPanels.size() > 0 && getSelectedTexturePackIndex() >= 0) {
                    TextureManager man = new TextureManager(new JFrame(), true);
                    man.setVisible(true);
                }
            }
        });

        tpInstallLocation = new JComboBox();
        tpInstallLocation.setMinimumSize(new Dimension(160, 30));
        tpInstallLocation.setToolTipText("Install to...");
        tpInstallLocation.setVisible(false);

        tpInstallLocLbl.setText("Install to...");
        tpInstallLocLbl.setMinimumSize(new Dimension(80, 30));
        tpInstallLocLbl.setVisible(false);

        // Panel for the items in the bottom left
        JPanel logoPanel = new JPanel();
        logoPanel.setBackground(LauncherStyle.getCurrentStyle().footerColor);
        logoPanel.add(footerLogo);
        logoPanel.add(footerCreeper);
        logoPanel.add(footerCurse);

        // Panel for the items in the bottom right
        JPanel buttonFooterPanel = new JPanel();
        buttonFooterPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonFooterPanel.setBackground(LauncherStyle.getCurrentStyle().footerColor);
        buttonFooterPanel.add(edit);
        buttonFooterPanel.add(users);
        buttonFooterPanel.add(launch);

        // Buttons for texture pack pane
        buttonFooterPanel.add(tpInstallLocation);
        buttonFooterPanel.add(tpInstall);

        // Buttons for custom map pane
        buttonFooterPanel.add(mapInstallLocation);
        buttonFooterPanel.add(mapInstall);

        // Button if server map
        buttonFooterPanel.add(serverMap);

        footer.add(logoPanel, BorderLayout.LINE_START);
        footer.add(buttonFooterPanel, BorderLayout.LINE_END);


        newsPane = new NewsPane();
        if (!CommandLineSettings.getSettings().isDisableNews()) {
            NewsWorker nw = new NewsWorker() {
                @Override
                protected void done () {
                    String html = null;
                    try {
                        html = get();
                    } catch (InterruptedException e) {
                        Logger.logDebug("Swingworker Exception", e);
                    } catch (ExecutionException e) {
                        Logger.logDebug("Swingworker Exception", e.getCause());
                    }

                    newsPane.setContent(html);
                }
            };
            nw.execute();
        }
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
            tabbedPane.setIconAt(2, LauncherStyle.getCurrentStyle().filterHeaderIcon(this.getClass().getResource("/image/tabs/ftbpacks.png")));
            tabbedPane.setIconAt(3, LauncherStyle.getCurrentStyle().filterHeaderIcon(this.getClass().getResource("/image/tabs/thirdpartypacks.png")));
            tabbedPane.setIconAt(4, LauncherStyle.getCurrentStyle().filterHeaderIcon(this.getClass().getResource("/image/tabs/mapstextures.png")));
        } catch (Exception e1) {
            Logger.logError("error changing colors", e1);
        }
        // this will be fired when
        // * tab is clicked
        // * swapTabs(): tabbedPane.setSelectedIndex() is invoked and when tab is clicked
        // Is that true?!
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged (ChangeEvent event) {
                if (tabbedPane.getSelectedComponent() instanceof ILauncherPane) {
                    ((ILauncherPane) tabbedPane.getSelectedComponent()).onVisible();
                    // When called by swapTabs currentPane will be THIRDPARTY until this ActionListener is called again
                    currentPane = Panes.values()[tabbedPane.getSelectedIndex()];
                    updateFooter();
                }
            }
        });

        panel.addComponentListener(new ComponentAdapter() {
            // Reset splitter on window resize to avoid being in an unreachable location
            @Override
            public void componentResized (ComponentEvent arg0) {
                modPacksPane.getSplitPane().resetToPreferredSizes();
                thirdPartyPane.getSplitPane().resetToPreferredSizes();
                tpPane.getSplitPane().resetToPreferredSizes();
                mapsPane.getSplitPane().resetToPreferredSizes();
            }
        });

    }

    public static void checkDoneLoading () {
        int callCount = checkDoneLoadingCallCount.incrementAndGet();
        if (callCount == 1) {
            Benchmark.start("Waiting for main window");
            while (LaunchFrame.instance == null) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {}
            }
            Benchmark.logBench("Waiting for main window");

            SwingUtilities.invokeLater(new Runnable() {
                public void run () {
                    LoadingDialog.advance("Opening main window");
                    Benchmark.logBenchAs("main", "Launcher Startup(Modpacks loaded)");

                    // set last run packs active
                    Logger.logDebug("Last used packs: " + Settings.getSettings().getLastFTBPack() + " " + Settings.getSettings().getLastThirdPartyPack());
                    FTBPacksPane.getInstance().setSelectedPack(ModPack.getPack(Settings.getSettings().getLastFTBPack()));
                    ThirdPartyPane.getInstance().setSelectedPack(ModPack.getPack(Settings.getSettings().getLastThirdPartyPack()));
                    // update panes
                    FTBPacksPane.getInstance().filterPacks();
                    ThirdPartyPane.getInstance().filterPacks();

                    // indicate things are ready to use
                    FTBPacksPane.getInstance().loaded = true;
                    ThirdPartyPane.getInstance().loaded = true;

                    // ugly hacks which does not even work
                    // TODO: someone: how to fix this?
                    //FTBPacksPane.getInstance().getPacksScroll().getViewport().setViewPosition(new Point(0, 0));
                    instance.tabbedPane.setSelectedIndex(instance.tab);

                    instance.setVisible(true);
                    instance.toFront();

                    //TODO: add checks if loader is disabled
                    loader.setVisible(false);
                    loader.dispose();

                    Benchmark.logBenchAs("main", "Launcher Startup(main window opened and ready to use)");
                    String packDir = CommandLineSettings.getSettings().getPackDir();
                    if (packDir != null) {
                        ModPack.setSelectedPack(packDir);
                        LaunchFrame.getInstance().doLaunch();
                    }
                }
            });
            Map.loadAll();
            TexturePack.loadAll();
        }
        if (callCount == 2) {
            Benchmark.logBenchAs("main", "Launcher Startup(maps or texturepacks loaded)");
        }
        if (callCount == 3) {
            Benchmark.logBenchAs("main", "Launcher Startup(maps and texturepacks loaded)");
        }
    }

    public void setNewsIcon () {
        /* Call unreadNews swingworker
         * done() will set news tab icon
         */
        UnreadNewsWorker unreadNews = new UnreadNewsWorker() {
            @Override
            protected void done () {
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
                    Logger.logDebug("Swingworker Exception", e);
                } catch (ExecutionException e) {
                    Logger.logDebug("Swingworker Exception", e.getCause());
                }
            }
        };
        unreadNews.execute();
    }

    /**
     * call this to login
     */
    private void doLogin (final String username, String password, String mojangData, String selectedProfile) {
        if (ModPack.getSelectedPack().getDisclaimer() != null && !ModPack.getSelectedPack().getDisclaimer().isEmpty()) {
            ErrorUtils.tossError(ModPack.getSelectedPack().getDisclaimer());
        }
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

        LoginWorker loginWorker = new LoginWorker(username, password, mojangData, selectedProfile) {
            @Override
            public void done () {
                String responseStr;
                try {
                    responseStr = get();
                } catch (InterruptedException err) {
                    Logger.logError("User cancelled login process", err);
                    enableObjects();
                    return;
                } catch (ExecutionException err) {
                    // Worker should not leak ExecutionExceptions to caller: all Exceptions are handled internally twice
                    if (err.getCause() instanceof IOException) {
                        Logger.logError("Error while logging in", err);
                        PlayOfflineDialog d = new PlayOfflineDialog("mcDown", username, UserManager.getUUID(username), getResp());
                        d.setVisible(true);
                    } else {
                        Logger.logDebug("Swingworker Exception", err.getCause());
                    }
                    enableObjects();
                    return;
                }

                RESPONSE = getResp();
                Logger.logDebug("responseStr: " + responseStr);
                String uuid = UserManager.getUUID(username);
                if (responseStr.equals("good")) {
                    Logger.logInfo("Login complete.");
                    try {
                        // save userdata, including new mojangData
                        Main.getUserManager().write();
                        Logger.logDebug("user data saved");
                    } catch (IOException e) {
                        Logger.logError("logindata saving failed!");
                    }
                    runGameUpdater();
                } else if (uuid != null && !uuid.isEmpty() && RESPONSE != null && responseStr.equals("offline")) {
                    Logger.logDebug("Asking user for offline mode");
                    PlayOfflineDialog d = new PlayOfflineDialog("mcDown", username, uuid, RESPONSE);
                    d.setVisible(true);
                } else {
                    Logger.logDebug("Bad responseStr, not starting MC");
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

        Logger.logDebug("verFile: " + storedVersion + " onlineVersion/getPackVer(): " + Settings.getSettings().getPackVer() + " onlineVersion/getVersion(): " + pack.getVersion());

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
     */
    private void runGameUpdater () {

        final String installPath = Settings.getSettings().getInstallPath();
        final ModPack pack = ModPack.getSelectedPack();
        boolean softUpdate = false;

        Logger.logDebug("ForceUpdate: " + Settings.getSettings().isForceUpdateEnabled());
        Logger.logDebug("installPath: " + installPath);
        Logger.logDebug("pack dir: " + pack.getDir());
        Logger.logDebug("pack check path: " + pack.getDir() + File.separator + "version");

        File verFile = new File(installPath, pack.getDir() + File.separator + "version");

        if (verFile.exists()) {
            softUpdate = true;
        }

        if (Settings.getSettings().isForceUpdateEnabled() && verFile.exists()) {
            verFile.delete();
            softUpdate = false;
            Logger.logDebug("Pack found and delete attempted");
        }

        if (Settings.getSettings().isForceUpdateEnabled() || !verFile.exists() || checkVersion(verFile, pack)) {
            if (doVersionBackup) {
                try {
                    File destination = new File(OSUtils.getCacheStorageLocation(), "backups" + File.separator + pack.getDir() + File.separator + "config_backup");
                    if (destination.exists()) {
                        FTBFileUtils.delete(destination);
                    }
                    FTBFileUtils.copyFolder(new File(Settings.getSettings().getInstallPath(), pack.getDir() + File.separator + "minecraft" + File.separator + "config"), destination);
                } catch (IOException e) {
                    Logger.logError("Error while doing backups", e);
                }
            }

            if (!initializeMods(softUpdate)) {
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
        if (pack.getMcVersion().startsWith("1.6") || pack.getMcVersion().startsWith("1.7") || pack.getMcVersion().startsWith("1.8") || pack.getMcVersion().startsWith("1.9") || pack.getMcVersion().startsWith("1.10") || pack.getMcVersion().startsWith("1.11") || pack.getMcVersion().startsWith("1.12") || pack.getMcVersion().startsWith("14w")|| pack.getMcVersion().startsWith("15w")|| pack.getMcVersion().startsWith("16w")) {
            isLegacy = false;
        }
        MCInstaller.setupNewStyle(installPath, pack, isLegacy, RESPONSE);
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
            Main.getUserManager().write();
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
                ModPack m = ModPack.getPack(location.trim());
                if (m == null) {
                    Logger.logWarn("Can't find modpack: " + location);
                    continue;
                }
                String s = m.getNameWithVersion();
                tpInstallLocation.addItem(s);
            }
        }
        //TODO:
        // Decide later if we want to do this? How to handle selection from two modpack panes?
        //tpInstallLocation.setSelectedItem(ModPack.getSelectedPack(true).getNameWithVersion());
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
        return MapUtils.getSelectedMapIndex();
    }

    /**
     * @return - Outputs selected texturepack index
     */
    public static int getSelectedTexturePackIndex () {
        return TexturepackPane.getSelectedTexturePackIndex();
    }

    /**
     * @return - Outputs selected map install index
     */
    public static int getSelectedMapInstallIndex () {
        return mapInstallLocation.getSelectedIndex();
    }

    /**
     * @return - Outputs selected texturepack install index
     */
    public static int getSelectedTPInstallIndex () {
        return tpInstallLocation.getSelectedIndex();
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

    @Subscribe
    private void handleEnableObjectsEvent (EnableObjectsEvent e) {
        enableObjects();
    }

    /**
     * Download and install mods
     * @return boolean - represents whether it was successful in initializing mods
     */
    private boolean initializeMods (boolean softUpdate) {
        Logger.logDebug("pack dir...");
        Logger.logInfo(ModPack.getSelectedPack().getDir());
        ModManager man = new ModManager(new JFrame(), true);
        man.setVisible(true);
        while (man == null) {
        }
        while (!ModManager.worker.isDone()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
        }
        if (ModManager.erroneous) {
            return false;
        }
        try {
            MCInstaller.installMods(ModPack.getSelectedPack().getDir(), softUpdate);
            ModManager.cleanUp();
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
            if (tpEnabled) {
                tpInstall.setVisible(true);
                tpInstallLocation.setVisible(true);
                disableMainButtons();
                disableMapButtons();
            } else {
                result = MapUtils.type.equals("Server");
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
        launch.setText(I18N.getLocaleString("LAUNCH_BUTTON"));
        edit.setText(I18N.getLocaleString("EDIT_BUTTON"));
        serverbutton.setText(I18N.getLocaleString("DOWNLOAD_SERVER_PACK"));
        mapInstall.setText(I18N.getLocaleString("INSTALL_MAP"));
        serverMap.setText(I18N.getLocaleString("DOWNLOAD_MAP_SERVER"));
        tpInstall.setText(I18N.getLocaleString("INSTALL_TEXTUREPACK"));
        dropdown_[0] = I18N.getLocaleString("PROFILE_SELECT");
        dropdown_[1] = I18N.getLocaleString("PROFILE_CREATE");
        optionsPane.updateLocale();
        modPacksPane.updateLocale();
        thirdPartyPane.updateLocale();
        mapsPane.updateLocale();
        tpPane.updateLocale();
        if (trayMenu != null) {
            trayMenu.updateLocale();
        }
    }

    public void doLaunch () {
        JavaInfo java = Settings.getSettings().getCurrentJava();
        ModPack pack = ModPack.getSelectedPack();

        // save ´pack being launched
        if (LaunchFrame.currentPane == Panes.MODPACK) {
            Settings.getSettings().setLastFTBPack(FTBPacksPane.getInstance().getSelectedPack().getDir());
        } else {
            Settings.getSettings().setLastThirdPartyPack(ThirdPartyPane.getInstance().getSelectedPack().getDir());
        }
        saveSettings();

        // check launcher version
        if (ModPack.getSelectedPack().getMinLaunchSpec() > Constants.buildNumber) {
            ErrorUtils.tossError("Please update your launcher in order to launch this pack! This can be done by restarting your launcher, an update dialog will pop up.");
            return;
        }

        // check if user profile is selected
        if (users.getSelectedIndex() <= 1) {
            if (UserManager._users.size() == 0) {
                ProfileAdderDialog p = new ProfileAdderDialog(getInstance(), true);
                p.setVisible(true);
            } else {
                ErrorUtils.tossError("Please select a profile!");
                return;
            }
        }

        // check selected java is at least version specified in pack's XML
        JavaVersion minSup = JavaVersion.createJavaVersion(pack.getMinJRE());
        if (minSup.isOlder(java) || minSup.isSameVersion(java)) {
            Logger.logDebug("Selected user: saved password: " + (UserManager.getPassword(users.getSelectedItem().toString()).length() > 0));
            Logger.logDebug("Selected user: will save auth token if online: " + UserManager.getSaveMojangData(users.getSelectedItem().toString()));
            doLogin(UserManager.getUsername(users.getSelectedItem().toString()), UserManager.getPassword(users.getSelectedItem().toString()),
                    UserManager.getMojangData(users.getSelectedItem().toString()), UserManager.getName(users.getSelectedItem().toString()));
        } else {//user can't run pack-- JRE not high enough
            String message = "You must use at least java " + pack.getMinJRE() + " to play this pack! Please go to Options to get a link or Advanced Options enter a path.";
            ErrorUtils.tossError(message, message);
            return;
        }
    }

    public void swapTabs (boolean toMaps) {
        if (toMaps) {
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

    public static void setUpSystemTray () {
        trayMenu = new TrayMenu();

        SystemTray tray = SystemTray.getSystemTray();
        TrayIcon trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage(instance.getClass().getResource("/image/logo_ftb.png")));

        trayIcon.setPopupMenu(trayMenu);
        trayIcon.setToolTip(Constants.name);
        trayIcon.setImageAutoSize(true);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public static void main (String args[]) {
        Main.main(args);// just in case someone is launching w/ this as the main class
    }
}
