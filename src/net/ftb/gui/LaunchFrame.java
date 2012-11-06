package net.ftb.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.ProgressMonitor;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.ftb.data.LoginResponse;
import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.data.UserManager;
import net.ftb.gui.dialogs.LauncherUpdateDialog;
import net.ftb.gui.dialogs.PasswordDialog;
import net.ftb.gui.dialogs.ProfileAdderDialog;
import net.ftb.gui.dialogs.ProfileEditorDialog;
import net.ftb.gui.panes.ILauncherPane;
import net.ftb.gui.panes.MapsPane;
import net.ftb.gui.panes.ModpacksPane;
import net.ftb.gui.panes.NewsPane;
import net.ftb.gui.panes.OptionsPane;
import net.ftb.gui.panes.TexturepackPane;
import net.ftb.log.Logger;
import net.ftb.mclauncher.MinecraftLauncher;
import net.ftb.tools.MinecraftVersionDetector;
import net.ftb.updater.UpdateChecker;
import net.ftb.util.FileUtils;
import net.ftb.workers.GameUpdateWorker;
import net.ftb.workers.LoginWorker;

public class LaunchFrame extends JFrame {
	/**
	 * the panels to appear in the tabs
	 */
	private NewsPane newsPane;
	private OptionsPane optionsPane;
	private ModpacksPane modPacksPane;
	private JPanel mapsPane, tpPane;

	/**
	 * an array of all mods to be added to classpath
	 */
	static String[] jarMods;

	/**
	 * the panel that contains the footer and the tabbed pane
	 */
	private JPanel panel = new JPanel();

	/**
	 * tabbedpane and footer
	 */
	private final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	private JPanel footer = new JPanel();

	/**
	 * the things to go on the footer
	 */
	private JLabel footerLogo = new JLabel(new ImageIcon(this.getClass().getResource("/image/logo_ftb.png")));
	private JLabel footerCreeper = new JLabel(new ImageIcon(this.getClass().getResource("/image/logo_creeperHost.png")));
	private JCheckBox savePassword = new JCheckBox();
	private JButton launch = new JButton("Launch");
	private static String[] dropdown_ = {"Select Profile", "Create Profile" };
	private static JComboBox users;
	private JButton edit;

	/**
	 * random crap
	 */
	private static String version = "0.3.2";
	private static int buildNumber = 32;
	private FileOutputStream fos;
	private static final long serialVersionUID = 1L;
	private static LaunchFrame instance = null;
	private static final String FORGENAME = "MinecraftForge.zip";
	protected static UserManager userManager;
	private LoginResponse RESPONSE;
	public static String tempPass = "";

	/**
	 * Launch the application.
	 * @param args - CLI arguments
	 */
	public static void main(String[] args) {
		Logger.logInfo("FTBLaunch starting up (version "+ version + ")");
		SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
		String now = dateFormatGmt.format(new Date());
		Logger.logInfo("Now is: "+now);
		Logger.logInfo("Java version: "+System.getProperty("java.version"));
		Logger.logInfo("Java vendor: "+System.getProperty("java.vendor"));
		Logger.logInfo("Java home: "+System.getProperty("java.home"));
		Logger.logInfo("Java specification: " + System.getProperty("java.vm.specification.name") + " version: " +
				System.getProperty("java.vm.specification.version") + " by " + System.getProperty("java.vm.specification.vendor"));
		Logger.logInfo("Java vm: "+System.getProperty("java.vm.name") + " version: " + System.getProperty("java.vm.version") 
				+ " by " + System.getProperty("java.vm.vendor"));
		Logger.logInfo("OS: "+System.getProperty("os.arch") + " " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
		Logger.logInfo("Working directory: " + System.getProperty("user.dir"));
		// TODO: Format this to MB or GB?
		Logger.logInfo("Max Memory: " + Runtime.getRuntime().maxMemory());

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				Color baseColor = new Color(40, 40, 40);

				UIManager.put("control", baseColor);
				UIManager.put("text", new Color(222, 222, 222));
				UIManager.put("nimbusBase", new Color(0, 0, 0));
				UIManager.put("nimbusFocus", baseColor);
				UIManager.put("nimbusBorder", baseColor);
				UIManager.put("nimbusLightBackground", baseColor);
				UIManager.put("info", new Color(55, 55, 55));

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
					} catch (ClassNotFoundException e1) { Logger.logWarn("Exception occured",e1); 
					} catch (InstantiationException e1) { Logger.logWarn("Exception occured",e1); 
					} catch (IllegalAccessException e1) { Logger.logWarn("Exception occured",e1); 
					} catch (UnsupportedLookAndFeelException e1) { Logger.logWarn("Exception occured",e1); }
				}

				// Load settings
				try {
					Settings.initSettings();
				} catch (IOException e) {
					Logger.logWarn("Exception occured",e); 
					JOptionPane.showMessageDialog(null, "Failed to load config file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}

				// Create the install directory if it does not exist.
				File installDir = new File(Settings.getSettings().getInstallPath());
				if (!installDir.exists()) {
					installDir.mkdirs();
				}

				userManager = new UserManager(new File(installDir, "logindata"));

				LauncherConsole con = new LauncherConsole();
				con.setVisible(true);

				LaunchFrame frame = new LaunchFrame(0);
				instance = frame;
				frame.setVisible(true);

				ModPack.addListener(frame.modPacksPane);
				ModPack.loadAll();

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
		setTitle("Feed the Beast Launcher Beta v" + version);
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));

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
				try {
					hLink(event, new URI("http://www.feed-the-beast.com"));
				} catch (URISyntaxException e) {Logger.logWarn("Exception occured",e); }
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
				try {
					hLink(event, new URI("http://www.creeperhost.net/aff.php?aff=293"));
				} catch (URISyntaxException e) { Logger.logWarn("Exception occured",e); }
			}
			@Override public void mouseReleased(MouseEvent arg0) { }
			@Override public void mousePressed(MouseEvent arg0) { }
			@Override public void mouseExited(MouseEvent arg0) { }
			@Override public void mouseEntered(MouseEvent arg0) { }
		});

		userManager.read();

		String[] dropdown = merge(dropdown_, UserManager.getNames().toArray(new String[] {}));
		users = new JComboBox(dropdown);
		if(Settings.getSettings().getLastUser() != null) {
			for(int i = 0; i < dropdown.length; i++) {
				if(dropdown[i].equalsIgnoreCase(Settings.getSettings().getLastUser())) {
					users.setSelectedIndex(i);
				}
			}
		}

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

		edit = new JButton("Edit");
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

		launch.setBounds(711, 20, 100, 30);
		launch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(modPacksPane.typeFilter == 0) {
					if(users.getSelectedIndex() > 1 && modPacksPane.packPanels.size() > 0) {
						saveSettings();
						doLogin(UserManager.getUsername(users.getSelectedItem().toString()), UserManager.getPassword(users.getSelectedItem().toString()));
					}
				} else {
					if(modPacksPane.packPanels.size() > 0) {
					}
				}
			}
		});

		footer.add(edit);
		footer.add(users);
		footer.add(footerLogo);
		footer.add(footerCreeper);
		footer.add(launch);

		newsPane = new NewsPane();
		modPacksPane = new ModpacksPane();
		mapsPane = new MapsPane();
		tpPane = new TexturepackPane();
		optionsPane = new OptionsPane();

		getRootPane().setDefaultButton(launch);

		loadSettings();

		tabbedPane.add(newsPane, 0);
		tabbedPane.setIconAt(0, new ImageIcon(this.getClass().getResource("/image/tabs/news.png")));

		tabbedPane.add(optionsPane, 1);
		tabbedPane.setIconAt(1, new ImageIcon(this.getClass().getResource("/image/tabs/options.png")));

		tabbedPane.add(modPacksPane, 2);
		tabbedPane.setIconAt(2, new ImageIcon(this.getClass().getResource("/image/tabs/modpacks.png")));

		tabbedPane.add(mapsPane, 3);
		tabbedPane.setIconAt(3, new ImageIcon(this.getClass().getResource("/image/tabs/maps.png")));
		tabbedPane.setEnabledAt(3, false);

		tabbedPane.add(tpPane, 4);
		tabbedPane.setIconAt(4, new ImageIcon(this.getClass().getResource("/image/tabs/texturepacks.png")));
		tabbedPane.setEnabledAt(4, false);

		tabbedPane.setSelectedIndex(tab);

		tabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event){
				if(tabbedPane.getSelectedComponent() instanceof ILauncherPane) {
					((ILauncherPane)tabbedPane.getSelectedComponent()).onVisible();
				}
			}
		});
	}

	/**
	 * call this to login
	 */
	public void doLogin(String username, String password) {
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

		tabbedPane.getSelectedComponent().setEnabled(false);

		launch.setEnabled(false);
		users.setEnabled(false);
		edit.setEnabled(false);

		LoginWorker loginWorker = new LoginWorker(username, password) {
			@Override
			public void done() {
				String responseStr;
				try {
					responseStr = get();
				} catch (InterruptedException err) {
					Logger.logWarn("Exception occured",err); 
					return;
				} catch (ExecutionException err) {
					if (err.getCause() instanceof IOException) {
						Logger.logWarn("Login failed due IOException",err);
					} else if (err.getCause() instanceof MalformedURLException) {
						Logger.logWarn("Login failed due malformed URL",err); 
					}
					return;
				}

				LoginResponse response;
				try {
					response = new LoginResponse(responseStr);
					RESPONSE = response;
				} catch (IllegalArgumentException e) {
					// TODO: Add in error dialogs to represent login errors.
					if (responseStr.contains(":")) {
						Logger.logError("Received invalid response from server.");
					} else {
						if (responseStr.equalsIgnoreCase("bad login")) {
							Logger.logWarn("Invalid username or password.");
						} else if (responseStr.equalsIgnoreCase("old version")) {
							Logger.logWarn("Outdated launcher.");
						} else {
							Logger.logWarn("Login failed: " + responseStr);
						}
					}
					enableObjects();
					return;
				}
				Logger.logInfo("Login complete.");
				runGameUpdater(response);
			}
		};
		loginWorker.execute();
	}

	public void runGameUpdater(final LoginResponse response) {
		final String installPath = Settings.getSettings().getInstallPath();
		final ModPack modpack = ModPack.getPack(modPacksPane.getSelectedModIndex());
		MinecraftVersionDetector mvd = new MinecraftVersionDetector();
		// TODO: If minecraft updates to the newest minecraft required by the mod pack, but they have an older version... What do?
		if(!new File(installPath + "/" + modpack.getDir() + "/.minecraft/bin/minecraft.jar").exists() 
				|| mvd.shouldUpdate(modpack.getMcVersion(), installPath + "/" + modpack.getDir() + "/.minecraft")) {
			final ProgressMonitor progMonitor = new ProgressMonitor(this, "Downloading minecraft...", "", 0, 100);
			final GameUpdateWorker updater = new GameUpdateWorker(modpack.getMcVersion(), "minecraft.jar", new File(new File(installPath, modpack.getDir()), ".minecraft/bin").getPath(), false) {
				@Override
				public void done() {
					progMonitor.close();
					try {
						if (get()) {
							Logger.logInfo("Game update complete");
							initializeMods();
							FileUtils.killMetaInf();
							launchMinecraft(installPath + "/" + modpack.getDir() + "/.minecraft", RESPONSE.getUsername(), RESPONSE.getSessionID());
						} else {
							Logger.logError("Error occured during downloading the game");
						}
					} catch (CancellationException e) { 
						Logger.logInfo("Game update canceled",e);
						enableObjects();
					} catch (InterruptedException e) { Logger.logWarn("Game update interrupted",e);
					} catch (ExecutionException e) { Logger.logError("Failed to download game",e); }
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
			initializeMods();
			launchMinecraft(installPath + "/" + modpack.getDir() + "/.minecraft", RESPONSE.getUsername(), RESPONSE.getSessionID());
		}
	}

	/**
	 * @param file - the name of the file, as saved to the repo (including extension)
	 * @return - the direct link
	 * @throws NoSuchAlgorithmException - see md5
	 */
	public static String getCreeperhostLink(String file) throws NoSuchAlgorithmException {
		String resolved = "http://repo.creeperhost.net/direct/FTB2/" + md5("mcepoch1" + getTime()) + "/" + file;
		Logger.logInfo(resolved);
		return resolved; 
	}

	/**
	 * @param input - String to hash
	 * @return - hashed string
	 * @throws NoSuchAlgorithmException - in case "MD5" isnt a correct input
	 */
	public static String md5(String input) throws NoSuchAlgorithmException {
		String result = input;
		if (input != null) {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(input.getBytes());
			BigInteger hash = new BigInteger(1, md.digest());
			result = hash.toString(16);
			while (result.length() < 32) {
				result = "0" + result;
			}
		}
		return result;
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
	protected void launchMinecraft(String workingDir, String username, String password) {
		int result = MinecraftLauncher.launchMinecraft(workingDir, username, password, FORGENAME, Settings.getSettings().getRamMin(),
				Settings.getSettings().getRamMax());
		Logger.logInfo("MinecraftLauncher said: "+result);
		if (result > 0) {
			System.exit(0);
		}
	}

	/**
	 * @param modPackName - The pack to install (should already be downloaded)
	 * @throws IOException
	 */
	protected void installMods(String modPackName) throws IOException {
		String installpath = Settings.getSettings().getInstallPath();
		ModPack pack = ModPack.getPack(modPacksPane.getSelectedModIndex());
		new File(installpath + "/" + pack.getDir() + "/instMods/").mkdirs();
		Logger.logInfo("dirs mk'd");
		FileUtils.copyFolder(new File(installpath + "/temp/" + pack.getDir() + "/.minecraft"), new File(installpath + "/" + pack.getDir() + "/.minecraft"));
		FileUtils.copyFolder(new File(installpath + "/temp/" + pack.getDir() + "/instMods/"), new File(installpath + "/" + pack.getDir() + "/instMods/"));
	}

	/**
	 * "Loads" the settings from the settings class into their respective GUI
	 * controls.
	 */
	private void loadSettings() {
		Settings settings = Settings.getSettings();
		optionsPane.loadSettings(settings);
	}

	/**
	 * "Saves" the settings from the GUI controls into the settings class.
	 */
	public void saveSettings() {
		Settings settings = Settings.getSettings();
		settings.setLastUser((String)users.getSelectedItem());
		instance.optionsPane.saveSettingsInto(settings);
		try {
			settings.save();
		} catch (FileNotFoundException e) {
			Logger.logError("Exception occured",e);
			JOptionPane.showMessageDialog(this, "Failed to save config file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			Logger.logError("Exception occured",e);
			JOptionPane.showMessageDialog(this, "Failed to save config file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * @param user - user added/edited
	 */
	public static void writeUsers(String user) {
		try {
			userManager.write();
		} catch (IOException e) { Logger.logError("Exception occured", e); }
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
		tabbedPane.getSelectedComponent().setEnabled(true);
		launch.setEnabled(true);
		edit.setEnabled(users.getSelectedIndex() > 1);
		users.setEnabled(true);
	}

	/**
	 * Install mods
	 */
	private void initializeMods() {
		Logger.logInfo(ModPack.getPack(modPacksPane.getSelectedModIndex()).getDir());
		ModManager man = new ModManager(new JFrame(), true);
		man.setVisible(true);
		try {
			installMods(ModPack.getPack(modPacksPane.getSelectedModIndex()).getDir());
			ModManager.cleanUp();
		} catch (IOException e) { Logger.logError("Exception ocured", e); }
	}


	public void hLink(MouseEvent me, URI uri) {
		if(Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			try {
				desktop.browse(uri);
			} catch(Exception exc) { Logger.logError("Exception occured durring opening Link",exc); }
		} else {
			Logger.logWarn("Desktop not supported.");
		}
	}
	
	private static String getTime() {
		String content = null;
		Scanner scanner = null;
		try {
			URLConnection connection = new URL("http://repo.creeperhost.net/getdate").openConnection();
			scanner = new Scanner( connection.getInputStream() );
			scanner.useDelimiter( "\\Z" );
			content = scanner.next();
		} catch (java.net.UnknownHostException uhe) {
		} catch (Exception ex) { ex.printStackTrace(); 
		} finally {
			if (scanner != null) {
				scanner.close();
			}
		}
		return content;
	}
}
