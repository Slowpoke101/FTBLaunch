package net.ftb.gui.dialogs;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.gui.LaunchFrame;

public class EditModPackDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	
	private JButton jarButton = new JButton("Open Jar Mods Folder");
	private JButton modsButton = new JButton("Open Mods Folder");
	
	private JButton enableMods = new JButton(">");
	private JButton disableMods = new JButton("<");
	
	private JList enabledMods;
	private JList disabledMods;
	
	private JButton enableCoreMods = new JButton(">");
	private JButton disableCoreMods = new JButton("<");

	public EditModPackDialog(LaunchFrame instance) {
		super(instance, true);

		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
		setTitle("Mod Pack Editor");
		setBounds(300, 300, 440, 580);
		setResizable(false);
		getContentPane().setLayout(null);

		tabbedPane.setBounds(0, 0, 440, 580);
		JPanel modsFolderPane = new JPanel();
		modsFolderPane.setLayout(null);
		JPanel jarCoreMods = new JPanel();
		jarCoreMods.setLayout(null);
//		test.setName("TEST");
		getContentPane().add(tabbedPane);
		tabbedPane.add(modsFolderPane, 0);
		tabbedPane.add(jarCoreMods, 1);
		tabbedPane.setIconAt(0, new ImageIcon(instance.getClass().getResource("/image/tabs/news.png")));
		tabbedPane.setIconAt(1, new ImageIcon(instance.getClass().getResource("/image/tabs/maps.png")));
		
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
		modsButton.setBounds(50, 10, 200, 40);
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
		
		File modsFolder = new File(Settings.getSettings().getInstallPath() + File.separator + ModPack.getPack(LaunchFrame.getSelectedModIndex()).getDir() + File.separator + ".minecraft" + File.separator + "mods");
		
		List<String> enabledModsList_ = new ArrayList<String>();
		List<String> disabledModsList_ = new ArrayList<String>();
		
		for(String name : modsFolder.list()) {
			if(name.toLowerCase().endsWith(".zip")) {
				enabledModsList_.add(name.replace(".zip", ""));
			} else if(name.toLowerCase().endsWith(".jar")) {
				enabledModsList_.add(name.replace(".jar", ""));
			} else if(name.toLowerCase().endsWith(".zip.disabled")) {
				disabledModsList_.add(name.replace(".zip.disabled", ""));
			} else if(name.toLowerCase().endsWith(".jar.disabled")) {
				disabledModsList_.add(name.replace(".jar.disabled", ""));
			}
		}
		
		String[] enabledModsList = new String[enabledModsList_.size()];
		for(int i = 0; i < enabledModsList_.size(); i++) {
			enabledModsList[i] = enabledModsList_.get(i);
		}
		
		String[] disabledModsList = new String[disabledModsList_.size()];
		for(int i = 0; i < disabledModsList_.size(); i++) {
			disabledModsList[i] = disabledModsList_.get(i);
		}
		
		enabledMods = new JList(enabledModsList);
		JScrollPane enabledModsScroll = new JScrollPane(enabledMods);
		enabledModsScroll.setBounds(10, 80, 150, 350);
		modsFolderPane.add(enabledModsScroll);
		
		enableMods.setVisible(true);
		enableMods.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
			}
		});
		modsFolderPane.add(enableMods);
	}
}