package net.ftb.tools;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.dialogs.ModpackUpdateDialog;
import net.ftb.log.Logger;
import net.ftb.util.DownloadUtils;
import net.ftb.util.ErrorUtils;
import net.ftb.util.FileUtils;
import net.ftb.util.OSUtils;

public class AnimationManager extends JDialog {
	private static final long serialVersionUID = 6897832855341265019L;
	public static boolean update = false, backup = false, erroneous = false;
	private static int curVersion = 0;
	private JPanel contentPane;
	private double downloadedPerc;
	private final JProgressBar progressBar;
	private final JLabel label;
	private static String sep = File.separator;

	private class ModManagerWorker extends SwingWorker<Boolean, Void> {
		@Override
		protected Boolean doInBackground() throws IOException, NoSuchAlgorithmException {
			if(!ModManager.upToDate) {
				Logger.logInfo("Not up to date!");
				String installPath = OSUtils.getDynamicStorageLocation();
				ModPack pack = ModPack.getSelectedPack();
				pack.setUpdated(true);
				File modPackZip = new File(installPath, "ModPacks" + sep + pack.getDir() + sep + pack.getAnimation());
				if(modPackZip.exists()) {
					FileUtils.delete(modPackZip);
				}
				erroneous = !downloadAnimation(pack.getAnimation(), pack.getDir());
			}
			return true;
		}

		public void downloadUrl(String filename, String urlString) throws IOException, NoSuchAlgorithmException {
			BufferedInputStream in = null;
			FileOutputStream fout = null;
			try {
				URL url_ = new URL(urlString);
				in = new BufferedInputStream(url_.openStream());
				fout = new FileOutputStream(filename);
				byte data[] = new byte[1024];
				int count, amount = 0, modPackSize = url_.openConnection().getContentLength(), steps = 0;
				progressBar.setMaximum(10000);
				while((count = in.read(data, 0, 1024)) != -1) {
					fout.write(data, 0, count);
					downloadedPerc += (count * 1.0 / modPackSize) * 100;
					amount += count;
					steps++;
					if(steps > 100) {
						steps = 0;
						progressBar.setValue((int)downloadedPerc * 100);
						label.setText((amount / 1024) + "Kb / " + (modPackSize / 1024) + "Kb");
					}
				}
			} finally {
				in.close();
				fout.flush();
				fout.close();
			}
		}

		protected boolean downloadAnimation(String animation, String dir) throws IOException, NoSuchAlgorithmException {
			Logger.logInfo("Downloading Something Shiny.");			
			String dynamicLoc = OSUtils.getDynamicStorageLocation();
			File baseDynamic = new File(dynamicLoc, "ModPacks" + sep + dir + sep);
			baseDynamic.mkdirs();
			new File(baseDynamic, animation).createNewFile();
			downloadUrl(baseDynamic.getPath() + sep + animation, DownloadUtils.getCreeperhostLink("modpacks%5E" + dir + "%5E" + curVersion + "%5E" + animation));
			return true;
		}
	}

	/**
	 * Create the frame.
	 */
	public AnimationManager(JFrame owner, Boolean model) {
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

		JLabel lblDownloadingModPack = new JLabel("<html><body><center>Downloading Something Shiny...<br/>Please Wait</center></body></html>");
		lblDownloadingModPack.setHorizontalAlignment(SwingConstants.CENTER);
		lblDownloadingModPack.setBounds(0, 5, 313, 30);
		contentPane.add(lblDownloadingModPack);
		label = new JLabel("");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setBounds(0, 42, 313, 14);
		contentPane.add(label);

		addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent arg0) {
				ModManagerWorker worker = new ModManagerWorker() {
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