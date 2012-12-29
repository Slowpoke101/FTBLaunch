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
	public static boolean update = false, backup = false, erroneous = false, upToDate = false;
	private static boolean backdated = false;
	private static String curVersion = "";
	private JPanel contentPane;
	private double downloadedPerc;
	private final JProgressBar progressBar;
	private final JLabel label;
	private static String sep = File.separator;

	private class ModManagerWorker extends SwingWorker<Boolean, Void> {
		@Override
		protected Boolean doInBackground() throws IOException, NoSuchAlgorithmException {
			upToDate = upToDate();
			if(!upToDate) {
				String installPath = OSUtils.getDynamicStorageLocation();
				ModPack pack = ModPack.getSelectedPack();
				pack.setUpdated(true);
				File modPackZip = new File(installPath, "ModPacks" + sep + pack.getDir() + sep + pack.getUrl());
				if(modPackZip.exists()) {
					FileUtils.delete(modPackZip);
				}
				File animationGif = new File(OSUtils.getDynamicStorageLocation(), "ModPacks" + sep + pack.getDir() + sep + pack.getAnimation());
				if(animationGif.exists()) {
					FileUtils.delete(animationGif);
				}
				erroneous = !downloadModPack(pack.getUrl(), pack.getDir());
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

		protected boolean downloadModPack(String modPackName, String dir) throws IOException, NoSuchAlgorithmException {
			Logger.logInfo("Downloading mod pack.");			
			String dynamicLoc = OSUtils.getDynamicStorageLocation();
			String installPath = Settings.getSettings().getInstallPath();
			ModPack pack = ModPack.getSelectedPack();
			if(pack.getPrivatePack()) {
				File baseDynamic = new File(dynamicLoc, "ModPacks" + sep + dir + sep);
				baseDynamic.mkdirs();
				new File(baseDynamic, modPackName).createNewFile();
				downloadUrl(baseDynamic.getPath() + sep + modPackName, DownloadUtils.getCreeperhostLink("privatepacks%5E" + dir + "%5E" + curVersion + "%5E" + modPackName));
				String animation = pack.getAnimation();
				if(!animation.equalsIgnoreCase("empty")) {
					downloadUrl(baseDynamic.getPath() + sep + animation, DownloadUtils.getCreeperhostLink("privatepacks%5E" + dir + "%5E" + curVersion + "%5E" + animation));
				}
				if(DownloadUtils.isValid(new File(baseDynamic, modPackName), "privatepacks%5E" + dir + "%5E" + curVersion + "%5E" + modPackName)) {
					FileUtils.extractZipTo(baseDynamic.getPath() + sep + modPackName, baseDynamic.getPath());
					clearModsFolder(pack);
					FileUtils.delete(new File(installPath, dir + "/minecraft/coremods"));
					FileUtils.delete(new File(installPath, dir + "/instMods/"));
					File version = new File(installPath, dir + sep + "version");
					BufferedWriter out = new BufferedWriter(new FileWriter(version));
					out.write(pack.getVersion());
					out.flush();
					out.close();
					return true;
				} else {
					ErrorUtils.tossError("Error downloading modpack!!!");
					return false;
				}
			} else {
				File baseDynamic = new File(dynamicLoc, "ModPacks" + sep + dir + sep);
				baseDynamic.mkdirs();
				new File(baseDynamic, modPackName).createNewFile();
				downloadUrl(baseDynamic.getPath() + sep + modPackName, DownloadUtils.getCreeperhostLink("modpacks%5E" + dir + "%5E" + curVersion + "%5E" + modPackName));
				String animation = pack.getAnimation();
				if(!animation.equalsIgnoreCase("empty")) {
					downloadUrl(baseDynamic.getPath() + sep + animation, DownloadUtils.getCreeperhostLink("modpacks%5E" + dir + "%5E" + curVersion + "%5E" + animation));
				}
				if(DownloadUtils.isValid(new File(baseDynamic, modPackName), "modpacks%5E" + dir + "%5E" + curVersion + "%5E" + modPackName)) {
					FileUtils.extractZipTo(baseDynamic.getPath() + sep + modPackName, baseDynamic.getPath());
					clearModsFolder(pack);
					FileUtils.delete(new File(installPath, dir + "/minecraft/coremods"));
					FileUtils.delete(new File(installPath, dir + "/instMods/"));
					File version = new File(installPath, dir + sep + "version");
					BufferedWriter out = new BufferedWriter(new FileWriter(version));
					out.write(pack.getVersion());
					out.flush();
					out.close();
					return true;
				} else {
					ErrorUtils.tossError("Error downloading modpack!!!");
					return false;
				}
			}
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
		File version = new File(Settings.getSettings().getInstallPath(), pack.getDir() + sep + "version");
		if(!version.exists()) {
			version.getParentFile().mkdirs();
			version.createNewFile();
			curVersion = (Settings.getSettings().getPackVer().equalsIgnoreCase("recommended version") ? pack.getVersion() : Settings.getSettings().getPackVer()).replace(".", "_");
			return false;
		}
		BufferedReader in = new BufferedReader(new FileReader(version));
		String line = in.readLine();
		in.close();
		int currentVersion, requestedVersion;
		currentVersion = (line != null) ? Integer.parseInt(line.replace(".", "")) : 0;
		if(!Settings.getSettings().getPackVer().equalsIgnoreCase("recommended version") || !Settings.getSettings().getPackVer().equalsIgnoreCase("newest version")) {
			requestedVersion = Integer.parseInt(Settings.getSettings().getPackVer().trim().replace(".", ""));
			if(requestedVersion != currentVersion) {
				Logger.logInfo("Modpack is out of date.");
				backdated = true;
				curVersion = Settings.getSettings().getPackVer().replace(".", "_");
				return false;
			} else {
				Logger.logInfo("Modpack is up to date.");
				return true;
			}
		} else if(Integer.parseInt(pack.getVersion().replace(".", "")) > currentVersion) {
			Logger.logInfo("Modpack is out of date.");
			ModpackUpdateDialog p = new ModpackUpdateDialog(LaunchFrame.getInstance(), true);
			p.setVisible(true);
			if(!update) {
				return true;
			}
			if(backup) {
				File destination = new File(OSUtils.getDynamicStorageLocation(), "backups" + sep + pack.getDir() + sep + "config_backup");
				if(destination.exists()) {
					FileUtils.delete(destination);
				}
				FileUtils.copyFolder(new File(Settings.getSettings().getInstallPath(), pack.getDir() + sep + "minecraft" + sep + "config"), destination);
			}
			curVersion = pack.getVersion().replace(".", "_");
			return false;
		} else {
			Logger.logInfo("Modpack is up to date.");
			return true;
		}
	}

	public static void cleanUp() {
		ModPack pack = ModPack.getSelectedPack();
		File tempFolder = new File(OSUtils.getDynamicStorageLocation(), "ModPacks" + sep + pack.getDir() + sep);
		for(String file : tempFolder.list()) {
			if(!file.equals(pack.getLogoName()) && !file.equals(pack.getImageName()) && !file.equals("version") && !file.equals(pack.getAnimation())) {
				try {
					FileUtils.delete(new File(tempFolder, file));
				} catch (IOException e) {
					Logger.logError(e.getMessage(), e);
				}
			}
		}
	}

	public static void clearModsFolder(ModPack pack) throws IOException {
		File modsFolder = new File(Settings.getSettings().getInstallPath(), pack.getDir() + "/minecraft/mods");
		for(String file : modsFolder.list()) {
			if(file.toLowerCase().endsWith(".zip") || file.toLowerCase().endsWith(".jar") || file.toLowerCase().endsWith(".disabled") || file.toLowerCase().endsWith(".litemod")) {
				FileUtils.delete(new File(modsFolder, file));
			}
		}
	}
}