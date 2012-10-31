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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.ImageIcon;
import javax.swing.JButton;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.ftb.data.LoginResponse;
import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.data.UserManager;
import net.ftb.gui.panes.ILauncherPane;
import net.ftb.gui.panes.MapsPane;
import net.ftb.gui.panes.ModpacksPane;
import net.ftb.gui.panes.NewsPane;
import net.ftb.gui.panes.OptionsPane;
import net.ftb.gui.panes.TexturepackPane;
import net.ftb.log.Logger;
import net.ftb.workers.GameUpdateWorker;
import net.ftb.workers.LoginWorker;

//import com.sun.xml.internal.ws.api.config.management.policy.ManagementAssertion.Setting;

public class LaunchFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private static LaunchFrame instance = null;
	
	public static UserManager userManager;
	LoginResponse RESPONSE;

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
	private static JComboBox<String> users;
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
	public static int ramMin = 512;
	public static int ramMax = 1024;
	private URLClassLoader cl;
	private FileOutputStream fos;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		Logger.logInfo("FTBLaunch starting up");
		{
			SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
			String now = dateFormatGmt.format(new Date());
			Logger.logInfo("Now is: "+now);
		}
		Logger.logInfo("Java version: "+System.getProperty("java.version"));
		Logger.logInfo("Java vendor: "+System.getProperty("java.vendor"));
		Logger.logInfo("Java home: "+System.getProperty("java.home"));
		Logger.logInfo("Java specification: "+System.getProperty("java.vm.specification.name")+" version: "+
				System.getProperty("java.vm.specification.version")+" by "+
				System.getProperty("java.vm.specification.vendor"));
		Logger.logInfo("Java vm: "+System.getProperty("java.vm.name")+" version: "+
				System.getProperty("java.vm.version")+" by "+
				System.getProperty("java.vm.vendor"));
		Logger.logInfo("OS: "+System.getProperty("os.arch")+" "+
				System.getProperty("os.name")+" "+
				System.getProperty("os.version"));
		Logger.logInfo("Working directory: "+
				System.getProperty("user.dir"));
		
		EventQueue.invokeLater(new Runnable() {
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
					} catch (ClassNotFoundException e1) { }
					catch (InstantiationException e1) { }
					catch (IllegalAccessException e1) { }
					catch (UnsupportedLookAndFeelException e1) { }
				}

				// Load settings
				try {
					Settings.initSettings();
				} catch (IOException e) {
					Logger.logError("while loading settings: IOException "+e.getMessage());
					JOptionPane.showMessageDialog(null, "Failed to load config file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}

				// Create the install directory if it does not exist.
				File installDir = new File(Settings.getSettings().getInstallPath());
				if (!installDir.exists()) {
					installDir.mkdirs();
				}

				userManager = new UserManager(new File(installDir, "logindata"));

				//KeyChecker k = new KeyChecker();
				//k.setVisible(true);
				try {
					LauncherConsole con = new LauncherConsole();
					con.setVisible(true);
				} catch (IOException e) {
					Logger.logError("while console startup: IOException "+e.getMessage());
				}
				LaunchFrame frame = new LaunchFrame(2);
				instance = frame;
				frame.setVisible(true);

				ModPack.addListener(frame.modPacksPane);
				ModPack.LoadAll();
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public LaunchFrame(final int tab) {
		setFont(new Font("a_FuturaOrto", Font.PLAIN, 12));
		setResizable(false);
		setTitle("Feed the Beast Launcher Beta v0.1.1");
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
		footerLogo.setBounds(20, 20, 42, 42);
		footerLogo.addMouseListener(new MouseListener() {
			@Override 
			public void mouseClicked(MouseEvent arg0) {
				try {
					Hlink(arg0, new URI("http://www.feed-the-beast.com"));
				} catch (URISyntaxException e) {
					Logger.logError("Could not open link: URISytanxException "+e.getMessage());
				}
			}
			@Override public void mouseReleased(MouseEvent arg0) { }
			@Override public void mousePressed(MouseEvent arg0) { }
			@Override public void mouseExited(MouseEvent arg0) { }
			@Override public void mouseEntered(MouseEvent arg0) { }
		});
		footerCreeper.setBounds(72, 20, 132, 42);
		footerCreeper.addMouseListener(new MouseListener() {
			@Override 
			public void mouseClicked(MouseEvent arg0) {
				try {
					Hlink(arg0, new URI("http://www.creeperhost.net/aff.php?aff=293"));
				} catch (URISyntaxException e) {
					Logger.logError("Could not open link: URISyntaxException "+e.getMessage());
				}
			}
			@Override public void mouseReleased(MouseEvent arg0) { }
			@Override public void mousePressed(MouseEvent arg0) { }
			@Override public void mouseExited(MouseEvent arg0) { }
			@Override public void mouseEntered(MouseEvent arg0) { }
		});

		try {
			userManager.read();
		} catch (IOException e1) {
			Logger.logError("Could not load userdata: IOException "+e1.getMessage());
		}
		
		String[] usernames = UserManager.getNames().toArray(new String[] {});
		String[] dropdown = merge(dropdown_, usernames);

		users = new JComboBox<String>(dropdown);
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
					ProfileAdder p = new ProfileAdder();
					p.setVisible(true);
					users.setSelectedIndex(0);
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
					ProfileEditor p = new ProfileEditor((String)users.getSelectedItem());
					p.setVisible(true);
					users.setSelectedIndex(0);
				}
				edit.setEnabled(users.getSelectedIndex() > 1);
			}
		});

		launch.setBounds(711, 20, 100, 30);
		launch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(users.getSelectedIndex() > 1) {
					saveSettings();
					doLogin(UserManager.getUsername(users.getSelectedItem().toString()), UserManager.getPassword(users.getSelectedItem().toString()));
				}
			}
		});

		footerLogo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		footerCreeper.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
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

		tabbedPane.add(tpPane, 4);
		tabbedPane.setIconAt(4, new ImageIcon(this.getClass().getResource("/image/tabs/texturepacks.png")));

		tabbedPane.setEnabledAt(3, false);
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
					Logger.logInfo("Interupted");
					return;
				} catch (ExecutionException err) {
					Logger.logError("Login error: ExecutionException "+err.getMessage());
					if (err.getCause() instanceof IOException) {
							Logger.logError("Login failed: IOException "+err.getMessage());	
					} else if (err.getCause() instanceof MalformedURLException) {
						Logger.logError("Login failed (Malformed): "+err.getMessage());	
					}
					return;
				}

				LoginResponse response;
				try {
					response = new LoginResponse(responseStr);
					RESPONSE = response;
				} catch (IllegalArgumentException e) {
					if (responseStr.contains(":")) {
						Logger.logError("Login error: receiving invalid data from server");
					} else {
						if (responseStr.equalsIgnoreCase("bad login")) {
							Logger.logWarn("Login failed: Invalid username or password.");
						} else if (responseStr.equalsIgnoreCase("old version")) {
							Logger.logWarn("Login failed: Outdated launcher.");
						} else {
							Logger.logWarn("Login failed: "+responseStr);
						}
					}
					enableObjects();
					return;
				}

				Logger.logInfo("Login success");
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
				public void done() {
					progMonitor.close();
					try {
						if (get() == true) {
							// Success
							Logger.logInfo("Game update complete.");
							
							// Throw an error out if there is no mod selected (then called prior load ie)
							if (modPacksPane.getSelectedModIndex() < 0) {
								Logger.logWarn("No Modpack selected");	
								return;
							}
							Logger.logInfo(ModPack.getPack(modPacksPane.getSelectedModIndex()).getDir());
					
							
							killMetaInf();
							ModManager man = new ModManager(new JFrame(), true);
							man.setVisible(true);
							try {
								installMods(ModPack.getPack(modPacksPane.getSelectedModIndex()).getDir());
							} catch (IOException e) {
								Logger.logError("while game update: IOException "+e.getMessage());
							}

							launchMinecraft(new File(Settings.getSettings().getInstallPath()).getPath()+ "/" + ModPack.getPack(modPacksPane.getSelectedModIndex()).getDir() 
									+ "/.minecraft",RESPONSE.getUsername(), RESPONSE.getSessionID());
						} else {
							Logger.logError("Error downloading game.");
						}
					} catch (CancellationException e) {
						Logger.logInfo("Game update cancelled...");
					} catch (InterruptedException e) {
						Logger.logInfo("Game update interrupted...");
					} catch (ExecutionException e) {
						Logger.logError("Failed to download game: ExecutionException " + e.getCause().getMessage());
						return;
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
			
			Logger.logInfo(ModPack.getPack(modPacksPane.getSelectedModIndex()).getDir());
			
			killMetaInf();
			ModManager man = new ModManager(new JFrame(), true);
			man.setVisible(true);
			try {
				installMods(ModPack.getPack(modPacksPane.getSelectedModIndex()).getDir());
			} catch (IOException e) {
				Logger.logError("while game update: IOException "+e.getMessage());
			}
			
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
		Logger.logInfo("resolved file to "+resolved);
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
	 * @throws MalformedURLException - for URL
	 * @throws IOException - various
	 */
	public void downloadUrl(String filename, String urlString) throws MalformedURLException, IOException {
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
	 * @throws IOException
	 */
	protected void launchMinecraft(String workingDir, String username, String password) {
		try {
			Logger.logInfo("Loading jars...");
			String[] jarFiles = new String[] { "minecraftforge-universal-6.0.1.353.zip","minecraft.jar", "lwjgl.jar", "lwjgl_util.jar","jinput.jar" };
			URL[] urls = new URL[jarFiles.length];

			for (int i = 0; i < urls.length; i++) {
				try {
					File f = new File(new File(workingDir, "bin"), jarFiles[i]);
					urls[i] = f.toURI().toURL();
					Logger.logInfo("Loading URL: " + urls[i].toString());
				} catch (MalformedURLException e) {
					Logger.logError("MalformedURLException, " + e.toString());
					System.exit(5);
				}
			}

			Logger.logInfo("Loading natives...");
			String nativesDir = new File(new File(workingDir, "bin"), "natives").toString();

			System.setProperty("org.lwjgl.librarypath", nativesDir);
			System.setProperty("net.java.games.input.librarypath", nativesDir);

			System.setProperty("user.home", new File(workingDir).getParent());

			cl = new URLClassLoader(urls, LaunchFrame.class.getClassLoader());

			// Get the Minecraft Class.
			Class<?> mc = cl.loadClass("net.minecraft.client.Minecraft");
			Field[] fields = mc.getDeclaredFields();

			for (int i = 0; i < fields.length; i++) {
				Field f = fields[i];
				if (f.getType() != File.class) {
					// Has to be File
					continue;
				}
				if (f.getModifiers() != (Modifier.PRIVATE + Modifier.STATIC)) {
					// And Private Static.
					continue;
				}
				f.setAccessible(true);
				f.set(null, new File(workingDir));
				// And set it.
				Logger.logInfo("Fixed Minecraft Path: Field was "
						+ f.toString());
			}

			String[] mcArgs = new String[2];
			mcArgs[0] = username;
			mcArgs[1] = password;

			String mcDir = mc.getMethod("a", String.class).invoke(null, (Object) "minecraft").toString();

			Logger.logInfo("MCDIR: " + mcDir);

			mc.getMethod("main", String[].class).invoke(null, (Object) mcArgs);
			this.setVisible(false);
		} catch (ClassNotFoundException e) {
			this.setVisible(true);
			Logger.logError("failed to load natives: ClassNotFound" + e.getMessage());
		} catch (IllegalArgumentException e) {
			Logger.logError("failed to load natives: IllegalArgument" + e.getMessage());
			System.exit(2);
		} catch (IllegalAccessException e) {
			Logger.logError("failed to load natives: IllegalAccess" + e.getMessage());
			System.exit(2);
		} catch (InvocationTargetException e) {
			Logger.logError("failed to load natives: InvocationTarget" + e.getMessage());
			System.exit(3);
		} catch (NoSuchMethodException e) {
			Logger.logError("failed to load natives: NoSuchMethod" + e.getMessage());
			System.exit(3);
		} catch (SecurityException e) {
			Logger.logError("failed to load natives: SecurityException" + e.getMessage());
			System.exit(4);
		}
	}

	/**
	 * deletes the META-INF
	 */
	public static void killMetaInf() {
		File inputFile = new File(Settings.getSettings().getInstallPath() + "/.minecraft/bin", "minecraft.jar");
		File outputTmpFile = new File(Settings.getSettings().getInstallPath() + "/.minecraft/bin", "minecraft.jar.tmp");
		try {
			JarInputStream input = new JarInputStream(new FileInputStream(inputFile));
			JarOutputStream output = new JarOutputStream(new FileOutputStream(outputTmpFile));
			JarEntry entry;

			while ((entry = input.getNextJarEntry()) != null) {
				if (entry.getName().contains("META-INF")) {
					continue;
				}
				output.putNextEntry(entry);
				byte buffer[] = new byte[1024];
				int amo = 0;
				while ((amo = input.read(buffer, 0, 1024)) != -1) {
					output.write(buffer, 0, amo);
				}
				output.closeEntry();
			}

			input.close();
			output.close();

			inputFile.delete();
			outputTmpFile.renameTo(inputFile);
		} catch (FileNotFoundException e) {
			Logger.logError("Killing metainf failed: FileNotFound "+e.getMessage());
		} catch (IOException e) {
			Logger.logError("Killing metainf failed: IOException "+e.getMessage());
		}
	}

	/**
	 * @param modPackName - the name of the pack 
	 * @throws NoSuchAlgorithmException - see getCreeperHostLink
	 */
	protected void downloadModPack(String modPackName) throws NoSuchAlgorithmException {
		Logger.logInfo("Downloading...");
		URL website;
		try {
			Logger.logInfo("still downloading...");
			website = new URL(getCreeperhostLink(modPackName));
			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			fos = new FileOutputStream(Settings.getSettings().getInstallPath() + "/temp/" + modPackName);
			fos.getChannel().transferFrom(rbc, 0, 1 << 24);
		} catch (MalformedURLException e) {
			// No message for security
			Logger.logError("failed to download modpack: MalformedURL");
		} catch (IOException e) {
			// No Message for security
			Logger.logError("failed to download modpack: UIException ");
		}
		extractZip(Settings.getSettings().getInstallPath() + "/temp/" + modPackName);
		new File(Settings.getSettings().getInstallPath() + "/" + ModPack.getPack(modPacksPane.getSelectedModIndex()).getDir() + "/.minecraft/mods").delete();
		new File(Settings.getSettings().getInstallPath() + "/" + ModPack.getPack(modPacksPane.getSelectedModIndex()).getDir() + "/.minecraft/coremods").delete();
	 	File[] contents = new File(Settings.getSettings().getInstallPath() + "/" + ModPack.getPack(modPacksPane.getSelectedModIndex()).getDir() + "/.minecraft/bin/").listFiles();
		String files;
		for (int i = 0; i < contents.length; i++) {             
			if (contents[i].isFile()) {
				files = contents[i].getName();
				if (files.endsWith(".zip") || files.endsWith(".ZIP")) {
					contents[i].delete();
				}
			}	
		}
	}

	/**
	 * extracts zip to the location of the zip
	 * @param zipLocation - the location
	 */
	public void extractZip(String zipLocation) {
		try {
			byte[] buf = new byte[1024];
			ZipInputStream zipinputstream = null;
			ZipEntry zipentry;
			zipinputstream = new ZipInputStream(new FileInputStream(zipLocation));

			zipentry = zipinputstream.getNextEntry();
			while (zipentry != null) {
				// for each entry to be extracted
				String entryName = zipentry.getName();
				Logger.logInfo("entryname " + entryName);
				int n;
				FileOutputStream fileoutputstream;
				File newFile = new File(entryName);
				String directory = newFile.getParent();

				if (directory == null) {
					if (newFile.isDirectory()) {
						break;
					}
				}

				fileoutputstream = new FileOutputStream(zipLocation);

				while ((n = zipinputstream.read(buf, 0, 1024)) > -1) {
					fileoutputstream.write(buf, 0, n);
				}

				fileoutputstream.close();
				zipinputstream.closeEntry();
				zipentry = zipinputstream.getNextEntry();
			}
			zipinputstream.close();
		} catch (Exception e) {
			Logger.logError("failed to unzip: " + e.getMessage());
		}
	}

	/**
	 * @param src - the folder to be moved
	 * @param dest - where to move to
	 * @throws IOException
	 */
	public static void copyFolder(File src, File dest) throws IOException {
		if (src.isDirectory()) {
			// if directory not exists, create it
			if (!dest.exists()) {
				dest.mkdir();
				
				Logger.logInfo("Directory copied from " + src + "  to "
						+ dest);
			}

			// list all the directory contents
			String files[] = src.list();

			for (String file : files) {
				// construct the src and dest file structure
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				// recursive copy
				copyFolder(srcFile, destFile);
			}
		} else {
			// if file, then copy it
			// Use bytes stream to support all file types
			if (src.exists()) {
				InputStream in = new FileInputStream(src);
				OutputStream out = new FileOutputStream(dest);

				byte[] buffer = new byte[1024];

				int length;
				// copy the file content in bytes
				while ((length = in.read(buffer)) > 0) {
					out.write(buffer, 0, length);
				}

				in.close();
				out.close();
				Logger.logInfo("File copied from " + src + " to " + dest);
			}
		}
	}

	/**
	 * @param src - the file to be moved
	 * @param dest - where to move to
	 * @throws IOException
	 */
	public static void copyFile(File src, File dest) throws IOException {
		if (src.exists()) {
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dest);

			byte[] buffer = new byte[1024];

			int length;
			// copy the file content in bytes
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}

			in.close();
			out.close();
			Logger.logInfo("File copied from " + src + " to " + dest);
		}
	}

	/**
	 * @param resource - the resource to delete
	 * @return - the deleted resource
	 * @throws IOException
	 */
	public static boolean delete(File resource) throws IOException {
		if (resource.isDirectory()) {
			File[] childFiles = resource.listFiles();
			for (File child : childFiles) {
				delete(child);
			}
		}
		return resource.delete();
	}

	/**
	 * @param modPackName - The pack to install (should already be downloaded)
	 * @throws IOException
	 */
	protected void installMods(String modPackName) throws IOException {
		new File(Settings.getSettings().getInstallPath() + "/"+ ModPack.getPack(modPacksPane.getSelectedModIndex()).getDir() + "/.minecraft").mkdirs();
		Logger.logInfo("dirs mk'd");
		copyFolder(new File(Settings.getSettings().getInstallPath()+ "/.minecraft/bin/"), new File(Settings.getSettings().getInstallPath() + "/" 
				+ ModPack.getPack(modPacksPane.getSelectedModIndex()).getDir()+ "/.minecraft/bin"));
		File minecraft = new File(Settings.getSettings().getInstallPath()+ "/.minecraft/bin/minecraft.jar");
		File mcbackup = new File(Settings.getSettings().getInstallPath() + "/"+ modPackName + "/.minecraft/bin/mcbackup.jar");
		copyFile(minecraft, mcbackup);
		copyFolder(new File(Settings.getSettings().getInstallPath() + "/temp/" + ModPack.getPack(modPacksPane.getSelectedModIndex()).getDir() + "/.minecraft"), 
				new File(Settings.getSettings().getInstallPath() + "/" + ModPack.getPack(modPacksPane.getSelectedModIndex()).getDir() + "/.minecraft"));
	 	copyFile(new File(Settings.getSettings().getInstallPath() + "/temp/" + ModPack.getPack(modPacksPane.getSelectedModIndex()).getDir() + "/instMods/minecraftforge-universal-6.0.1.353.zip"), 
	 			new File(Settings.getSettings().getInstallPath() + "/" + ModPack.getPack(modPacksPane.getSelectedModIndex()).getDir() + "/.minecraft/bin/minecraftforge-universal-6.0.1.353.zip"));
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
			Logger.logError("failed save settings: FileNotFound " + e.getMessage());
			JOptionPane.showMessageDialog(this, "Failed to save config file: "
					+ e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			Logger.logError("failed save settings: IOException " + e.getMessage());
			JOptionPane.showMessageDialog(this, "Failed to save config file: "
					+ e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public static void writeUsers(String user) {
		try {
			userManager.write();
		} catch (IOException e) {
			Logger.logError("failed save userdata: IOException " + e.getMessage());
		}

		String[] usernames = UserManager.getNames().toArray(new String[] {});

		users.removeAllItems();
		for (String s : dropdown_) {
			users.addItem(s);
		}

		int counter = 1;
		for(String name : usernames) {
			counter++;
			users.addItem(name);
			if(name.equals(user)){
				users.setSelectedIndex(counter);
			}
		}
	}

	public static String[] merge(String[] A, String[] B) {
		String[] merged = new String[A.length+B.length];
		System.arraycopy(A, 0, merged, 0, A.length);
		System.arraycopy(B, 0, merged, A.length, B.length);
		return merged;
	}
	
	public static int getSelectedModIndex() {
		return instance.modPacksPane.getSelectedModIndex();
	} 

	public static LaunchFrame getInstance() {
		return instance;
	}

	private void enableObjects(){
		tabbedPane.setEnabledAt(0, true);
		tabbedPane.setEnabledAt(1, true);
		tabbedPane.setEnabledAt(2, true);
		tabbedPane.getSelectedComponent().setEnabled(true);
		launch.setEnabled(true);
		if(users.getSelectedIndex() > 1){
			edit.setEnabled(true);
		}
		users.setEnabled(true);
	}

	public void Hlink(MouseEvent me, URI uri) {
		if(Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			try {
				desktop.browse(uri);
			} catch(Exception exc) {
				Logger.logError("Exception while opening link: " + exc.getMessage());
			}
		} else {
			Logger.logWarn("Enviorment dont support opening of urls");
		}
	}
}
