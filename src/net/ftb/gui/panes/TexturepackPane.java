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
import net.ftb.data.ModPack;
import net.ftb.data.TexturePack;
import net.ftb.data.events.TexturePackListener;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.dialogs.SearchDialog;
import net.ftb.gui.dialogs.TexturePackFilterDialog;
import net.ftb.locale.I18N;
import net.ftb.log.Logger;
import net.ftb.util.OSUtils;

public class TexturepackPane extends JPanel implements ILauncherPane, TexturePackListener {
	private static JPanel texturePacks;
	public static ArrayList<JPanel> texturePackPanels;
	private static JScrollPane texturePacksScroll;
	private static JLabel splash;

	private static JLabel typeLbl;
	public static String origin = "All", compatible = "All";
	private JButton filter;
	private static boolean texturePacksAdded = false;
	private static int selectedTexturePack = 0;
	private static JEditorPane textureInfo;

	private TexturepackPane instance = this;

	private static HashMap<Integer, TexturePack> currentTexturePacks = new HashMap<Integer, TexturePack>();

	public static boolean loaded = false;

	public TexturepackPane() {
		super();
		this.setBorder(new EmptyBorder(5, 5, 5, 5));
		this.setLayout(null);

		splash = new JLabel();
		splash.setBounds(420, 0, 410, 200);
		this.add(splash);

		texturePackPanels = new ArrayList<JPanel>();

		// TODO: Set loading animation while we wait
		texturePacks = new JPanel();
		texturePacks.setLayout(null);
		texturePacks.setOpaque(false);

		final JPanel p = new JPanel();
		p.setBounds(0, 0, 420, 55);
		p.setLayout(null);

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
		typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + origin + "</font>";
		typeLblText += "</body></html>";

		typeLbl = new JLabel(typeLblText);
		typeLbl.setBounds(115, 5, 295, 25);
		typeLbl.setHorizontalAlignment(SwingConstants.CENTER);
		add(typeLbl);

		JTextArea filler = new JTextArea(I18N.getLocaleString("TEXTURE_WAIT_WHILE_LOADING"));
		filler.setBorder(null);
		filler.setEditable(false);
		filler.setForeground(Color.white);
		filler.setBounds(58, 6, 378, 42);
		filler.setBackground(new Color(255, 255, 255, 0));
		p.add(filler);
		texturePacks.add(p);

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
		infoScroll.setBounds(420, 210, 410, 90);
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

	/*
	 * GUI Code to add a texture pack to the selection
	 */
	public static void addTexturePack(TexturePack texturePack) {
		if (!texturePacksAdded) {
			texturePacksAdded = true;
			texturePacks.removeAll();
		}

		final int texturePackIndex = texturePackPanels.size();

		final JPanel p = new JPanel();
		p.setBounds(0, (texturePackIndex * 55), 420, 55);
		p.setLayout(null);
		JLabel logo = new JLabel(new ImageIcon(texturePack.getLogo()));
		logo.setBounds(6, 6, 42, 42);
		logo.setVisible(true);
		String info = "";
		if(texturePack.getInfo().length() > 60) {
			info = texturePack.getInfo().substring(0, 59) + "...";
		} else {
			info = texturePack.getInfo();
		}
		JTextArea filler = new JTextArea(texturePack.getName() + " : " + texturePack.getAuthor() + "\n" + info);
		filler.setBorder(null);
		filler.setEditable(false);
		filler.setForeground(Color.white);
		filler.setBounds(58, 6, 378, 42);
		filler.setBackground(new Color(255, 255, 255, 0));
		MouseListener lin = new MouseListener() {
			@Override public void mouseClicked(MouseEvent e) {
				selectedTexturePack = texturePackIndex;
				updateTexturePacks();
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
		texturePackPanels.add(p);
		texturePacks.add(p);
		if(origin.equalsIgnoreCase("all")) {
			texturePacks.setMinimumSize(new Dimension(420, (TexturePack.getTexturePackArray().size()) * 55));
			texturePacks.setPreferredSize(new Dimension(420, (TexturePack.getTexturePackArray().size()) * 55));
		} else {
			texturePacks.setMinimumSize(new Dimension(420, (currentTexturePacks.size()) * 55));
			texturePacks.setPreferredSize(new Dimension(420, (currentTexturePacks.size()) * 55));
		}
		texturePacksScroll.revalidate();
	}

	@Override
	public void onTexturePackAdded(TexturePack texturePack) {
		addTexturePack(texturePack);
		Logger.logInfo("Adding texture pack " + getTexturePackNum());
		updateTexturePacks();
	}

	public static void sortTexturePacks() {
		texturePackPanels.clear();
		texturePacks.removeAll();
		currentTexturePacks.clear();
		int counter = 0;
		selectedTexturePack = 0;
		texturePacks.repaint();
		HashMap<Integer, List<TexturePack>> sorted = new HashMap<Integer, List<TexturePack>>();			
		sorted.put(0, new ArrayList<TexturePack>());
		sorted.put(1, new ArrayList<TexturePack>());
		for(TexturePack texturePack : TexturePack.getTexturePackArray()) {
			if(compatibilityCheck(texturePack) && textSearch(texturePack)) {
				sorted.get((texturePack.isCompatible(ModPack.getSelectedPack().getDir())) ? 1 : 0).add(texturePack);
			}
		}
		for(TexturePack tp : sorted.get(1)) {
			addTexturePack(tp);
			currentTexturePacks.put(counter, tp);
			counter++;
		}
		for(TexturePack tp : sorted.get(0)) {
			addTexturePack(tp);
			currentTexturePacks.put(counter, tp);
			counter++;
		}
		updateTexturePacks();
	}

	private static void updateTexturePacks() {
		for (int i = 0; i < texturePackPanels.size(); i++) {
			if(selectedTexturePack == i) {
				texturePackPanels.get(i).setBackground(UIManager.getColor("control").darker().darker());
				splash.setIcon(new ImageIcon(TexturePack.getTexturePack(getIndex()).getImage()));
				texturePackPanels.get(i).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				LaunchFrame.updateTpInstallLocs(TexturePack.getTexturePack(getIndex()).getCompatible());
				textureInfo.setText(TexturePack.getTexturePack(getIndex()).getInfo());
				textureInfo.setCaretPosition(0);
			} else {
				texturePackPanels.get(i).setBackground(UIManager.getColor("control"));
				texturePackPanels.get(i).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}
		}
	}

	public static int getSelectedTexturePackIndex() {
		return texturePacksAdded ? getIndex() : -1;
	}

	public static void updateFilter() {
		String filterTextColor = LauncherStyle.getColorAsString(LauncherStyle.getCurrentStyle().filterTextColor);
		String filterInnerTextColor = LauncherStyle.getColorAsString(LauncherStyle.getCurrentStyle().filterInnerTextColor);

		String typeLblText = "<html><body>";
		typeLblText += "<strong><font color=rgb\"(" + filterTextColor + ")\">Filter: </strong></font>";
		typeLblText += "<font color=rgb\"(" + filterInnerTextColor + ")\">" + origin + "</font>";
		typeLblText += "</body></html>";

		typeLbl.setText(typeLblText);
		sortTexturePacks();
		LaunchFrame.getInstance().updateFooter();
	}

	private static int getIndex() {
		return (currentTexturePacks.size() > 0) ? currentTexturePacks.get(selectedTexturePack).getIndex() : selectedTexturePack;
	}

	private static int getTexturePackNum() {
		if(currentTexturePacks.size() > 0) {
			if(!origin.equalsIgnoreCase("all")) {
				return currentTexturePacks.get((texturePackPanels.size() - 1)).getIndex();
			}
		}
		return texturePackPanels.size();
	}

	public void updateLocale() {
		filter.setText(I18N.getLocaleString("FILTER_SETTINGS"));
	}

	private static boolean compatibilityCheck(TexturePack tp) {
		return (compatible.equalsIgnoreCase("all") || tp.isCompatible(compatible));
	}

	private static boolean textSearch(TexturePack tp) {
		String searchString = SearchDialog.lastTextureSearch.toLowerCase();
		return ((searchString.isEmpty()) || tp.getName().toLowerCase().contains(searchString) || tp.getAuthor().toLowerCase().contains(searchString));
	}
}