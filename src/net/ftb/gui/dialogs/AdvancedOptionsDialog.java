package net.ftb.gui.dialogs;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JButton;
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
	private JButton exitBtn;
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
		setBounds(440, 260, 440, 260);

		JLabel downloadLocation = new JLabel("Download Location:");
		downloadLocation.setBounds(10, 10, 110, 25);
		add(downloadLocation);

		downloadServers = new JComboBox(getDownloadServerNames());
		downloadServers.setBounds(190, 10, 222, 25);
		downloadServers.addFocusListener(settingsChangeListener);
		if(DownloadUtils.serversLoaded) {
			if(DownloadUtils.downloadServers.containsKey(settings.getDownloadServer())) {
				downloadServers.setSelectedItem(settings.getDownloadServer());
			}
		}
		add(downloadServers);

		JLabel additionalJavaOptionsLbl = new JLabel("Additional Java Parameters: ");
		additionalJavaOptionsLbl.setBounds(10, 45, 320, 25);
		add(additionalJavaOptionsLbl);

		additionalJavaOptions = new JTextField(settings.getAdditionalJavaOptions());
		additionalJavaOptions.setBounds(190, 45, 222, 28);
		additionalJavaOptions.addFocusListener(settingsChangeListener);
		add(additionalJavaOptions);

		minecraftX = new JTextField();
		minecraftX.setBounds(190, 80, 95, 25);
		minecraftX.setText(Integer.toString(settings.getLastDimension().width));
		add(minecraftX);
		minecraftX.addFocusListener(settingsChangeListener);
		minecraftX.setColumns(10);

		JLabel lblMinecraftWindowSize = new JLabel("Minecraft Window Size:");
		lblMinecraftWindowSize.setBounds(10, 80, 170, 25);
		add(lblMinecraftWindowSize);

		minecraftY = new JTextField();
		minecraftY.setBounds(317, 80, 95, 25);
		minecraftY.setText(Integer.toString(settings.getLastDimension().height));
		add(minecraftY);
		minecraftY.addFocusListener(settingsChangeListener);
		minecraftY.setColumns(10);

		JLabel lblX_1 = new JLabel("x");
		lblX_1.setBounds(297, 80, 15, 25);
		add(lblX_1);

		JLabel lblMinecraftWindowPosition = new JLabel("Minecraft Window Position:");
		lblMinecraftWindowPosition.setBounds(10, 115, 170, 25);
		add(lblMinecraftWindowPosition);

		xPosField = new JTextField();
		xPosField.setBounds(190, 115, 95, 25);
		xPosField.setText(Integer.toString(settings.getLastPosition().x));
		add(xPosField);
		xPosField.addFocusListener(settingsChangeListener);
		xPosField.setColumns(10);

		JLabel label = new JLabel("x");
		label.setBounds(297, 115, 15, 25);
		add(label);

		yPosField = new JTextField();
		yPosField.setBounds(317, 115, 95, 25);
		yPosField.setText(Integer.toString(settings.getLastPosition().y));
		add(yPosField);
		yPosField.addFocusListener(settingsChangeListener);
		yPosField.setColumns(10);

		autoMaxCheck = new JCheckBox("Auto Maximised?");
		autoMaxCheck.setBounds(10, 150, 170, 25);
		autoMaxCheck.setSelected((settings.getLastExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH);
		autoMaxCheck.addFocusListener(settingsChangeListener);
		add(autoMaxCheck);

		snooper = new JCheckBox("Disable Google Analytic Tracking");
		snooper.setBounds(190, 150, 300, 25);
		snooper.setSelected(settings.getSnooper());
		snooper.addFocusListener(settingsChangeListener);
		add(snooper);

		exitBtn = new JButton("EXIT");
		exitBtn.setBounds(150, 190, 140, 28);
		exitBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		add(exitBtn);
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
