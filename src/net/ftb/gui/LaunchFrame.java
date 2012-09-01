package net.ftb.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JRadioButton;
import javax.swing.JButton;

import net.ftb.data.LoginResponse;
import net.ftb.data.PasswordSettings;
import net.ftb.data.Settings;
import net.ftb.util.OSUtils;
import net.ftb.workers.GameUpdateWorker;
import net.ftb.workers.LoginWorker;

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

import javax.swing.JList;

import org.eclipse.wb.swing.FocusTraversalOnArray;

public class LaunchFrame extends JFrame {

	JCheckBox chckbxRemember;
	JButton btnOptions;
	JLabel lblError;
	JButton btnLogin;
	private JPanel contentPane;
	private JTextField usernameField;
	private JPasswordField passwordField;
	
	private PasswordSettings passwordSettings;

	/**
	 * Launch the application.
	 */

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try
				{
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e)
				{
					e.printStackTrace();
				}

				// Load settings
				try
				{
					Settings.initSettings();
				} catch (IOException e)
				{
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "Failed to load config file: "
							+ e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}

				// Create the install directory if it does not exist.
				File installDir = new File(Settings.getSettings().getInstallPath());
				if (!installDir.exists())
					installDir.mkdirs();
				
				try {
					LaunchFrame frame = new LaunchFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		});
	}

	/**
	 * Create the frame.
	 */
	public LaunchFrame() {
		setResizable(false);
		setTitle("Feed the Beast Launcher");
		try {
			UIManager.setLookAndFeel(
					UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		passwordSettings = new PasswordSettings(new File(Settings.getSettings().getInstallPath(), "loginData"));

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 821, 480);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JPanel loginPanel = new JPanel();
		loginPanel.setBounds(496, 11, 305, 139);
		contentPane.add(loginPanel);
		loginPanel.setLayout(null);

		chckbxRemember = new JCheckBox("Remember Password");
		chckbxRemember.setBounds(86, 101, 125, 23);
		loginPanel.add(chckbxRemember);

		btnOptions = new JButton("Options");
		btnOptions.setBounds(226, 39, 69, 23);
		loginPanel.add(btnOptions);
		btnOptions.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				OptionsDialog optionsDlg = new OptionsDialog();
				optionsDlg.setVisible(true);
			}
		});

		btnLogin = new JButton("Login");
		btnLogin.setBounds(226, 72, 69, 23);
		loginPanel.add(btnLogin);
		btnLogin.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (e.getActionCommand().equalsIgnoreCase("login"))
				{
					doLogin();
				}
			}
		});

		JButton btnPlayOffline = new JButton("Play Offline");
		btnPlayOffline.setBounds(199, 11, 96, 23);
		loginPanel.add(btnPlayOffline);

		lblError = new JLabel();
		lblError.setBounds(14, 15, 144, 14);
		loginPanel.add(lblError);
		lblError.setHorizontalAlignment(SwingConstants.LEFT);
		lblError.setForeground(Color.RED);

		usernameField = new JTextField("", 17);
		usernameField.setBounds(76, 39, 144, 22);
		usernameField.setText(passwordSettings.getUsername());
		loginPanel.add(usernameField);

		passwordField = new JPasswordField("", 17);
		passwordField.setBounds(76, 72, 144, 22);
		passwordField.setText(passwordSettings.getPassword());
		loginPanel.add(passwordField);

		JLabel lblUsername = new JLabel("Username:");
		lblUsername.setBounds(14, 43, 52, 14);
		loginPanel.add(lblUsername);
		lblUsername.setDisplayedMnemonic('u');

		JLabel lblPassword = new JLabel("Password:");
		lblPassword.setBounds(16, 76, 50, 14);
		loginPanel.add(lblPassword);
		lblPassword.setDisplayedMnemonic('p');

		JScrollPane newsPane = new JScrollPane();
		newsPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		newsPane.setBounds(226, 41, 260, 234);
		contentPane.add(newsPane);

		JTextArea txtrNews = new JTextArea();
		txtrNews.setWrapStyleWord(true);
		txtrNews.setLineWrap(true);
		txtrNews.setEditable(false);
		txtrNews.setText("Hello world, these are the news! And this is just a test to see if the text can be scrolled down as needed, when the news are too long, which they will maybe be. I think this is enough");
		newsPane.setViewportView(txtrNews);

		JScrollPane modPacksPane = new JScrollPane();
		modPacksPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		modPacksPane.setBounds(6, 11, 210, 426);
		contentPane.add(modPacksPane);

		JPanel panel = new JPanel();
		modPacksPane.setViewportView(panel);
		panel.setLayout(null);

		JRadioButton modPack1RB = new JRadioButton("");
		modPack1RB.setBounds(6, 24, 28, 23);
		panel.add(modPack1RB);

		JRadioButton modPack2RB = new JRadioButton("");
		modPack2RB.setBounds(6, 85, 28, 23);
		panel.add(modPack2RB);

		JLabel lblModPack2 = new JLabel("");
		lblModPack2.setBounds(29, 72, 175, 50);
		panel.add(lblModPack2);

		JLabel lblModPack1 = new JLabel("");
		lblModPack1.setBounds(29, 11, 175, 50);
		panel.add(lblModPack1);

		JPanel sponsorPanel = new JPanel();
		sponsorPanel.setBounds(496, 166, 305, 109);
		contentPane.add(sponsorPanel);

		JLabel lblTexturePacks = new JLabel("Texture packs");
		lblTexturePacks.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblTexturePacks.setBounds(226, 286, 260, 19);
		contentPane.add(lblTexturePacks);

		JList texturesList = new JList();
		texturesList.setBounds(226, 305, 258, 132);
		contentPane.add(texturesList);

		JLabel lblWorldPacks = new JLabel("World packs");
		lblWorldPacks.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblWorldPacks.setBounds(496, 286, 91, 19);
		contentPane.add(lblWorldPacks);

		JLabel label = new JLabel("");
		label.setBounds(226, 305, 260, 132);
		contentPane.add(label);

		JList worldsList = new JList();
		worldsList.setBounds(496, 305, 305, 132);
		contentPane.add(worldsList);

		JLabel lblNews = new JLabel("News");
		lblNews.setFont(new Font("Tahoma", Font.BOLD, 17));
		lblNews.setBounds(226, 11, 113, 19);
		contentPane.add(lblNews);


		setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[] {
				usernameField, passwordField, chckbxRemember, btnLogin,
				btnOptions, txtrNews }));

	}
	public void doLogin()
	{
		btnLogin.setEnabled(false);
		btnOptions.setEnabled(false);
		usernameField.setEnabled(false);
		passwordField.setEnabled(false);
		chckbxRemember.setEnabled(false);

		lblError.setForeground(Color.black);
		lblError.setText("Logging in...");

		LoginWorker loginWorker = new LoginWorker(usernameField.getText(),
				new String(passwordField.getPassword()))
		{
			@Override
			public void done()
			{
				lblError.setText("");

				btnLogin.setEnabled(true);
				btnOptions.setEnabled(true);
				usernameField.setEnabled(true);
				passwordField.setEnabled(true);
				chckbxRemember.setEnabled(true);

				String responseStr;
				try
				{
					responseStr = get();
				} catch (InterruptedException err)
				{
					err.printStackTrace();
					return;
				} catch (ExecutionException err)
				{
					err.printStackTrace();
					if (err.getCause() instanceof IOException)
					{
						lblError.setForeground(Color.red);
						lblError.setText("Login failed: "
								+ err.getCause().getMessage());
					}
					else if (err.getCause() instanceof MalformedURLException)
					{
						lblError.setForeground(Color.red);
						lblError.setText("Error: Malformed URL");
					}
					return;
				}

				LoginResponse response;
				try
				{
					response = new LoginResponse(responseStr);
					passwordSettings.storeUP(usernameField.getText(), new String(passwordField.getPassword()));
				} catch (IllegalArgumentException e)
				{
					lblError.setForeground(Color.red);

					if (responseStr.contains(":"))
					{
						lblError.setText("Received invalid response from server.");
					}
					else
					{
						if (responseStr.equalsIgnoreCase("bad login"))
							lblError.setText("Invalid username or password.");
						else if (responseStr.equalsIgnoreCase("old version"))
							lblError.setText("Outdated launcher.");
						else
							lblError.setText("Login failed: " + responseStr);
					}
					return;
				}

				lblError.setText("Login complete.");
				if(getVersionMD5().equals("d41d8cd98f00b204e9800998ecf8427e")){
					try {
						launchMinecraft(new File(Settings.getSettings().getInstallPath()).getPath() + "//.minecraft", "PlayerTesting", "-");
					} catch (IOException ex) {
						System.out.println(ex.toString());
					}
				}else{
					runGameUpdater(response);
				}
				
			}
		};
		loginWorker.execute();
	}

	public void runGameUpdater(LoginResponse response)
	{
		btnLogin.setEnabled(false);
		btnOptions.setEnabled(false);
		usernameField.setEnabled(false);
		passwordField.setEnabled(false);
		chckbxRemember.setEnabled(false);

		final ProgressMonitor progMonitor = 
				new ProgressMonitor(this, "Downloading minecraft...", "", 0, 100);

		final GameUpdateWorker updater = new GameUpdateWorker(response.getLatestVersion(), 
				"minecraft.jar", 
				new File(Settings.getSettings().getInstallPath(), ".minecraft//bin").getPath(), 
				false)
		{
			public void done()
			{
				btnLogin.setEnabled(true);
				btnOptions.setEnabled(true);
				usernameField.setEnabled(true);
				passwordField.setEnabled(true);
				chckbxRemember.setEnabled(true);

				progMonitor.close();
				try
				{
					if (get() == true)
					{
						// Success
						lblError.setForeground(Color.black);
						lblError.setText("Game update complete.");
						try {
							launchMinecraft(new File(Settings.getSettings().getInstallPath()).getPath() + "//.minecraft", "PlayerTesting", "-");
						} catch (IOException ex) {
							System.out.println(ex.toString());
						}
					}
					else
					{
						lblError.setForeground(Color.red);
						lblError.setText("Error downloading game.");
					}
				} catch (CancellationException e)
				{
					lblError.setForeground(Color.black);
					lblError.setText("Game update cancelled...");
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				} catch (ExecutionException e)
				{
					e.printStackTrace();
					lblError.setForeground(Color.red);
					lblError.setText("Failed to download game: " + e.getCause().getMessage());
					return;
				}
			}
		};

		updater.addPropertyChangeListener(new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				if (progMonitor.isCanceled())
				{
					updater.cancel(false);
				}

				if (!updater.isDone())
				{
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
	}

	protected String getVersionMD5(){
		InputStream is = null;
		MessageDigest md = null;
		try{
		md = MessageDigest.getInstance("MD5");
		is = new FileInputStream(OSUtils.getDefInstallPath() + "\\.minecraft\\bin\\minecraft.jar");
		}catch(Exception e){
			e.printStackTrace();
		}
		try {
		  is = new DigestInputStream(is, md);
		  // read stream to EOF as normal...
		}
		finally {
		  try {
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		String result = "";
		byte[] digest = md.digest();
		for (int i=0; i < digest.length; i++) {
	           result += Integer.toString( ( digest[i] & 0xff ) + 0x100, 16).substring( 1 );
	       }
	       return result;
	}
	
	protected void launchMinecraft(String workingDir, String username, String password) throws IOException {
		try
		{
			System.out.println("Loading jars...");
			String[] jarFiles = new String[] {
					"minecraft.jar", "lwjgl.jar", "lwjgl_util.jar", "jinput.jar"
			};

			URL[] urls = new URL[jarFiles.length];

			for (int i = 0; i < urls.length; i++)
			{
				try
				{
					File f = new File(new File(workingDir, "bin"), jarFiles[i]);
					urls[i] = f.toURI().toURL();
					System.out.println("Loading URL: " + urls[i].toString());
				} catch (MalformedURLException e)
				{
					//					e.printStackTrace();
					System.err.println("MalformedURLException, " + e.toString());
					System.exit(5);
				}
			}

			System.out.println("Loading natives...");
			String nativesDir = new File(new File(workingDir, "bin"), "natives").toString();

			System.setProperty("org.lwjgl.librarypath", nativesDir);
			System.setProperty("net.java.games.input.librarypath", nativesDir);

			System.setProperty("user.home", new File(workingDir).getParent());

			URLClassLoader cl = 
					new URLClassLoader(urls, LaunchFrame.class.getClassLoader());

			// Get the Minecraft Class.
			Class<?> mc = cl.loadClass("net.minecraft.client.Minecraft");
			Field[] fields = mc.getDeclaredFields();

			for (int i = 0; i < fields.length; i++)
			{
				Field f = fields[i];
				if (f.getType() != File.class)
				{
					// Has to be File
					continue;
				}
				if (f.getModifiers() != (Modifier.PRIVATE + Modifier.STATIC))
				{
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

			String mcDir = 	mc.getMethod("a", String.class).invoke(null, (Object) "minecraft").toString();

			System.out.println("MCDIR: " + mcDir);

			mc.getMethod("main", String[].class).invoke(null, (Object) mcArgs);
		} catch (ClassNotFoundException e)
		{
			e.printStackTrace();
			System.exit(1);
		} catch (IllegalArgumentException e)
		{
			e.printStackTrace();
			System.exit(2);
		} catch (IllegalAccessException e)
		{
			e.printStackTrace();
			System.exit(2);
		} catch (InvocationTargetException e)
		{
			e.printStackTrace();
			System.exit(3);
		} catch (NoSuchMethodException e)
		{
			e.printStackTrace();
			System.exit(3);
		} catch (SecurityException e)
		{
			e.printStackTrace();
			System.exit(4);
		}
	}
}
