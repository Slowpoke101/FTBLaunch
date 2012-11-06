package net.ftb.gui.panes;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.data.events.ModPackListener;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.dialogs.EditModPackDialog;
import net.ftb.gui.dialogs.FilterDialog;

public class ModpacksPane extends JPanel implements ILauncherPane, ModPackListener {
	private static final long serialVersionUID = 1L;

	private JPanel packs;
	public ArrayList<JPanel> packPanels;
	public ArrayList<JPanel> unfilteredPackPanels;
	private JScrollPane packsScroll;
	private JLabel splash, typeLbl;
	private JButton serverLink, modsFolder, donate, filter;
	private static JComboBox packType;
	private static int selectedPack = 0;
	private boolean modPacksAdded = false;
	public static int typeFilter = 0;

	public ModpacksPane () {
		super();

		this.setBorder(new EmptyBorder(5, 5, 5, 5));
		this.setLayout(null);

		splash = new JLabel();
		splash.setBounds(420, 0, 410, 200);
		this.add(splash);

		packPanels = new ArrayList<JPanel>();

		// TODO: Set loading animation while we wait
		packs = new JPanel();
		packs.setBounds(0, 0, 420, (ModPack.getPackArray().size()) * 55);
		packs.setLayout(null);
		packs.setOpaque(false);

		// stub for a real wait message
		final JPanel p = new JPanel();
		p.setBounds(0, 0, 420, 55);
		p.setLayout(null);

		// TODO: Change from drop down to filter button
		filter = new JButton("Filter");
		filter.setBounds(5, 5, 60, 25);
		filter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				FilterDialog filterDia = new FilterDialog(LaunchFrame.getInstance());
				filterDia.setVisible(true);
			}
		});
		add(filter);

		JTextArea filler = new JTextArea("Please wait while mods are being loaded...");
		filler.setBorder(null);
		filler.setEditable(false);
		filler.setForeground(Color.white);
		filler.setBounds(6 + 42 + 10, 36, 420 - (6 + 42 - 6), 42);
		filler.setBackground(new Color(255, 255, 255, 0));
		p.add(filler);
		packs.add(p);

		packsScroll = new JScrollPane();
		packsScroll.setBounds(0, 0, 420, 300);
		packsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		packsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		packsScroll.setWheelScrollingEnabled(true);
		packsScroll.setOpaque(false);
		packsScroll.setViewportView(packs);
		this.add(packsScroll);

		// TODO: Remove this and replace with filters
		serverLink = new JButton("Grab The Server Version Here!!");
		serverLink.setBounds(420, 210, 250, 90);
		serverLink.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(packPanels.size() > 0) {
					try {
						hLink(arg0, new URI(LaunchFrame.getCreeperhostLink(ModPack.getPack(LaunchFrame.getSelectedModIndex()).getServerUrl())));
					} catch (URISyntaxException e) {
						e.printStackTrace();
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					}
				}
			}
		});
		this.add(serverLink);

		donate = new JButton("Donate to this Pack!");
		donate.setBounds(670, 255, 170, 45);
		donate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(packPanels.size() > 0) {
				}
			}
		});
		donate.setEnabled(false);
		donate.setToolTipText("Coming Soon...");
		this.add(donate);

		modsFolder = new JButton("Edit Mod Pack!");
		modsFolder.setBounds(670, 210, 170, 45);
		modsFolder.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(packPanels.size() > 0) {
					EditModPackDialog empd = new EditModPackDialog(LaunchFrame.getInstance());
					empd.setVisible(true);
				}
			}
		});
		this.add(modsFolder);
	}

	@Override public void onVisible() { }

	/*
	 * GUI Code to add a modpack to the selection
	 */
	public void addPack(ModPack pack) {
		if (!modPacksAdded) {
			modPacksAdded = true;
			packs.removeAll();
		}

		final int packIndex = packPanels.size();
		System.out.println("Adding pack "+packIndex);
		final JPanel p = new JPanel();
		p.setBounds(0, (packIndex * 55) + 30, 420, 55);
		p.setLayout(null);
		JLabel logo = new JLabel(new ImageIcon(pack.getLogo()));
		logo.setBounds(6, 6, 42, 42);
		logo.setVisible(true);
		JTextArea filler = new JTextArea(pack.getName() + " : " + pack.getAuthor() + "\n" + pack.getInfo());
		filler.setBorder(null);
		filler.setEditable(false);
		filler.setForeground(Color.white);
		filler.setBounds(6 + 42 + 10, 6, 420 - (6 + 42 - 6), 42);
		filler.setBackground(new Color(255, 255, 255, 0));
		MouseListener lin = new MouseListener() {
			@Override public void mouseClicked(MouseEvent e) {
				selectedPack = packIndex;
				updatePacks();
			}
			@Override public void mouseReleased(MouseEvent e) { }
			@Override public void mousePressed(MouseEvent e) { }
			@Override public void mouseExited(MouseEvent e) { }
			@Override public void mouseEntered(MouseEvent e) { }
		};
		p.addMouseListener(lin);
		filler.addMouseListener(lin);
		logo.addMouseListener(lin);
		p.add(filler);
		p.add(logo);
		packPanels.add(p);
		packs.add(p);
	}

	@Override
	public void onModPackAdded(ModPack pack) {
		addPack(pack);
		updatePacks();
	}

	private void updateSortedPacks() {
		unfilteredPackPanels = packPanels;
		// Based on the typeFilter we now need to change up the look and feel
		// Apply base gui, client/server
		// Apply secondary filter (all, ftb, 3rd party)
		updatePacks();
	}

	private void updatePacks() {
		for (int i = 0; i < packPanels.size(); i++) {
			if(selectedPack == i) {
				packPanels.get(i).setBackground(UIManager.getColor("control").darker().darker());
				splash.setIcon(new ImageIcon(ModPack.getPack(i).getImage()));
				packPanels.get(i).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			} else {
				packPanels.get(i).setBackground(UIManager.getColor("control"));
				packPanels.get(i).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}
		}
	}

	public int getSelectedModIndex() {
		return modPacksAdded ? selectedPack : -1;
	}

	public void hLink(ActionEvent ae, URI uri) {
		if(Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			try {
				desktop.browse(uri);
			} catch (IOException e) { e.printStackTrace(); }
		} else {
			System.out.println("else working");
		}
	}
}