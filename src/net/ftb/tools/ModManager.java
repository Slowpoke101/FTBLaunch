package net.ftb.tools;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
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

import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.dialogs.ModpackUpdateDialog;
import net.ftb.log.Logger;
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
				ModPack pack = ModPack.getPack(LaunchFrame.getSelectedModIndex());
				File modPackZip = new File(installPath + "/ModPacks/" + pack.getDir() + "/" + pack.getUrl());
				if(modPackZip.exists()) {
					modPackZip.delete();
				}
				try {
					File modsFolder = new File(Settings.getSettings().getInstallPath(), pack.getDir() + "/minecraft/mods");
					File coreModsFolder = new File(Settings.getSettings().getInstallPath(), pack.getDir() + "/minecraft/coremods");
					File instModsFolder = new File(Settings.getSettings().getInstallPath(), pack.getDir() + "/instMods/");
					FileUtils.delete(modsFolder);
					FileUtils.delete(coreModsFolder);
					FileUtils.delete(instModsFolder);
					new File(installPath + "/ModPacks/" + pack.getDir() + "/").mkdir();
					downloadModPack(pack.getUrl(), pack.getDir());
				} catch (MalformedURLException e) { 
				} catch (NoSuchAlgorithmException e) { 
				} catch (IOException e) { }
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
				URL url_ = new URL(LaunchFrame.getCreeperhostLink(ModPack.getPack(LaunchFrame.getSelectedModIndex()).getUrl()));
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
			ModPack pack = ModPack.getPack(LaunchFrame.getSelectedModIndex());
			new File(installPath + "/ModPacks/" + dir + "/").mkdirs();
			new File(installPath + "/ModPacks/" + dir + "/" + modPackName).createNewFile();
			downloadUrl(installPath + "/ModPacks/" + dir + "/" + modPackName, LaunchFrame.getCreeperhostLink(modPackName));
			FileUtils.extractZipTo(installPath + "/ModPacks/" + pack.getDir() + "/" + pack.getUrl(), installPath + "/ModPacks/" + pack.getDir());
			installMods(modPackName, dir);
		}

		protected void installMods(String modPackName, String dir) throws IOException {
			System.out.println("Installing");
			String installPath = OSUtils.getDynamicStorageLocation();
			LaunchFrame.jarMods = new String[new File(installPath, "ModPacks/" + modPackName + "/instMods").listFiles().length];
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
		ModPack pack = ModPack.getPack(LaunchFrame.getSelectedModIndex());
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
				File destination = new File(Settings.getSettings().getInstallPath(), pack.getDir() + sep + "minecraft" + sep + "config_backup");
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
		ModPack pack = ModPack.getPack(LaunchFrame.getSelectedModIndex());
		File tempFolder = new File(OSUtils.getDynamicStorageLocation(), "ModPacks" + sep + pack.getDir() + sep);
		for(String file : tempFolder.list()) {
			if(!file.equals(pack.getLogoName()) && !file.equals(pack.getImageName()) && !file.equals("version")) {
				try {
					FileUtils.delete(new File(tempFolder, file));
				} catch (IOException e) { }
			}
		}
	}
}
