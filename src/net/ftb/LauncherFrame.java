package net.ftb;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

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
		
		final int width = 600;
		final int height = 500;
		
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((int)size.getWidth() / 2 - width / 2, 
				(int)size.getHeight() / 2 - height / 2, 
				width, height);
	}
}
