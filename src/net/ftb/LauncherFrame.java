package net.ftb;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

public class LauncherFrame extends JFrame
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param args Program arguments.
	 */
	public static void main(String[] args)
	{
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
		
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		
		newsPane = new JTextPane();
		newsPane.setContentType("text/html");
		newsPane.setText("<html><body>insert news here</body></html>");
		newsPane.setEditable(false);
		
		JScrollPane newsScroll = new JScrollPane(newsPane);
		layout.addLayoutComponent(newsScroll, 
				new GridBagConstraints(0, 0, 2, 1, 1, 0.8, 
						GridBagConstraints.FIRST_LINE_START, 
						GridBagConstraints.BOTH, 
						new Insets(0, 0, 0, 0), 0, 0));
		add(newsScroll);
		
		
		JPanel filler = new JPanel();
		layout.addLayoutComponent(filler, 
				new GridBagConstraints(0, 1, 1, 1, 0.7, 0.2, 
						GridBagConstraints.SOUTHWEST,
						GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
		this.add(filler);
		
		
		// Login panel
		loginPanel = new JPanel();
		layout.addLayoutComponent(loginPanel, 
				new GridBagConstraints(1, 1, 1, 1, 0, 0.2, 
						GridBagConstraints.SOUTHEAST,
						GridBagConstraints.BOTH,
						new Insets(4, 4, 4, 4), 0, 0));
		add(loginPanel);
		
		GridBagLayout loginLayout = new GridBagLayout();
		loginPanel.setLayout(loginLayout);
		
		JLabel usernameLabel = new JLabel("Username:");
		loginLayout.addLayoutComponent(usernameLabel, 
				new GridBagConstraints(0, 0, 1, 1, 0, 0,
						GridBagConstraints.WEST,
						GridBagConstraints.HORIZONTAL,
						new Insets(4, 4, 4, 0), 8, 8));
		loginPanel.add(usernameLabel);
		
		usernameField = new JComboBox();
		usernameField.setEditable(true);
		loginLayout.addLayoutComponent(usernameField, 
				new GridBagConstraints(1, 0, 2, 1, 1, 0,
						GridBagConstraints.EAST,
						GridBagConstraints.HORIZONTAL,
						new Insets(4, 0, 4, 4), 8, 8));
		loginPanel.add(usernameField);
		
		JLabel passwordLabel = new JLabel("Password:");
		loginLayout.addLayoutComponent(passwordLabel, 
				new GridBagConstraints(0, 1, 1, 1, 0, 0,
						GridBagConstraints.NORTH,
						GridBagConstraints.HORIZONTAL,
						new Insets(4, 4, 4, 0), 8, 8));
		loginPanel.add(passwordLabel);
		
		passwordField = new JPasswordField();
		loginLayout.addLayoutComponent(passwordField, 
				new GridBagConstraints(1, 1, 2, 1, 1, 0,
						GridBagConstraints.NORTH,
						GridBagConstraints.HORIZONTAL,
						new Insets(4, 0, 4, 4), 0, 8));
		loginPanel.add(passwordField);
		
		rememberUsernameCheckBox = new JCheckBox("Remember username?");
		loginLayout.addLayoutComponent(rememberUsernameCheckBox, 
				new GridBagConstraints(1, 2, 1, 1, 0, 0,
						GridBagConstraints.CENTER,
						GridBagConstraints.HORIZONTAL,
						new Insets(4, 4, 4, 4), 8, 8));
		loginPanel.add(rememberUsernameCheckBox);
		
		rememberPasswordCheckBox = new JCheckBox("Remember password?");
		loginLayout.addLayoutComponent(rememberPasswordCheckBox, 
				new GridBagConstraints(2, 2, 1, 1, 0, 0,
						GridBagConstraints.CENTER,
						GridBagConstraints.HORIZONTAL,
						new Insets(4, 4, 4, 4), 8, 8));
		loginPanel.add(rememberPasswordCheckBox);
	}
	
	
	JPanel loginPanel;
	
	JTextPane newsPane;
	
	JComboBox usernameField;
	JPasswordField passwordField;
	
	JCheckBox rememberUsernameCheckBox;
	JCheckBox rememberPasswordCheckBox;
}
