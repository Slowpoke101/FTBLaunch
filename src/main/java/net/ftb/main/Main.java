/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2018, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
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
package net.ftb.main;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.common.eventbus.EventBus;
import lombok.Getter;
import lombok.Setter;
import net.ftb.data.CommandLineSettings;
import net.ftb.data.Constants;
import net.ftb.data.Map;
import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.data.TexturePack;
import net.ftb.data.UserManager;
import net.ftb.download.Locations;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.LauncherConsole;
import net.ftb.gui.dialogs.FirstRunDialog;
import net.ftb.gui.dialogs.LauncherUpdateDialog;
import net.ftb.gui.dialogs.LoadingDialog;
import net.ftb.gui.panes.OptionsPane;
import net.ftb.locale.I18N;
import net.ftb.log.LogLevel;
import net.ftb.log.LogSource;
import net.ftb.log.LogWriter;
import net.ftb.log.Logger;
import net.ftb.log.OutputOverride;
import net.ftb.log.StdOutLogger;
import net.ftb.tracking.google.AnalyticsConfigData;
import net.ftb.tracking.google.JGoogleAnalyticsTracker;
import net.ftb.updater.UpdateChecker;
import net.ftb.util.*;
import net.ftb.util.winreg.JavaInfo;
import net.ftb.util.winreg.JavaVersion;
import net.ftb.workers.AuthlibDLWorker;
import net.ftb.workers.RetiredPacksLoader;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;

public class Main {
    public static JGoogleAnalyticsTracker tracker;
    public static AnalyticsConfigData AnalyticsConfigData = new AnalyticsConfigData("UA-37330489-2");
    @Getter
    private static UserManager userManager;
    @Getter
    private static int beta;
    @Setter
    @Getter
    private static boolean authlibReadyToUse = false;

    private static JCommander jc;

    /**
     * @return FTB Launcher event bus
     */
    @Getter
    private static EventBus eventBus = new EventBus();
    @Getter
    private static boolean disableLaunchButton = false;

    /**
     * Launch the application.
     * @param args - CLI arguments
     */
    public static void main (String[] args) {
        Benchmark.start("main");

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException (Thread t, Throwable e) {
                Logger.logError("Unhandled exception in " + t.toString(), e);
            }
        });

        try {
            jc = new JCommander(CommandLineSettings.getSettings(), args);
        } catch (ParameterException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }

        if (CommandLineSettings.getSettings().isHelp()) {
            jc.setProgramName("FTB_Launcher.jar");
            jc.usage();
            System.exit(0);
        }

        /*
         *  Create dynamic storage location as soon as possible
         */
        OSUtils.createStorageLocations();

        // Use IPv4 when possible, only use IPv6 when connecting to IPv6 only addresses
        System.setProperty("java.net.preferIPv4Stack", "true");

        if (Settings.getSettings().getUseSystemProxy()) {
            // Use system default proxy settings
            System.setProperty("java.net.useSystemProxies", "true");
        }

        if (new File(Settings.getSettings().getInstallPath(), Locations.launcherLogFile).exists()) {
            new File(Settings.getSettings().getInstallPath(), Locations.launcherLogFile).delete();
        }

        if (new File(Settings.getSettings().getInstallPath(), Locations.minecraftLogFile).exists()) {
            new File(Settings.getSettings().getInstallPath(), Locations.minecraftLogFile).delete();
        }

        /*
         * Create new StdoutLogger as soon as possible
         */
        int logLevel = CommandLineSettings.getSettings().getVerbosity();
        LogLevel stdoutLogLevel = LogLevel.values()[logLevel];
        LogSource stdoutLogSource = CommandLineSettings.getSettings().isMcLogs() ? LogSource.ALL : LogSource.LAUNCHER;

        Logger.addListener(new StdOutLogger(stdoutLogLevel, stdoutLogSource));
        /*
         * Setup System.out and System.err redirection as soon as possible
         */
        System.setOut(new OutputOverride(System.out, LogLevel.INFO));
        System.setErr(new OutputOverride(System.err, LogLevel.ERROR));

        /*
         * Setup LogWriters as soon as possible
         * At first run log will be created same directory with launcher
         */
        try {
            Logger.addListener(new LogWriter(new File(Settings.getSettings().getInstallPath(), Locations.launcherLogFile), LogSource.LAUNCHER));
            Logger.addListener(new LogWriter(new File(Settings.getSettings().getInstallPath(), Locations.minecraftLogFile), LogSource.EXTERNAL));
        } catch (IOException e1) {
            if (!Settings.getSettings().isNoConfig()) {
                Logger.logDebug("Could not create LogWriters.", e1);
                Logger.logError("Check your FTB installation location's write access. Launch button is disabled until installation location is fixed.");
                Main.disableLaunchButton = true;
            }
        }
        Logger.logDebug("Launcher arguments: " + Arrays.toString(args));
        Logger.logDebug("Launcher PID: " + OSUtils.getPID());
        URL mf = LaunchFrame.class.getResource("/buildproperties.properties");
        beta = 9999999;
        String mfStr = "";
        try {
            Properties props = new Properties();
            props.load(mf.openStream());
            mfStr = props.getProperty("LauncherJenkins");
            if (!mfStr.equals("${LauncherJenkins}")) {
                beta = Integer.parseInt(mfStr);
            }
            Logger.logDebug("FTB Launcher CI Build #: " + beta + ", Git SHA: " + props.getProperty("Git-SHA"));
        } catch (Exception e) {
            Logger.logError("Check your launcher binary's path. It might contain unsupported characters");
            Logger.logError("Error getting beta information, assuming beta channel not usable!", e);
            beta = 9999999;
        }

        System.setProperty("http.agent", "FTB Launcher/" + Constants.version);

        LetsEncryptFix.fix();

        /*
         *  Posts information about OS, JVM and launcher version into Google Analytics
         */
        AnalyticsConfigData.setUserAgent("Java/" + System.getProperty("java.version") + " (" + System.getProperty("os.name") + "; " + System.getProperty("os.arch") + ")");
        tracker = new JGoogleAnalyticsTracker(AnalyticsConfigData, JGoogleAnalyticsTracker.GoogleAnalyticsVersion.V_4_7_2);
        tracker.setEnabled(true);
        TrackerUtils.sendPageView("net/ftb/gui/LaunchFrame.java", "Launcher Start / " + Constants.version + "." + beta);
        if (!new File(OSUtils.getDynamicStorageLocation(), "FTBOSSent" + Constants.version + "." + beta + ".txt").exists()) {
            TrackerUtils.sendPageView("net/ftb/gui/LaunchFrame.java", "Launcher " + Constants.version + "." + beta + " OS " + OSUtils.getOSString());
            try {
                new File(OSUtils.getDynamicStorageLocation(), "FTBOSSent" + Constants.version + ".txt").createNewFile();
            } catch (IOException e) {
                Logger.logError("Error creating os cache text file");
            }
        }

        MainHelpers.printInfo();

        /*
         * Resolves servers in background thread
         */
        DownloadUtils thread = new DownloadUtils();
        thread.start();

        // later add other main()s for 100% headless and CLI clients
        mainGUI(args);
    }

    private static void mainGUI (String[] args) {
        /*
         * Setup GUI style & create and show Splash screen in EDT
         * NEVER add code with Thread.sleep() or I/O blocking, including network usage in EDT
         *  => If this guideline is followed then GUI should work smoothly
         */
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run () {
                try {
                    String path = new File(LaunchFrame.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getCanonicalPath();
                    path = URLDecoder.decode(path, "UTF-8");
                    if (path.contains("!"))
                        ErrorUtils.tossError("Warning current location of the launcher binary contains character: \"!\", \n"
                                + "which is not supported. Please move launcher binary and try again");
                } catch (Exception e) {
                    Logger.logError("Couldn't get path to current launcher jar/exe", e);
                }

                StyleUtil.loadUiStyles();
                try {
                    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                        if ("Nimbus".equals(info.getName())) {
                            UIManager.setLookAndFeel(info.getClassName());
                            break;
                        }
                    }
                    if (OSUtils.getCurrentOS()==OSUtils.OS.MACOSX) {
                        InputMap im = (InputMap) UIManager.get("TextField.focusInputMap");
                        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_DOWN_MASK), DefaultEditorKit.copyAction);
                        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_DOWN_MASK), DefaultEditorKit.pasteAction);
                        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_DOWN_MASK), DefaultEditorKit.cutAction);
                        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.META_DOWN_MASK), DefaultEditorKit.selectAllAction);
                    }
                } catch (Exception e) {
                    try {
                        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                    } catch (Exception e1) {
                    }
                }
                LaunchFrame.loader = new LoadingDialog();
                LaunchFrame.loader.setVisible(true);
                LaunchFrame.loader.toFront();
            }
        });

        I18N.setupLocale();
        I18N.setLocale(Settings.getSettings().getLocale());

        if (Settings.getSettings().isNoConfig() && !CommandLineSettings.getSettings().isSkipFirst()) {
            Logger.logDebug("FirstRunDialog");
            try {
                EventQueue.invokeAndWait(new Runnable() {
                    @Override
                    public void run () {
                        FirstRunDialog firstRunDialog = new FirstRunDialog();
                        firstRunDialog.setVisible(true);
                    }
                });
            } catch (Exception e) {
                Logger.logDebug("failed", e.getCause());
            }
        } else if (CommandLineSettings.getSettings().isSkipFirst()) {
            String installDir = CommandLineSettings.getSettings().getInstallDir();
            if (installDir == null) {
                Logger.logWarn("Bad command line argument combination. Please, use both --pack-dir and --skip-first");
            } else {
                Settings.getSettings().setInstallPath(installDir);
                Settings.getSettings().save();
            }
        }

        // NOTE: this messagage will be missed because laoder is not created when this is executed
        // should we invokeAndWait when creating LoadingDialog?
        // if we wait other things in main thread will be executed later
        LoadingDialog.advance("Checking installation location");

        File installDir = new File(Settings.getSettings().getInstallPath());
        if (!installDir.exists()) {
            installDir.mkdirs();
        }

        // CheckInstallPath() does Error/Warning logging in english
        final CheckInstallPath checkResult = new CheckInstallPath(Settings.getSettings().getInstallPath(), true);
        if (!CommandLineSettings.getSettings().isDisableInstallLocChecks() &&
                (checkResult.action == CheckInstallPath.Action.BLOCK || checkResult.action == CheckInstallPath.Action.WARN))
        {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override public void run () {
                        ErrorUtils.showClickableMessage(checkResult.localizedMessage, JOptionPane.ERROR_MESSAGE);
                    }
                });
            } catch (Exception e) {
                Logger.logDebug("failed", e.getCause());
            }
        }

        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override public void run () {
                    // Same warnings are logged as errors in MainHelpers.printInfo()
                    if (!OSUtils.is64BitOS() && !CommandLineSettings.getSettings().isDisableJVMBitnessCheck()) {
                        ErrorUtils.showClickableMessage(I18N.getLocaleString("WARN_32BIT_OS"), JOptionPane.WARNING_MESSAGE);
                    }
                    if (OSUtils.is64BitOS() && !Settings.getSettings().getCurrentJava().is64bits && !CommandLineSettings.getSettings().isDisableJVMBitnessCheck() ) {
                        ErrorUtils.showClickableMessage(I18N.getLocaleString("WARN_32BIT_JAVA"), JOptionPane.WARNING_MESSAGE);
                    }
                    JavaInfo java = Settings.getSettings().getCurrentJava();
                    JavaVersion java7 = JavaVersion.createJavaVersion("1.7.0");
                    if (java.isOlder(java7) && !CommandLineSettings.getSettings().isDisableJVMVersionCheck()) {
                        ErrorUtils.showClickableMessage(I18N.getLocaleString("WARN_JAVA6"), JOptionPane.WARNING_MESSAGE);
                    }
                }
            });
        } catch (Exception e) {
            Logger.logDebug("failed", e.getCause());
        }
        // NOTE: this is also missed
        LoadingDialog.advance("Loading user data");

        ModPack.loadXml(getXmls());

        // not good location for this. Loader must wait until other packs are loaded....
        try {
            RetiredPacksLoader retiredPacksLoader = new RetiredPacksLoader(new URL(Locations.masterRepo + "/FTB2/static/hiddenpacks.json"),
                    OSUtils.getCacheStorageLocation(), Settings.getSettings().getInstallPath());
            retiredPacksLoader.start();
        } catch (Exception e) {
            Logger.logDebug("RetiredPacksLoader failed", e);
        }

        // Store this in the cache (local) storage, since it's machine specific.
        userManager = new UserManager(new File(OSUtils.getCacheStorageLocation(), "logindata"), new File(OSUtils.getDynamicStorageLocation(), "logindata"));

        /*
         * Execute AuthlibDLWorker swingworker. done() will enable launch button as soon as possible
         */
        AuthlibDLWorker authworker = new AuthlibDLWorker(OSUtils.getDynamicStorageLocation() + File.separator + "authlib" + File.separator, "1.5.22") {
            @Override
            protected void done () {
                boolean workerSuccess = true;
                try {
                    workerSuccess = get();
                } catch (InterruptedException e) {
                    Logger.logDebug("Swingworker Exception", e);
                } catch (ExecutionException e) {
                    Logger.logDebug("Swingworker Exception", e.getCause());
                }

                if (!workerSuccess) {
                    ErrorUtils.tossError("No usable authlib available. Please check your firewall rules and network connection. Can't start MC without working authlib. Launch button will be disabled.");
                }

                if (workerSuccess && disableLaunchButton == false) {
                    LaunchFrame.getInstance().getLaunch().setEnabled(true);
                }
            }
        };
        authworker.execute();

        LoadingDialog.advance("Creating Console window");

        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run () {
                if (!CommandLineSettings.getSettings().isNoConsole() && Settings.getSettings().getConsoleActive()) {
                    LaunchFrame.con = new LauncherConsole();
                    Logger.addListener(LaunchFrame.con);
                    LaunchFrame.con.refreshLogs();
                    LaunchFrame.con.setVisible(true);
                }
            }
        });

        MainHelpers.googleAnalytics();
        LoadingDialog.advance("Creating main window");

        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override public void run () {
                    LaunchFrame frame = new LaunchFrame(2);
                    LaunchFrame.setInstance(frame);

                    // Set up System Tray
                    if (SystemTray.isSupported() && !CommandLineSettings.getSettings().isDisableTray()) {
                        LaunchFrame.getInstance().setUpSystemTray();
                    } else {
                        Logger.logDebug("System Tray not supported");
                    }
                }
            });
        } catch (InvocationTargetException e) {
            Logger.logDebug("failed", e.getCause());
        } catch (InterruptedException e) {
        }

        LoadingDialog.advance("Setting up Launcher");

        /*
         * Show the main form but hide it behind any active windows until
         * loading is complete to prevent display issues.
         *
         * @TODO ModpacksPane has a display issue with packScroll if the
         * main form is not visible when constructed.
         */
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run () {
                //LaunchFrame.getInstance().setVisible(true);
                //LaunchFrame.getInstance().toBack();
            }
        });

        eventBus.register(LaunchFrame.getInstance().thirdPartyPane);
        eventBus.register(LaunchFrame.getInstance().modPacksPane);

        //ModPack.loadXml(getXmls());

        Map.addListener(LaunchFrame.getInstance().mapsPane);
        TexturePack.addListener(LaunchFrame.getInstance().tpPane);


        /*
         * Run UpdateChecker swingworker. done() will open LauncherUpdateDialog if needed
         */
        int beta_ = beta;
        int v = CommandLineSettings.getSettings().getManualVersion();
        int b = CommandLineSettings.getSettings().getManualBuildNumber();
        UpdateChecker updateChecker = new UpdateChecker(
                (v == 0  ? Constants.buildNumber : v),
                LaunchFrame.getInstance().minUsable,
                (b == 0 ? beta_ : b)
        ) {
            @Override
            protected void done () {
                try {
                    if (get()) {
                        LauncherUpdateDialog p = new LauncherUpdateDialog(this, LaunchFrame.getInstance().minUsable);
                        p.setVisible(true);
                    }
                } catch (InterruptedException e) {
                    Logger.logDebug("Swingworker Exception", e);
                } catch (ExecutionException e) {
                    Logger.logDebug("Swingworker Exception", e.getCause());
                }
            }
        };
        updateChecker.execute();
        LoadingDialog.advance("Downloading pack data");
        OptionsPane.exportData();
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
}
