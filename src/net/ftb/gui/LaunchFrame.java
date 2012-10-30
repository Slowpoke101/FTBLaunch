package net.ftb.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
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
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JFrame;

import net.ftb.data.LoginResponse;
import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.data.UserManager;
import net.ftb.data.events.ModPackListener;
import net.ftb.workers.GameUpdateWorker;
import net.ftb.workers.LoginWorker;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ProgressMonitor;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

//import com.sun.xml.internal.ws.api.config.management.policy.ManagementAssertion.Setting;

public class LaunchFrame extends JFrame implements ModPackListener {

	private static final long serialVersionUID = 1L;
	public static UserManager userManager;
	LoginResponse RESPONSE;

	/**
	 * the panels to appear in the tabs
	 */
	private JPanel newsPane;
	private JPanel optionsPane;
	private JPanel modPacksPane;
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
	@SuppressWarnings({"rawtypes"})
	private static JComboBox users;

	/**
	 * things to go on the modpacks panel
	 */
	JPanel packs;
	ArrayList<JPanel> packPanels;
	JScrollPane packsScroll;
	JLabel splash;
	JTextArea packInfo;
	public static int selectedPack = 0;
	

	/**
	 * things to go on the options panel
	 */
	public static JTextField installFolderTextField;
	private JToggleButton tglbtnForceUpdate;
	private JTextField ramMinimum;
	private JTextField ramMaximum;
	
	/**
	 * things to go on the news panel
	 */
	JEditorPane news;
	JScrollPane newsPanel;
	
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
					e.printStackTrace();
					JOptionPane.showMessageDialog(null,
							"Failed to load config file: " + e.getMessage(),
							"Error", JOptionPane.ERROR_MESSAGE);
				}

				// Create the install directory if it does not exist.
				File installDir = new File(Settings.getSettings()
						.getInstallPath());
				if (!installDir.exists())
					installDir.mkdirs();
				
				userManager = new UserManager(new File(installDir, "logindata"));
				
				//KeyChecker k = new KeyChecker();
				//k.setVisible(true);
				try {
					LauncherConsole con = new LauncherConsole();
					con.setVisible(true);
				} catch (IOException e) {
					e.printStackTrace();
				}
				LaunchFrame frame = new LaunchFrame(2);
				frame.setVisible(true);
				
				ModPack.addListener(frame);
				ModPack.LoadAll();
				
				
			}

		});
	}

	/**
	 * Create the frame.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public LaunchFrame(int tab) {
		setFont(new Font("a_FuturaOrto", Font.PLAIN, 12));
		setResizable(false);
		setTitle("Feed the Beast Launcher Beta v0.1");
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
		footerLogo.setBounds(20, 20, 32, 32);
		footerCreeper.setBounds(72, 20, 132, 42);
		
		try {
			userManager.read();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		String[] usernames = UserManager.getNames().toArray(new String[] {});
		
		String[] dropdown = merge(dropdown_, usernames);
		
		users = new JComboBox(dropdown);
		users.setBounds(550, 20, 150, 30);
		users.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(users.getSelectedIndex() == 1) {
					ProfileAdder p = new ProfileAdder();
					p.setVisible(true);
					users.setSelectedIndex(0);
				}
			}
		});
		
		launch.setBounds(711, 20, 100, 30);
		launch.addActionListener(new ActionListener() {
			@SuppressWarnings("static-access")
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(users.getSelectedIndex() > 1) {
					doLogin(userManager.getUsername(users.getSelectedItem().toString()), userManager.getPassword(users.getSelectedItem().toString()));
				}
			}
		});
		
		footer.add(users);
		footer.add(footerLogo);
		footer.add(footerCreeper);
		footer.add(launch);

		newsPane = new JPanel();
		newsPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		newsPane.setLayout(null);
		//newsPane.add(backgroundImage1);
		//newsPane.setBackground(back);
		
		modPacksPane = new JPanel();
		modPacksPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		modPacksPane.setLayout(null);
		//modPacksPane.add(backgroundImage3);
		//modPacksPane.setBackground(back);
		
		splash = new JLabel();
		splash.setBounds(420, 0, 410, 200);
//		splash.setIcon(new ImageIcon(ModPack.getPack(0).getImage()));
		modPacksPane.add(splash);
		
		packPanels = new ArrayList<JPanel>();
		
		//packPanels = new JPanel[ModPack.getPackArray().size()];
		//for(int i = 0; i < packPanels.length; i++) {
			
		//}
		//for (ModPack pack : ModPack.getPackArray())
		//	addPack(pack);
		
		// updatePacks(); 
		
		
		// i suggest some loading animation here until first mod gets added
		
		
		
		
		packs = new JPanel();
		packs.setBounds(0, 0, 420, (ModPack.getPackArray().size()) * 55);
		packs.setLayout(null);
		packs.setOpaque(false);
		
		// Not needed anymore
		/*for(JPanel p : packPanels) {
			packs.add(p);
		}*/
		
		packsScroll = new JScrollPane();
		packsScroll.setBounds(0, 0, 420, 300);
		packsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		packsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		packsScroll.setWheelScrollingEnabled(true);
		packsScroll.setOpaque(false);
		packsScroll.setViewportView(packs);
		modPacksPane.add(packsScroll);
		
		packInfo = new JTextArea("N/A");//ModPack.getPack(0).getInfo());
		packInfo.setBounds(420, 210, 410, 90);
		modPacksPane.add(packInfo);

		mapsPane = new JPanel();
		mapsPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		mapsPane.setLayout(null);
		//mapsPane.add(backgroundImage4);
		//mapsPane.setBackground(back);
		
		tpPane = new JPanel();
		tpPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		tpPane.setLayout(null);
		//tpPane.add(backgroundImage5);
		//tpPane.setBackground(back);

		news = new JEditorPane();
		news.setEditable(false);
		
		try {
			news.setPage("http://feed-the-beast.com/lanuchernews.php");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		newsPanel = new JScrollPane(news);
		newsPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		newsPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		newsPanel.setBounds(10, 10, 790, 290);
		newsPane.add(newsPanel);
		
		//Options Tab
		optionsPane = new JPanel();
		optionsPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		//optionsPane.add(backgroundImage2);
		//optionsPane.setBackground(back);

		getContentPane().add(optionsPane, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 87, 78, 117, 73, 97, 81, 38 };
		gbl_contentPanel.rowHeights = new int[] { 0, 0, 20, 26, 0, 29, 31, 0,0, 0, 0 };
		gbl_contentPanel.columnWeights = new double[] { 1.0, 0.0, 1.0, 1.0,1.0, 1.0, 0.0 };
		gbl_contentPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0,0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		optionsPane.setLayout(gbl_contentPanel);

		
		ramMaximum = new JTextField();
		GridBagConstraints gbc_textField_2 = new GridBagConstraints();
		gbc_textField_2.insets = new Insets(0, 0, 5, 5);
		gbc_textField_2.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_2.gridx = 2;
		gbc_textField_2.gridy = 7;
		ramMaximum.setText("1024");
		ramMaximum.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				ramMax = Integer.parseInt(ramMaximum.getText());
				saveSettings();
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				
			}
		});
				
		ramMinimum = new JTextField();
		GridBagConstraints gbc_textField_1 = new GridBagConstraints();
		gbc_textField_1.insets = new Insets(0, 0, 5, 5);
		gbc_textField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_1.gridx = 2;
		gbc_textField_1.gridy = 6;
		ramMinimum.setText("256");
		ramMinimum.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				ramMin = Integer.parseInt(ramMinimum.getText());
				saveSettings();
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				
			}
		});
		
		JButton installBrowseBtn = new JButton("...");
		installBrowseBtn.addActionListener(new ChooseDir());
		
		JLabel lblInstallFolder = new JLabel("Install folder:");
		GridBagConstraints gbc_lblInstallFolder = new GridBagConstraints();
		gbc_lblInstallFolder.anchor = GridBagConstraints.EAST;
		gbc_lblInstallFolder.insets = new Insets(8, 8, 5, 5);
		gbc_lblInstallFolder.gridx = 0;
		gbc_lblInstallFolder.gridy = 3;
		optionsPane.add(lblInstallFolder, gbc_lblInstallFolder);
		
		installFolderTextField = new JTextField();
		GridBagConstraints gbc_installFolderTextField = new GridBagConstraints();
		gbc_installFolderTextField.gridwidth = 5;
		gbc_installFolderTextField.insets = new Insets(8, 8, 5, 8);
		gbc_installFolderTextField.fill = GridBagConstraints.BOTH;
		gbc_installFolderTextField.gridx = 1;
		gbc_installFolderTextField.gridy = 3;
		installFolderTextField.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent arg0) {
				saveSettings();
			}
			
			@Override
			public void focusGained(FocusEvent arg0) {
				
			}
		});
		optionsPane.add(installFolderTextField, gbc_installFolderTextField);
		installFolderTextField.setColumns(10);
		
		GridBagConstraints gbc_installBrowseBtn = new GridBagConstraints();
		gbc_installBrowseBtn.insets = new Insets(8, 0, 5, 8);
		gbc_installBrowseBtn.gridx = 6;
		gbc_installBrowseBtn.gridy = 3;
		optionsPane.add(installBrowseBtn, gbc_installBrowseBtn);
		
		tglbtnForceUpdate = new JToggleButton("Force update?");
		GridBagConstraints gbc_tglbtnForceUpdate = new GridBagConstraints();
		gbc_tglbtnForceUpdate.insets = new Insets(4, 8, 8, 8);
		gbc_tglbtnForceUpdate.gridwidth = 5;
		gbc_tglbtnForceUpdate.fill = GridBagConstraints.HORIZONTAL;
		gbc_tglbtnForceUpdate.gridx = 1;
		gbc_tglbtnForceUpdate.gridy = 4;
		optionsPane.add(tglbtnForceUpdate, gbc_tglbtnForceUpdate);
		
		JLabel lblRamMinimum = new JLabel("RAM Minimum (M):");
		GridBagConstraints gbc_lblRamMinimum = new GridBagConstraints();
		gbc_lblRamMinimum.anchor = GridBagConstraints.EAST;
		gbc_lblRamMinimum.insets = new Insets(0, 0, 5, 5);
		gbc_lblRamMinimum.gridx = 1;
		gbc_lblRamMinimum.gridy = 6;
		optionsPane.add(lblRamMinimum, gbc_lblRamMinimum);
		optionsPane.add(ramMinimum, gbc_textField_1);
		ramMinimum.setColumns(10);
		
		JLabel lblRamMaximum = new JLabel("RAM Maximum (M):");
		GridBagConstraints gbc_lblRamMaximum = new GridBagConstraints();
		gbc_lblRamMaximum.anchor = GridBagConstraints.EAST;
		gbc_lblRamMaximum.insets = new Insets(0, 0, 5, 5);
		gbc_lblRamMaximum.gridx = 1;
		gbc_lblRamMaximum.gridy = 7;
		optionsPane.add(lblRamMaximum, gbc_lblRamMaximum);
		optionsPane.add(ramMaximum, gbc_textField_2);
		ramMaximum.setColumns(10);
		
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

		//if (passwordSettings.getUsername() != "") {
		//	//dostuff
		//} else {
		//	//dootherstuff
		//}
	}
	
	/*
	 * GUI Code to add a modpack to the selection
	 */
	public void addPack(ModPack pack) {
		final int packIndex = packPanels.size();
		System.out.println("adding pack "+packIndex);
		//ModPack pack = ModPack.getPack(i);
		final JPanel p = new JPanel();
		p.setBounds(0, packIndex * 55, 420, 55);
		p.setLayout(null);
		JLabel logo = new JLabel(new ImageIcon(pack.getLogo()));
		logo.setBounds(6, 6, 42, 42);
		logo.setVisible(true);
		JTextArea filler = new JTextArea(pack.getName() + " : " + pack.getAuthor() + "\n" + pack.getInfo());
		filler.setBorder(null);
		filler.setEditable(false);
		filler.setForeground(Color.white);
		filler.setBounds(6 + 42 + 10, 6, 420 - (6 + 42 - 6), 42);
		filler.setBackground(new Color(255, 255, 255, 0));
		MouseListener lin = new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				selectedPack = packIndex;
				updatePacks();
			}
		};
		p.addMouseListener(lin);
		filler.addMouseListener(lin);
		logo.addMouseListener(lin);
		p.add(filler);
		p.add(logo);
		packPanels.add(p);
		packs.add(p);
	}

	@Override
	public void onMobPackAdded(ModPack pack) {
		addPack(pack);
		updatePacks();
	}
	
	
	private void updatePacks() {
		for (int i = 0; i < packPanels.size(); i++) {
			if(selectedPack == i) {
				packPanels.get(i).setBackground(UIManager.getColor("control").darker().darker());
				splash.setIcon(new ImageIcon(ModPack.getPack(i).getImage()));
			} else {
				packPanels.get(i).setBackground(UIManager.getColor("control"));
			}
		}
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

		LoginWorker loginWorker = new LoginWorker(username, password) {
			@Override
			public void done() {
				System.out.println("");


				String responseStr;
				try {
					responseStr = get();
				} catch (InterruptedException err) {
					err.printStackTrace();
					return;
				} catch (ExecutionException err) {
					err.printStackTrace();
					if (err.getCause() instanceof IOException) {
						System.out.println("Login failed: "
								+ err.getCause().getMessage());
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
			
			final GameUpdateWorker updater = new GameUpdateWorker(RESPONSE.getLatestVersion(), "minecraft.jar", new File(Settings.getSettings().getInstallPath(), ".minecraft//bin").getPath(), false) {
				
				public void done() {
					
					progMonitor.close();
					try {
						if (get() == true) {
							// Success
							System.out.println("Game update complete.");

							// try {
							System.out.println(ModPack.getPack(selectedPack).getDir());
							killMetaInf();
							ModManager man = new ModManager(new JFrame(), true);
							man.setVisible(true);
							try {
								installMods(ModPack.getPack(selectedPack).getDir());
							} catch (IOException e) {
								e.printStackTrace();
							}
							// the old start testing code just put me in a
							// infinite loop.
							launchMinecraft(new File(Settings.getSettings().getInstallPath()).getPath()+ "/" + ModPack.getPack(selectedPack).getDir() + "/.minecraft",RESPONSE.getUsername(), RESPONSE.getSessionID());
							
						} else {
							System.out.println("Error downloading game.");
						}
					} catch (CancellationException e) {
						System.out.println("Game update cancelled...");
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
						System.out.println("Failed to download game: " + e.getCause().getMessage());
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
						if (prog < 0)
							prog = 0;
						else if (prog > 100)
							prog = 100;
						progMonitor.setProgress(prog);
						progMonitor.setNote(updater.getStatus());
					}
				}
			});
			updater.execute();
		} else {
			launchMinecraft(new File(Settings.getSettings().getInstallPath()).getPath()+ "/" + ModPack.getPack(selectedPack).getDir() + "/.minecraft",RESPONSE.getUsername(), RESPONSE.getSessionID());
		}
	}

	/**
	 * @param file - the name of the file, as saved o the repo
	 * @return - the direct link
	 * @throws NoSuchAlgorithmException - see md5
	 */
	public static String getCreeperhostLink(String file) throws NoSuchAlgorithmException {

		DateFormat sdf = new SimpleDateFormat("ddMMyy");

//		if (TimeZone.getTimeZone("Europe/London").inDaylightTime(new Date())) {
//			sdf.setTimeZone(TimeZone.getTimeZone("Etc/GMT+1"));
//		} else {
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
//		}

		String date = sdf.format(new Date());
		
//		String resolved = "file:///E:/Projects/mc/local/"+file;
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

	public void downloadPack(String dest, String file) throws NoSuchAlgorithmException, IOException{
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
			System.out.println("Loading jars...");
			String[] jarFiles = new String[] { "minecraftforge.zip","minecraft.jar", "lwjgl.jar", "lwjgl_util.jar","jinput.jar" };
			URL[] urls = new URL[jarFiles.length];

			for (int i = 0; i < urls.length; i++) {
				try {
					File f = new File(new File(workingDir, "bin"), jarFiles[i]);
					urls[i] = f.toURI().toURL();
					System.out.println("Loading URL: " + urls[i].toString());
				} catch (MalformedURLException e) {
					// e.printStackTrace();
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
				System.out.println("Fixed Minecraft Path: Field was "
						+ f.toString());
			}

			String[] mcArgs = new String[2];
			mcArgs[0] = username;
			mcArgs[1] = password;

			String mcDir = mc.getMethod("a", String.class)
					.invoke(null, (Object) "minecraft").toString();

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
	 * deletes the META-INF
	 */
	public static void killMetaInf() {
		File inputFile = new File(Settings.getSettings().getInstallPath()
				+ "/.minecraft/bin", "minecraft.jar");
		File outputTmpFile = new File(Settings.getSettings().getInstallPath()
				+ "/.minecraft/bin", "minecraft.jar.tmp");
		try {
			JarInputStream input = new JarInputStream(new FileInputStream(
					inputFile));
			JarOutputStream output = new JarOutputStream(new FileOutputStream(
					outputTmpFile));

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
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * @param modPackName - the name of the pack 
	 * @throws NoSuchAlgorithmException - see getCreeperHostLink
	 */
	protected void downloadModPack(String modPackName) throws NoSuchAlgorithmException {
		System.out.println("DOWNLAODING!!!");
		URL website;
		try {
			System.out.println("STILL DOWNLOADING!!!");
			website = new URL(getCreeperhostLink(modPackName));
			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			fos = new FileOutputStream(Settings.getSettings().getInstallPath() + "/temp/" + modPackName);
			fos.getChannel().transferFrom(rbc, 0, 1 << 24);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		extractZip(Settings.getSettings().getInstallPath() + "/temp/" + modPackName);
		new File(Settings.getSettings().getInstallPath() + "/" + ModPack.getPack(selectedPack).getDir() + "/.minecraft/mods").delete();
		new File(Settings.getSettings().getInstallPath() + "/" + ModPack.getPack(selectedPack).getDir() + "/.minecraft/coremods").delete();
		File[] contents = new File(Settings.getSettings().getInstallPath() + "/" + ModPack.getPack(selectedPack).getDir() + "/.minecraft/bin/").listFiles();
		String files;
		for (int i = 0; i < contents.length; i++){             
			if (contents[i].isFile()){
				files = contents[i].getName();
				if (files.endsWith(".zip") || files.endsWith(".ZIP")){
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
				System.out.println("entryname " + entryName);
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

			}// while

			zipinputstream.close();
		} catch (Exception e) {
			e.printStackTrace();
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
				System.out.println("Directory copied from " + src + "  to "
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
				System.out.println("File copied from " + src + " to " + dest);
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
			System.out.println("File copied from " + src + " to " + dest);
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
		new File(Settings.getSettings().getInstallPath() + "/"+ ModPack.getPack(selectedPack).getDir() + "/.minecraft").mkdirs();
		System.out.println("dirs mk'd");
		copyFolder(new File(Settings.getSettings().getInstallPath()+ "/.minecraft/bin/"), new File(Settings.getSettings().getInstallPath() + "/"+ ModPack.getPack(selectedPack).getDir()+ "/.minecraft/bin"));
		File minecraft = new File(Settings.getSettings().getInstallPath()+ "/.minecraft/bin/minecraft.jar");
		File mcbackup = new File(Settings.getSettings().getInstallPath() + "/"+ modPackName + "/.minecraft/bin/mcbackup.jar");
		minecraft.renameTo(new File(Settings.getSettings().getInstallPath() + "/" + modPackName + "/.minecraft/bin/mcbackup.jar"));
		System.out.println("Renamed minecraft.jar to mcbackup.jar");
		copyFile(minecraft, mcbackup);
		copyFolder(new File(Settings.getSettings().getInstallPath() + "/temp/" + ModPack.getPack(selectedPack).getDir() + "/.minecraft"), new File(Settings.getSettings().getInstallPath() + "/" + ModPack.getPack(selectedPack).getDir() + "/.minecraft"));
		copyFile(new File(Settings.getSettings().getInstallPath() + "/temp/" + ModPack.getPack(selectedPack).getDir() + "/instMods/minecraftforge.zip"), new File(Settings.getSettings().getInstallPath() + "/" + ModPack.getPack(selectedPack).getDir() + "/.minecraft/bin/minecraftforge.zip"));
	}

	/**
	 * "Loads" the settings from the settings class into their respective GUI
	 * controls.
	 */
	private void loadSettings() {
		Settings settings = Settings.getSettings();

		installFolderTextField.setText(settings.getInstallPath());
		
		tglbtnForceUpdate.getModel().setPressed(settings.getForceUpdate());
	}

	/**
	 * "Saves" the settings from the GUI controls into the settings class.
	 */
	private void saveSettings() {
		Settings settings = Settings.getSettings();

		settings.setInstallPath(installFolderTextField.getText());

		settings.setForceUpdate(tglbtnForceUpdate.getModel().isPressed());

		try {
			settings.save();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Failed to save config file: "
					+ e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Failed to save config file: "
					+ e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	@SuppressWarnings({ "unchecked" })
	public static void writeUsers() {
		try {
			userManager.write();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String[] usernames = UserManager.getNames().toArray(new String[] {});
		
		users.removeAllItems();
		for (String s : dropdown_) {
			users.addItem(s);
		}
		
		for(String name : usernames) {
			users.addItem(name);
		}
	}
	
	public static String[] merge(String[] A, String[] B) {
		String[] merged = new String[A.length+B.length];
		System.arraycopy(A, 0, merged, 0, A.length);
		System.arraycopy(B, 0, merged, A.length, B.length);
		return merged;
	}
	
	public static int getSelectedPack() {
		return selectedPack;
	}

	public static void setSelectedPack(int selectedPack) {
		LaunchFrame.selectedPack = selectedPack;
	}

}
