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
import java.awt.Component;
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
import java.util.Locale;

import javax.swing.AbstractListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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

class MapListModelAdapter extends AbstractListModel implements MapListener {
	private HashMap<Integer, Integer> filteredMaps;

	public MapListModelAdapter() {
		super();
		filteredMaps = new HashMap<Integer, Integer>();
	}

	public void filter(String origin, String compatible, String query) {
		filteredMaps.clear();
		int counter = 0;
		for(int i = 0; i < Map.size(); ++i) {
			Map map = Map.getMap(i);
			if(map.isCompatible(ModPack.getSelectedPack().getName()) && originCheck(map, origin) && compatibilityCheck(map, compatible) && textSearch(map, query)) {
				filteredMaps.put(counter, i);
				counter++;
			}
		}
		for(int i = 0; i < Map.size(); ++i) {
			Map map = Map.getMap(i);
			if((!map.isCompatible(ModPack.getSelectedPack().getName())) && originCheck(map, origin) && compatibilityCheck(map, compatible) && textSearch(map, query)) {
				filteredMaps.put(counter, i);
				counter++;
			}
		}
		if(counter + 1 == Map.size()) {
			filteredMaps.clear();
			fireIntervalRemoved(this, 0, Map.size());
			fireIntervalAdded(this, 0, Map.size());
		}
		else {
			fireIntervalRemoved(this, 0, Map.size());
			fireIntervalAdded(this, 0, filteredMaps.size());
		}
	}

	public int getSize() {
		return (!filteredMaps.isEmpty()) ? filteredMaps.size() : Map.size();
	}

	public Object getElementAt(int index) {
		return (!filteredMaps.isEmpty()) ? Map.getMap(filteredMaps.get(index)) : Map.getMap(index);
	}

	@Override
	public void onMapAdded(Map map) {
		Logger.logInfo("Adding map " + Map.size());
		filteredMaps.clear();
		fireIntervalAdded(this, Map.size() - 1, Map.size());
	}

	private static boolean originCheck(Map map, String origin) {
		return (origin.equalsIgnoreCase(I18N.getLocaleString("MAIN_ALL"))) || (origin.equalsIgnoreCase("ftb") && map.getAuthor().equalsIgnoreCase("the ftb team")) || (origin.equalsIgnoreCase(I18N.getLocaleString("FILTER_3THPARTY")) && !map.getAuthor().equalsIgnoreCase("the ftb team"));
	}

	private static boolean compatibilityCheck(Map map, String compatible) {
		return (compatible.equalsIgnoreCase(I18N.getLocaleString("MAIN_ALL")) || map.isCompatible(compatible));
	}

	private static boolean textSearch(Map map, String query) {
		return ((query.isEmpty()) || map.getName().toLowerCase().contains(query) || map.getAuthor().toLowerCase().contains(query));
	}
}

class MapCellRenderer extends JPanel implements ListCellRenderer {
	private JLabel logo;
	private JTextArea description;

	public MapCellRenderer() {
		super();

		logo = new JLabel();
		description = new JTextArea();

		setLayout(null);
		logo.setBounds(6, 6, 42, 42);

		description.setBorder(null);
		description.setEditable(false);
		description.setForeground(Color.white);
		description.setBounds(58, 6, 378, 42);
		description.setBackground(new Color(255, 255, 255, 0));

		add(description);
		add(logo);

		setMinimumSize(new Dimension(420, 55));
		setPreferredSize(new Dimension(420, 55));
	}

	public Component getListCellRendererComponent(
		JList list, Object value, int index, boolean isSelected, boolean cellHasFocus
	) {
		Map map = (Map)value;

		if(cellHasFocus || isSelected) {
			setBackground(UIManager.getColor("control").darker().darker());
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		} else {
			setBackground(UIManager.getColor("control"));
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}

		logo.setIcon(new ImageIcon(map.getLogo()));
		description.setText(map.getName() + " (v." + map.getVersion() + ")\n" + "By " + map.getAuthor());

		return this;
	}
}

@SuppressWarnings("serial")
public class MapsPane extends JPanel implements ILauncherPane, MapListener {
	private MapListModelAdapter model;
	private static JList maps;
	private static JScrollPane mapsScroll;

	private static JLabel typeLbl;
	private JButton filter;

	private final MapsPane instance = this;
	private static JEditorPane mapInfo;

	public static String type = "Client", origin = "All", compatible = "All";
	public static boolean loaded = false;

	private static HashMap<Integer, Map> currentMaps = new HashMap<Integer, Map>();

	public MapsPane() {
		super();
		model = new MapListModelAdapter();

		setLayout(null);

		maps = new JList(model);
		maps.setCellRenderer(new MapCellRenderer());

		maps.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				Map map = (Map)maps.getSelectedValue();
				if(map != null) {
					String packs = "";
					if (map.getCompatible() != null) {
						packs += "<p>This map works with the following packs:</p><ul>";
						for (String name : map.getCompatible()) {
							packs += "<li>" + ModPack.getPack(name).getName() + "</li>";
						}
						packs += "</ul>";
					}
					LaunchFrame.updateMapInstallLocs(map.getCompatible());
					File tempDir = new File(OSUtils.getDynamicStorageLocation(), "Maps" + File.separator + map.getMapName());
					mapInfo.setText("<html><img src='file:///" + tempDir.getPath() + File.separator + map.getImageName() + "' width=400 height=200></img> <br>" + map.getInfo() + packs);
					mapInfo.setCaretPosition(0);
				}
			}
		});

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

	@Override
	public void onMapAdded(Map map) {
		model.onMapAdded(map);
	}

	public void sortMaps() {
		model.filter(origin, compatible, SearchDialog.lastMapSearch.toLowerCase());
		LaunchFrame.updateMapInstallLocs(new String[]{""});
	}

	public static int getSelectedMapIndex() {
		return maps.getSelectedIndex();
	}

	public void updateFilter() {
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

	public void updateLocale() {
		filter.setText(I18N.getLocaleString("FILTER_SETTINGS"));
	}
}
