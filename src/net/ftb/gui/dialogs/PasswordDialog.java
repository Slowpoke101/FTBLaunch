package net.ftb.gui.dialogs;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import net.ftb.gui.LaunchFrame;

public class PasswordDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	JPanel panel = new JPanel();
	JPasswordField password = new JPasswordField(1);
	JLabel passLabel = new JLabel("Password:");
	JButton submitButton = new JButton("Submit");

	public PasswordDialog(LaunchFrame instance, boolean modal) {
		super(instance, modal);

		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
		setTitle("Please Enter Password");
		setBounds(300, 300, 300, 120);
		setResizable(false);

		getRootPane().setDefaultButton(submitButton);

		panel.setBounds(0, 0, 300, 100);
		setContentPane(panel);
		panel.setLayout(null);

		passLabel.setBounds(10, 10, 80, 30);
		passLabel.setVisible(true);
		panel.add(passLabel);

		password.setBounds(100, 10, 170, 30);
		password.setVisible(true);
		panel.add(password);

		submitButton.setBounds(105, 50, 90, 25);
		submitButton.setVisible(true);
		submitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if(!new String(password.getPassword()).isEmpty()){
					LaunchFrame.getInstance().tempPass = new String(password.getPassword());
					setVisible(false);
				}
			}
		});
		panel.add(submitButton);
	}
}
