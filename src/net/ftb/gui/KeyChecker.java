package net.ftb.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextField;

public class KeyChecker extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private JTextField key;
	private JButton button;
	
	public KeyChecker() {
		setBounds(100, 100, 200, 200);
		setResizable(false);
		
		key = new JTextField("Enter key here");
		key.setBounds(50,50,100,30);
		key.setVisible(true);
		add(key);
		button = new JButton("Go!!!");
		button.setBounds(50, 100, 100, 30);
		button.setVisible(true);
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(rightKey(key.getText())) {
					LaunchFrame frame = new LaunchFrame();
					frame.setVisible(true);
					setVisible(false);
				} else {
					System.exit(0);
				}
			}
		});
		add(button);
		
	}
	
	public boolean rightKey(String key) {
		if(key.equals("abc")) {
			return true;
		} else {
			return false;
		}
	}
}
