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
package net.ftb.main;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import net.ftb.data.*;
import net.ftb.download.Locations;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.LauncherConsole;
import net.ftb.gui.dialogs.FirstRunDialog;
import net.ftb.gui.dialogs.LauncherUpdateDialog;
import net.ftb.gui.dialogs.LoadingDialog;
import net.ftb.locale.I18N;
import net.ftb.log.*;
import net.ftb.tracking.google.AnalyticsConfigData;
import net.ftb.tracking.google.JGoogleAnalyticsTracker;
import net.ftb.updater.UpdateChecker;
import net.ftb.util.*;
import net.ftb.util.winreg.JavaInfo;
import net.ftb.workers.AuthlibDLWorker;

import com.google.common.eventbus.EventBus;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class Main {
    public static JGoogleAnalyticsTracker tracker;
    public static AnalyticsConfigData AnalyticsConfigData = new AnalyticsConfigData("UA-37330489-2");
    @Getter
    private static UserManager userManager;
    @Getter
    private static int beta;

    private static JCommander jc;

    /**
     * @return FTB Launcher event bus
     */
    @Getter
    private static EventBus eventBus = new EventBus();

    /**
     * Launch the application.
     * @param args - CLI arguments
     */
    public static void main (String[] args) {
        Benchmark.start("main");

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
        LogSource stdoutLogSource = CommandLineSettings.getSettings().isMcLogs()?LogSource.ALL:LogSource.LAUNCHER;

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
                Logger.logError("Could not create LogWriters. Check your FTB installation location write access", e1);
            }
        }
        URL mf = LaunchFrame.class.getResource("/buildproperties.properties");
        beta = 9999999;
        String mfStr = "";
        try {
            Properties props = new Properties();
            props.load(mf.openStream());
            mfStr = props.getProperty("LauncherJenkins");
            if (!mfStr.equals("${LauncherJenkins}"))
                beta = Integer.parseInt(mfStr);
            Logger.logDebug("FTB Launcher CI Build #: " + beta + ", Git SHA: " + props.getProperty("Git-SHA"));
        } catch (Exception e) {
            Logger.logError("Error getting beta information, assuming beta channel not usable!", e);
            beta = 9999999;
        }

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

    private static void mainGUI(String[] args) {
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
                    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
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
                LaunchFrame.loader = new LoadingDialog();
                LaunchFrame.loader.setModal(false);
                LaunchFrame.loader.setVisible(true);

                I18N.setupLocale();
                I18N.setLocale(Settings.getSettings().getLocale());

                if (Settings.getSettings().isNoConfig()) {
                    FirstRunDialog firstRunDialog = new FirstRunDialog();
                    firstRunDialog.setVisible(true);
                }

                LoadingDialog.setProgress(120);

                File installDir = new File(Settings.getSettings().getInstallPath());
                if (!installDir.exists()) {
                    installDir.mkdirs();
                }

                // CheckInstallPath() does Error/Warning logging in english
                CheckInstallPath checkResult = new CheckInstallPath(Settings.getSettings().getInstallPath(), true);
                if (checkResult.action == CheckInstallPath.Action.BLOCK || checkResult.action == CheckInstallPath.Action.WARN) {
                    // ErrorUtils.tossOKIgnoreDialog() does not write logs => can be called with localized strings
                    int result = ErrorUtils
                            .tossOKIgnoreDialog(checkResult.localizedMessage, (checkResult.action == CheckInstallPath.Action.BLOCK) ? JOptionPane.ERROR_MESSAGE : JOptionPane.WARNING_MESSAGE);
                    // pressing OK or closing dialog does not do anything
                    if (result != 0 && result != JOptionPane.CLOSED_OPTION) {
                        // if user select ignore we save setting and that type of error will be ignored
                        if (checkResult.setting != null) {
                            Settings.getSettings().setBoolean(checkResult.setting, true);
                            Settings.getSettings().save();
                        }
                    }
                }

                // Same warnings are logged as errors in MainHelpers.printInfo()
                if (!OSUtils.is64BitOS()) {
                    MainHelpers.tossNag("launcher_32OS", I18N.getLocaleString("WARN_32BIT_OS"));
                }
                if (OSUtils.is64BitOS() && !Settings.getSettings().getCurrentJava().is64bits) {
                    MainHelpers.tossNag("launcher_32java", I18N.getLocaleString("WARN_32BIT_JAVA"));
                }
                JavaInfo java = Settings.getSettings().getCurrentJava();
                if (java.getMajor() < 1 || (java.getMajor() == 1 && java.getMinor() < 7)) {
                    MainHelpers.tossNag("launcher_java6", I18N.getLocaleString("WARN_JAVA6"));
                }

                LoadingDialog.setProgress(130);

                // Store this in the cache (local) storage, since it's machine specific.
                userManager = new UserManager(new File(OSUtils.getCacheStorageLocation(), "logindata"), new File(OSUtils.getDynamicStorageLocation(), "logindata"));

                LoadingDialog.setProgress(140);

                if (!CommandLineSettings.getSettings().isNoConsole() && Settings.getSettings().getConsoleActive()) {
                    LaunchFrame.con = new LauncherConsole();
                    Logger.addListener(LaunchFrame.con);
                    LaunchFrame.con.refreshLogs();
                    LaunchFrame.con.setVisible(true);
                }

                MainHelpers.googleAnalytics();

                LoadingDialog.setProgress(160);

                LaunchFrame frame = new LaunchFrame(2);
                LaunchFrame.setInstance(frame);

                // Set up System Tray
                if (SystemTray.isSupported()) {
                    LaunchFrame.getInstance().setUpSystemTray();
                } else {
                    Logger.logDebug("System Tray not supported");
                }

                /*
                 * Execute AuthlibDLWorker swingworker. done() will enable launch button as soon as possible
                 */
                AuthlibDLWorker authworker = new AuthlibDLWorker(OSUtils.getDynamicStorageLocation() + File.separator + "authlib" + File.separator, "1.5.16") {
                    @Override
                    protected void done () {
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
                frame.setVisible(true);
                frame.toBack();

                eventBus.register(frame.thirdPartyPane);
                eventBus.register(frame.modPacksPane);
                eventBus.register(this);

                ModPack.loadXml(getXmls());

                Map.addListener(frame.mapsPane);
                //				Map.loadAll();

                TexturePack.addListener(frame.tpPane);
                //				TexturePack.loadAll();


                /*
                 * Run UpdateChecker swingworker. done() will open LauncherUpdateDialog if needed
                 */
                final int beta_ = beta;
                UpdateChecker updateChecker = new UpdateChecker(Constants.buildNumber, LaunchFrame.getInstance().minUsable, beta_) {
                    @Override
                    protected void done () {
                        try {
                            if (get()) {
                                LauncherUpdateDialog p = new LauncherUpdateDialog(this, LaunchFrame.getInstance().minUsable);
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
