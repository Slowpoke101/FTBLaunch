package net.ftb.gui;

import javax.swing.JDialog;
import javax.swing.JPanel;

public class ProfileAdder extends JDialog {
	private static final long serialVersionUID = 1L;
	
	JPanel panel = new JPanel();
	
	JRoundTextField username = new JRoundTextField(10);
	JRoundPasswordField password = new JRoundPasswordField(10);
	
	public ProfileAdder() {
		setBounds(300, 300, 300, 300);
		
		panel.setBounds(0, 0, 300, 300);
		setContentPane(panel);
		panel.setLayout(null);
		
		username.setBounds(100, 10, 100, 20);
		username.setVisible(true);
		panel.add(username);
		
		password.setBounds(100, 40, 100, 20);
		password.setVisible(true);
		panel.add(password);
	}
}
