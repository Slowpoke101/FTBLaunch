package net.ftb.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.ProgressMonitor;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.ftb.data.LoginResponse;
import net.ftb.data.Map;
import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.data.UserManager;
import net.ftb.gui.dialogs.LauncherUpdateDialog;
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
import net.ftb.tools.MapManager;
import net.ftb.tools.MinecraftVersionDetector;
import net.ftb.tools.ModManager;
import net.ftb.tools.ProcessMonitor;
import net.ftb.tools.TextureManager;
import net.ftb.updater.UpdateChecker;
import net.ftb.util.DownloadUtils;
import net.ftb.util.ErrorUtils;
import net.ftb.util.FileUtils;
import net.ftb.util.OSUtils;
import net.ftb.workers.GameUpdateWorker;
import net.ftb.workers.LoginWorker;

public class LaunchFrame extends JFrame {
	private LoginResponse RESPONSE;
	private NewsPane newsPane;
	public static JPanel panel;
	private JPanel footer = new JPanel();
	private JLabel footerLogo = new JLabel(new ImageIcon(this.getClass().getResource("/image/logo_ftb.png")));
	private JLabel footerCreeper = new JLabel(new ImageIcon(this.getClass().getResource("/image/logo_creeperHost.png")));
	private JLabel tpInstallLocLbl = new JLabel();
	private JButton launch = new JButton(), edit = new JButton(), donate = new JButton(), serverbutton = new JButton(), mapInstall = new JButton(), serverMap = new JButton(), tpInstall = new JButton();

	private static String[] dropdown_ = {"Select Profile", "Create Profile"};
	private static JComboBox users, tpInstallLocation, mapInstallLocation;
	private static LaunchFrame instance = null;
	private static String version = "1.1.8";
	private static final long serialVersionUID = 1L;

	public final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);	

	protected static UserManager userManager;

	public ModpacksPane modPacksPane;
	public MapsPane mapsPane;
	public TexturepackPane tpPane;
	public OptionsPane optionsPane;

	public static int buildNumber = 118;
	public static LauncherConsole con;
	public static String tempPass = "";
	public static Panes currentPane = Panes.MODPACK;

	public static final String FORGENAME = "MinecraftForge.zip";

	protected enum Panes {
		NEWS,
		OPTIONS,
		MODPACK,
		MAPS,
		TEXTURE
	}

	/**
	 * Launch the application.
	 * @param args - CLI arguments
	 */
	public static void main(String[] args) {
		if(new File(Settings.getSettings().getInstallPath(), "FTBLauncherLog.txt").exists()) {
			new File(Settings.getSettings().getInstallPath(), "FTBLauncherLog.txt").delete();
		}
		if(new File(Settings.getSettings().getInstallPath(), "MinecraftLog.txt").exists()) {
			new File(Settings.getSettings().getInstallPath(), "MinecraftLog.txt").delete();
		}

		DownloadUtils thread = new DownloadUtils();
		thread.start();

		Logger.logInfo("FTBLaunch starting up (version "+ version + ")");
		Logger.logInfo("Java version: "+System.getProperty("java.version"));
		Logger.logInfo("Java vendor: "+System.getProperty("java.vendor"));
		Logger.logInfo("Java home: "+System.getProperty("java.home"));
		Logger.logInfo("Java specification: " + System.getProperty("java.vm.specification.name") + " version: " +
				System.getProperty("java.vm.specification.version") + " by " + System.getProperty("java.vm.specification.vendor"));
		Logger.logInfo("Java vm: "+System.getProperty("java.vm.name") + " version: " + System.getProperty("java.vm.version") + " by " + System.getProperty("java.vm.vendor"));
		Logger.logInfo("OS: "+System.getProperty("os.arch") + " " + System.getProperty("os.name") + " " + System.getProperty("os.version"));

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				Color baseColor = new Color(40, 40, 40);
				UIManager.put("control", baseColor);
				UIManager.put("text", baseColor.brighter().brighter().brighter().brighter().brighter());
				UIManager.put("nimbusBase", new Color(0, 0, 0));
				UIManager.put("nimbusFocus", baseColor);
				UIManager.put("nimbusBorder", baseColor);
				UIManager.put("nimbusLightBackground", baseColor);
				UIManager.put("info", baseColor.brighter().brighter());
				UIManager.put("nimbusSelectionBackground", baseColor.brighter().brighter());
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
					} catch (Exception e1) { }
				}
				I18N.setupLocale();
				I18N.setLocale(Settings.getSettings().getLocale());

				File installDir = new File(Settings.getSettings().getInstallPath());
				if (!installDir.exists()) {
					installDir.mkdirs();
				}
				File dynamicDir = new File(OSUtils.getDynamicStorageLocation());
				if(!dynamicDir.exists()) {
					dynamicDir.mkdirs();
				}

				userManager = new UserManager(new File(OSUtils.getDynamicStorageLocation(), "logindata"));
				con = new LauncherConsole();
				if(Boolean.parseBoolean(Settings.getSettings().getConsoleActive())) {
					con.setVisible(true);
				}

				LaunchFrame frame = new LaunchFrame(2);
				instance = frame;
				frame.setVisible(true);

				Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
					@Override
					public void uncaughtException(Thread t, Throwable e) {
						Logger.logError("Unhandled exception in " + t.toString(), e);
					}
				});

				ModPack.addListener(frame.modPacksPane);
				ModPack.loadXml(getXmls());
				
				Map.addListener(frame.mapsPane);
				Map.loadAll();

				//				TexturePack.addListener(frame.tpPane);
				//				TexturePack.loadAll();

				UpdateChecker updateChecker = new UpdateChecker(buildNumber);
				if(updateChecker.shouldUpdate()){
					LauncherUpdateDialog p = new LauncherUpdateDialog(updateChecker);
					p.setVisible(true);
				}
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
		setBounds(100, 100, 850, 480);
		panel.setBounds(0, 0, 850, 480);
		panel.setLayout(null);
		footer.setBounds(0, 380, 850, 100);
		footer.setLayout(null);
		footer.setBackground(new Color(25, 25, 25));
		tabbedPane.setBounds(0, 0, 850, 380);
		panel.add(tabbedPane);
		panel.add(footer);
		setContentPane(panel);

		//Footer
		footerLogo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		footerLogo.setBounds(20, 20, 42, 42);
		footerLogo.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent event) {
				OSUtils.browse("http://www.feed-the-beast.com");
			}
			@Override public void mouseReleased(MouseEvent arg0) { }
			@Override public void mousePressed(MouseEvent arg0) { }
			@Override public void mouseExited(MouseEvent arg0) { }
			@Override public void mouseEntered(MouseEvent arg0) { }
		});

		footerCreeper.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		footerCreeper.setBounds(72, 20, 132, 42);
		footerCreeper.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent event) {
				OSUtils.browse("http://www.creeperhost.net/aff.php?aff=293");
			}
			@Override public void mouseReleased(MouseEvent arg0) { }
			@Override public void mousePressed(MouseEvent arg0) { }
			@Override public void mouseExited(MouseEvent arg0) { }
			@Override public void mouseEntered(MouseEvent arg0) { }
		});

		dropdown_[0] = I18N.getLocaleString("PROFILE_SELECT");
		dropdown_[1] = I18N.getLocaleString("PROFILE_CREATE");

		String[] dropdown = merge(dropdown_, UserManager.getNames().toArray(new String[] {}));
		users = new JComboBox(dropdown);
		if(Settings.getSettings().getLastUser() != null) {
			for(int i = 0; i < dropdown.length; i++) {
				if(dropdown[i].equalsIgnoreCase(Settings.getSettings().getLastUser())) {
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
			public void actionPerformed(ActionEvent e) {
			}
		});

		users.setBounds(550, 20, 150, 30);
		users.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(users.getSelectedIndex() == 1) {
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
			public void actionPerformed(ActionEvent event) {
				if(users.getSelectedIndex() > 1) {
					ProfileEditorDialog p = new ProfileEditorDialog(getInstance(), (String)users.getSelectedItem(), true);
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
			public void actionPerformed(ActionEvent arg0) {
				if(users.getSelectedIndex() > 1 && modPacksPane.packPanels.size() > 0) {
					Settings.getSettings().setLastPack(ModPack.getSelectedPack().getDir());
					saveSettings();
					doLogin(UserManager.getUsername(users.getSelectedItem().toString()), UserManager.getPassword(users.getSelectedItem().toString()));
				} else if(users.getSelectedIndex() <= 1) {
					ErrorUtils.tossError("Please select a profile!");
				}
			}
		});

		serverbutton.setBounds(480, 20, 330, 30);
		serverbutton.setText(I18N.getLocaleString("DOWNLOAD_SERVER_PACK"));
		serverbutton.setVisible(false);
		serverbutton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if(!ModPack.getSelectedPack().getServerUrl().isEmpty()) {
					if(modPacksPane.packPanels.size() > 0 && getSelectedModIndex() >= 0) {
						try {
							String version = (Settings.getSettings().getPackVer().equalsIgnoreCase("newest version")) ? ModPack.getSelectedPack().getVersion().replace(".", "_") : Settings.getSettings().getPackVer().replace(".", "_");
							OSUtils.browse(DownloadUtils.getCreeperhostLink("modpacks%5E" + ModPack.getSelectedPack().getDir() + "%5E" + version + "%5E" + ModPack.getSelectedPack().getServerUrl()));
						} catch (NoSuchAlgorithmException e) { }
					}
				}
			}
		});

		mapInstall.setBounds(650, 20, 160, 30);
		mapInstall.setText(I18N.getLocaleString("INSTALL_MAP"));
		mapInstall.setVisible(false);
		mapInstall.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(mapsPane.mapPanels.size() > 0 && getSelectedMapIndex() >= 0) {
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
			public void actionPerformed(ActionEvent event) {
				if(mapsPane.mapPanels.size() > 0 && getSelectedMapIndex() >= 0) {
					try {
						OSUtils.browse(DownloadUtils.getCreeperhostLink(Map.getMap(LaunchFrame.getSelectedMapIndex()).getUrl()));
					} catch (NoSuchAlgorithmException e) { }
				}
			}
		});

		tpInstall.setBounds(650, 20, 160, 30);
		tpInstall.setText(I18N.getLocaleString("INSTALL_TEXTUREPACK"));
		tpInstall.setVisible(false);
		tpInstall.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				TextureManager.installDir = (String)tpInstallLocation.getSelectedItem();
				TextureManager man = new TextureManager(new JFrame(), true);
				man.setVisible(true);
				TextureManager.cleanUp();
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
		optionsPane = new OptionsPane();

		getRootPane().setDefaultButton(launch);
		updateLocale();

		tabbedPane.add(newsPane, 0);
		tabbedPane.add(optionsPane, 1);
		tabbedPane.add(modPacksPane, 2);
		tabbedPane.add(mapsPane, 3);
		tabbedPane.add(tpPane, 4);
		setTabbedPaneIcons();
		tabbedPane.setEnabledAt(4, false);
		tabbedPane.setSelectedIndex(tab);

		tabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event) {
				if(tabbedPane.getSelectedComponent() instanceof ILauncherPane) {
					((ILauncherPane)tabbedPane.getSelectedComponent()).onVisible();
					currentPane = Panes.values()[tabbedPane.getSelectedIndex()];
					updateFooter();
				}
			}
		});
	}
	
	public void setTabbedPaneIcons() {
		tabbedPane.setIconAt(0, new ImageAndTextIcon(this.getClass().getResource("/image/tabs/news.png"), getUnreadNews()));
		tabbedPane.setIconAt(1, new ImageIcon(this.getClass().getResource("/image/tabs/options.png")));
		tabbedPane.setIconAt(2, new ImageIcon(this.getClass().getResource("/image/tabs/modpacks.png")));
		tabbedPane.setIconAt(3, new ImageIcon(this.getClass().getResource("/image/tabs/modpacks.png")));
		tabbedPane.setIconAt(4, new ImageIcon(this.getClass().getResource("/image/tabs/texturepacks.png")));
	}

	/**
	 * call this to login
	 */
	public void doLogin(final String username, String password) {
		if(password.isEmpty()) {
			PasswordDialog p = new PasswordDialog(this, true);
			p.setVisible(true);
			if(tempPass.isEmpty()){
				enableObjects();
				return;
			}
			password = tempPass;
		}

		Logger.logInfo("Logging in...");

		tabbedPane.setEnabledAt(0, false);
		tabbedPane.setEnabledAt(1, false);
		tabbedPane.setEnabledAt(2, false);
		tabbedPane.setEnabledAt(3, false);
		//		tabbedPane.setEnabledAt(4, false);
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
			public void done() {
				String responseStr;
				try {
					responseStr = get();
				} catch (InterruptedException err) {
					Logger.logError(err.getMessage(), err);
					enableObjects();
					return;
				} catch (ExecutionException err) {
					if(err.getCause() instanceof IOException || err.getCause() instanceof MalformedURLException) {
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
					if(responseStr.contains(":")) {
						Logger.logError("Received invalid response from server.");
					} else {
						if(responseStr.equalsIgnoreCase("bad login")) {
							ErrorUtils.tossError("Invalid username or password.");
						} else if(responseStr.equalsIgnoreCase("old version")) {
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

	/**
	 * checks whether an update is needed, and then starts the update process off
	 * @param response - the response from the minecraft servers
	 */
	public void runGameUpdater(final LoginResponse response) {
		final String installPath = Settings.getSettings().getInstallPath();
		final ModPack pack = ModPack.getSelectedPack();
		if(Settings.getSettings().getForceUpdate() && new File(installPath, pack.getDir() + File.separator + "version").exists()) {
			new File(installPath, pack.getDir() + File.separator + "version").delete();
		}
		if(!initializeMods()) {
			enableObjects();
			return;
		}
		MinecraftVersionDetector mvd = new MinecraftVersionDetector();
		if(!new File(installPath, pack.getDir() + "/minecraft/bin/minecraft.jar").exists() || mvd.shouldUpdate(installPath + "/" + pack.getDir() + "/minecraft")) {
			final ProgressMonitor progMonitor = new ProgressMonitor(this, "Downloading minecraft...", "", 0, 100);
			final GameUpdateWorker updater = new GameUpdateWorker(pack.getMcVersion(), new File(installPath, pack.getDir() + "/minecraft/bin").getPath()) {
				@Override
				public void done() {
					progMonitor.close();
					try {
						if(get()) {
							Logger.logInfo("Game update complete");
							FileUtils.killMetaInf();
							launchMinecraft(installPath + "/" + pack.getDir() + "/minecraft", RESPONSE.getUsername(), RESPONSE.getSessionID());
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
				public void propertyChange(PropertyChangeEvent evt) {
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
			launchMinecraft(installPath + "/" + pack.getDir() + "/minecraft", RESPONSE.getUsername(), RESPONSE.getSessionID());
		}
	}

	/**
	 * @param filename - what to save it as on the system
	 * @param urlString - the url to download
	 * @throws IOException - various
	 */
	public void downloadUrl(String filename, String urlString) throws IOException {
		BufferedInputStream in = null;
		FileOutputStream fout = null;
		try {
			in = new BufferedInputStream(new URL(urlString).openStream());
			fout = new FileOutputStream(filename);
			byte data[] = new byte[1024];
			int count;
			while ((count = in.read(data, 0, 1024)) != -1) {
				fout.write(data, 0, count);
			}
		} finally {
			if (in != null) {
				in.close();
			}
			if (fout != null) {
				fout.flush();
				fout.close();
			}	
		}
	}

	/**
	 * launch the game with the mods in the classpath
	 * @param workingDir - install path
	 * @param username - the MC username
	 * @param password - the MC password
	 */
	public void launchMinecraft(String workingDir, String username, String password) {
		try {
			Process minecraftProcess = MinecraftLauncher.launchMinecraft(workingDir, username, password, FORGENAME, Settings.getSettings().getRamMax());
			StreamLogger.start(minecraftProcess.getInputStream(), new LogEntry().level(LogLevel.UNKNOWN));
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) { }
			try {
				minecraftProcess.exitValue();
			} catch (IllegalThreadStateException e) {
				this.setVisible(false);
				ProcessMonitor.create(minecraftProcess, new Runnable() {
					@Override
					public void run() {
						System.exit(0);
					}
				});
			}
		} catch(Exception e) { }
	}

	/**
	 * @param modPackName - The pack to install (should already be downloaded)
	 * @throws IOException
	 */
	protected void installMods(String modPackName) throws IOException {
		String installpath = Settings.getSettings().getInstallPath();
		String temppath = OSUtils.getDynamicStorageLocation();
		ModPack pack = ModPack.getPack(modPacksPane.getSelectedModIndex());
		Logger.logInfo("dirs mk'd");
		File source = new File(temppath, "ModPacks/" + pack.getDir() + "/.minecraft");
		if(!source.exists()) {
			source = new File(temppath, "ModPacks/" + pack.getDir() + "/minecraft");
		}
		FileUtils.copyFolder(source, new File(installpath, pack.getDir() + "/minecraft/"));
		FileUtils.copyFolder(new File(temppath, "ModPacks/" + pack.getDir() + "/instMods/"), new File(installpath, pack.getDir() + "/instMods/"));
	}

	/**
	 * "Saves" the settings from the GUI controls into the settings class.
	 */
	public void saveSettings() {
		Settings.getSettings().setLastUser(String.valueOf(users.getSelectedItem()));
		instance.optionsPane.saveSettingsInto(Settings.getSettings());
	}

	/**
	 * @param user - user added/edited
	 */
	public static void writeUsers(String user) {
		try {
			userManager.write();
		} catch (IOException e) { }
		String[] usernames = merge(dropdown_, UserManager.getNames().toArray(new String[]{}));
		users.removeAllItems();
		for(int i = 0; i < usernames.length; i++) {
			users.addItem(usernames[i]);
			if(usernames[i].equals(user)) {
				users.setSelectedIndex(i);
			}
		}
	}

	/**
	 * updates the tpInstall to the available ones
	 * @param locations - the available locations to install the tp to
	 */
	public static void updateTpInstallLocs(String[] locations) {
		tpInstallLocation.removeAllItems();
		for(int i = 0; i < locations.length; i++) {
			tpInstallLocation.addItem(locations[i]);
		}
	}

	/**
	 * updates the mapInstall to the available ones
	 * @param locations - the available locations to install the map to
	 */
	public static void updateMapInstallLocs(String[] locations) {
		mapInstallLocation.removeAllItems();
		for(int i = 0; i < locations.length; i++) {
			mapInstallLocation.addItem(locations[i]);
		}
	}

	/**
	 * @param A - First string array
	 * @param B - Second string array
	 * @return - Outputs resulting merged string array from the passed arrays
	 */
	public static String[] merge(String[] A, String[] B) {
		String[] merged = new String[A.length+B.length];
		System.arraycopy(A, 0, merged, 0, A.length);
		System.arraycopy(B, 0, merged, A.length, B.length);
		return merged;
	}

	/**
	 * @return - Outputs selected modpack index
	 */
	public static int getSelectedModIndex() {
		return instance.modPacksPane.getSelectedModIndex();
	}

	/**
	 * @return - Outputs selected map index
	 */
	public static int getSelectedMapIndex() {
		return instance.mapsPane.getSelectedMapIndex();
	}

	/**
	 * @return - Outputs selected texturepack index
	 */
	public static int getSelectedTexturePackIndex() {
		return instance.tpPane.getSelectedTexturePackIndex();
	}

	/**
	 * @return - Outputs selected map install index
	 */
	public static int getSelectedMapInstallIndex() {
		return instance.mapInstallLocation.getSelectedIndex();
	}

	/**
	 * @return - Outputs selected texturepack install index
	 */
	public static int getSelectedTPInstallIndex() {
		return instance.tpInstallLocation.getSelectedIndex();
	}

	/**
	 * @return - Outputs LaunchFrame instance
	 */
	public static LaunchFrame getInstance() {
		return instance;
	}

	/**
	 * Enables all items that are disabled upon launching
	 */
	private void enableObjects(){
		tabbedPane.setEnabledAt(0, true);
		tabbedPane.setEnabledAt(1, true);
		tabbedPane.setEnabledAt(2, true);
		tabbedPane.setEnabledAt(3, true);
		//		tabbedPane.setEnabledAt(4, true);
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
	}

	/**
	 * Download and install mods
	 * @return boolean - represents whether it was successful in initializing mods
	 */
	private boolean initializeMods() {
		Logger.logInfo(ModPack.getSelectedPack().getDir());
		ModManager man = new ModManager(new JFrame(), true);
		man.setVisible(true);
		if(man.erroneous) {
			return false;
		}
		try {
			installMods(ModPack.getSelectedPack().getDir());
			man.cleanUp();
		} catch (IOException e) { }
		return true;
	}

	/**
	 * disables the buttons that are usually active on the footer
	 */
	public void disableMainButtons() {
		serverbutton.setVisible(false);
		launch.setVisible(false);
		edit.setVisible(false);
		users.setVisible(false);
	}

	/**
	 * disables the footer buttons active when the modpack tab is selected
	 */
	public void disableMapButtons() {
		mapInstall.setVisible(false);
		mapInstallLocation.setVisible(false);
		serverMap.setVisible(false);
	}

	/**
	 * disables the footer buttons active when the texture pack tab is selected
	 */
	public void disableTextureButtons() {
		tpInstall.setVisible(false);
		tpInstallLocation.setVisible(false);
	}

	/**
	 * update the footer to the correct buttons for active tab
	 */
	public void updateFooter() {
		boolean result;
		switch(currentPane) {
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
			result = modPacksPane.type.equals("Server");
			launch.setVisible(!result);
			edit.setEnabled(users.getSelectedIndex() > 1);
			edit.setVisible(!result);
			users.setVisible(!result);
			serverbutton.setVisible(result);
			disableMapButtons();
			disableTextureButtons();
			break;
		}
	}

	// TODO: Make buttons dynamically sized.
	/**
	 * updates the buttons/text to language specific
	 */
	public void updateLocale() {
		if(I18N.currentLocale == Locale.deDE) {
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
		writeUsers((String)users.getSelectedItem());
		optionsPane.updateLocale();
		modPacksPane.updateLocale();
		mapsPane.updateLocale();
		tpPane.updateLocale();
	}
	
	private static String[] getXmls() {
		ArrayList<String> s = new ArrayList<String>();
		String[] privPacks = Settings.getSettings().getPrivatePacks();
		s.add("modpacks.xml");
		for(int i = 0; i < privPacks.length; i++) {
			if(!privPacks[i].equals("")) {
				s.add(privPacks[i] + ".xml");
			}
		}
		return s.toArray(new String[] {});
	}
	
	public String getUnreadNews() {
		int i = 0;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new URL("http://launcher.feed-the-beast.com/newsupdate.php").openStream()));
			
			ArrayList<Long> timeStamps = new ArrayList<Long>();

			String s = reader.readLine();
			s = s.trim();
			System.out.println("read: " + s);
			String[] str = s.split(",");
			for(int j = 0; j < str.length; j++) {
				if(!timeStamps.contains(Long.parseLong(str[j]))) {
					timeStamps.add(Long.parseLong(str[j]));
				}
			}
			
			DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			
			Date d = format.parse(Settings.getSettings().getNewsDate());
			
			long l = d.getTime();
			System.out.println(l);
			for(int j = 0; j < timeStamps.size(); j++) {
				long time = timeStamps.get(j);
				System.out.println(time);
				if(time > l) {
					i++;
				}
			}
			
		} catch (Exception e) {
			Logger.logError(e.getMessage(), e);
		}
		
		return Integer.toString(i);
	}
	
//	public static JTabbedPane getTabbedPane() {
//		return tabbedPane;
//	}
}
