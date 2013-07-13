/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2013, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
 * FTB Launcher is licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ftb.gui.panes;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

import net.ftb.data.LauncherStyle;
import net.ftb.data.Map;
import net.ftb.data.ModPack;
import net.ftb.data.events.MapListener;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.dialogs.MapFilterDialog;
import net.ftb.gui.dialogs.SearchDialog;
import net.ftb.locale.I18N;
import net.ftb.log.Logger;
import net.ftb.util.OSUtils;

@SuppressWarnings("serial")
public class MapsPane extends JPanel implements ILauncherPane, MapListener {
	private static JPanel maps;
	public static ArrayList<JPanel> mapPanels;
	private static JScrollPane mapsScroll;

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
					MapFilterDialog filterDia = new MapFilterDialog(instance);
					filterDia.setVisible(true);
				}
			}
		});
		add(filter);

		String filterTextColor = LauncherStyle.getColorAsString(LauncherStyle.getCurrentStyle().filterTextColor);
		String filterInnerTextColor = LauncherStyle.getColorAsString(LauncherStyle.getCurrentStyle().filterInnerTextColor);

		String typeLblText = "<html><body>";
		typeLblText += "<strong><font color=rgb\"(" + filterTextColor + ")\">Filter: </strong></font>";
		typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + type + "</font>";
		typeLblText += "<font color=rgb\"(" + filterTextColor + ")\"> / </font>";
		typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + origin + "</font>";
		typeLblText += "<font color=rgb\"(" + filterTextColor + ")\"> / </font>";
		typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + compatible + "</font>";
		typeLblText += "</body></html>";

		typeLbl = new JLabel(typeLblText);
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
		mapsScroll.setBounds(-3, 30, 420, 283);
		mapsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		mapsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		mapsScroll.setWheelScrollingEnabled(true);
		mapsScroll.setOpaque(false);
		mapsScroll.setViewportView(maps);
		mapsScroll.getVerticalScrollBar().setUnitIncrement(19);
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
		infoScroll.setBounds(410, 25, 430, 290);
		infoScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		infoScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		infoScroll.setWheelScrollingEnabled(true);
		infoScroll.setViewportView(mapInfo);
		infoScroll.setOpaque(false);
		add(infoScroll);
	}

	@Override public void onVisible() {
		sortMaps();
		updateFilter();
	}

	/*
	 * GUI Code to add a map to the selection
	 */
	public static void addMap(Map map) {
		if (!mapsAdded) {
			mapsAdded = true;
			maps.removeAll();
		}

		final int mapIndex = mapPanels.size();	
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
		MouseAdapter lin = new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
				selectedMap = mapIndex;
				updateMaps();
			}
			@Override public void mousePressed(MouseEvent e) { 
				selectedMap = mapIndex;
				updateMaps();
			}
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
		Logger.logInfo("Adding map " + getMapNum());
		updateMaps();
	}

	public static void sortMaps() {
		mapPanels.clear();
		maps.removeAll();
		currentMaps.clear();
		int counter = 0;
		selectedMap = 0;
		maps.repaint();
		LaunchFrame.updateMapInstallLocs(new String[]{""});
		mapInfo.setText("");
		HashMap<Integer, List<Map>> sorted = new HashMap<Integer, List<Map>>();			
		sorted.put(0, new ArrayList<Map>());
		sorted.put(1, new ArrayList<Map>());
		for(Map map : Map.getMapArray()) {
			if(originCheck(map) && compatibilityCheck(map) && textSearch(map)) {
				sorted.get((map.isCompatible(ModPack.getSelectedPack().getName())) ? 1 : 0).add(map);
			}
		}
		for(Map map : sorted.get(1)) {
			addMap(map);
			currentMaps.put(counter, map);
			counter++;
		}
		for(Map map : sorted.get(0)) {
			addMap(map);
			currentMaps.put(counter, map);
			counter++;
		}
		updateMaps();
	}

	private static void updateMaps() {
		for (int i = 0; i < mapPanels.size(); i++) {
			if(selectedMap == i) {
				String packs = "";
				if (Map.getMap(getIndex()).getCompatible() != null) {
					packs += "<p>This map works with the following packs:</p><ul>";
					for (String name : Map.getMap(getIndex()).getCompatible()) {
						packs += "<li>" + ModPack.getPack(name).getName() + "</li>";
					}
					packs += "</ul>";
				}
				mapPanels.get(i).setBackground(UIManager.getColor("control").darker().darker());
				mapPanels.get(i).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				LaunchFrame.updateMapInstallLocs(Map.getMap(getIndex()).getCompatible());
				File tempDir = new File(OSUtils.getDynamicStorageLocation(), "Maps" + File.separator + Map.getMap(getIndex()).getMapName());
				mapInfo.setText("<html><img src='file:///" + tempDir.getPath() + File.separator + Map.getMap(getIndex()).getImageName() + "' width=400 height=200></img> <br>" + Map.getMap(getIndex()).getInfo() + packs);
				mapInfo.setCaretPosition(0);
			} else {
				mapPanels.get(i).setBackground(UIManager.getColor("control"));
				mapPanels.get(i).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}
		}
	}

	public static int getSelectedMapIndex() {
		return mapsAdded ? getIndex() : -1;
	}

	public static void updateFilter() {
		// TODO: Show Modpack specific filtering
		String filterTextColor = LauncherStyle.getColorAsString(LauncherStyle.getCurrentStyle().filterTextColor);
		String filterInnerTextColor = LauncherStyle.getColorAsString(LauncherStyle.getCurrentStyle().filterInnerTextColor);

		String typeLblText = "<html><body>";
		typeLblText += "<strong><font color=rgb\"(" + filterTextColor + ")\">Filter: </strong></font>";
		typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + type + "</font>";
		typeLblText += "<font color=rgb\"(" + filterTextColor + ")\"> / </font>";
		typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + origin + "</font>";
		typeLblText += "<font color=rgb\"(" + filterTextColor + ")\"> / </font>";
		typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + compatible + "</font>";
		typeLblText += "</body></html>";

		typeLbl.setText(typeLblText);
		sortMaps();
		LaunchFrame.getInstance().updateFooter();
	}

	private static int getIndex() {
		return (currentMaps.size() > 0) ? currentMaps.get(selectedMap).getIndex() : selectedMap;
	}

	private static int getMapNum() {
		if(currentMaps.size() > 0) {
			if(!origin.equalsIgnoreCase(I18N.getLocaleString("MAIN_ALL"))) {
				return currentMaps.get((mapPanels.size() - 1)).getIndex();
			}
		}
		return mapPanels.size();
	}

	public void updateLocale() {
		filter.setText(I18N.getLocaleString("FILTER_SETTINGS"));
	}

	private static boolean originCheck(Map map) {
		return (origin.equalsIgnoreCase(I18N.getLocaleString("MAIN_ALL"))) || (origin.equalsIgnoreCase("ftb") && map.getAuthor().equalsIgnoreCase("the ftb team")) || (origin.equalsIgnoreCase(I18N.getLocaleString("FILTER_3THPARTY")) && !map.getAuthor().equalsIgnoreCase("the ftb team"));
	}

	private static boolean compatibilityCheck(Map map) {
		return (compatible.equalsIgnoreCase(I18N.getLocaleString("MAIN_ALL")) || map.isCompatible(compatible));
	}

	private static boolean textSearch(Map map) {
		String searchString = SearchDialog.lastMapSearch.toLowerCase();
		return ((searchString.isEmpty()) || map.getName().toLowerCase().contains(searchString) || map.getAuthor().toLowerCase().contains(searchString));
	}
}