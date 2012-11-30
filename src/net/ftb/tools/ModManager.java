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

public class ModManager extends JDialog {
	private static final long serialVersionUID = 6897832855341265019L;
	public static boolean update = false, backup = false;
	private JPanel contentPane;
	private double downloadedPerc;
	private final JProgressBar progressBar;
	private final JLabel label;
	private static String sep = File.separator;

	private class ModManagerWorker extends SwingWorker<Boolean, Void> {
		@Override
		protected Boolean doInBackground() throws IOException, NoSuchAlgorithmException {
			if(!upToDate()) {
				Logger.logInfo("Not up to date!");
				String installPath = OSUtils.getDynamicStorageLocation();
				ModPack pack = ModPack.getSelectedPack();
				File modPackZip = new File(installPath, "ModPacks" + sep + pack.getDir() + sep + pack.getUrl());
				if(modPackZip.exists()) {
					FileUtils.delete(modPackZip);
				}
				downloadModPack(pack.getUrl(), pack.getDir());
			}
			return false;
		}

		public void downloadUrl(String filename, String urlString) throws IOException, NoSuchAlgorithmException {
			BufferedInputStream in = null;
			FileOutputStream fout = null;
			try {
				in = new BufferedInputStream(new URL(urlString).openStream());
				fout = new FileOutputStream(filename);
				byte data[] = new byte[1024];
				int count;
				int amount = 0;
				URL url_ = new URL(urlString);
				int modPackSize = url_.openConnection().getContentLength();
				progressBar.setMaximum(10000);
				int steps = 0;
				while ((count = in.read(data, 0, 1024)) != -1) {
					fout.write(data, 0, count);
					downloadedPerc += (count * 1.0 / modPackSize) * 100;
					amount += count;
					steps++;
					if (steps > 100) {
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

		protected void downloadModPack(String modPackName, String dir) throws IOException, NoSuchAlgorithmException {
			System.out.println("Downloading");
			String installPath = OSUtils.getDynamicStorageLocation();
			ModPack pack = ModPack.getSelectedPack();
			new File(installPath, "ModPacks/" + dir + sep).mkdirs();
			new File(installPath, "ModPacks/" + dir + sep + modPackName).createNewFile();
			downloadUrl(installPath + "/ModPacks/" + dir + sep + modPackName, DownloadUtils.getCreeperhostLink(ModPack.getSelectedPack().getUrl()));
			FileUtils.extractZipTo(installPath + "/ModPacks/" + pack.getDir() + sep + pack.getUrl(), installPath + "/ModPacks/" + pack.getDir());
			clearModsFolder(pack);
			FileUtils.delete(new File(Settings.getSettings().getInstallPath(), pack.getDir() + "/minecraft/coremods"));
			FileUtils.delete(new File(Settings.getSettings().getInstallPath(), pack.getDir() + "/instMods/"));
			if(DownloadUtils.isValid(new File(installPath, "ModPacks" + sep + pack.getDir() + sep + pack.getUrl()))) {
				installMods(modPackName, dir);
			} else {
				ErrorUtils.tossError("Error downloading modpack!!!");
				return;
			}
		}

		protected void installMods(String modPackName, String dir) throws IOException {
			System.out.println("Installing");
			String installPath = OSUtils.getDynamicStorageLocation();
			LaunchFrame.jarMods = new String[new File(installPath, "ModPacks/" + modPackName + "/instMods").listFiles().length];
		}
	}

	/**
	 * Create the frame.
	 */
	public ModManager(JFrame owner, Boolean model) {
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

		JLabel lblDownloadingModPack = new JLabel("<html><body><center>Downloading mod pack...<br/>Please Wait</center></body></html>");
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

	private boolean upToDate() throws IOException {
		ModPack pack = ModPack.getSelectedPack();
		File version = new File(Settings.getSettings().getInstallPath() + sep + pack.getDir() + sep + "version");
		if(!version.exists()) {
			System.out.println("File not found.");
			version.getParentFile().mkdirs();
			version.createNewFile();
			BufferedWriter out = new BufferedWriter(new FileWriter(version));
			out.write(pack.getVersion());
			out.flush();
			out.close();
			return false;
		}
		BufferedReader in = new BufferedReader(new FileReader(version));
		String line;
		if((line = in.readLine()) == null || Integer.parseInt(pack.getVersion()) > Integer.parseInt(line)) {
			System.out.println("File found, out of date.");
			ModpackUpdateDialog p = new ModpackUpdateDialog(LaunchFrame.getInstance(), true);
			p.setVisible(true);
			in.close();
			if(!update) {
				return true;
			}
			pack.setUpToDate(true);
			if(backup) {
				File destination = new File(OSUtils.getDynamicStorageLocation(), "backups" + sep + pack.getDir() + sep + "config_backup");
				if(destination.exists()) {
					FileUtils.delete(destination);
				}
				FileUtils.copyFolder(new File(Settings.getSettings().getInstallPath(), pack.getDir() + sep + "minecraft" + sep + "config"), destination);
			}
			BufferedWriter out = new BufferedWriter(new FileWriter(version));
			out.write(pack.getVersion());
			out.flush();
			out.close();
			return false;
		} else {
			System.out.println("File found, up to date.");
			in.close();
			return true;
		}
	}

	public static void cleanUp() {
		ModPack pack = ModPack.getSelectedPack();
		File tempFolder = new File(OSUtils.getDynamicStorageLocation(), "ModPacks" + sep + pack.getDir() + sep);
		for(String file : tempFolder.list()) {
			if(!file.equals(pack.getLogoName()) && !file.equals(pack.getImageName()) && !file.equals("version")) {
				try {
					FileUtils.delete(new File(tempFolder, file));
				} catch (IOException e) { }
			}
		}
	}

	public static void clearModsFolder(ModPack pack) throws IOException {
		File modsFolder = new File(Settings.getSettings().getInstallPath(), pack.getDir() + "/minecraft/mods");
		for(String file : modsFolder.list()) {
			if(file.toLowerCase().endsWith(".zip") || file.toLowerCase().endsWith(".jar") || file.toLowerCase().endsWith(".disabled")) {
				FileUtils.delete(new File(modsFolder, file));
			}
		}
	}
}
