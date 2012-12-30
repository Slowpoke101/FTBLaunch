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
	private JLabel typeLbl = new JLabel("Mod Pack Type:"), originLbl = new JLabel("Mod Pack Origin:"), packLbl = new JLabel("Compatible Pack:"), lblModPackAval = new JLabel("Mod Pack Avaliability:");
	private JComboBox typeBox = new JComboBox(new String[] {"Client", "Server"}), 
			originBox = new JComboBox(new String[] {"All", "FTB", "3rd Party"}), compatibleBox, mcVersionBox, 
			avalBox = new JComboBox(new String[]{"All", "Public", "Private"});
	private JButton applyButton = new JButton("Apply Filter"), cancelButton = new JButton("Cancel"), btnSearch = new JButton("Search"), btnAddPack = new JButton("Add Pack");
	private final JLabel lblMinecraftVersion = new JLabel("Minecraft Version:");

	private ModpacksPane pane;

	public FilterDialogPacks(ModpacksPane instance) {
		super(LaunchFrame.getInstance(), true);
		setupGui();
		this.pane = instance;
	}

	private void setupGui() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
		setTitle("Filter");
		setBounds(300, 300, 300, 236);
		setResizable(false);
		panel.setBounds(0, 0, 230, 140);
		panel.setLayout(null);
		setContentPane(panel);
		typeLbl.setBounds(10, 10, 150, 30);
		typeBox.setBounds(184, 10, 100, 30);
		originLbl.setBounds(10, 40, 150, 30);
		originBox.setBounds(184, 40, 100, 30);
		applyButton.setBounds(10, 172, 150, 25);
		getRootPane().setDefaultButton(applyButton);
		cancelButton.setBounds(184, 172, 100, 25);
		btnSearch.setBounds(10, 141, 150, 25);
		btnAddPack.setBounds(184, 141, 100, 25);
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

		for(ModPack pack : ModPack.getPackArray()) {
			if(!mcVersions.contains(pack.getMcVersion())) {
				mcVersions.add(pack.getMcVersion());
			}
		}

		mcVersionBox = new JComboBox(mcVersions.toArray());

		typeBox.setSelectedItem(pane.type);
		originBox.setSelectedItem(pane.origin);
		avalBox.setSelectedItem(pane.avaliability);
		mcVersionBox.setSelectedItem(pane.mcVersion);
		lblMinecraftVersion.setBounds(10, 70, 150, 30);

		panel.add(lblMinecraftVersion);
		mcVersionBox.setBounds(184, 70, 100, 30);

		panel.add(mcVersionBox);

		avalBox.setBounds(184, 100, 100, 30);
		panel.add(avalBox);

		lblModPackAval.setBounds(10, 100, 150, 25);
		panel.add(lblModPackAval);

		btnAddPack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AddPackDialog ap = new AddPackDialog();
				setVisible(false);
				ap.setVisible(true);
			}
		});

		btnSearch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SearchDialog sd = new SearchDialog(pane);
				sd.setVisible(true);
			}
		});

		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				pane.type = (String)typeBox.getSelectedItem();
				pane.origin = (String)originBox.getSelectedItem();
				pane.mcVersion = (String)mcVersionBox.getSelectedItem();
				pane.avaliability = (String)avalBox.getSelectedItem();
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
