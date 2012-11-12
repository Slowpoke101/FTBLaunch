package net.ftb.gui.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.gui.LaunchFrame;
import net.ftb.log.Logger;
import net.ftb.mclauncher.MinecraftLauncher;

public class PlayOfflineDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private JTextArea text;
	private JButton yes, no;
	
	public PlayOfflineDialog(String cause, final String username) {
		if(cause.equals("mcDown")) {
			text = new JTextArea("Minecraft Servers are down..\nWould you like to Play Offline?");
		} else if (cause.equals("other")) {
			text = new JTextArea("Something went wrong..\nWould you like to Play Offline?");
		}
		
		yes = new JButton("Yes");
		yes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int result = MinecraftLauncher.launchMinecraft(Settings.getSettings().getInstallPath() + File.separator + ModPack.getPack(LaunchFrame.getSelectedModIndex()).getDir() + File.separator + "minecraft", username, "offlinemods", LaunchFrame.FORGENAME, Settings.getSettings().getRamMin(), Settings.getSettings().getRamMax());
				Logger.logInfo("MinecraftLauncher said: "+result);
				if (result > 0) {
					System.exit(0);
				}
			}
		});
		no = new JButton("No");
		no.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		drawGui();
	}
	
	public void drawGui() {
		setBounds(300, 300, 225, 150);
		JScrollPane pane = new JScrollPane(text);
		pane.setBounds(10, 10, 190, 60);
		no.setBounds(110, 80, 90, 25);
		yes.setBounds(10, 80, 90, 25);
		getContentPane().setLayout(null);
		getContentPane().add(pane);
		getContentPane().add(no);
		getContentPane().add(yes);
	}
}
