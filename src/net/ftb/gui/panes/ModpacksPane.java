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
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.data.events.ModPackListener;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.dialogs.EditModPackDialog;
import net.ftb.gui.dialogs.ModPackFilterDialog;
import net.ftb.gui.dialogs.PrivatePackDialog;
import net.ftb.gui.dialogs.SearchDialog;
import net.ftb.locale.I18N;
import net.ftb.locale.I18N.Locale;
import net.ftb.log.Logger;
import net.ftb.util.DownloadUtils;
import net.ftb.util.OSUtils;
import net.ftb.util.TrackerUtils;

class ModPackListModelAdapter extends AbstractListModel implements ModPackListener {
	private Map<Integer, Integer> filteredPacks;

	public ModPackListModelAdapter() {
		super();
		filteredPacks = new HashMap<Integer, Integer>();
	}

	public void filter(String origin, String mcVersion, String availability, String query) {
		filteredPacks.clear();
		int counter = 0;
		for(int i = 0; i < ModPack.size(); ++i) {
			ModPack pack = ModPack.getPack(i);
			if(originCheck(pack, origin) && mcVersionCheck(pack, mcVersion) && availabilityCheck(pack, availability) && textSearch(pack, query)) {
				filteredPacks.put(counter, i);
				counter++;
			}
		}
		if(counter + 1 == ModPack.size()) {
			fireIntervalRemoved(this, 0, ModPack.size());
			fireIntervalAdded(this, 0, ModPack.size());
			filteredPacks.clear();
		}
		else {
			fireIntervalRemoved(this, 0, ModPack.size());
			fireIntervalAdded(this, 0, filteredPacks.size());
		}
	}

	public int getSize() {
		return (!filteredPacks.isEmpty()) ? filteredPacks.size() : ModPack.size();
	}

	public Object getElementAt(int index) {
		return (!filteredPacks.isEmpty()) ? ModPack.getPack(filteredPacks.get(index)) : ModPack.getPack(index);
	}

	@Override
	public void onModPackAdded(ModPack pack) {
		Logger.logInfo("Adding pack " + ModPack.size());
		filteredPacks.clear();
		fireIntervalAdded(this, ModPack.size() - 1, ModPack.size());
	}

	private static boolean availabilityCheck(ModPack pack, String availability) {
		return (availability.equalsIgnoreCase(I18N.getLocaleString("MAIN_ALL"))) || (availability.equalsIgnoreCase(I18N.getLocaleString("FILTER_PUBLIC")) && !pack.isPrivatePack()) || (availability.equalsIgnoreCase(I18N.getLocaleString("FILTER_PRIVATE")) && pack.isPrivatePack());
	}

	private static boolean mcVersionCheck(ModPack pack, String mcVersion) {
		return (mcVersion.equalsIgnoreCase(I18N.getLocaleString("MAIN_ALL"))) || (mcVersion.equalsIgnoreCase(pack.getMcVersion()));
	}

	private static boolean originCheck(ModPack pack, String origin) {
		return (origin.equalsIgnoreCase(I18N.getLocaleString("MAIN_ALL"))) || (origin.equalsIgnoreCase("ftb") && pack.getAuthor().equalsIgnoreCase("the ftb team")) || (origin.equalsIgnoreCase(I18N.getLocaleString("FILTER_3THPARTY")) && !pack.getAuthor().equalsIgnoreCase("the ftb team"));
	}

	private static boolean textSearch(ModPack pack, String query) {
		return ((query.isEmpty()) || pack.getName().toLowerCase().contains(query) || pack.getAuthor().toLowerCase().contains(query));
	}
}

class ModPackCellRenderer extends JPanel implements ListCellRenderer {
	private JLabel logo;
	private JTextArea description;

	public ModPackCellRenderer() {
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
		ModPack pack = (ModPack)value;

		if(cellHasFocus || isSelected) {
			setBackground(UIManager.getColor("control").darker().darker());
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		} else {
			setBackground(UIManager.getColor("control"));
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}

		logo.setIcon(new ImageIcon(pack.getLogo()));
		description.setText(pack.getName() + " (v" + pack.getVersion() + ") Minecraft Version " + pack.getMcVersion() + "\n" + "By " + pack.getAuthor());

		return this;
	}
}

@SuppressWarnings("serial")
public class ModpacksPane extends JPanel implements ILauncherPane, ModPackListener {
	private ModPackListModelAdapter model;
	private static JList packs;
	private static JScrollPane packsScroll;

	private static JLabel typeLbl;
	private JButton filter, editModPack;

	private static JButton server;

	private JButton privatePack;
	private static JComboBox version;
	private final ModpacksPane instance = this;
	private static JEditorPane packInfo;

	//	private JLabel loadingImage;
	public static String origin = "All", mcVersion = "All", avaliability = "All";
	public static boolean loaded = false;

	private static JScrollPane infoScroll;

	public ModpacksPane() {
		super();
		model = new ModPackListModelAdapter();

		setLayout(null);

		packs = new JList(model);
		packs.setCellRenderer(new ModPackCellRenderer());

		packs.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2)
				{
					LaunchFrame.getInstance().doLaunch();
				}
			}
		});

		packs.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				ModPack pack = (ModPack)packs.getSelectedValue();
				if(pack != null) {
					String mods = "";
					if(pack.getMods() != null) {
						mods += "<p>This pack contains the following mods by default:</p><ul>";
						for (String name : pack.getMods()) {
							mods += "<li>" + name + "</li>";
						}
						mods += "</ul>";
					}
					File tempDir = new File(OSUtils.getDynamicStorageLocation(), "ModPacks" + File.separator + pack.getDir());
					packInfo.setText("<html><img src='file:///" + tempDir.getPath() + File.separator + pack.getImageName() +"' width=400 height=200></img> <br>" + pack.getInfo() + mods);
					packInfo.setCaretPosition(0);

					if(ModPack.getSelectedPack().getServerUrl().equals("") || ModPack.getSelectedPack().getServerUrl() == null) {
						server.setEnabled(false);
					} else {
						server.setEnabled(true);
					}
					String tempVer = Settings.getSettings().getPackVer();
					version.removeAllItems();
					version.addItem("Recommended");
					if(pack.getOldVersions() != null) {
						for(String s : pack.getOldVersions()) {
							version.addItem(s);
						}
						version.setSelectedItem(tempVer);
					}
				}
			}
		});

		filter = new JButton(I18N.getLocaleString("FILTER_SETTINGS"));
		filter.setBounds(5, 5, 105, 25);
		filter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(loaded) {
					ModPackFilterDialog filterDia = new ModPackFilterDialog(instance);
					filterDia.setVisible(true);
				}
			}
		});
		add(filter);

		String filterTextColor = LauncherStyle.getColorAsString(LauncherStyle.getCurrentStyle().filterTextColor);
		String filterInnerTextColor = LauncherStyle.getColorAsString(LauncherStyle.getCurrentStyle().filterInnerTextColor);

		String typeLblText = "<html><body>";
		typeLblText += "<strong><font color=rgb\"(" + filterTextColor + ")\">Filter: </strong></font>";
		typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + origin + "</font>";
		typeLblText += "<font color=rgb\"(" + filterTextColor + ")\"> / </font>";
		typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + mcVersion + "</font>";
		typeLblText += "</body></html>";

		typeLbl = new JLabel(typeLblText);
		typeLbl.setBounds(115, 5, 175, 25);
		typeLbl.setHorizontalAlignment(SwingConstants.CENTER);
		add(typeLbl);

		editModPack = new JButton(I18N.getLocaleString("MODS_EDIT_PACK"));
		editModPack.setBounds(300, 5, 110, 25);
		editModPack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(packs.getSelectedIndex() >= 0) {
					EditModPackDialog empd = new EditModPackDialog(LaunchFrame.getInstance());
					empd.setVisible(true);
				}
			}
		});
		add(editModPack);

		packsScroll = new JScrollPane();
		packsScroll.setBounds(-3, 30, 420, 283);
		packsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		packsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		packsScroll.setWheelScrollingEnabled(true);
		packsScroll.setOpaque(false);
		packsScroll.setViewportView(packs);
		packsScroll.getVerticalScrollBar().setUnitIncrement(19);
		add(packsScroll);

		packInfo = new JEditorPane();
		packInfo.setEditable(false);
		packInfo.setContentType("text/html");
		packInfo.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent event) {
				if(event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					OSUtils.browse(event.getURL().toString());
				}
			}
		});
		// TODO: Fix darker background for text area? Or is it better blending in?
		packInfo.setBackground(UIManager.getColor("control").darker().darker());
		add(packInfo);

		infoScroll = new JScrollPane();
		infoScroll.setBounds(410, 25, 430, 290);
		infoScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		infoScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		infoScroll.setWheelScrollingEnabled(true);
		infoScroll.setViewportView(packInfo);
		infoScroll.setOpaque(false);
		add(infoScroll);

		server = new JButton("Download Server");
		server.setBounds(420, 5, 130, 25);

		server.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if(!ModPack.getSelectedPack().getServerUrl().isEmpty()) {
					if(packs.getSelectedIndex() >= 0) {
						try {
							if(!ModPack.getSelectedPack().getServerUrl().equals("") && ModPack.getSelectedPack().getServerUrl() != null) {
								String version = (Settings.getSettings().getPackVer().equalsIgnoreCase("recommended version") || Settings.getSettings().getPackVer().equalsIgnoreCase("newest version")) ? ModPack.getSelectedPack().getVersion().replace(".", "_") : Settings.getSettings().getPackVer().replace(".", "_");
								if(ModPack.getSelectedPack().isPrivatePack()) {
									OSUtils.browse(DownloadUtils.getCreeperhostLink("privatepacks%5E" + ModPack.getSelectedPack().getDir() + "%5E" + version + "%5E" + ModPack.getSelectedPack().getServerUrl()));
								} else {
									OSUtils.browse(DownloadUtils.getCreeperhostLink("modpacks%5E" + ModPack.getSelectedPack().getDir() + "%5E" + version + "%5E" + ModPack.getSelectedPack().getServerUrl()));
								}
								TrackerUtils.sendPageView(ModPack.getSelectedPack().getName() + " Server Download", ModPack.getSelectedPack().getName());
							}
						} catch (NoSuchAlgorithmException e) { }
					}
				}
			}
		});
		add(server);

		version = new JComboBox(new String[]{});
		version.setBounds(560, 5, 130, 25);
		version.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Settings.getSettings().setPackVer((String.valueOf(version.getSelectedItem()).equalsIgnoreCase("recommended") ? "Recommended Version" : String.valueOf(version.getSelectedItem())));
				Settings.getSettings().save();
			}
		});
		version.setToolTipText("Modpack Versions");
		add(version);

		privatePack = new JButton("Private Packs");
		privatePack.setBounds(700, 5, 120, 25);
		privatePack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PrivatePackDialog ap = new PrivatePackDialog();
				ap.setVisible(true);
			}
		});

		add(privatePack);
	}

	@Override public void onVisible() { }

	@Override
	public void onModPackAdded(ModPack pack) {
		model.onModPackAdded(pack);

		if(pack.getDir().equalsIgnoreCase(Settings.getSettings().getLastPack())) {
			packs.setSelectedValue(pack, true);
		}
	}

	public void sortPacks() {
		model.filter(origin, mcVersion, avaliability, SearchDialog.lastPackSearch.toLowerCase());
	}

	public static int getSelectedModIndex() {
		return packs.getSelectedIndex();
	}

	public void updateFilter() {
		String filterTextColor = LauncherStyle.getColorAsString(LauncherStyle.getCurrentStyle().filterTextColor);
		String filterInnerTextColor = LauncherStyle.getColorAsString(LauncherStyle.getCurrentStyle().filterInnerTextColor);

		String typeLblText = "<html><body>";
		typeLblText += "<strong><font color=rgb\"(" + filterTextColor + ")\">Filter: </strong></font>";
		typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + origin + "</font>";
		typeLblText += "<font color=rgb\"(" + filterTextColor + ")\"> / </font>";
		typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + mcVersion + "</font>";
		typeLblText += "</body></html>";

		typeLbl.setText(typeLblText);
		sortPacks();
		LaunchFrame.getInstance().updateFooter();
	}

	public void updateLocale() {
		filter.setText(I18N.getLocaleString("FILTER_SETTINGS"));
		editModPack.setText(I18N.getLocaleString("MODS_EDIT_PACK"));
		if(I18N.currentLocale == Locale.deDE) {
			editModPack.setBounds(290, 5, 120, 25);
			typeLbl.setBounds(115, 5, 165, 25);
		} else {
			editModPack.setBounds(300, 5, 110, 25);
			typeLbl.setBounds(115, 5, 175, 25);
		}
	}
}
