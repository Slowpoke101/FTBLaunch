package net.ftb.gui.panes;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import net.ftb.data.Map;
import net.ftb.data.ModPack;
import net.ftb.data.events.MapListener;
import net.ftb.gui.dialogs.FilterDialog;

public class MapsPane extends JPanel implements ILauncherPane, MapListener {
	private static final long serialVersionUID = 1L;

	private static JPanel maps;
	public static ArrayList<JPanel> mapPanels;
	private JScrollPane mapsScroll;
	private static JLabel splash;

	private static JLabel typeLbl;
	private JButton filter;
	private static JComboBox mapType;
	private static int selectedMap = 0;
	private static boolean mapsAdded = false;
	public static String type = "Client", origin = "All";
	private final MapsPane instance = this;

	public static boolean loaded = false;

	private static HashMap<Integer, Map> currentMaps = new HashMap<Integer, Map>();

	public MapsPane() {
		super();
		this.setBorder(new EmptyBorder(5, 5, 5, 5));
		this.setLayout(null);

		splash = new JLabel();
		splash.setBounds(420, 0, 410, 200);
		this.add(splash);

		mapPanels = new ArrayList<JPanel>();

		// TODO: Set loading animation while we wait
		maps = new JPanel();
		maps.setBounds(0, 0, 420, (ModPack.getPackArray().size()) * 55);
		maps.setLayout(null);
		maps.setOpaque(false);

		// stub for a real wait message
		final JPanel p = new JPanel();
		p.setBounds(0, 0, 420, 55);
		p.setLayout(null);

		filter = new JButton("Filter Settings");
		filter.setBounds(5, 5, 105, 25);
		filter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(loaded) {
					FilterDialog filterDia = new FilterDialog(instance);
					filterDia.setVisible(true);
				}
			}
		});
		add(filter);

		typeLbl = new JLabel("<html><body><strong><font color=rgb\"(243,119,31)\">Filter:</strong></font> " + type + "<font color=rgb\"(243,119,31)\"> / </font>" + origin +"</body></html>");
		typeLbl.setBounds(115, 5, 160, 25);
		typeLbl.setHorizontalAlignment(SwingConstants.CENTER);
		add(typeLbl);

		JTextArea filler = new JTextArea("Please wait while maps are being loaded...");
		filler.setBorder(null);
		filler.setEditable(false);
		filler.setForeground(Color.white);
		filler.setBounds(6 + 42 + 10, 36, 420 - (6 + 42 - 6), 42);
		filler.setBackground(new Color(255, 255, 255, 0));
		p.add(filler);
		maps.add(p);

		mapsScroll = new JScrollPane();
		mapsScroll.setBounds(0, 0, 420, 300);
		mapsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		mapsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		mapsScroll.setWheelScrollingEnabled(true);
		mapsScroll.setOpaque(false);
		mapsScroll.setViewportView(maps);
		this.add(mapsScroll);
	}

	@Override public void onVisible() { }

	/*
	 * GUI Code to add a modpack to the selection
	 */
	public static void addMap(Map map) {
		if (!mapsAdded) {
			mapsAdded = true;
			maps.removeAll();
		}

		final int mapIndex = mapPanels.size();
		System.out.println("Adding pack " + getMapNum());
		final JPanel p = new JPanel();
		p.setBounds(0, (mapIndex * 55) + 30, 420, 55);
		p.setLayout(null);
		JLabel logo = new JLabel(new ImageIcon(map.getLogo()));
		logo.setBounds(6, 6, 42, 42);
		logo.setVisible(true);
		JTextArea filler = new JTextArea(map.getName() + " : " + map.getAuthor() + "\n" + map.getInfo());
		filler.setBorder(null);
		filler.setEditable(false);
		filler.setForeground(Color.white);
		filler.setBounds(6 + 42 + 10, 6, 420 - (6 + 42 - 6), 42);
		filler.setBackground(new Color(255, 255, 255, 0));
		MouseListener lin = new MouseListener() {
			@Override public void mouseClicked(MouseEvent e) {
				selectedMap = mapIndex;
				updateMaps();
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
		mapPanels.add(p);
		maps.add(p);
	}

	@Override
	public void onMapAdded(Map map) {
		addMap(map);
		updateMaps();
	}

	private static void sortMaps() {
		mapPanels.clear();
		maps.removeAll();
		currentMaps.clear();
		maps.setBounds(0, 0, 420, (Map.getMapArray().size()) * 55);
		maps.setLayout(null);
		maps.setOpaque(false);
		int counter = 0;
		selectedMap = 0;
		if(origin.equalsIgnoreCase("all")) {
			for(Map map : Map.getMapArray()) {
				addMap(map);
				currentMaps.put(counter, map);
				counter++;
			}
		} else if(origin.equalsIgnoreCase("ftb")) {
			for(Map map : Map.getMapArray()) {
				if(map.getAuthor().equalsIgnoreCase("the ftb team")) {
					addMap(map);
					currentMaps.put(counter, map);
					counter++;
				}
			}
		} else {
			for(Map map : Map.getMapArray()) {
				if(!map.getAuthor().equalsIgnoreCase("the ftb team")) {
					addMap(map);
					currentMaps.put(counter, map);
					counter++;
				}
			}
		}
		updateMaps();
	}

	private static void updateMaps() {
		for (int i = 0; i < mapPanels.size(); i++) {
			if(selectedMap == i) {
				mapPanels.get(i).setBackground(UIManager.getColor("control").darker().darker());
				splash.setIcon(new ImageIcon(ModPack.getPack(getIndex()).getImage()));
				mapPanels.get(i).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			} else {
				mapPanels.get(i).setBackground(UIManager.getColor("control"));
				mapPanels.get(i).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}
		}
	}

	public int getSelectedModIndex() {
		return mapsAdded ? getIndex() : -1;
	}

	public static void updateFilter() {
		typeLbl.setText("<html><body><strong><font color=rgb\"(243,119,31)\">Filter:</strong></font> " + type + "<font color=rgb\"(243,119,31)\"> / </font>" + origin +"</body></html>");
		sortMaps();
	}

	private static int getIndex() {
		if(currentMaps.size() > 0) {
			if(currentMaps.size() != Map.getMapArray().size()) {
				if(!origin.equalsIgnoreCase("all")) {
					return currentMaps.get(selectedMap).getIndex();
				}
			}
		}
		return selectedMap;
	}

	private static int getMapNum() {
		if(currentMaps.size() > 0) {
			if(!origin.equalsIgnoreCase("all")) {
				return currentMaps.get((mapPanels.size() - 1)).getIndex();
			}
		}
		return mapPanels.size();
	}
}
