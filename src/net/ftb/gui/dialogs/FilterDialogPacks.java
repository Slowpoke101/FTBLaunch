package net.ftb.gui.dialogs;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.ftb.data.ModPack;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.panes.ModpacksPane;

public class FilterDialogPacks extends JDialog {
	private JPanel panel = new JPanel();
	private JLabel typeLbl = new JLabel("Mod Pack Type:"), originLbl = new JLabel("Mod Pack Origin:"), packLbl = new JLabel("Compatible Pack:");
	private JComboBox typeBox = new JComboBox(new String[] {"Client", "Server"}), originBox = new JComboBox(new String[] {"All", "FTB", "3rd Party"}), compatibleBox;
	private JButton applyButton = new JButton("Apply Filter"), cancelButton = new JButton("Cancel"), btnSearch = new JButton("Search"), btnAddPack = new JButton("Add Pack");
	private final JLabel lblMinecraftVersion = new JLabel("Minecraft Version:");
	private JComboBox comboBox;

	private ModpacksPane pane;

	public FilterDialogPacks(ModpacksPane instance) {
		super(LaunchFrame.getInstance(), true);
		setupGui();
		this.pane = instance;
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
		applyButton.setBounds(10, 142, 100, 25);
		getRootPane().setDefaultButton(applyButton);
		cancelButton.setBounds(120, 142, 100, 25);
		btnSearch.setBounds(10, 111, 100, 25);
		btnAddPack.setBounds(120, 111, 100, 25);
		panel.add(btnAddPack);
		panel.add(btnSearch);
		panel.add(typeLbl);
		panel.add(typeBox);
		panel.add(originLbl);
		panel.add(originBox);
		panel.add(applyButton);
		panel.add(cancelButton);

		ArrayList<String> mcVersions = new ArrayList<String>();

		mcVersions.add("All");

		for(int i = 0; i < ModPack.getPackArray().size(); i++) {
			String mcVersion = ModPack.getPack(i).getMcVersion();
			if(!mcVersions.contains(mcVersion)) {
				mcVersions.add(mcVersion);
			}
		}

		comboBox = new JComboBox(mcVersions.toArray());

		typeBox.setSelectedItem(pane.type);
		originBox.setSelectedItem(pane.origin);
		lblMinecraftVersion.setBounds(10, 70, 100, 30);

		panel.add(lblMinecraftVersion);
		comboBox.setBounds(120, 70, 100, 30);

		panel.add(comboBox);
		
		btnAddPack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AddPackDialog ap = new AddPackDialog();
				setVisible(false);
				ap.setVisible(true);
				ap.setAlwaysOnTop(true);
			}
		});
		
		btnSearch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SearchDialog sd = new SearchDialog(pane);
				sd.setVisible(true);
				setVisible(false);
			}
		});
		
		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				pane.type = (String)typeBox.getSelectedItem();
				pane.origin = (String)originBox.getSelectedItem();
				pane.mcVersion = (String)comboBox.getSelectedItem();
				pane.updateFilter();
				setVisible(false);
			}
		});
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
	}
}
