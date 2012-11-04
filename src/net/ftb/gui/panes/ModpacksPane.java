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

public class ModpacksPane extends JPanel implements ILauncherPane, ModPackListener {
	private static final long serialVersionUID = 1L;

	private JPanel packs;
	public ArrayList<JPanel> packPanels;
	private JScrollPane packsScroll;
	private JLabel splash;
	private JButton serverLink;
	private JButton modsFolder;
	private JButton donate;
	private static int selectedPack = 0;
	private boolean modPacksAdded = false;


	public ModpacksPane () {
		super();

		this.setBorder(new EmptyBorder(5, 5, 5, 5));
		this.setLayout(null);
		//modPacksPane.add(backgroundImage3);
		//modPacksPane.setBackground(back);

		splash = new JLabel();
		splash.setBounds(420, 0, 410, 200);
		//		splash.setIcon(new ImageIcon(ModPack.getPack(0).getImage()));
		this.add(splash);

		packPanels = new ArrayList<JPanel>();

		// i suggest some loading animation here until first mod gets added

		packs = new JPanel();
		packs.setBounds(0, 0, 420, (ModPack.getPackArray().size()) * 55);
		packs.setLayout(null);
		packs.setOpaque(false);

		// stub for a real wait message
		final JPanel p = new JPanel();
		p.setBounds(0, 0, 420, 55);
		p.setLayout(null);

		JTextArea filler = new JTextArea("please wait while mods are beeing load...");
		filler.setBorder(null);
		filler.setEditable(false);
		filler.setForeground(Color.white);
		filler.setBounds(6 + 42 + 10, 6, 420 - (6 + 42 - 6), 42);
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

		serverLink = new JButton("Grab The Server Version Here!!");
		serverLink.setBounds(420, 210, 250, 90);
		serverLink.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					hLink(arg0, new URI(LaunchFrame.getCreeperhostLink(ModPack.getPack(LaunchFrame.getSelectedModIndex()).getServerUrl())));
				} catch (URISyntaxException e) {
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
			}
		});
		this.add(serverLink);
		
		donate = new JButton("Donate to this Pack!");
		donate.setBounds(670, 255, 170, 45);
		donate.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//TODO
			}
		});
		donate.setEnabled(false);
		donate.setToolTipText("Coming Soon...");
		this.add(donate);
		
		modsFolder = new JButton("Open the Jar mods folder");
		modsFolder.setBounds(670, 210, 170, 45);
		modsFolder.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(Desktop.isDesktopSupported()) {
					Desktop desktop = Desktop.getDesktop();
					try {
						desktop.open(new File(Settings.getSettings().getInstallPath() + "/" + ModPack.getPack(LaunchFrame.getSelectedModIndex()).getDir() + "/instMods"));
					} catch (IOException e1) {
						e1.printStackTrace();
					}
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
		System.out.println("adding pack "+packIndex);
		final JPanel p = new JPanel();
		p.setBounds(0, packIndex * 55, 420, 55);
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