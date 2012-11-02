package net.ftb.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics2D;
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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.Painter;
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
import net.ftb.gui.dialogs.ProfileAdderDialog;
import net.ftb.gui.dialogs.ProfileEditorDialog;
import net.ftb.gui.panes.ILauncherPane;
import net.ftb.gui.panes.MapsPane;
import net.ftb.gui.panes.ModpacksPane;
import net.ftb.gui.panes.NewsPane;
import net.ftb.gui.panes.OptionsPane;
import net.ftb.gui.panes.TexturepackPane;
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
	private JPanel mapsPane;
	private JPanel tpPane;

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
	private JButton launch = new JButton("Launch");
	private static String[] dropdown_ = {"Select Username", "Create Username"};
	private static JComboBox users;
	private JButton edit;

	/**
	 * things to go on the texture packs panel
	 */
	
	/**
	 * things to go on the maps panel
	 */

	/**
	 * random crap
	 */
	private static final int version = 12;
	private URLClassLoader cl;
	private FileOutputStream fos;
	private static final long serialVersionUID = 1L;
	private static LaunchFrame instance = null;
	private static final String FORGENAME = "minecraftforge-universal-6.0.1.353.zip";
	public static UserManager userManager;
	private LoginResponse RESPONSE;

	/**
	 * Launch the application.
	 * @param args - CLI arguments
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				Color baseColor = new Color(40, 40, 40);
				final Color tabColor = new Color(27, 27, 27);

				UIManager.put("control", baseColor);
				UIManager.put("text", new Color(222, 222, 222));
				UIManager.put("nimbusBase", new Color(0, 0, 0));
				UIManager.put("nimbusFocus", baseColor);
				UIManager.put("nimbusBorder", baseColor);
				UIManager.put("nimbusLightBackground", baseColor);
				UIManager.put("info", new Color(55, 55, 55));
				
				UIManager.put("TabbedPane:TabbedPaneTab[Disabled].backgroundPainter", new Painter() {
					@Override
					public void paint(Graphics2D g, Object o, int width, int height) {
						g.setColor(tabColor);
						g.drawRect(0, 0, width, height);
					}
				});

				UIManager.put("TabbedPane:TabbedPaneTab[Enabled].backgroundPainter", new Painter() {
					@Override
					public void paint(Graphics2D g, Object o, int width, int height) {
						g.setColor(tabColor);
						g.drawRect(0, 0, width, height);
					}
				});

				UIManager.put("TabbedPane:TabbedPaneTab[Selected].backgroundPainter", new Painter() {
					@Override
					public void paint(Graphics2D g, Object object, int width, int height) {
						g.setColor(tabColor.darker());
						g.drawRect(0, 0, width, height);
					}
				});

				

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
					} catch (ClassNotFoundException e1) { }
					catch (InstantiationException e1) { }
					catch (IllegalAccessException e1) { }
					catch (UnsupportedLookAndFeelException e1) { }
				}

				// Load settings
				try {
					Settings.initSettings();
				} catch (IOException e) {
					e.printStackTrace();
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

				LaunchFrame frame = new LaunchFrame(2);
				instance = frame;
				frame.setVisible(true);

				ModPack.addListener(frame.modPacksPane);
				ModPack.loadAll();
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public LaunchFrame(final int tab) {
		setFont(new Font("a_FuturaOrto", Font.PLAIN, 12));
		setResizable(false);
		setTitle("Feed the Beast Launcher Beta v0.1.2");
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
					Hlink(event, new URI("http://www.feed-the-beast.com"));
				} catch (URISyntaxException e) { e.printStackTrace(); }
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
					Hlink(event, new URI("http://www.creeperhost.net/aff.php?aff=293"));
				} catch (URISyntaxException e) { e.printStackTrace(); }
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
			public void actionPerformed(ActionEvent e) {
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
				if(users.getSelectedIndex() > 1 && modPacksPane.packPanels.size() > 0) {
					saveSettings();
					doLogin(userManager.getUsername(users.getSelectedItem().toString()), userManager.getPassword(users.getSelectedItem().toString()));
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

		//Adding tabs to the panel
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
		System.out.println("Logging in...");

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
				System.out.println();

				String responseStr;
				try {
					responseStr = get();
				} catch (InterruptedException err) {
					err.printStackTrace();
					return;
				} catch (ExecutionException err) {
					err.printStackTrace();
					if (err.getCause() instanceof IOException) {
						System.out.println("Login failed: "	+ err.getCause().getMessage());
					} else if (err.getCause() instanceof MalformedURLException) {
						System.out.println("Error: Malformed URL");
					}
					return;
				}

				LoginResponse response;
				try {
					response = new LoginResponse(responseStr);
					RESPONSE = response;
				} catch (IllegalArgumentException e) {
					if (responseStr.contains(":")) {
						System.out.println("Received invalid response from server.");
					} else {
						if (responseStr.equalsIgnoreCase("bad login")) {
							System.out.println("Invalid username or password.");
						} else if (responseStr.equalsIgnoreCase("old version")) {
							System.out.println("Outdated launcher.");
						} else {
							System.out.println("Login failed: " + responseStr);
						}
					}
					enableObjects();
					return;
				}

				System.out.println("Login complete.");
				runGameUpdater(response);
			}
		};
		loginWorker.execute();
	}

	public void runGameUpdater(final LoginResponse response) {
		if (!new File(Settings.getSettings().getInstallPath() + "/.minecraft/bin/minecraft.jar").exists()) {
			final ProgressMonitor progMonitor = new ProgressMonitor(this, "Downloading minecraft...", "", 0, 100);
			final GameUpdateWorker updater = new GameUpdateWorker(RESPONSE.getLatestVersion(), "minecraft.jar", 
					new File(Settings.getSettings().getInstallPath(), ".minecraft//bin").getPath(), false) {
				@Override
				public void done() {
					progMonitor.close();
					try {
						if (get()) {
							// Success
							System.out.println("Game update complete.");

							if(modPacksPane.getSelectedModIndex() < 0) {
								System.err.println("No Modpack selected!");
								return;
							}
							
							System.out.println(ModPack.getPack(modPacksPane.getSelectedModIndex()).getDir());
							
							FileUtils.killMetaInf();
							ModManager man = new ModManager(new JFrame(), true);
							man.setVisible(true);
							try {
								installMods(ModPack.getPack(modPacksPane.getSelectedModIndex()).getDir());
								ModManager.cleanUp();
							} catch (IOException e) { e.printStackTrace(); }
							launchMinecraft(new File(Settings.getSettings().getInstallPath()).getPath()+ "/" + ModPack.getPack(modPacksPane.getSelectedModIndex()).getDir() 
									+ "/.minecraft",RESPONSE.getUsername(), RESPONSE.getSessionID());
						} else {
							System.out.println("Error downloading game.");
						}
					} catch (CancellationException e) { 
						System.out.println("Game update cancelled..."); 
						enableObjects();
					} catch (InterruptedException e) { e.printStackTrace(); 
					} catch (ExecutionException e) {
						e.printStackTrace();
						System.out.println("Failed to download game: " + e.getCause().getMessage());
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
			if(modPacksPane.getSelectedModIndex() < 0) {
				System.err.println("No Modpack selected!");
				return;
			}
			
			System.out.println(ModPack.getPack(modPacksPane.getSelectedModIndex()).getDir());
			FileUtils.killMetaInf();
			ModManager man = new ModManager(new JFrame(), true);
			man.setVisible(true);
			try {
				installMods(ModPack.getPack(modPacksPane.getSelectedModIndex()).getDir());
				ModManager.cleanUp();
			} catch (IOException e) { e.printStackTrace(); }
			launchMinecraft(new File(Settings.getSettings().getInstallPath()).getPath()+ "/" + ModPack.getPack(modPacksPane.getSelectedModIndex()).getDir() 
					+ "/.minecraft",RESPONSE.getUsername(), RESPONSE.getSessionID());
		}
	}

	/**
	 * @param file - the name of the file, as saved to the repo (including extension)
	 * @return - the direct link
	 * @throws NoSuchAlgorithmException - see md5
	 */
	public static String getCreeperhostLink(String file) throws NoSuchAlgorithmException {
		DateFormat sdf = new SimpleDateFormat("ddMMyy");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		String date = sdf.format(new Date());
		String resolved = "http://repo.creeperhost.net/direct/FTB2/" + md5("mcepoch1" + date) + "/" + file;
		System.out.println(resolved);
		return resolved; 
	}

	/**
	 * @param dest - the destination to be saved
	 * @param file - the file as on the repo
	 * @throws NoSuchAlgorithmException - see getCreeperHostLink
	 * @throws IOException - see downloadUrl
	 */
	public void downloadPack(String dest, String file) throws NoSuchAlgorithmException, IOException {
		downloadUrl(dest, getCreeperhostLink(file));
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
			}
			fout.close();
		}
	}

	/**
	 * launch the game with the mods in the classpath
	 * @param workingDir - install path
	 * @param username - the MC username
	 * @param password - the MC password
	 */
	protected void launchMinecraft(String workingDir, String username, String password) {
		try {
			System.out.println("Loading jars...");
			String[] jarFiles = new String[] { FORGENAME,"minecraft.jar", "lwjgl.jar", "lwjgl_util.jar","jinput.jar" };
			URL[] urls = new URL[jarFiles.length];

			for (int i = 0; i < urls.length; i++) {
				try {
					File f = new File(new File(workingDir, "bin"), jarFiles[i]);
					urls[i] = f.toURI().toURL();
					System.out.println("Loading URL: " + urls[i].toString());
				} catch (MalformedURLException e) {
					System.err.println("MalformedURLException, " + e.toString());
					System.exit(5);
				}
			}

			System.out.println("Loading natives...");
			String nativesDir = new File(new File(workingDir, "bin"), "natives").toString();

			System.setProperty("org.lwjgl.librarypath", nativesDir);
			System.setProperty("net.java.games.input.librarypath", nativesDir);

			System.setProperty("user.home", new File(workingDir).getParent());

			cl = new URLClassLoader(urls, LaunchFrame.class.getClassLoader());

			// Get the Minecraft Class.
			Class<?> mc = cl.loadClass("net.minecraft.client.Minecraft");
			Field[] fields = mc.getDeclaredFields();

			for(Field f : fields) {
				if(f.getType() != File.class) {
					// Has to be File
					continue;
				}
				if(0 == (f.getModifiers() & (Modifier.PRIVATE | Modifier.STATIC))){
					// And Private Static.
					continue;
				}
				f.setAccessible(true);
				f.set(null, new File(workingDir));
				// And set it.
				System.out.println("Fixed Minecraft Path: Field was " + f.toString());
			}

			String[] mcArgs = new String[2];
			mcArgs[0] = username;
			mcArgs[1] = password;

			String mcDir = mc.getMethod("a", String.class).invoke(null, (Object) "minecraft").toString();

			System.out.println("MCDIR: " + mcDir);

			mc.getMethod("main", String[].class).invoke(null, (Object) mcArgs);
			this.setVisible(false);
		} catch (ClassNotFoundException e) {
			this.setVisible(true);
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			System.exit(2);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			System.exit(2);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			System.exit(3);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			System.exit(3);
		} catch (SecurityException e) {
			e.printStackTrace();
			System.exit(4);
		}
	}

	/**
	 * @param modPackName - the name of the pack 
	 * @throws NoSuchAlgorithmException - see getCreeperHostLink
	 */
	protected void downloadModPack(String modPackName) throws NoSuchAlgorithmException {
		System.out.println("DOWNLOADING!!!");
		URL website;
		try {
			System.out.println("STILL DOWNLOADING!!!");
			website = new URL(getCreeperhostLink(modPackName));
			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			fos = new FileOutputStream(Settings.getSettings().getInstallPath() + "/temp/" + modPackName);
			fos.getChannel().transferFrom(rbc, 0, 1 << 24);
		} catch (MalformedURLException e) { e.printStackTrace(); 
		} catch (IOException e) { e.printStackTrace(); }

		FileUtils.extractZip(Settings.getSettings().getInstallPath() + "/temp/" + modPackName);
		new File(Settings.getSettings().getInstallPath() + "/" + ModPack.getPack(modPacksPane.getSelectedModIndex()).getDir() + "/.minecraft/mods").delete();
		new File(Settings.getSettings().getInstallPath() + "/" + ModPack.getPack(modPacksPane.getSelectedModIndex()).getDir() + "/.minecraft/coremods").delete();
	 	File[] contents = new File(Settings.getSettings().getInstallPath() + "/" + ModPack.getPack(modPacksPane.getSelectedModIndex()).getDir() + "/.minecraft/bin/").listFiles();
		String files;
		for(File content : contents) {
			if(content.isFile()) {
				files = content.getName();
				if(files.toLowerCase().endsWith(".zip")) {
					content.delete();
				}
			}
		}
	}

	/**
	 * @param modPackName - The pack to install (should already be downloaded)
	 * @throws IOException
	 */
	protected void installMods(String modPackName) throws IOException {
		new File(Settings.getSettings().getInstallPath() + "/"+ ModPack.getPack(modPacksPane.getSelectedModIndex()).getDir() + "/.minecraft").mkdirs();
		System.out.println("dirs mk'd");
		FileUtils.copyFolder(new File(Settings.getSettings().getInstallPath()+ "/.minecraft/bin/"), new File(Settings.getSettings().getInstallPath() + "/" 
				+ ModPack.getPack(modPacksPane.getSelectedModIndex()).getDir()+ "/.minecraft/bin"));
		File minecraft = new File(Settings.getSettings().getInstallPath()+ "/.minecraft/bin/minecraft.jar");
		File mcbackup = new File(Settings.getSettings().getInstallPath() + "/"+ modPackName + "/.minecraft/bin/mcbackup.jar");
		FileUtils.copyFile(minecraft, mcbackup);
		FileUtils.copyFolder(new File(Settings.getSettings().getInstallPath() + "/temp/" + ModPack.getPack(modPacksPane.getSelectedModIndex()).getDir() + "/.minecraft"), 
				new File(Settings.getSettings().getInstallPath() + "/" + ModPack.getPack(modPacksPane.getSelectedModIndex()).getDir() + "/.minecraft"));
		FileUtils.copyFile(new File(Settings.getSettings().getInstallPath() + "/temp/" + ModPack.getPack(modPacksPane.getSelectedModIndex()).getDir() + "/instMods/" + FORGENAME), 
	 			new File(Settings.getSettings().getInstallPath() + "/" + ModPack.getPack(modPacksPane.getSelectedModIndex()).getDir() + "/.minecraft/bin/" + FORGENAME));
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
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Failed to save config file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Failed to save config file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * @param user - user added/edited
	 */
	public static void writeUsers(String user) {
		try {
			userManager.write();
		} catch (IOException e) { e.printStackTrace(); }
		String[] usernames = merge(dropdown_, UserManager.getNames().toArray(new String[] {}));;
		users.removeAllItems();
		for(int i = 0; i < usernames.length; i++){
			users.addItem(usernames[i]);
			if(usernames[i].equals(user)){
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

	public void Hlink(MouseEvent me, URI uri) {
		if(Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			try {
				desktop.browse(uri);
			} catch(Exception exc) {
				System.out.println(exc);
			}
		} else {
			System.out.println("else working");
		}
	}
}
