package net.ftb.gui;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JRadioButton;
import javax.swing.JButton;

import net.ftb.data.LoginResponse;
import net.ftb.data.PasswordSettings;
import net.ftb.data.Settings;
import net.ftb.workers.GameUpdateWorker;
import net.ftb.workers.LoginWorker;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JCheckBox;
import javax.swing.ProgressMonitor;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.Color;
import javax.swing.SwingConstants;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.wb.swing.FocusTraversalOnArray;
import java.awt.SystemColor;
import javax.swing.border.BevelBorder;
import javax.swing.ImageIcon;

public class LauncherFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	JPanel loginPanel;
	JButton btnPlayOffline;
	private PasswordSettings passwordSettings;
	LoginResponse RESPONSE;
	JCheckBox chckbxRemember;
	JButton btnOptions;
	JLabel lblError;
	JButton btnLogin;
	private JPanel contentPane;
	private JTextField usernameField;
	private JPasswordField passwordField;
	public static String sysArch;
	private String[] jarMods;
	Image img;
	private static Point point = new Point();

	/**
	 * Launch the application.
	 */

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {

				try {
					UIManager.setLookAndFeel(UIManager
							.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					e.printStackTrace();
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

				try {
					LauncherFrame frame = new LauncherFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		});

		if(Integer.parseInt(System.getProperty("sun.arch.data.model")) == 64) {
			sysArch = "64";
		} else if (Integer.parseInt(System.getProperty("sun.arch.data.model")) == 32) {
			sysArch = "32";
		} else {
			System.out.println("Unknown");
			sysArch = "Unknown";
		}
	}

	/**
	 * Create the frame.
	 */

	public LauncherFrame() {
		final JFrame frame = this;
		frame.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				point.x = e.getX();
				point.y = e.getY();
			}
		});
		frame.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
				Point p = frame.getLocation();
				frame.setLocation(p.x + e.getX() - point.x, p.y + e.getY() - point.y);
			}
		});
		setFont(new Font("a_FuturaOrto", Font.PLAIN, 12));
		setIconImage(Toolkit.getDefaultToolkit().getImage("res//logo.png"));
		setResizable(false);
		setTitle("Feed the Beast Launcher");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		passwordSettings = new PasswordSettings(new File(Settings.getSettings()
				.getInstallPath(), "loginData"));

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 328, 229);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		loginPanel = new JPanel();
		loginPanel.setBorder(UIManager.getBorder("FileChooser.listViewBorder"));
		loginPanel.setForeground(new Color(24, 24, 24));
		loginPanel.setBounds(10, 11, 308, 128);
		loginPanel.setLayout(null);

		chckbxRemember = new JCheckBox("Remember Password");
		chckbxRemember.setBounds(86, 101, 125, 23);
		if (passwordSettings.getUsername() != "") {
			chckbxRemember.setSelected(true);
		} else {

		}

		btnOptions = new JButton("Options");
		btnOptions.setBounds(226, 39, 69, 23);
		btnOptions.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				OptionsDialogBeta optionsDlg = new OptionsDialogBeta();
				optionsDlg.setVisible(true);
			}
		});

		btnLogin = new JButton("Login");
		btnLogin.setBounds(226, 72, 69, 23);
		btnLogin.setEnabled(true);
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (chckbxRemember.isSelected()) {
					passwordSettings.storeUP(usernameField.getText(),
							new String(passwordField.getPassword()));
				} else {
					try {
						passwordSettings.flush();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				if (e.getActionCommand().equalsIgnoreCase("login")) {
					doLogin();
				}
			}
		});

		btnPlayOffline = new JButton("Play Offline");
		btnPlayOffline.setBounds(199, 11, 96, 23);
		btnPlayOffline.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					launchMinecraft(new File(Settings.getSettings()
							.getInstallPath()).getPath()
							+ "\\"
							+ getSelectedModPack() + "\\.minecraft",
							"OFFLINE", "1");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});


		lblError = new JLabel();
		lblError.setBounds(14, 15, 175, 14);
		lblError.setHorizontalAlignment(SwingConstants.LEFT);
		lblError.setForeground(Color.RED);

		usernameField = new JTextField("", 17);
		usernameField.setBounds(76, 39, 144, 22);
		usernameField.setText(passwordSettings.getUsername());

		passwordField = new JPasswordField("", 17);
		passwordField.setBounds(76, 72, 144, 22);
		passwordField.setText(passwordSettings.getPassword());
		loginPanel.add(passwordField);

		JLabel lblUsername = new JLabel("Username:");
		lblUsername.setBounds(14, 43, 52, 14);
		lblUsername.setDisplayedMnemonic('u');

		JLabel lblPassword = new JLabel("Password:");
		lblPassword.setBounds(16, 76, 50, 14);
		lblPassword.setDisplayedMnemonic('p');

		JLabel lblVersion = new JLabel("");
		lblVersion.setBounds(10, 150, 308, 48);

		lblVersion.setText("<html><body><center>FTB Launcher BETA 0.1<br>The non-beta version is more complete.<br>Please report all errors.</center></body></html>");
		lblVersion.setHorizontalAlignment(SwingConstants.CENTER);

		setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{usernameField, passwordField, chckbxRemember, btnLogin, btnOptions}));
		ButtonGroup group = new ButtonGroup();
		contentPane.add(lblVersion);
		contentPane.add(loginPanel);
		loginPanel.add(lblPassword);
		loginPanel.add(lblUsername);
		loginPanel.add(usernameField);
		loginPanel.add(lblError);
		loginPanel.add(btnLogin);
		loginPanel.add(btnOptions);
		loginPanel.add(chckbxRemember);
	}
	public void paintComponent(Graphics g)
	{
		g.drawImage(img, 0, 0, null);

	}


	public void doLogin() {
		btnLogin.setEnabled(false);
		btnOptions.setEnabled(false);
		usernameField.setEnabled(false);
		passwordField.setEnabled(false);
		chckbxRemember.setEnabled(false);

		lblError.setForeground(Color.black);
		lblError.setText("Logging in...");

		LoginWorker loginWorker = new LoginWorker(usernameField.getText(),
				new String(passwordField.getPassword())) {
			@Override
			public void done() {
				lblError.setText("");

				btnOptions.setEnabled(true);
				usernameField.setEnabled(true);
				passwordField.setEnabled(true);
				chckbxRemember.setEnabled(true);

				String responseStr;
				try {
					responseStr = get();
				} catch (InterruptedException err) {
					err.printStackTrace();
					loginPanel.add(btnPlayOffline);
					loginPanel.revalidate();
					loginPanel.repaint();
					return;
				} catch (ExecutionException err) {
					err.printStackTrace();
					if (err.getCause() instanceof IOException) {
						lblError.setForeground(Color.red);
						loginPanel.add(btnPlayOffline);
						loginPanel.revalidate();
						loginPanel.repaint();
						loginPanel.add(btnPlayOffline);
						lblError.setText("Login failed: "
								+ err.getCause().getMessage());
					} else if (err.getCause() instanceof MalformedURLException) {
						lblError.setForeground(Color.red);
						loginPanel.add(btnPlayOffline);
						loginPanel.revalidate();
						loginPanel.repaint();
						loginPanel.add(btnPlayOffline);
						lblError.setText("Error: Malformed URL");
					}
					return;
				}

				LoginResponse response;
				try {
					response = new LoginResponse(responseStr);
					RESPONSE = response;

				} catch (IllegalArgumentException e) {

					lblError.setForeground(Color.red);

					if (responseStr.contains(":")) {
						lblError.setText("Received invalid response from server.");
						btnLogin.setEnabled(true);
					} else {
						if (responseStr.equalsIgnoreCase("bad login")) {
							lblError.setText("Invalid username or password.");
							btnLogin.setEnabled(true);
							loginPanel.add(btnPlayOffline);
							loginPanel.revalidate();
							loginPanel.repaint();
							btnLogin.setEnabled(true);
						} else if (responseStr.equalsIgnoreCase("old version")){

							lblError.setText("Outdated launcher.");
							btnLogin.setEnabled(true);
						}
						else{
							btnLogin.setEnabled(true);
							lblError.setText("Login failed: " + responseStr);
						}

					}
					return;
				}

				lblError.setText("Login complete.");
				runGameUpdater(response);
			}
		};
		loginWorker.execute();
	}

	public String getSelectedModPack() {
		return "FTBLITE";
	}

	public void runGameUpdater(final LoginResponse response) {

		if (!new File(Settings.getSettings().getInstallPath() + "\\.minecraft\\bin\\minecraft.jar").exists()) {
			btnLogin.setEnabled(false);
			btnOptions.setEnabled(false);
			usernameField.setEnabled(false);
			passwordField.setEnabled(false);
			chckbxRemember.setEnabled(false);

			final ProgressMonitor progMonitor = new ProgressMonitor(this, "Downloading minecraft...", "", 0, 100);

			final GameUpdateWorker updater = new GameUpdateWorker(
					RESPONSE.getLatestVersion(), "minecraft.jar", new File(Settings.getSettings().getInstallPath(), ".minecraft//bin").getPath(), false) {
				public void done() {

					btnLogin.setEnabled(true);
					btnOptions.setEnabled(true);
					usernameField.setEnabled(true);
					passwordField.setEnabled(true);
					chckbxRemember.setEnabled(true);

					progMonitor.close();
					try {
						if (get() == true) {
							// Success
							lblError.setForeground(Color.black);
							lblError.setText("Game update complete.");

							// try {
							killMetaInf();
							try {
								// the old start testing code just put me in a infinite loop.
								
								launchMinecraft(new File(Settings.getSettings().getInstallPath()).getPath() + "/.minecraft", "TestingPlayer", "-");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						} else {
							lblError.setForeground(Color.red);
							lblError.setText("Error downloading game.");
						}
					} catch (CancellationException e) {
						lblError.setForeground(Color.black);
						lblError.setText("Game update cancelled...");
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
						lblError.setForeground(Color.red);
						lblError.setText("Failed to download game: "
								+ e.getCause().getMessage());
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
			try {
				launchMinecraft(new File(Settings.getSettings()
						.getInstallPath()).getPath()
						+ "\\"
						+ getSelectedModPack() + "\\.minecraft",
						RESPONSE.getUsername(), RESPONSE.getSessionID());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	protected String getVersionMD5(String modPackName) {
		InputStream is = null;
		MessageDigest md = null;
		File f = new File(Settings.getSettings().getInstallPath() + "\\"
				+ modPackName + "\\.minecraft\\bin\\minecraft.jar");
		if (f.exists()) {
			try {
				md = MessageDigest.getInstance("MD5");
				is = new FileInputStream(Settings.getSettings()
						.getInstallPath()
						+ "\\"
						+ modPackName
						+ "\\.minecraft\\bin\\minecraft.jar");
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				is = new DigestInputStream(is, md);
				// read stream to EOF as normal...
			} finally {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			String result = "";
			byte[] digest = md.digest();
			for (int i = 0; i < digest.length; i++) {
				result += Integer.toString((digest[i] & 0xff) + 0x100, 16)
						.substring(1);
			}
			return result;
		}
		return "0";
	}

	// Vbitz : I'm changing this back, there's a reason why we launch minecraft like this
	// A we can get the console easier and 2 we have complete control over it, including the location of the .minecraft dir
	// Once the mod loading code is working I will fix the ram settings, to fix them we will need to launch a new copy of the launcher
	// and then exit the old one. This is the same way the technic launcher does it for a good reason, we pretty much run
	// minecraft the same way.

	protected void launchMinecraft(String workingDir, String username,			
			String password) throws IOException {
		downloadModPack(getSelectedModPack());
		installMods(getSelectedModPack());
		try {
			System.out.println("Loading jars...");
			// if you want to test with forge then uncomment these following 2 lines after downloading the latest 1.3.2 version of minecraft forge from the forums
			// and putting it in your bin directory, you do not need to unzip the file just make sure it's named minecraftforge.zip

			String[] vanillaJarFiles = new String[] { "minecraft.jar", "lwjgl.jar", "lwjgl_util.jar", "jinput.jar" };			
			String[] jarFiles = concat(reverse(jarMods), vanillaJarFiles);
			for(int i = 0;i<jarFiles.length;i++){
				System.out.println(jarFiles[i]);
			}
			//String[] jarFiles = {"OptiFine_1.2.5_HD_S_C6.zip","NotEnoughItems-Client1.3.0.1.zip","CodeChickenCore-Client0.5.5.zip" ,"minecraftforge-client-3.3.8.164.zip","minecraft.jar", "lwjgl.jar", "lwjgl_util.jar", "jinput.jar" };
			
			//String[] jarFiles = new String[] { "minecraft.jar", "lwjgl.jar", "lwjgl_util.jar", "jinput.jar" };

			URL[] urls = new URL[jarFiles.length];

			for (int i = 0; i < urls.length; i++) {
				try {
					File f = new File(new File(workingDir, "bin"), jarFiles[i]);
					urls[i] = f.toURI().toURL();
					System.out.println("Loading URL: " + urls[i].toString());
				} catch (MalformedURLException e) {
					// e.printStackTrace();
					System.err
					.println("MalformedURLException, " + e.toString());
				}
			}

			System.out.println("Loading natives...");
			String nativesDir = new File(new File(workingDir, "bin"), "natives")
			.toString();

			System.setProperty("org.lwjgl.librarypath", nativesDir);
			System.setProperty("net.java.games.input.librarypath", nativesDir);

			System.setProperty("user.home", new File(workingDir).getParent());

			URLClassLoader cl = new URLClassLoader(urls,
					LauncherFrame.class.getClassLoader());

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
				System.out.println("Fixed Minecraft Path: Field was " + f.toString());
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
			lblError.setText("Minecraft not found");
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

	public static void killMetaInf() {
		// TODO Auto-generated method stub
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void downloadUrl(String filename, String urlString) throws MalformedURLException, IOException
    {
        BufferedInputStream in = null;
        FileOutputStream fout = null;
        try
        {
                in = new BufferedInputStream(new URL(urlString).openStream());
                fout = new FileOutputStream(filename);

                byte data[] = new byte[1024];
                int count;
                while ((count = in.read(data, 0, 1024)) != -1)
                {
                        fout.write(data, 0, count);
                }
        }
        finally
        {
                if (in != null)
                        in.close();
                if (fout != null)
                        fout.close();
        }
    }
	
	protected void downloadModPack(String modPackName) throws IOException {
		System.out.println("Downloading modpack");
		lblError.setText("Downloading modpack");
		new File(Settings.getSettings().getInstallPath() + "\\temp\\" + modPackName + "\\").mkdirs();
		new File(Settings.getSettings().getInstallPath() + "\\temp\\" + modPackName + "\\" + modPackName +".zip").createNewFile();
	    downloadUrl(Settings.getSettings().getInstallPath() + "\\temp\\" + modPackName + "\\" + modPackName +".zip","https://dl.dropbox.com/s/rgn3g179rdsobej/FTBLITE.zip?dl=1");
		extractZip(Settings.getSettings().getInstallPath() + "\\temp\\" + modPackName + "\\" + modPackName +".zip");
	}

	public void extractZip(String zipLocation) {
		try {
			byte[] buf = new byte[1024];
			ZipInputStream zipinputstream = null;
			ZipEntry zipentry;
			zipinputstream = new ZipInputStream(
					new FileInputStream(zipLocation));

			zipentry = zipinputstream.getNextEntry();
			while (zipentry != null) {
				// for each entry to be extracted
				String entryName = zipentry.getName();
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

				while ((n = zipinputstream.read(buf, 0, 1024)) > -1){
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

	public void extractZipTo(String zipLocation, String outputLocation)
			throws IOException {
		try {
			File fSourceZip = new File(zipLocation);
			String zipPath = outputLocation;
			File temp = new File(zipPath);
			temp.mkdir();
			ZipFile zipFile = new ZipFile(fSourceZip);
			Enumeration e = zipFile.entries();

			while (e.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) e.nextElement();
				File destinationFilePath = new File(zipPath, entry.getName());
				destinationFilePath.getParentFile().mkdirs();
				if (entry.isDirectory()) {
					continue;
				} else {
					BufferedInputStream bis = new BufferedInputStream(
							zipFile.getInputStream(entry));

					int b;
					byte buffer[] = new byte[1024];

					FileOutputStream fos = new FileOutputStream(
							destinationFilePath);
					BufferedOutputStream bos = new BufferedOutputStream(fos,
							1024);

					while ((b = bis.read(buffer, 0, 1024)) != -1) {
						bos.write(buffer, 0, b);
					}

					bos.flush();
					bos.close();
					bis.close();
				}
			}
		} catch (IOException ioe) {
			System.out.println("IOError :" + ioe);
		}

	}

	public static void copyFolder(File src, File dest) throws IOException {

		if (src.isDirectory()) {

			// if directory not exists, create it
			if (!dest.exists()) {
				dest.mkdir();
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
			}
		}
	}


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
		}
	}

	public static boolean delete(File resource) throws IOException {
		if (resource.isDirectory()) {
			File[] childFiles = resource.listFiles();
			for (File child : childFiles) {
				delete(child);
			}
		}
		return resource.delete();

	}

	protected String[] reverse(String[] x){
		String buffer[] = new String[x.length];
		for(int i = 0; i<x.length;i++){
			buffer[i] = x[x.length-i-1];
		}


		return buffer;

	}
	protected void installMods(String modPackName) throws IOException {
		new File(Settings.getSettings().getInstallPath() + "\\"+ getSelectedModPack() + "\\.minecraft").mkdirs();
		copyFolder(new File(Settings.getSettings().getInstallPath()+ "\\.minecraft\\bin\\"), new File(Settings.getSettings().getInstallPath()+ "\\"+ getSelectedModPack()+ "\\.minecraft\\bin"));
		File minecraft = new File(Settings.getSettings().getInstallPath()+ "\\.minecraft\\bin\\minecraft.jar");
		File mcbackup = new File(Settings.getSettings().getInstallPath() + "\\"+ modPackName + "\\.minecraft\\bin\\mcbackup.jar");
		//		minecraft.renameTo(new File(Settings.getSettings().getInstallPath()+ "\\" + modPackName + "\\.minecraft\\bin\\mcbackup.jar"));
		//		System.out.println("Renamed minecraft.jar to mcbackup.jar");
		JarFile packMinecraft = new JarFile(Settings.getSettings().getInstallPath()+ "\\"+ getSelectedModPack()+ "\\.minecraft\\bin\\minecraft.jar");
		copyFile(minecraft, mcbackup);
		jarMods = new String[new File(Settings.getSettings().getInstallPath() + "\\temp\\" + modPackName + "\\instMods").listFiles().length];
	
		try{
			// Open the file that is the first 
			// command line parameter
			FileInputStream fstream = new FileInputStream(Settings.getSettings().getInstallPath() + "\\temp\\" + modPackName + "\\modlist");
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			//Read File Line By Line
			int i=0;
				while ((strLine = br.readLine()) != null)   {
					// Print the content on the console
					jarMods[i] = strLine;
					i++;		
			}
			//Close the input stream
			in.close();
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		jarMods = reverse(jarMods);
		copyFolder(new File(Settings.getSettings().getInstallPath()+ "\\temp\\" + getSelectedModPack() + "\\instMods"), new File(Settings.getSettings().getInstallPath()+ "\\" + getSelectedModPack() +"\\.minecraft\\bin\\"));
		copyFolder(new File(Settings.getSettings().getInstallPath()+ "\\temp\\" + getSelectedModPack() + "\\.minecraft"), new File(Settings.getSettings().getInstallPath()+ "\\" + getSelectedModPack() +"\\.minecraft\\"));
		


	}

	protected String[] concat(String[] x, String[] y){
		String buffer[] = new String[x.length+y.length];
		int i = 0;
		for(i=0;i<buffer.length-1;i++){
			if(i<x.length){
				buffer[i] = x[i];
			}
			if(i<y.length){
				buffer[i+x.length] = y[i];
			}
		}
		return buffer;

	}
}
