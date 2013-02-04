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
import net.ftb.data.TexturePack;
import net.ftb.data.events.TexturePackListener;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.dialogs.SearchDialog;
import net.ftb.gui.dialogs.TexturePackFilterDialog;
import net.ftb.locale.I18N;
import net.ftb.log.Logger;
import net.ftb.util.OSUtils;

class TexturePackListModelAdapter extends AbstractListModel implements TexturePackListener {
	private HashMap<Integer, Integer> filteredPacks;

	public TexturePackListModelAdapter() {
		super();
		filteredPacks = new HashMap<Integer, Integer>();
	}

	public void filter(String compatible, String resolution, String query) {
		filteredPacks.clear();
		int counter = 0;
		for(int i = 0; i < TexturePack.size(); ++i) {
			TexturePack texturePack = TexturePack.getTexturePack(i);
			if(texturePack.isCompatible(ModPack.getSelectedPack().getDir()) && compatibilityCheck(texturePack, compatible) && resolutionCheck(texturePack, resolution) && textSearch(texturePack, query)) {
				filteredPacks.put(counter, i);
				counter++;
			}
		}
		for(int i = 0; i < TexturePack.size(); ++i) {
			TexturePack texturePack = TexturePack.getTexturePack(i);
			if(!texturePack.isCompatible(ModPack.getSelectedPack().getDir()) && compatibilityCheck(texturePack, compatible) && resolutionCheck(texturePack, resolution) && textSearch(texturePack, query)) {
				filteredPacks.put(counter, i);
				counter++;
			}
		}
		if(counter + 1 == TexturePack.size()) {
			filteredPacks.clear();
			fireIntervalRemoved(this, 0, TexturePack.size());
			fireIntervalAdded(this, 0, TexturePack.size());
		}
		else {
			fireIntervalRemoved(this, 0, TexturePack.size());
			fireIntervalAdded(this, 0, filteredPacks.size());
		}
	}

	public int getSize() {
		return (!filteredPacks.isEmpty()) ? filteredPacks.size() : TexturePack.size();
	}

	public Object getElementAt(int index) {
		return (!filteredPacks.isEmpty()) ? TexturePack.getTexturePack(filteredPacks.get(index)) : TexturePack.getTexturePack(index);
	}

	@Override
	public void onTexturePackAdded(TexturePack texturePack) {
		Logger.logInfo("Adding texture pack " + TexturePack.size());
		filteredPacks.clear();
		fireIntervalAdded(this, TexturePack.size() - 1, TexturePack.size());
	}

	private static boolean compatibilityCheck(TexturePack tp, String compatible) {
		return (compatible.equalsIgnoreCase(I18N.getLocaleString("MAIN_ALL")) || tp.isCompatible(compatible));
	}

	private static boolean resolutionCheck(TexturePack tp, String resolution) {
		return (resolution.equalsIgnoreCase(I18N.getLocaleString("MAIN_ALL")) || tp.getResolution().equalsIgnoreCase(resolution));
	}

	private static boolean textSearch(TexturePack tp, String query) {
		return ((query.isEmpty()) || tp.getName().toLowerCase().contains(query) || tp.getAuthor().toLowerCase().contains(query));
	}
}

class TexturePackCellRenderer extends JPanel implements ListCellRenderer {
	private JLabel logo;
	private JTextArea description;

	public TexturePackCellRenderer() {
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
		TexturePack pack = (TexturePack)value;

		if(cellHasFocus || isSelected) {
			setBackground(UIManager.getColor("control").darker().darker());
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		} else {
			setBackground(UIManager.getColor("control"));
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}

		String info = "";
		if(pack.getInfo().length() > 60) {
			info = pack.getInfo().substring(0, 59) + "...";
		} else {
			info = pack.getInfo();
		}

		logo.setIcon(new ImageIcon(pack.getLogo()));
		description.setText(pack.getName() + " : " + pack.getAuthor() + "\n" + info);

		return this;
	}
}

@SuppressWarnings("serial")
public class TexturepackPane extends JPanel implements ILauncherPane, TexturePackListener {
	private TexturePackListModelAdapter model;
	private static JList texturePacks;
	private static JScrollPane texturePacksScroll;

	private static JLabel typeLbl;
	private JButton filter;

	private TexturepackPane instance = this;
	private static JEditorPane textureInfo;

	public static String compatible = "All", resolution = "All";
	public static boolean loaded = false;

	public TexturepackPane() {
		super();
		model = new TexturePackListModelAdapter();

		setLayout(null);

		texturePacks = new JList(model);
		texturePacks.setCellRenderer(new TexturePackCellRenderer());

		texturePacks.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				TexturePack pack = (TexturePack)texturePacks.getSelectedValue();
				if(pack != null) {
					String packs = "";
					if (pack.getCompatible() != null) {
						packs += "<p>This texture pack works with the following packs:</p><ul>";
						for (String name : pack.getCompatible()) {
							packs += "<li>" + ModPack.getPack(name).getName() + "</li>";
						}
						packs += "</ul>";
					}
					LaunchFrame.updateTpInstallLocs(pack.getCompatible());
					File tempDir = new File(OSUtils.getDynamicStorageLocation(), "TexturePacks" + File.separator + pack.getName());
					textureInfo.setText("<html><img src='file:///" + tempDir.getPath() + File.separator + pack.getImageName() +"' width=400 height=200></img> <br>" + pack.getInfo() + packs);
					textureInfo.setCaretPosition(0);
				}
			}
		});

		filter = new JButton(I18N.getLocaleString("FILTER_SETTINGS"));
		filter.setBounds(5, 5, 105, 25);
		filter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				TexturePackFilterDialog filter = new TexturePackFilterDialog(instance);
				filter.setVisible(true);
			}
		});
		add(filter);

		String filterTextColor = LauncherStyle.getColorAsString(LauncherStyle.getCurrentStyle().filterTextColor);
		String filterInnerTextColor = LauncherStyle.getColorAsString(LauncherStyle.getCurrentStyle().filterInnerTextColor);

		String typeLblText = "<html><body>";
		typeLblText += "<strong><font color=rgb\"(" + filterTextColor + ")\">Filter: </strong></font>";
		typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + compatible + "</font>";
		typeLblText += "<strong><font color=rgb\"(" + filterTextColor + ")\"> / </strong></font>";
		typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + resolution + "</font>";
		typeLblText += "</body></html>";

		typeLbl = new JLabel(typeLblText);
		typeLbl.setBounds(115, 5, 295, 25);
		typeLbl.setHorizontalAlignment(SwingConstants.CENTER);
		add(typeLbl);

		texturePacksScroll = new JScrollPane();
		texturePacksScroll.setBounds(-3, 30, 420, 283);
		texturePacksScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		texturePacksScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		texturePacksScroll.setWheelScrollingEnabled(true);
		texturePacksScroll.setOpaque(false);
		texturePacksScroll.setViewportView(texturePacks);
		texturePacksScroll.getVerticalScrollBar().setUnitIncrement(19);
		add(texturePacksScroll);

		textureInfo = new JEditorPane();
		textureInfo.setEditable(false);
		textureInfo.setContentType("text/html");
		textureInfo.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent event) {
				if(event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					OSUtils.browse(event.getURL().toString());
				}
			}
		});
		textureInfo.setBounds(420, 210, 410, 90);
		textureInfo.setBackground(UIManager.getColor("control").darker().darker());
		add(textureInfo);

		JScrollPane infoScroll = new JScrollPane();
		infoScroll.setBounds(410, 25, 430, 290);
		infoScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		infoScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		infoScroll.setWheelScrollingEnabled(true);
		infoScroll.setViewportView(textureInfo);
		infoScroll.setOpaque(false);
		add(infoScroll);
	}

	@Override public void onVisible() {
		updateFilter();
	}

	@Override
	public void onTexturePackAdded(TexturePack texturePack) {
		model.onTexturePackAdded(texturePack);
	}

	public void sortTexturePacks() {
		model.filter(compatible, resolution, SearchDialog.lastTextureSearch.toLowerCase());
	}

	public static int getSelectedTexturePackIndex() {
		return texturePacks.getSelectedIndex();
	}

	public void updateFilter() {
		String filterTextColor = LauncherStyle.getColorAsString(LauncherStyle.getCurrentStyle().filterTextColor);
		String filterInnerTextColor = LauncherStyle.getColorAsString(LauncherStyle.getCurrentStyle().filterInnerTextColor);

		String typeLblText = "<html><body>";
		typeLblText += "<strong><font color=rgb\"(" + filterTextColor + ")\">Filter: </strong></font>";
		typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + compatible + "</font>";
		typeLblText += "<font color=rgb\"(" + filterTextColor + ")\"> / </font>";
		typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + resolution + "</font>";
		typeLblText += "</body></html>";

		typeLbl.setText(typeLblText);
		sortTexturePacks();
		LaunchFrame.getInstance().updateFooter();
	}

	public void updateLocale() {
		filter.setText(I18N.getLocaleString("FILTER_SETTINGS"));
	}
}
