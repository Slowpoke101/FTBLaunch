package net.ftb.gui.dialogs;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.gui.LaunchFrame;

public class EditModPackDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	
	private JButton jarButton = new JButton("Open Jar Mods Folder");
	private JButton modsButton = new JButton("Open Folder");
	private JButton disableMod = new JButton("Disable Mod >>");
	private JButton enableMod = new JButton("<< Enable Mod");
	private JButton enableCoreMods = new JButton(">");
	private JButton disableCoreMods = new JButton("<");
	
	private JLabel enabledLabel = new JLabel("<html><body><h1>Enabled Mods</h1></html></body>");
	private JLabel disabledLabel = new JLabel("<html><body><h1>Disabled Mods</h1></html></body>");
	
	private JList enabledMods;
	private JList disabledMods;
	
	private List<String> enabledModsList_ = new ArrayList<String>();
	private List<String> disabledModsList_ = new ArrayList<String>();
	
	private final File modsFolder = new File(Settings.getSettings().getInstallPath() + File.separator + ModPack.getPack(LaunchFrame.getSelectedModIndex()).getDir() + File.separator + ".minecraft" + File.separator + "mods");

	public EditModPackDialog(LaunchFrame instance) {
		super(instance, true);

		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
		setTitle("Mod Pack Editor");
		setBounds(instance.getBounds());
		setResizable(false);
		getContentPane().setLayout(null);

		tabbedPane.setLocation(0, 0);
		tabbedPane.setSize(getSize());
		JPanel modsFolderPane = new JPanel();
		modsFolderPane.setLayout(null);
		JPanel jarCoreMods = new JPanel();
		jarCoreMods.setLayout(null);
		getContentPane().add(tabbedPane);
		tabbedPane.addTab("<html><body leftMargin=15 topmargin=8 marginwidth=15 marginheight=5>Mods</body></html>", modsFolderPane);
		tabbedPane.addTab("<html><body leftMargin=15 topmargin=8 marginwidth=15 marginheight=5>JarMods</body></html>", jarCoreMods);
		
//		tabbedPane.add(modsFolderPane, 0);
//		tabbedPane.add(jarCoreMods, 1);
//		tabbedPane.setIconAt(0, new ImageIcon(instance.getClass().getResource("/image/tabs/news.png")));
//		tabbedPane.setIconAt(1, new ImageIcon(instance.getClass().getResource("/image/tabs/maps.png")));
		
		tabbedPane.setSelectedIndex(0);
		
		jarButton.setVisible(true);
		jarButton.setBounds(50, 10, 200, 40);
		jarButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(Desktop.isDesktopSupported()) {
					File instMods = new File(Settings.getSettings().getInstallPath() + File.separator + ModPack.getPack(LaunchFrame.getSelectedModIndex()).getDir() 
							+ File.separator + "instMods");
					if(!instMods.exists()) {
						instMods.mkdirs();
					}
					Desktop desktop = Desktop.getDesktop();
					try {
						desktop.open(instMods);
					} catch (IOException e1) { }
				}
			}
		});
		jarCoreMods.add(jarButton);

		modsButton.setVisible(true);
		modsButton.setBounds((instance.getWidth() - 210), (instance.getHeight() - 120), 200, 40);
		modsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(Desktop.isDesktopSupported()) {
					File instMods = new File(Settings.getSettings().getInstallPath() + File.separator + ModPack.getPack(LaunchFrame.getSelectedModIndex()).getDir() 
							+ File.separator + ".minecraft" + File.separator + "mods");
					if(!instMods.exists()) {
						instMods.mkdirs();
					}
					Desktop desktop = Desktop.getDesktop();
					try {
						desktop.open(instMods);
					} catch (IOException e1) { }
				}
			}
		});
		modsFolderPane.add(modsButton);
		
		enabledLabel.setBounds(10, 10, 240, 30);
		enabledLabel.setHorizontalAlignment(SwingConstants.CENTER);
		modsFolderPane.add(enabledLabel);
		
		disabledLabel.setBounds(380, 10, 240, 30);
		disabledLabel.setHorizontalAlignment(SwingConstants.CENTER);
		modsFolderPane.add(disabledLabel);
		
		if(!modsFolder.exists()) {
			modsFolder.mkdirs();
		}
		
		String[] enabledModsList = getEnabledMods();
		String[] disabledModsList = getDisabledMods();
		
		enabledMods = new JList(enabledModsList);
		enabledMods.setBackground(UIManager.getColor("control").darker().darker());
		JScrollPane enabledModsScroll = new JScrollPane(enabledMods);
		enabledModsScroll.setBounds(10, 40, 240, 360);
		modsFolderPane.add(enabledModsScroll);
		
		disabledMods = new JList(disabledModsList);
		disabledMods.setBackground(UIManager.getColor("control").darker().darker());
		JScrollPane disabledModsScroll = new JScrollPane(disabledMods);
		disabledModsScroll.setBounds(380, 40, 240, 360);
		modsFolderPane.add(disabledModsScroll);
		
		disableMod.setBounds(255, 80, 115, 30);
		disableMod.setVisible(true);
		disableMod.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(enabledMods.getSelectedIndices().length > 1) {
					for(int i = 0; i < enabledMods.getSelectedIndices().length; i++) {
						String name = enabledModsList_.get(enabledMods.getSelectedIndices()[i]);
						new File(modsFolder, name).renameTo(new File(modsFolder, name + ".disabled"));
					}
					updateLists();
				} else {
					if(enabledMods.getSelectedIndex() > 0) {
						String name = enabledModsList_.get(enabledMods.getSelectedIndex());
						new File(modsFolder, name).renameTo(new File(modsFolder, name + ".disabled"));
					}
					updateLists();
				}
			}
		});
		modsFolderPane.add(disableMod);
		
		enableMod.setBounds(255, 120, 115, 30);
		enableMod.setVisible(true);
		enableMod.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(disabledMods.getSelectedIndices().length > 1) {
					for(int i = 0; i < disabledMods.getSelectedIndices().length; i++) {
						String name = disabledModsList_.get(disabledMods.getSelectedIndices()[i]);
						new File(modsFolder, name).renameTo(new File(modsFolder, name.replace(".disabled", "")));
					}
					updateLists();
				} else {
					if(disabledMods.getSelectedIndex() > 0) {
						String name = disabledModsList_.get(disabledMods.getSelectedIndex());
						new File(modsFolder, name).renameTo(new File(modsFolder, name.replace(".disabled", "")));
					}
					updateLists();
				}
			}
		});
		modsFolderPane.add(enableMod);
	}
	
	private String[] getEnabledMods() {
		enabledModsList_.clear();
		for(String name : modsFolder.list()) {
			if(name.toLowerCase().endsWith(".zip")) {
				enabledModsList_.add(name);
			} else if(name.toLowerCase().endsWith(".jar")) {
				enabledModsList_.add(name);
			}
		}
		String[] enabledModsList = new String[enabledModsList_.size()];
		for(int i = 0; i < enabledModsList_.size(); i++) {
			enabledModsList[i] = enabledModsList_.get(i).replace(".zip", "").replace(".jar", "");
		}
		return enabledModsList;
	}
	
	private String[] getDisabledMods() {
		disabledModsList_.clear();
		for(String name : modsFolder.list()) {
			if(name.toLowerCase().endsWith(".zip.disabled")) {
				disabledModsList_.add(name);
			} else if(name.toLowerCase().endsWith(".jar.disabled")) {
				disabledModsList_.add(name);
			}
		}
		String[] disabledModsList = new String[disabledModsList_.size()];
		for(int i = 0; i < disabledModsList_.size(); i++) {
			disabledModsList[i] = disabledModsList_.get(i).replace(".zip.disabled", "").replace(".jar.disabled", "");
		}
		return disabledModsList;
	}
	
	private void updateLists() {
		enabledMods.setListData(getEnabledMods());
		disabledMods.setListData(getDisabledMods());
	}
}