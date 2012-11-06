package net.ftb.gui.dialogs;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.ftb.gui.LaunchFrame;
import net.ftb.gui.panes.ModpacksPane;

public class FilterDialog extends JDialog {
	private JPanel panel = new JPanel();
	private JLabel typeLbl = new JLabel("Mod Pack Type:"), originLbl = new JLabel("Mod Pack Origin:");
	private JComboBox typeBox = new JComboBox(new String[] {"Client", "Server"}), originBox = new JComboBox(new String[] {"All", "FTB", "3rd Party"});
	private JButton applyButton = new JButton("Apply Filter"), cancelButton = new JButton("Cancel");

	public FilterDialog(final ModpacksPane instance) {
		super(LaunchFrame.getInstance(), true);

		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
		setTitle("Filter");
		setBounds(300, 300, 230, 140);
		setResizable(false);

		panel.setBounds(0, 0, 230, 140);
		panel.setLayout(null);
		setContentPane(panel);

		typeLbl.setBounds(10, 10, 100, 30);
		typeBox.setBounds(120, 10, 100, 30);
		typeBox.setSelectedItem(instance.type);

		originLbl.setBounds(10, 40, 100, 30);
		originBox.setBounds(120, 40, 100, 30);
		originBox.setSelectedItem(instance.origin);

		applyButton.setBounds(10, 80, 100, 25);
		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String type = "", origin = "";
				switch(typeBox.getSelectedIndex()) {
				case 0:
					type = "Client";
					break;
				case 1:
					type = "Server";
					break;
				}
				switch(originBox.getSelectedIndex()) {
				case 0:
					origin = "All";
					break;
				case 1:
					origin = "FTB";
					break;
				case 2:
					origin = "3rd Party";
					break;
				}
				instance.type = type;
				instance.origin = origin;
				instance.updateFilter();
				setVisible(false);
			}
		});

		getRootPane().setDefaultButton(applyButton);

		cancelButton.setBounds(120, 80, 100, 25);
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});

		panel.add(typeLbl);
		panel.add(typeBox);
		panel.add(originLbl);
		panel.add(originBox);
		panel.add(applyButton);
		panel.add(cancelButton);
	}
}
