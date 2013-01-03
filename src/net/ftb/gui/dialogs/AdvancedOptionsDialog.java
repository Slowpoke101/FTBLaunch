package net.ftb.gui.dialogs;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import net.ftb.data.Settings;
import net.ftb.gui.LaunchFrame;
import net.ftb.log.Logger;
import net.ftb.util.DownloadUtils;

public class AdvancedOptionsDialog extends JDialog {
	private JTextField minecraftX, minecraftY, xPosField, yPosField, additionalJavaOptions;
	private JCheckBox autoMaxCheck, snooper;
	private static JComboBox downloadServers;
	private final Settings settings = Settings.getSettings();
	
	public AdvancedOptionsDialog() {
		super(LaunchFrame.getInstance(), true);
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
		setTitle("Advanced Options");
		setResizable(false);
		getContentPane().setLayout(null);
		setBounds(560, 150, 560, 150);
		
		JLabel downloadLocation = new JLabel("Download Location:");
		downloadLocation.setBounds(500, 88, 110, 23);
		add(downloadLocation);
		
		downloadServers = new JComboBox(getDownloadServerNames());
		downloadServers.setBounds(613, 88, 222, 23);
		downloadServers.addFocusListener(settingsChangeListener);
		if(DownloadUtils.serversLoaded) {
			if(DownloadUtils.downloadServers.containsKey(settings.getDownloadServer())) {
				downloadServers.setSelectedItem(settings.getDownloadServer());
			}
		}
		add(downloadServers);
		
		minecraftX = new JTextField();
		minecraftX.setBounds(190, 182, 95, 23);
		minecraftX.setText(Integer.toString(settings.getLastDimension().width));
		add(minecraftX);
		minecraftX.addFocusListener(settingsChangeListener);
		minecraftX.setColumns(10);

		JLabel lblMinecraftWindowSize = new JLabel("Minecraft Window Size");
		lblMinecraftWindowSize.setBounds(10, 182, 170, 20);
		add(lblMinecraftWindowSize);

		minecraftY = new JTextField();
		minecraftY.setBounds(317, 182, 95, 23);
		minecraftY.setText(Integer.toString(settings.getLastDimension().height));
		add(minecraftY);
		minecraftY.addFocusListener(settingsChangeListener);
		minecraftY.setColumns(10);

		JLabel lblX_1 = new JLabel("x");
		lblX_1.setBounds(295, 185, 15, 14);
		add(lblX_1);

		JLabel lblMinecraftWindowPosition = new JLabel("Minecraft Window Position");
		lblMinecraftWindowPosition.setBounds(10, 222, 170, 23);
		add(lblMinecraftWindowPosition);

		xPosField = new JTextField();
		xPosField.setBounds(190, 222, 95, 23);
		xPosField.setText(Integer.toString(settings.getLastPosition().x));
		add(xPosField);
		xPosField.addFocusListener(settingsChangeListener);
		xPosField.setColumns(10);

		JLabel label = new JLabel("x");
		label.setBounds(295, 226, 15, 14);
		add(label);

		yPosField = new JTextField();
		yPosField.setBounds(317, 222, 95, 23);
		yPosField.setText(Integer.toString(settings.getLastPosition().y));
		add(yPosField);
		yPosField.addFocusListener(settingsChangeListener);
		yPosField.setColumns(10);

		autoMaxCheck = new JCheckBox("Auto Maximised?");
		autoMaxCheck.setBounds(10, 252, 170, 23);
		autoMaxCheck.setSelected((settings.getLastExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH);
		autoMaxCheck.addFocusListener(settingsChangeListener);
		add(autoMaxCheck);
		
		snooper = new JCheckBox("Disable Google Analytic Tracking");
		snooper.setBounds(190, 252, 300, 23);
		snooper.setSelected(settings.getSnooper());
		snooper.addFocusListener(settingsChangeListener);
		add(snooper);

		JLabel additionalJavaOptionsLbl = new JLabel("Additional Java Options (For Advanced Users Only) : ");
		additionalJavaOptionsLbl.setBounds(490, 182, 320, 14);
		add(additionalJavaOptionsLbl);

		additionalJavaOptions = new JTextField(settings.getAdditionalJavaOptions());
		additionalJavaOptions.setBounds(485, 207, 350, 28);
		additionalJavaOptions.addFocusListener(settingsChangeListener);
		add(additionalJavaOptions);
	}

	public static void setDownloadServers() {
		String downloadserver = Settings.getSettings().getDownloadServer();
		downloadServers.removeAllItems();
		for(String server : DownloadUtils.downloadServers.keySet()) {
			downloadServers.addItem(server);
		}
		if(DownloadUtils.downloadServers.containsKey(downloadserver)) {
			downloadServers.setSelectedItem(downloadserver);
		}
	}

	public String[] getDownloadServerNames() {
		if(!DownloadUtils.serversLoaded) {
			Logger.logWarn("Servers not loaded yet.");
			return new String[] { "Automatic" };
		} else {
			Logger.logInfo("Servers are loaded, inserting into combo box.");
			String[] out = new String[DownloadUtils.downloadServers.size()];
			for(int i = 0; i < out.length; i++) {
				out[i] = String.valueOf(DownloadUtils.downloadServers.keySet().toArray()[i]);
			}
			return out;
		}
	}
	
	public void saveSettingsInto(Settings settings) {
		settings.setDownloadServer(String.valueOf(downloadServers.getItemAt(downloadServers.getSelectedIndex())));
		settings.setLastDimension(new Dimension(Integer.parseInt(minecraftX.getText()), Integer.parseInt(minecraftY.getText())));
		int lastExtendedState = settings.getLastExtendedState();
		settings.setLastExtendedState(autoMaxCheck.isSelected() ? (lastExtendedState | JFrame.MAXIMIZED_BOTH) : (lastExtendedState & ~JFrame.MAXIMIZED_BOTH));
		settings.setLastPosition(new Point(Integer.parseInt(xPosField.getText()), Integer.parseInt(yPosField.getText())));
		settings.setAdditionalJavaOptions(additionalJavaOptions.getText());
		settings.setSnooper(snooper.isSelected());
		settings.save();
	}
	
	private FocusListener settingsChangeListener = new FocusListener() {
		@Override
		public void focusLost(FocusEvent e) {
			saveSettingsInto(settings);
		}
		@Override public void focusGained(FocusEvent e) { }
	};
}
