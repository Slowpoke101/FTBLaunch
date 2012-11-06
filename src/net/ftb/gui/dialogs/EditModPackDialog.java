package net.ftb.gui.dialogs;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.gui.LaunchFrame;

public class EditModPackDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	
	private JButton jarButton = new JButton("Open Jar Mods Folder");
	private JButton modsButton = new JButton("Open Mods Folder");

	public EditModPackDialog(LaunchFrame instance) {
		super(instance, true);

		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
		setTitle("Mod Pack Editor");
		setBounds(300, 300, 440, 580);
		setResizable(false);

		tabbedPane.setBounds(0, 0, 440, 580);
		JPanel test = new JPanel();
//		test.setName("TEST");
		getContentPane().add(tabbedPane);
		tabbedPane.add(test, 0);
		tabbedPane.setIconAt(0, new ImageIcon(instance.getClass().getResource("/image/tabs/news.png")));
		tabbedPane.setSelectedIndex(0);
		
		jarButton.setVisible(true);
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
//		panel.add(jarButton);

		modsButton.setVisible(true);
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
//		panel.add(modsButton);
	}
}