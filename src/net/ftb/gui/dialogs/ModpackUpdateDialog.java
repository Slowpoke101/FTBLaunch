package net.ftb.gui.dialogs;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import net.ftb.gui.LaunchFrame;
import net.ftb.tools.ModManager;

public class ModpackUpdateDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private JPanel panel = new JPanel();
	private JLabel textOne = new JLabel("A new mod pack version is available!");
	private JLabel textTwo = new JLabel("Do you wish to update?");
	private JButton yesButton = new JButton("Yes");
	private JButton noButton = new JButton("No");
	private JCheckBox backup = new JCheckBox("Back-up config files?");

	public ModpackUpdateDialog(LaunchFrame instance, boolean modal) {
		super(instance, modal);

		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
		setTitle("Mod Pack Update Available");
		setBounds(300, 300, 300, 140);
		setResizable(false);

		panel.setLayout(null);
		panel.setBounds(0, 0, 300, 140);
		setContentPane(panel);

		textOne.setBounds(0, 0, 300, 30);
		textOne.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(textOne);

		textTwo.setBounds(0, 20, 300, 30);
		textTwo.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(textTwo);

		yesButton.setBounds(65, 80, 80, 25);
		yesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ModManager.update = true;
				ModManager.backup = backup.isSelected();
				setVisible(false);
			}
		});
		panel.add(yesButton);

		noButton.setBounds(155, 80, 80, 25);
		noButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ModManager.update = false;
				setVisible(false);
			}
		});
		panel.add(noButton);

		backup.setBounds(0, 45, 300, 30);
		backup.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(backup);
	}
}
