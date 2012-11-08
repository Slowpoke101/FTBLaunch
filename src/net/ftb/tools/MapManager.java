package net.ftb.tools;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import net.ftb.data.Map;
import net.ftb.data.Settings;
import net.ftb.gui.LaunchFrame;
import net.ftb.log.Logger;

public class MapManager extends JDialog {
	private static final long serialVersionUID = 6897832855341265019L;
	
	private JPanel contentPane;
	private double downloadedPerc;
	private final JProgressBar progressBar;
	private final JLabel label;
	
	private class MapManagerWorker extends SwingWorker<Boolean, Void> {
		@Override
		protected Boolean doInBackground() throws Exception {
			// Check if map exists
			// Prompt user for overwrite/rename if folder is present
			// Download/Install Map
			return false;
		}
		
		public void downloadUrl(String filename, String urlString) throws MalformedURLException, IOException {
			BufferedInputStream in = null;
			FileOutputStream fout = null;
			try {
				in = new BufferedInputStream(new URL(urlString).openStream());
				fout = new FileOutputStream(filename);
				byte data[] = new byte[1024];
				int count, amount = 0, steps = 0;
				int mapSize = Map.getMap(LaunchFrame.getSelectedMapIndex()).getSize();
				progressBar.setMaximum(10000);
				while((count = in.read(data, 0, 1024)) != -1) {
					fout.write(data, 0, count);
					downloadedPerc += (count * 1.0 / mapSize) * 100;
					amount += count;
					steps++;
					if(steps > 100) {
						steps = 0;
						progressBar.setValue((int)downloadedPerc * 100);
						label.setText((amount / 1024) + "Kb / " + (mapSize / 1024) + "Kb");
					}
				}
			} finally {
				in.close();
				fout.flush();
				fout.close();
			}
		}
		
		protected void downloadMap(String mapName, String dir) throws IOException, NoSuchAlgorithmException {
			Logger.logInfo("Downloading");
			String installPath = Settings.getSettings().getInstallPath();
			Map map = Map.getMap(LaunchFrame.getSelectedMapIndex());
			new File(installPath + "/temp/maps/" + dir + "/").mkdirs();
			new File(installPath + "/temp/maps/" + dir + "/" + mapName).createNewFile();
			downloadUrl(installPath + "/temp/maps/" + dir + "/" + mapName, "http://repo.creeperhost.net/direct/FTB2/" + md5("mcepoch1" + LaunchFrame.getTime()) + "/" + mapName);
			
		}
		
		public String md5(String input) throws NoSuchAlgorithmException {
			String result = input;
			if(!input.isEmpty()) {
				MessageDigest md = MessageDigest.getInstance("MD5");
				md.update(input.getBytes());
				BigInteger hash = new BigInteger(1, md.digest());
				result = hash.toString(16);
				while(result.length() < 32) {
					result = "0" + result;
				}
			}
			return result;
		}
		
		protected String[] reverse(String[] x) {
			String buffer[] = new String[x.length];
			for(int i = 0; i < x.length; i++) {
				buffer[i] = x[x.length - i - 1];
			}
			return buffer;
		}
	}

	public MapManager(JFrame owner, Boolean model) {
		super(owner, model);
		setResizable(false);
		setTitle("Downloading...");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds(100, 100, 313, 138);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		progressBar = new JProgressBar();
		progressBar.setBounds(10, 63, 278, 22);
		contentPane.add(progressBar);
		
		JLabel lblDownloadingMap = new JLabel("Downloading map...\nPlease Wait");
		lblDownloadingMap.setHorizontalAlignment(SwingConstants.CENTER);
		lblDownloadingMap.setBounds(10, 11, 278, 14);
		contentPane.add(lblDownloadingMap);
		
		label = new JLabel("");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setBounds(10, 36, 278, 14);
		contentPane.add(label);
		
		addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent arg0) {
				MapManagerWorker worker = new MapManagerWorker() {
					@Override
					protected void done() {
						setVisible(false);
						super.done();
					}
				};
				worker.execute();
			}
			@Override public void windowActivated(WindowEvent e) { }
			@Override public void windowClosed(WindowEvent e) { }
			@Override public void windowClosing(WindowEvent e) { }
			@Override public void windowDeactivated(WindowEvent e) { }
			@Override public void windowDeiconified(WindowEvent e) { }
			@Override public void windowIconified(WindowEvent e) { }
		});
	}
}
