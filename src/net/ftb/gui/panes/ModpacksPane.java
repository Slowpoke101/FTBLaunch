package net.ftb.gui.panes;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
import net.ftb.data.events.ModPackListener;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.dialogs.FilterDialog;

public class ModpacksPane extends JPanel implements ILauncherPane, ModPackListener {
	private static final long serialVersionUID = 1L;

	private static JPanel packs;
	public static ArrayList<JPanel> packPanels;
	public ArrayList<JPanel> unfilteredPackPanels;
	private JScrollPane packsScroll;
	private static JLabel splash;

	private static JLabel typeLbl, originLbl;
	private JButton filter;
	private static JComboBox packType;
	private static int selectedPack = 0;
	private static boolean modPacksAdded = false;
	public static String type = "Client", origin = "All";
	private final ModpacksPane instance = this;

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

		filter = new JButton("Filter Settings");
		filter.setBounds(5, 5, 110, 25);
		filter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				FilterDialog filterDia = new FilterDialog(instance);
				filterDia.setVisible(true);
			}
		});
		add(filter);

		typeLbl = new JLabel("<html><body><strong>Pack Type:</strong> " + type + "</body></html>");
		typeLbl.setBounds(140, 1, 100, 30);
		add(typeLbl);

		originLbl = new JLabel("<html><body><strong>Origin:</strong> " + origin + "</body></html>");
		originLbl.setBounds(280, 1, 100, 30);
		add(originLbl);

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
	}

	@Override public void onVisible() { }

	/*
	 * GUI Code to add a modpack to the selection
	 */
	public static void addPack(ModPack pack) {
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

	private static void sortPacks() {
		packPanels.clear();
		packs.removeAll();
		packs.setBounds(0, 0, 420, (ModPack.getPackArray().size()) * 55);
		packs.setLayout(null);
		packs.setOpaque(false);
		if(origin.equalsIgnoreCase("all")) {
			for(ModPack pack : ModPack.getPackArray()) {
				addPack(pack);
			}
		} else if(origin.equalsIgnoreCase("ftb")) {
			for(ModPack pack : ModPack.getPackArray()) {
				if(pack.getAuthor().equalsIgnoreCase("the ftb team")) {
					addPack(pack);
				}
			}
		} else {
			for(ModPack pack : ModPack.getPackArray()) {
				if(!pack.getAuthor().equalsIgnoreCase("the ftb team")) {
					addPack(pack);
				}
			}
		}
		updatePacks();
	}

	private static void updatePacks() {
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

	public static void updateFilter() {
		typeLbl.setText("<html><body><strong>Pack Type:</strong> " + type + "</body></html>");
		originLbl.setText("<html><body><strong>Origin:</strong> " + origin + "</body></html>");
		sortPacks();
		LaunchFrame.getInstance().updateButtons();
	}
}