package net.ftb.gui;

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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

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
import net.ftb.gui.dialogs.UpdateDialog;
import net.ftb.util.FileUtils;

public class ModManager extends JDialog {
	private static final long serialVersionUID = 6897832855341265019L;

	public static boolean update = false;
	
	private JPanel contentPane;

	private double downloadedPerc;
	private Settings settings = new Settings();	

	private final JProgressBar progressBar;
	private final JLabel label;

	private class ModManagerWorker extends SwingWorker<Boolean, Void> {
		@Override
		protected Boolean doInBackground() throws Exception {
			if(!upToDate()){
				System.out.println("Not up to date!");
				String installPath = Settings.getSettings().getInstallPath();
				ModPack pack = ModPack.getPack(LaunchFrame.getSelectedModIndex());
				File modPackZip = new File(installPath + "/temp/" + pack.getDir() + "/" + pack.getUrl());
				if(!modPackZip.exists()) {
					System.out.println("Pack not found, downloading!");
					try {
						new File(installPath + "/temp/" + pack.getDir() +  "/").mkdir();
						downloadModPack(pack.getUrl(), pack.getDir());
					} catch (MalformedURLException e) { e.printStackTrace();
					} catch (NoSuchAlgorithmException e) { e.printStackTrace();
					} catch (IOException e) { e.printStackTrace(); }
				} else {
					System.out.println("Pack found!");
					installMods(pack.getUrl(), pack.getDir());
				}
			}
			return false;
		}

		public void downloadUrl(String filename, String urlString) throws MalformedURLException, IOException {
			BufferedInputStream in = null;
			FileOutputStream fout = null;
			try {
				in = new BufferedInputStream(new URL(urlString).openStream());
				fout = new FileOutputStream(filename);

				byte data[] = new byte[1024];
				int count;
				int amount = 0;
				int modPackSize = ModPack.getPack(LaunchFrame.getSelectedModIndex()).getSize();
				progressBar.setMaximum(10000);
				int steps = 0;
				while ((count = in.read(data, 0, 1024)) != -1) {
					fout.write(data, 0, count);
					downloadedPerc += (count*1.0/modPackSize)*100;
					amount += count;
					steps++;
					if (steps > 100) {
						steps = 0;
						progressBar.setValue((int)downloadedPerc*100);
						label.setText(String.valueOf(amount / 1024) + "Kb / " + String.valueOf(modPackSize / 1024) + "Kb");
					}
				}
			} finally {			
				in.close();
				fout.flush();	
				fout.close();
			}
		}

		public void downloadPack(String dest, String file) throws MalformedURLException, NoSuchAlgorithmException, IOException {
			DateFormat sdf = new SimpleDateFormat("ddMMyy");
			TimeZone zone = TimeZone.getTimeZone("GMT");
			sdf.setTimeZone(zone);
			String date = sdf.format(new Date());
			downloadUrl(dest, "http://repo.creeperhost.net/direct/FTB2/" + md5 ( "mcepoch1" + date ) + "/" + file);
		}

		protected void downloadModPack(String modPackName, String dir) throws IOException, NoSuchAlgorithmException {
			System.out.println("Downloading");
			String installPath = Settings.getSettings().getInstallPath();
			ModPack pack = ModPack.getPack(LaunchFrame.getSelectedModIndex());
			new File(installPath + "/temp/" + dir + "/").mkdirs();
			new File(installPath + "/temp/" + dir + "/" + modPackName).createNewFile();
			downloadPack(installPath + "/temp/" + dir + "/" + modPackName, modPackName);
			new File(installPath + "/temp/" + dir + "/instMods").mkdirs();
			new File(installPath + "/temp/" + dir + "/.minecraft").mkdirs();
			FileUtils.extractZipTo(installPath + "/temp/" + pack.getDir() + "/" + pack.getUrl(), installPath + "/temp/" + pack.getDir());
			installMods(modPackName, dir);
		}

		protected void installMods(String modPackName, String dir) throws IOException, NoSuchAlgorithmException {
			System.out.println("Installing");
			String installPath = Settings.getSettings().getInstallPath();
			new File(installPath + "/"+ dir + "/.minecraft").mkdirs();
			FileUtils.copyFolder(new File(installPath + "/.minecraft/bin/"), new File(installPath + "/"+ dir+ "/.minecraft/bin"));
			LaunchFrame.jarMods = new String[new File(installPath + "/temp/" + modPackName + "/instMods").listFiles().length];
			try {
				FileInputStream fstream = new FileInputStream(installPath + "/temp/" + modPackName + "/modlist");
				DataInputStream in1 = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in1));
				String strLine;
				int i=0;
				while ((strLine = br.readLine()) != null) {
					// Print the content on the console
					LaunchFrame.jarMods[i] = strLine;
					i++;		
				}
				//Close the input stream
				in1.close();
			} catch (Exception e) { System.err.println("Error: " + e.getMessage()); }
			LaunchFrame.jarMods = reverse(LaunchFrame.jarMods);
			FileUtils.copyFile(new File(installPath + "/temp/" + dir + "/version"), new File(installPath + "/" + dir));
			FileUtils.copyFolder(new File(installPath + "/temp/" + dir + "/instMods"), new File(installPath + "/" + dir +"/.minecraft/bin/"));
			FileUtils.copyFolder(new File(installPath + "/temp/" + dir + "/.minecraft"), new File(installPath + "/" + dir +"/.minecraft/"));
			// Test cleaning up files
		}

		public String md5(String input) throws NoSuchAlgorithmException {
			String result = input;
			if(input != null) {
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
			for(int i = 0; i<x.length;i++) {
				buffer[i] = x[x.length-i-1];
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

		JLabel lblDownloadingModPack = new JLabel("Downloading mod pack...\nPlease Wait");
		lblDownloadingModPack.setHorizontalAlignment(SwingConstants.CENTER);
		lblDownloadingModPack.setBounds(10, 11, 278, 14);
		contentPane.add(lblDownloadingModPack);

		label = new JLabel("");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setBounds(10, 36, 278, 14);
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
		File version = new File(Settings.getSettings().getInstallPath() + File.separator + pack.getDir() + File.separator + "version");
		if(!version.exists()){
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
			UpdateDialog p = new UpdateDialog(LaunchFrame.getInstance(), true);
			p.setVisible(true);
			in.close();
			if(!update){
				return true;
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
		File tempFolder = new File(Settings.getSettings().getInstallPath() + File.separator + "temp" + File.separator + ModPack.getPack(LaunchFrame.getSelectedModIndex()).getDir() + File.separator);
		for(String file : tempFolder.list()){
			if(!file.equals("logo_ftb.png") && !file.equals("splash_FTB.png") && !file.equals("version")){
				try {
					FileUtils.delete(new File(tempFolder, file));
				} catch (IOException e) { e.printStackTrace(); }
			}
		}
	}
}
