package net.ftb.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JRadioButton;
import javax.swing.JButton;

import net.ftb.util.OSUtils;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JCheckBox;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.Color;
import javax.swing.SwingConstants;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import java.awt.Font;
import javax.swing.JList;

public class LaunchFrame extends JFrame {

	private JPanel contentPane;
	private JTextField usernameField;
	private JPasswordField passwordField;

	/**
	 * Launch the application.
	 */
	

	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
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
		
		JCheckBox checkBox = new JCheckBox("Remember Password");
		checkBox.setBounds(86, 101, 125, 23);
		loginPanel.add(checkBox);
		
		JButton btnOptions = new JButton("Options");
		btnOptions.setBounds(226, 39, 69, 23);
		loginPanel.add(btnOptions);
		
		JButton btnLogin = new JButton("Login");
		btnLogin.setBounds(226, 66, 69, 23);
		loginPanel.add(btnLogin);
		
		JButton btnPlayOffline = new JButton("Play Offline");
		btnPlayOffline.setBounds(199, 11, 96, 23);
		loginPanel.add(btnPlayOffline);
		
		JLabel lblStatus = new JLabel();
		lblStatus.setBounds(14, 15, 144, 14);
		loginPanel.add(lblStatus);
		lblStatus.setText("Invalid login data");
		lblStatus.setHorizontalAlignment(SwingConstants.LEFT);
		lblStatus.setForeground(Color.RED);
		
		usernameField = new JTextField("", 17);
		usernameField.setBounds(76, 39, 144, 22);
		loginPanel.add(usernameField);
		
		passwordField = new JPasswordField("", 17);
		passwordField.setBounds(76, 72, 144, 22);
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
		newsPane.setBounds(226, 11, 260, 264);
		contentPane.add(newsPane);
		
		JTextArea txtrHelloWorld = new JTextArea();
		txtrHelloWorld.setWrapStyleWord(true);
		txtrHelloWorld.setLineWrap(true);
		txtrHelloWorld.setEditable(false);
		txtrHelloWorld.setText("Hello world, these are the news! And this is just a test to see if the text can be scrolled down as needed, when the news are too long, which they will maybe be. I think this is enough");
		newsPane.setViewportView(txtrHelloWorld);
		
		JScrollPane modPacksPane = new JScrollPane();
		modPacksPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		modPacksPane.setBounds(6, 11, 210, 426);
		contentPane.add(modPacksPane);
		
		JPanel panel = new JPanel();
		modPacksPane.setViewportView(panel);
		panel.setLayout(null);
		
		JRadioButton radioButton = new JRadioButton("");
		radioButton.setBounds(6, 24, 28, 23);
		panel.add(radioButton);
		
		JRadioButton radioButton_1 = new JRadioButton("");
		radioButton_1.setBounds(6, 85, 28, 23);
		panel.add(radioButton_1);
		
		JLabel label_1 = new JLabel("");
		label_1.setBounds(29, 72, 175, 50);
		panel.add(label_1);
		
		JLabel label_2 = new JLabel("");
		label_2.setBounds(29, 133, 175, 50);
		panel.add(label_2);
		
		JRadioButton radioButton_2 = new JRadioButton("");
		radioButton_2.setBounds(6, 146, 28, 23);
		panel.add(radioButton_2);
		
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
		

		
	}
}
