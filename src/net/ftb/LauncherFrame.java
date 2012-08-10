package net.ftb;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
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
				new GridBagConstraints(0, 0, 2, 1, 1, 0.75, 
						GridBagConstraints.FIRST_LINE_START, 
						GridBagConstraints.BOTH, 
						new Insets(0, 0, 0, 0), 0, 0));
		add(newsScroll);
		
		
		// Login panel
		loginPanel = new JPanel();
		layout.addLayoutComponent(loginPanel, 
				new GridBagConstraints(1, 1, 1, 1, 0, 0.25, 
						GridBagConstraints.SOUTHEAST,
						GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
		add(loginPanel);
		
		GridBagLayout loginLayout = new GridBagLayout();
		loginPanel.setLayout(loginLayout);
		
		usernameField = new JTextField();
		loginLayout.addLayoutComponent(usernameField, 
				new GridBagConstraints(0, 0, 1, 1, 0, 0,
						GridBagConstraints.NORTH,
						GridBagConstraints.HORIZONTAL,
						new Insets(0, 0, 0, 0), 8, 8));
		loginPanel.add(usernameField);
		
		passwordField = new JTextField();
		loginLayout.addLayoutComponent(passwordField, 
				new GridBagConstraints(0, 1, 1, 1, 0, 0,
						GridBagConstraints.NORTH,
						GridBagConstraints.HORIZONTAL,
						new Insets(0, 0, 0, 0), 8, 8));
		loginPanel.add(passwordField);
	}
	
	
	JPanel loginPanel;
	
	JTextPane newsPane;
	
	JTextField usernameField;
	JTextField passwordField;
}
