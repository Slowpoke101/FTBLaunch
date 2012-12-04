package net.ftb.gui.panes;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import net.ftb.data.Map;
import net.ftb.data.events.MapListener;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.dialogs.FilterDialog;
import net.ftb.locale.I18N;
import net.ftb.log.Logger;
import net.ftb.util.OSUtils;

public class MapsPane extends JPanel implements ILauncherPane, MapListener {
	private static final long serialVersionUID = 1L;

	private static JPanel maps;
	public static ArrayList<JPanel> mapPanels;
	private static JScrollPane mapsScroll;
	private static JLabel splash;

	private static JLabel typeLbl;
	private JButton filter;
	private static int selectedMap = 0;
	private static boolean mapsAdded = false;
	public static String type = "Client", origin = "All", compatible = "All";
	private final MapsPane instance = this;

	private static JEditorPane mapInfo;

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
		maps.setLayout(null);
		maps.setOpaque(false);

		final JPanel p = new JPanel();
		p.setBounds(0, 0, 420, 55);
		p.setLayout(null);

		filter = new JButton(I18N.getLocaleString("FILTER_SETTINGS"));
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

		typeLbl = new JLabel("<html><body><strong><font color=rgb\"(243,119,31)\">Filter:</strong></font> " + type + "<font color=rgb\"(243,119,31)\"> / </font>" + origin + "<font color=rgb\"(243,119,31)\"> / </font>" + compatible + "</body></html>");
		typeLbl.setBounds(115, 5, 295, 25);
		typeLbl.setHorizontalAlignment(SwingConstants.CENTER);
		add(typeLbl);

		JTextArea filler = new JTextArea(I18N.getLocaleString("MAPS_WAIT_WHILE_LOADING"));
		filler.setBorder(null);
		filler.setEditable(false);
		filler.setForeground(Color.white);
		filler.setBounds(58, 6, 378, 42);
		filler.setBackground(new Color(255, 255, 255, 0));
		p.add(filler);
		maps.add(p);

		mapsScroll = new JScrollPane();
		mapsScroll.setBounds(0, 30, 420, 280);
		mapsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		mapsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		mapsScroll.setWheelScrollingEnabled(true);
		mapsScroll.setOpaque(false);
		mapsScroll.setViewportView(maps);
		add(mapsScroll);

		mapInfo = new JEditorPane();
		mapInfo.setEditable(false);
		mapInfo.setContentType("text/html");
		mapInfo.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent event) {
				if(event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					OSUtils.browse(event.getURL().toString());
				}
			}
		});
		mapInfo.setBounds(420, 210, 410, 90);
		mapInfo.setBackground(UIManager.getColor("control").darker().darker());
		add(mapInfo);

		JScrollPane infoScroll = new JScrollPane();
		infoScroll.setBounds(420, 210, 410, 90);
		infoScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		infoScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		infoScroll.setWheelScrollingEnabled(true);
		infoScroll.setViewportView(mapInfo);
		infoScroll.setOpaque(false);
		add(infoScroll);
	}

	@Override public void onVisible() { }

	/*
	 * GUI Code to add a map to the selection
	 */
	public static void addMap(Map map) {
		if (!mapsAdded) {
			mapsAdded = true;
			maps.removeAll();
		}

		final int mapIndex = mapPanels.size();
		Logger.logInfo("Adding map " + getMapNum());
		final JPanel p = new JPanel();
		p.setBounds(0, (mapIndex * 55), 420, 55);
		p.setLayout(null);
		JLabel logo = new JLabel(new ImageIcon(map.getLogo()));
		logo.setBounds(6, 6, 42, 42);
		logo.setVisible(true);

		JTextArea filler = new JTextArea(map.getName() + " (v." + map.getVersion() + ")\n" + "By " + map.getAuthor());
		filler.setBorder(null);
		filler.setEditable(false);
		filler.setForeground(Color.white);
		filler.setBounds(58, 6, 378, 42);
		filler.setBackground(new Color(255, 255, 255, 0));
		MouseListener lin = new MouseListener() {
			@Override public void mouseClicked(MouseEvent e) {
				selectedMap = mapIndex;
				updateMaps();
			}
			@Override public void mouseReleased(MouseEvent e) { }
			@Override public void mousePressed(MouseEvent e) { 
				selectedMap = mapIndex;
				updateMaps();
			}
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
		if(origin.equalsIgnoreCase("all")) {
			maps.setMinimumSize(new Dimension(420, (Map.getMapArray().size() * 55)));
			maps.setPreferredSize(new Dimension(420, (Map.getMapArray().size() * 55)));
		} else {
			maps.setMinimumSize(new Dimension(420, (currentMaps.size() * 55)));
			maps.setPreferredSize(new Dimension(420, (currentMaps.size() * 55)));
		}
		mapsScroll.revalidate();
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
		int counter = 0;
		selectedMap = 0;
		LaunchFrame.getInstance().mapsPane.repaint();
		LaunchFrame.updateMapInstallLocs(new String[]{""});
		mapInfo.setText("");
		if(origin.equals("All")) {
			for(Map map : Map.getMapArray()) {
				if(compatible.equals("All") || map.isCompatible(compatible)) {
					addMap(map);
					currentMaps.put(counter, map);
					counter++;
				}
			}
		} else if(origin.equals("FTB")) {
			for(Map map : Map.getMapArray()) {
				if(map.getAuthor().equalsIgnoreCase("the ftb team")) {
					if(compatible.equals("All") || map.isCompatible(compatible)) {
						addMap(map);
						currentMaps.put(counter, map);
						counter++;
					}
				}
			}
		} else {
			for(Map map : Map.getMapArray()) {
				if(!map.getAuthor().equalsIgnoreCase("the ftb team")) {
					if(compatible.equals("All") || map.isCompatible(compatible)) {
						addMap(map);
						currentMaps.put(counter, map);
						counter++;
					}
				}
			}
		}
		updateMaps();
	}

	public static void searchMaps(String search) {
		System.out.println("Searching Maps for : " + search);
		mapPanels.clear();
		maps.removeAll();
		currentMaps.clear();
		maps.setMinimumSize(new Dimension(420, 0));
		maps.setPreferredSize(new Dimension(420, 0));
		maps.setLayout(null);
		maps.setOpaque(false);
		int counter = 0;
		selectedMap = 0;
		for(Map map : Map.getMapArray()) {
			if(map.getName().contains(search) || map.getAuthor().equalsIgnoreCase(search)) {
				addMap(map);
				currentMaps.put(counter, map);
				counter++;
			}
		}
		updateMaps();
	}

	private static void updateMaps() {
		for (int i = 0; i < mapPanels.size(); i++) {
			if(selectedMap == i) {
				String packs = "";
				if (Map.getMap(getIndex()).getCompatible() != null) {
					packs += "<p>This map works with the folowing packs:</p><ul>";
					for (String name : Map.getMap(getIndex()).getCompatible()) {
						packs += "<li>" + name + "</li>";
					}
					packs += "</ul>";
				}
				mapPanels.get(i).setBackground(UIManager.getColor("control").darker().darker());
				splash.setIcon(new ImageIcon(Map.getMap(getIndex()).getImage()));
				mapPanels.get(i).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				LaunchFrame.updateMapInstallLocs(Map.getMap(getIndex()).getCompatible());
				mapInfo.setText(Map.getMap(getIndex()).getInfo() + packs);
			} else {
				mapPanels.get(i).setBackground(UIManager.getColor("control"));
				mapPanels.get(i).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}
		}
	}

	public int getSelectedMapIndex() {
		return mapsAdded ? getIndex() : -1;
	}

	public static void updateFilter() {
		// TODO: Show Modpack specific filtering
		typeLbl.setText("<html><body><strong><font color=rgb\"(243,119,31)\">Filter:</strong></font> " + type + "<font color=rgb\"(243,119,31)\"> / </font>" + origin + "<font color=rgb\"(243,119,31)\"> / </font>" + compatible + "</body></html>");
		sortMaps();
		LaunchFrame.getInstance().updateFooter();
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

	public void updateLocale() {
		filter.setText(I18N.getLocaleString("FILTER_SETTINGS"));
	}
}
