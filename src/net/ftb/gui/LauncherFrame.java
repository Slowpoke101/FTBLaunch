package net.ftb.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidParameterException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;

import net.ftb.data.LoginResponse;
import net.ftb.data.Settings;
import net.ftb.util.AppUtils;

import javax.swing.SwingConstants;
import java.awt.Color;

public class LauncherFrame extends JFrame implements ActionListener
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param args Program arguments.
	 */
	public static void main(String[] args)
	{
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
			JOptionPane.showMessageDialog(null, 
					"Failed to load config file: " + e.getMessage(),
					"Error", JOptionPane.ERROR_MESSAGE);
		}
		
		// Create the install directory if it does not exist.
		File installDir = new File(Settings.getSettings().getInstallPath());
		if (!installDir.exists())
			installDir.mkdirs();
		
		LauncherFrame mainFrame = new LauncherFrame();
		mainFrame.setVisible(true);
	}
	
	public LauncherFrame()
	{
		super("FTB Launcher");
		
		initGui();
	}
	
	public void initGui()
	{
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		final int width = 1000;
		final int height = 600;
		
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((int)size.getWidth() / 2 - width / 2, 
				(int)size.getHeight() / 2 - height / 2, 
				width, height);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		newsPane = new JTextPane();
		newsPane.setContentType("text/html");
		newsPane.setText("<html><body>insert news here</body></html>");
		newsPane.setEditable(false);
		
		JScrollPane newsScroll = new JScrollPane(newsPane);
		getContentPane().add(newsScroll, BorderLayout.CENTER);
		
		bottomPanel = new JPanel();
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		bottomPanel.setLayout(new BorderLayout(0, 0));
		horizontalFiller = Box.createHorizontalGlue();
		bottomPanel.add(horizontalFiller, BorderLayout.CENTER);
		// Login panel
		loginPanel = new JPanel();
		bottomPanel.add(loginPanel, BorderLayout.EAST);
		GridBagLayout gbl_loginPanel = new GridBagLayout();
		gbl_loginPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0};
		gbl_loginPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE, 0.0, 0.0};
		loginPanel.setLayout(gbl_loginPanel);
		
		horizontalStrut = Box.createHorizontalStrut(5);
		GridBagConstraints gbc_horizontalStrut = new GridBagConstraints();
		gbc_horizontalStrut.insets = new Insets(0, 0, 5, 0);
		gbc_horizontalStrut.gridx = 3;
		gbc_horizontalStrut.gridy = 0;
		loginPanel.add(horizontalStrut, gbc_horizontalStrut);
		
		lblError = new JLabel();
		lblError.setForeground(Color.RED);
		lblError.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblError = new GridBagConstraints();
		gbc_lblError.gridwidth = 3;
		gbc_lblError.insets = new Insets(0, 0, 5, 5);
		gbc_lblError.gridx = 0;
		gbc_lblError.gridy = 1;
		loginPanel.add(lblError, gbc_lblError);
		
		lblUsername = new JLabel("Username:");
		GridBagConstraints gbc_lblUsername = new GridBagConstraints();
		gbc_lblUsername.insets = new Insets(0, 0, 5, 5);
		gbc_lblUsername.anchor = GridBagConstraints.EAST;
		gbc_lblUsername.gridx = 0;
		gbc_lblUsername.gridy = 2;
		loginPanel.add(lblUsername, gbc_lblUsername);
		
		btnOptions = new JButton("Options");
		btnOptions.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				OptionsDialog optionsDlg = new OptionsDialog();
				optionsDlg.setVisible(true);
			}
		});
		GridBagConstraints gbc_btnOptions = new GridBagConstraints();
		gbc_btnOptions.insets = new Insets(0, 0, 5, 5);
		gbc_btnOptions.gridx = 2;
		gbc_btnOptions.gridy = 2;
		loginPanel.add(btnOptions, gbc_btnOptions);
		
		lblPassword = new JLabel("Password:");
		GridBagConstraints gbc_lblPassword = new GridBagConstraints();
		gbc_lblPassword.insets = new Insets(0, 0, 5, 5);
		gbc_lblPassword.anchor = GridBagConstraints.EAST;
		gbc_lblPassword.gridx = 0;
		gbc_lblPassword.gridy = 3;
		loginPanel.add(lblPassword, gbc_lblPassword);
		
		usernameField = new JTextField("", 17);
		GridBagConstraints gbc_usernameField = new GridBagConstraints();
		gbc_usernameField.fill = GridBagConstraints.HORIZONTAL;
		gbc_usernameField.insets = new Insets(0, 0, 5, 5);
		gbc_usernameField.gridx = 1;
		gbc_usernameField.gridy = 2;
		loginPanel.add(usernameField, gbc_usernameField);
		
		passwordField = new JPasswordField("", 17);
		GridBagConstraints gbc_passwordField = new GridBagConstraints();
		gbc_passwordField.fill = GridBagConstraints.HORIZONTAL;
		gbc_passwordField.insets = new Insets(0, 0, 5, 5);
		gbc_passwordField.gridx = 1;
		gbc_passwordField.gridy = 3;
		loginPanel.add(passwordField, gbc_passwordField);
		
		chckbxRemember = new JCheckBox("Remember Password");
		GridBagConstraints gbc_chckbxRemember = new GridBagConstraints();
		gbc_chckbxRemember.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxRemember.anchor = GridBagConstraints.NORTHWEST;
		gbc_chckbxRemember.gridx = 1;
		gbc_chckbxRemember.gridy = 4;
		loginPanel.add(chckbxRemember, gbc_chckbxRemember);
		
		btnLogin = new JButton("Login");
		btnLogin.addActionListener(this);
		GridBagConstraints gbc_btnLogin = new GridBagConstraints();
		gbc_btnLogin.insets = new Insets(0, 0, 5, 5);
		gbc_btnLogin.gridx = 2;
		gbc_btnLogin.gridy = 3;
		loginPanel.add(btnLogin, gbc_btnLogin);
		
		verticalStrut = Box.createVerticalStrut(5);
		GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
		gbc_verticalStrut.insets = new Insets(0, 0, 5, 5);
		gbc_verticalStrut.gridy = 0;
		gbc_verticalStrut.gridx = 0;
		loginPanel.add(verticalStrut, gbc_verticalStrut);
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
		
		StringBuilder requestBuilder = new StringBuilder();
		requestBuilder.append("https://login.minecraft.net/?user=");
		requestBuilder.append(usernameField.getText());
		requestBuilder.append("&password=");
		requestBuilder.append(passwordField.getPassword());
		requestBuilder.append("&version=13");
		
		URL url;
		try
		{
			url = new URL(requestBuilder.toString());
		} catch (MalformedURLException e)
		{
			e.printStackTrace();
			return;
		}
		
		Thread loginThread = new Thread(new Runnable()
		{
			
			@Override
			public void run()
			{
				try
				{
					launcherFrame.loginComplete(AppUtils.downloadString(url));
				} catch (IOException e)
				{
					launcherFrame.loginFailed(e);
				}
			}
			
			public Runnable setValues(LauncherFrame frame, URL url)
			{
				this.launcherFrame = frame;
				this.url = url;
				return this;
			}
			
			public LauncherFrame launcherFrame;
			public URL url;
		}.setValues(this, url));
		loginThread.start();
	}
	
	public void loginComplete(String loginResponseStr)
	{
		lblError.setText("");
		
		btnLogin.setEnabled(true);
		btnOptions.setEnabled(true);
		usernameField.setEnabled(true);
		passwordField.setEnabled(true);
		chckbxRemember.setEnabled(true);
		
		LoginResponse response;
		try
		{
			response = new LoginResponse(loginResponseStr);
		} catch (IllegalArgumentException e)
		{
			lblError.setForeground(Color.red);
			
			if (loginResponseStr.contains(":"))
			{
				lblError.setText("Received invalid response from server.");
			}
			else
			{
				if (loginResponseStr.equalsIgnoreCase("bad login"))
					lblError.setText("Invalid username or password.");
				else if (loginResponseStr.equalsIgnoreCase("old version"))
					lblError.setText("Outdated launcher.");
				else
					lblError.setText("Login failed: " + loginResponseStr);
			}
			return;
		}
		
		// Temporary placeholder.
		JOptionPane.showMessageDialog(this, "Login complete.");
	}
	
	public void loginFailed(IOException e)
	{
		lblError.setForeground(Color.red);
		lblError.setText("Login failed: " + e);
		
		btnLogin.setEnabled(true);
		btnOptions.setEnabled(true);
		usernameField.setEnabled(true);
		passwordField.setEnabled(true);
		chckbxRemember.setEnabled(true);
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getActionCommand().equalsIgnoreCase("login"))
		{
			doLogin();
		}
	}
	
	private JPanel bottomPanel;
	private JPanel loginPanel;
	
	private JTextPane newsPane;
	
	private JTextField usernameField;
	private JPasswordField passwordField;
	private Component horizontalFiller;
	private JLabel lblUsername;
	private JLabel lblPassword;
	private JCheckBox chckbxRemember;
	private JButton btnLogin;
	private JButton btnOptions;
	private Component verticalStrut;
	private Component horizontalStrut;
	private JLabel lblError;
}
