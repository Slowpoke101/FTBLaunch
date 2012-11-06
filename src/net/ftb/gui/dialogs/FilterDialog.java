package net.ftb.gui.dialogs;

import java.awt.Toolkit;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.ftb.gui.LaunchFrame;

public class FilterDialog extends JDialog {
	private JPanel panel = new JPanel();
	private JLabel typeLbl = new JLabel("Mod Pack Type:"), originLbl = new JLabel("Mod Pack Origin:");
	private JComboBox typeBox = new JComboBox(new String[] {"Client", "Server"}), originBox = new JComboBox(new String[] {"All", "FTB", "3rd Party"});
	
	public FilterDialog(LaunchFrame instance) {
		super(instance, true);
		
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
		setTitle("Filter");
		setBounds(300, 300, 350, 230);
		setResizable(false);
		
		panel.setBounds(0, 0, 350, 230);
		panel.setLayout(null);
		setContentPane(panel);
                
		typeLbl.setBounds(10, 10, 100, 30);
		typeBox.setBounds(120, 10, 100, 30);
		
		originLbl.setBounds(10, 40, 100, 30);
		originBox.setBounds(120, 40, 100, 30);
                
		panel.add(typeLbl);
		panel.add(typeBox);
		panel.add(originLbl);
		panel.add(originBox);
	}
}
