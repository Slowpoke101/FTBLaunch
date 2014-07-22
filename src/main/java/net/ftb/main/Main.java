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

import net.ftb.data.*;
import net.ftb.download.Locations;
import net.ftb.gui.LaunchFrameHelpers;
import net.ftb.gui.LauncherConsole;
import net.ftb.gui.dialogs.FirstRunDialog;
import net.ftb.gui.dialogs.LauncherUpdateDialog;
import net.ftb.gui.dialogs.LoadingDialog;
import net.ftb.locale.I18N;
import net.ftb.log.*;
import net.ftb.tracking.google.JGoogleAnalyticsTracker;
import net.ftb.updater.UpdateChecker;
import net.ftb.util.*;
import net.ftb.util.winreg.JavaInfo;
import net.ftb.workers.AuthlibDLWorker;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class Main {
    /**
     * Launch the application.
     * @param args - CLI arguments
     */
    public static void main (String[] args) {
        Benchmark.start("main");
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
        Logger.addListener(new StdOutLogger());
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
            if (!noConfig) {
                Logger.logError("Could not create LogWriters. Check your FTB installation location write access", e1);
            }
        }
        URL mf = LaunchFrame.class.getResource("/buildproperties.properties");
        int beta = 9999999;
        String mfStr = "";
        try {
            Properties props = new Properties();
            props.load(mf.openStream());
            mfStr  = props.getProperty("LauncherJenkins");
            if(!mfStr.equals("${LauncherJenkins}"))
                beta = Integer.parseInt(mfStr);
            Logger.logDebug("FTB Launcher CI Build #: " + beta + ", Git SHA: " + props.getProperty("Git-SHA"));
        } catch (Exception e) {
            Logger.logError("Error getting beta information, assuming beta channel not usable!", e);
            beta = 9999999;
        }
        final int beta_ = beta;
        /*
         *  Posts information about OS, JVM and launcher version into Google Analytics
         */
        AnalyticsConfigData.setUserAgent("Java/" + System.getProperty("java.version") + " (" + System.getProperty("os.name") + "; " + System.getProperty("os.arch") + ")");
        tracker = new JGoogleAnalyticsTracker(AnalyticsConfigData, JGoogleAnalyticsTracker.GoogleAnalyticsVersion.V_4_7_2);
        tracker.setEnabled(true);
        TrackerUtils.sendPageView("net/ftb/gui/LaunchFrame.java", "Launcher Start v" + Constants.version + "." + beta);
        if (!new File(OSUtils.getDynamicStorageLocation(), "FTBOSSent" + Constants.version + "." + beta + ".txt").exists()) {
            TrackerUtils.sendPageView("net/ftb/gui/LaunchFrame.java", "Launcher " + Constants.version + "." + beta + " OS " + OSUtils.getOSString());
            try {
                new File(OSUtils.getDynamicStorageLocation(), "FTBOSSent" + Constants.version + ".txt").createNewFile();
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
                loader = new LoadingDialog();
                loader.setModal(false);
                loader.setVisible(true);

                I18N.setupLocale();
                I18N.setLocale(Settings.getSettings().getLocale());

                if (noConfig) {
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

                if (!OSUtils.is64BitOS()) {
                    LaunchFrameHelpers.tossNag("launcher_32OS", I18N.getLocaleString("WARN_32BIT_OS"));
                }
                if (OSUtils.is64BitOS() && !Settings.getSettings().getCurrentJava().is64bits) {
                    LaunchFrameHelpers.tossNag("launcher_32java", I18N.getLocaleString("WARN_32BIT_JAVA"));
                }
                JavaInfo java = Settings.getSettings().getCurrentJava();
                if (java.getMajor() < 1 || (java.getMajor() == 1 && java.getMinor() < 7)) {
                    LaunchFrameHelpers.tossNag("launcher_java6", I18N.getLocaleString("WARN_JAVA6"));
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

                // Set up System Tray
                if (SystemTray.isSupported()) {
                    setUpSystemTray();
                } else {
                    Logger.logWarn("System Tray not supported");
                }

                /*
                 * Execute AuthlibDLWorker swingworker. done() will enable launch button as soon as possible
                 */
                AuthlibDLWorker authworker = new AuthlibDLWorker(OSUtils.getDynamicStorageLocation() + File.separator + "authlib" + File.separator, "1.5.13") {
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
                instance.setVisible(true);
                instance.toBack();

                instance.eventBus.register(frame.thirdPartyPane);
                instance.eventBus.register(frame.modPacksPane);
                instance.eventBus.register(this);

                ModPack.loadXml(getXmls());

                Map.addListener(frame.mapsPane);
                //				Map.loadAll();

                TexturePack.addListener(frame.tpPane);
                //				TexturePack.loadAll();


                /*
                 * Run UpdateChecker swingworker. done() will open LauncherUpdateDialog if needed
                 */
                UpdateChecker updateChecker = new UpdateChecker(Constants.buildNumber, minUsable, beta_) {
                    @Override
                    protected void done () {
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
}
