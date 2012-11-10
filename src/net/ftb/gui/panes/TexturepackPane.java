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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import net.ftb.data.TexturePack;
import net.ftb.data.events.TexturePackListener;
import net.ftb.gui.LaunchFrame;
import net.ftb.locale.I18N;

public class TexturepackPane extends JPanel implements ILauncherPane, TexturePackListener {
	private static final long serialVersionUID = 1L;

	private static JPanel texturePacks;
	public static ArrayList<JPanel> texturePackPanels;
	private static JScrollPane texturePacksScroll;
	private static JLabel splash;

	private static JLabel typeLbl;
	public static String type = "Client", origin = "All";
	private JButton filter;
	private static boolean texturePacksAdded = false;
	private static int selectedTexturePack = 0;

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

			}
		});
		add(filter);

		typeLbl = new JLabel("<html><body><strong><font color=rgb\"(243,119,31)\">Filter:</strong></font> " + type + "<font color=rgb\"(243,119,31)\"> / </font>" + origin +"</body></html>");
		typeLbl.setBounds(115, 5, 175, 25);
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
		texturePacksScroll.setBounds(0, 30, 420, 280);
		texturePacksScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		texturePacksScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		texturePacksScroll.setWheelScrollingEnabled(true);
		texturePacksScroll.setOpaque(false);
		texturePacksScroll.setViewportView(texturePacks);
		add(texturePacksScroll);
	}

	@Override public void onVisible() { }

	/*
	 * GUI Code to add a texture pack to the selection
	 */
	public static void addTexturePack(TexturePack texturePack) {
		if (!texturePacksAdded) {
			texturePacksAdded = true;
			texturePacks.removeAll();
		}

		final int texturePackIndex = texturePackPanels.size();
		System.out.println("Adding texture pack " + getTexturePackNum());
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
		updateTexturePacks();
	}

	private static void sortTexturePacks() {
		texturePackPanels.clear();
		texturePacks.removeAll();
		currentTexturePacks.clear();
		texturePacks.setMinimumSize(new Dimension(420, 0));
		texturePacks.setPreferredSize(new Dimension(420, 0));
		texturePacks.setLayout(null);
		texturePacks.setOpaque(false);
		int counter = 0;
		selectedTexturePack = 0;
		if(origin.equalsIgnoreCase("all")) {
			for(TexturePack texturePack : TexturePack.getTexturePackArray()) {
				addTexturePack(texturePack);
				currentTexturePacks.put(counter, texturePack);
				counter++;
			}
		} else if(origin.equalsIgnoreCase("ftb")) {
			for(TexturePack texturePack : TexturePack.getTexturePackArray()) {
				if(texturePack.getAuthor().equalsIgnoreCase("the ftb team")) {
					addTexturePack(texturePack);
					currentTexturePacks.put(counter, texturePack);
					counter++;
				}
			}
		} else {
			for(TexturePack texturePack : TexturePack.getTexturePackArray()) {
				if(!texturePack.getAuthor().equalsIgnoreCase("the ftb team")) {
					addTexturePack(texturePack);
					currentTexturePacks.put(counter, texturePack);
					counter++;
				}
			}
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
			} else {
				texturePackPanels.get(i).setBackground(UIManager.getColor("control"));
				texturePackPanels.get(i).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}
		}
	}

	public int getSelectedTexturePackIndex() {
		return texturePacksAdded ? getIndex() : -1;
	}

	public static void updateFilter() {
		typeLbl.setText("<html><body><strong><font color=rgb\"(243,119,31)\">Filter:</strong></font> " + type + "<font color=rgb\"(243,119,31)\"> / </font>" + origin +"</body></html>");
		sortTexturePacks();
		LaunchFrame.getInstance().updateFooter();
	}

	private static int getIndex() {
		if(currentTexturePacks.size() > 0) {
			if(currentTexturePacks.size() != TexturePack.getTexturePackArray().size()) {
				if(!origin.equalsIgnoreCase("all")) {
					return currentTexturePacks.get(selectedTexturePack).getIndex();
				}
			}
		}
		return selectedTexturePack;
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
}