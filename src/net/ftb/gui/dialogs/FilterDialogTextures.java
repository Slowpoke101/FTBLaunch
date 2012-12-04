package net.ftb.gui.dialogs;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.ftb.data.ModPack;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.panes.MapsPane;
import net.ftb.gui.panes.ModpacksPane;
import net.ftb.gui.panes.TexturepackPane;

public class FilterDialogTextures extends JDialog {
	private JPanel panel = new JPanel();
	private JLabel typeLbl = new JLabel("Mod Pack Type:"), originLbl = new JLabel("Mod Pack Origin:"), packLbl = new JLabel("Compatible Pack:");
	private JComboBox typeBox = new JComboBox(new String[] {"Client", "Server"}), originBox = new JComboBox(new String[] {"All", "FTB", "3rd Party"}), compatibleBox;
	private JButton applyButton = new JButton("Apply Filter"), cancelButton = new JButton("Cancel"), searchButton = new JButton("Search Packs");
	private final JLabel lblMinecraftVersion = new JLabel("Minecraft Version:");
	private final JComboBox comboBox = new JComboBox();
		
	public FilterDialogTextures(final TexturepackPane instance) {
		super(LaunchFrame.getInstance(), true);
		// TODO: Overhaul Filter dialog towards texture packs
		// Because more than likely ftb won't have a texture pack, and there is no server versions.
		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				instance.origin = (String)originBox.getSelectedItem();
				instance.updateFilter();
				setVisible(false);
			}
		});
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		searchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SearchDialog sd = new SearchDialog(instance);
				sd.setVisible(true);
			}
		});
	}

	private void setupGui() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
		setTitle("Filter");
		setBounds(300, 300, 230, 205);
		setResizable(false);
		panel.setBounds(0, 0, 230, 140);
		panel.setLayout(null);
		setContentPane(panel);
		typeLbl.setBounds(10, 10, 100, 30);
		typeBox.setBounds(120, 10, 100, 30);
		originLbl.setBounds(10, 40, 100, 30);
		originBox.setBounds(120, 40, 100, 30);
		applyButton.setBounds(10, 106, 100, 25);
		searchButton.setBounds(10, 105, 210, 25);
		getRootPane().setDefaultButton(applyButton);
		cancelButton.setBounds(120, 106, 100, 25);
		panel.add(typeLbl);
		panel.add(typeBox);
		panel.add(originLbl);
		panel.add(originBox);
		panel.add(applyButton);
		panel.add(cancelButton);
		//		panel.add(searchButton);
	}
}
