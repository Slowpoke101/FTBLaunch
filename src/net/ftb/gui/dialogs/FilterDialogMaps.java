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

import net.ftb.data.Map;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.panes.MapsPane;

public class FilterDialogMaps extends JDialog {
	private JPanel panel = new JPanel();
	private JLabel typeLbl = new JLabel("Mod Pack Type:"), originLbl = new JLabel("Mod Pack Origin:"), packLbl = new JLabel("Compatible Pack:");
	private JComboBox typeBox = new JComboBox(new String[] {"Client", "Server"}), originBox = new JComboBox(new String[] {"All", "FTB", "3rd Party"}), compatibleBox;
	private JButton applyButton = new JButton("Apply Filter"), cancelButton = new JButton("Cancel"), searchButton = new JButton("Search Maps");
	private final JLabel lblMinecraftVersion = new JLabel("Minecraft Version:");
	private final JComboBox comboBox = new JComboBox();

	private MapsPane pane;

	public FilterDialogMaps(MapsPane instance) {
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
		applyButton.setBounds(10, 106, 100, 25);
		searchButton.setBounds(10, 141, 210, 25);
		getRootPane().setDefaultButton(applyButton);
		cancelButton.setBounds(120, 110, 100, 25);
		panel.add(typeLbl);
		panel.add(typeBox);
		panel.add(originLbl);
		panel.add(originBox);
		panel.add(applyButton);
		panel.add(cancelButton);
		panel.add(searchButton);
		panel.setBounds(0, 0, 230, 250);
		applyButton.setBounds(10, 110, 100, 25);
		cancelButton.setBounds(120, 110, 100, 25);

		typeBox.setSelectedItem(pane.type);
		originBox.setSelectedItem(pane.origin);

		packLbl.setBounds(10, 70, 100, 30);
		panel.add(packLbl);

		ArrayList<String> packs = new ArrayList<String>();
		packs.add("All");
		for(int i = 0; i < Map.getMapArray().size(); i++) {
			String[] compat = Map.getMap(i).getCompatible();
			for (String compatable : compat) {
				if (!packs.contains(compatable)) {
					packs.add(compatable);
				}
			}
		}

		compatibleBox = new JComboBox(packs.toArray());
		compatibleBox.setBounds(120, 70, 100, 30);
		compatibleBox.setSelectedItem(pane.compatible);
		panel.add(compatibleBox);

		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				pane.compatible = (String)compatibleBox.getSelectedItem();
				pane.type = (String)typeBox.getSelectedItem();
				pane.origin = (String)originBox.getSelectedItem();
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
		searchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SearchDialog sd = new SearchDialog(pane);
				sd.setVisible(true);
				setVisible(false);
			}
		});
	}
}
