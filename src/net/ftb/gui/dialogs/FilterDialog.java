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

public class FilterDialog extends JDialog {
	private JPanel panel = new JPanel();
	private JLabel typeLbl = new JLabel("Mod Pack Type:"), originLbl = new JLabel("Mod Pack Origin:"), packLbl = new JLabel("Compatible Pack:");
	private JComboBox typeBox = new JComboBox(new String[] {"Client", "Server"}), originBox = new JComboBox(new String[] {"All", "FTB", "3rd Party"}), compatibleBox;
	private JButton applyButton = new JButton("Apply Filter"), cancelButton = new JButton("Cancel"), searchButton = new JButton("Search Packs");
	private final JLabel lblMinecraftVersion = new JLabel("Minecraft Version:");
	private final JComboBox comboBox = new JComboBox();

	public FilterDialog(final ModpacksPane instance) {
		super(LaunchFrame.getInstance(), true);
		setupGui();
		typeBox.setSelectedItem(instance.type);
		originBox.setSelectedItem(instance.origin);

		searchButton.setBounds(10, 142, 210, 25);

		panel.add(searchButton);
		lblMinecraftVersion.setBounds(10, 70, 100, 25);

		panel.add(lblMinecraftVersion);
		comboBox.setBounds(120, 70, 100, 25);

		panel.add(comboBox);
		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				instance.type = (String)typeBox.getSelectedItem();
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
				setVisible(false);
			}
		});
	}

	public FilterDialog(final MapsPane instance) {
		super(LaunchFrame.getInstance(), true);
		setupGui();

		setBounds(300, 300, 230, 250);
		panel.setBounds(0, 0, 230, 250);
		applyButton.setBounds(10, 110, 100, 25);
		cancelButton.setBounds(120, 110, 100, 25);

		typeBox.setSelectedItem(instance.type);
		originBox.setSelectedItem(instance.origin);

		packLbl.setBounds(10, 70, 100, 30);
		panel.add(packLbl);

		String[] packs = new String[ModPack.getPackArray().size() + 1];
		packs[0] = "All";
		for(int i = 1; i < packs.length; i++) {
			packs[i] = ModPack.getPack(i - 1).getDir();
		}
		compatibleBox = new JComboBox(packs);
		compatibleBox.setBounds(120, 70, 100, 30);
		compatibleBox.setSelectedItem(instance.compatible);
		panel.add(compatibleBox);

		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				instance.compatible = (String)compatibleBox.getSelectedItem();
				instance.type = (String)typeBox.getSelectedItem();
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

	public FilterDialog(final TexturepackPane instance) {
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
