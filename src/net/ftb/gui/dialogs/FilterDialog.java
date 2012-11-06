package net.ftb.gui.dialogs;

import java.awt.Toolkit;

import javax.swing.JDialog;
import javax.swing.JPanel;

import net.ftb.gui.LaunchFrame;

public class FilterDialog extends JDialog {
	private JPanel panel = new JPanel();
	
	public FilterDialog(LaunchFrame instance) {
		super(instance, true);
		
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
		setTitle("Filter");
		setBounds(300, 300, 350, 230);
		setResizable(false);
		
		setContentPane(panel);
		
		
	}
}
