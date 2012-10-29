package net.ftb.gui;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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

public class ModManager extends JDialog {
	private final JProgressBar progressBar;
	private final JLabel label;
	
	private class ModManagerWorker extends SwingWorker<Boolean, Void> {

		private ZipFile zipFile;


		@Override
		protected Boolean doInBackground() throws Exception {
			
			File modPackZip = new File(Settings.getSettings().getInstallPath() + "/temp/" + ModPack.getPack(LaunchFrame.selectedPack).getDir() + "/" + ModPack.getPack(LaunchFrame.selectedPack).getUrl());
			if(modPackZip.exists()){
				try {
						new File(Settings.getSettings().getInstallPath() + "/temp/" + ModPack.getPack(LaunchFrame.selectedPack).getDir() +  "/").mkdir();
						downloadModPack(ModPack.getPack(LaunchFrame.selectedPack).getUrl(), ModPack.getPack(LaunchFrame.selectedPack).getDir());
						installMods(ModPack.getPack(LaunchFrame.selectedPack).getUrl(), ModPack.getPack(LaunchFrame.selectedPack).getDir());
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else{
				try {
					downloadModPack(ModPack.getPack(LaunchFrame.selectedPack).getUrl(), ModPack.getPack(LaunchFrame.selectedPack).getDir());
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			return false;
		}


		public void downloadUrl(String filename, String urlString) throws MalformedURLException, IOException
		{
			BufferedInputStream in = null;
			FileOutputStream fout = null;
			try
			{
				in = new BufferedInputStream(new URL(urlString).openStream());
				fout = new FileOutputStream(filename);

				byte data[] = new byte[1024];
				int count;
				int amount = 0;
				int modPackSize = ModPack.getPack(LaunchFrame.selectedPack).getSize();
				progressBar.setMaximum(10000);
				int steps = 0;
				while ((count = in.read(data, 0, 1024)) != -1)
				{
					fout.write(data, 0, count);
					downloadedPerc += (count*1.0/modPackSize)*100;
					amount += count;
					steps++;
					if (steps > 100) {
						steps = 0;
						progressBar.setValue((int)downloadedPerc*100);
						label.setText(String.valueOf(amount / 1024) + "Kb / " + String.valueOf(modPackSize / 1024) + "Kb");
					}
					//System.out.println(downloadedPerc);
				}

			} finally {			
				if (in != null)
					in.close();
				if (fout != null)
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
			new File(Settings.getSettings().getInstallPath() + "/temp/" + dir + "/").mkdirs();
			new File(Settings.getSettings().getInstallPath() + "/temp/" + dir + "/" + modPackName).createNewFile();
			downloadPack(Settings.getSettings().getInstallPath() + "/temp/" + dir + "/" + modPackName, modPackName);
			new File(Settings.getSettings().getInstallPath() + "/temp/" + dir + "/instMods").mkdirs();
			new File(Settings.getSettings().getInstallPath() + "/temp/" + dir + "/.minecraft").mkdirs();
			installMods(modPackName, dir);
		}
//
//		protected int getModPackSize() throws MalformedURLException, NoSuchAlgorithmException, IOException{
//			DateFormat sdf = new SimpleDateFormat("ddMMyy");
//			TimeZone zone = TimeZone.getTimeZone("GMT");
//			sdf.setTimeZone(zone);
//			String date = sdf.format(new Date());
//			URL url = new URL("http://repo.creeperhost.net/direct/FTB2/" + md5 ( "mcepoch1" + date ) + "/" + getSelectedModPack() + ".txt");
//			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
//			String str;
//			while((str = in.readLine()) != null){
//				return Integer.parseInt(str);
//			}
//			return 1;		
//		}


		protected void installMods(String modPackName, String dir) throws IOException, NoSuchAlgorithmException {
			//new File(Settings.getSettings().getInstallPath() + "/" + getSelectedModPack() + "/.minecraft").mkdir();
			File f = new File(Settings.getSettings().getInstallPath() + "/" + dir + "/.minecraft/md5.txt");		
			FileWriter writer = new FileWriter(f);
			BufferedWriter out = new BufferedWriter(writer);
			out.write(getFileMD5(new File(Settings.getSettings().getInstallPath() + "/temp/" + dir + "/" + modPackName)));
			out.flush();
			out.close();
			Scanner in = new Scanner(new File(Settings.getSettings().getInstallPath() + "/" + dir + "/.minecraft/md5.txt"));
			if(in.next() == getFileMD5(new File(Settings.getSettings().getInstallPath() + "/temp/" + dir + "/" + modPackName))){

			}
			else{
				extractZipTo(Settings.getSettings().getInstallPath() + "/temp/" + dir + "/" + modPackName, Settings.getSettings().getInstallPath() + "/temp/" + modPackName + "/");
				new File(Settings.getSettings().getInstallPath() + "/"+ dir + "/.minecraft").mkdirs();

				copyFolder(new File(Settings.getSettings().getInstallPath()+ "/.minecraft/bin/"), new File(Settings.getSettings().getInstallPath()+ "/"+ dir+ "/.minecraft/bin"));
				File minecraft = new File(Settings.getSettings().getInstallPath()+ "/.minecraft/bin/minecraft.jar");
				File mcbackup = new File(Settings.getSettings().getInstallPath() + "/"+ modPackName + "/.minecraft/bin/mcbackup.jar");
				//		minecraft.renameTo(new File(Settings.getSettings().getInstallPath()+ "/" + modPackName + "/.minecraft/bin/mcbackup.jar"));
				//		System.out.println("Renamed minecraft.jar to mcbackup.jar");
				copyFile(minecraft, mcbackup);
			}
			LaunchFrame.jarMods = new String[new File(Settings.getSettings().getInstallPath() + "/temp/" + modPackName + "/instMods").listFiles().length];

			try{
				// Open the file that is the first 
				// command line parameter
				FileInputStream fstream = new FileInputStream(Settings.getSettings().getInstallPath() + "/temp/" + modPackName + "/modlist");
				// Get the object of DataInputStream
				DataInputStream in1 = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in1));
				String strLine;
				//Read File Line By Line
				int i=0;
				while ((strLine = br.readLine()) != null)   {
					// Print the content on the console
					LaunchFrame.jarMods[i] = strLine;
					i++;		
				}
				//Close the input stream
				in.close();
				in1.close();
			}catch (Exception e){//Catch exception if any
				System.err.println("Error: " + e.getMessage());
			}
			LaunchFrame.jarMods = reverse(LaunchFrame.jarMods);
			copyFolder(new File(Settings.getSettings().getInstallPath()+ "/temp/" + dir + "/instMods"), new File(Settings.getSettings().getInstallPath()+ "/" + dir +"/.minecraft/bin/"));
			copyFolder(new File(Settings.getSettings().getInstallPath()+ "/temp/" + dir + "/.minecraft"), new File(Settings.getSettings().getInstallPath()+ "/" + dir +"/.minecraft/"));
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

		protected String[] reverse(String[] x){
			String buffer[] = new String[x.length];
			for(int i = 0; i<x.length;i++){
				buffer[i] = x[x.length-i-1];
			}


			return buffer;

		}


		protected String getFileMD5(File x){
			InputStream is = null;
			MessageDigest md = null;
			if (x.exists()) {
				try {
					md = MessageDigest.getInstance("MD5");
					is = new FileInputStream(x);
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					is = new DigestInputStream(is, md);
					// read stream to EOF as normal...
				} finally {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				String result = "";
				byte[] digest = md.digest();
				for (int i = 0; i < digest.length; i++) {
					result += Integer.toString((digest[i] & 0xff) + 0x100, 16)
							.substring(1);
				}
				return result;
			}
			return "0";
		}

		public void extractZipTo(String zipLocation, String outputLocation)
				throws IOException {
			try {
				File fSourceZip = new File(zipLocation);
				String zipPath = outputLocation;
				File temp = new File(zipPath);
				temp.mkdir();
				zipFile = new ZipFile(fSourceZip);
				Enumeration<?> e = zipFile.entries();

				while (e.hasMoreElements()) {
					ZipEntry entry = (ZipEntry) e.nextElement();
					File destinationFilePath = new File(zipPath, entry.getName());
					destinationFilePath.getParentFile().mkdirs();
					if (entry.isDirectory()) {
						continue;
					} else {
						BufferedInputStream bis = new BufferedInputStream(
								zipFile.getInputStream(entry));

						int b;
						byte buffer[] = new byte[1024];

						FileOutputStream fos = new FileOutputStream(
								destinationFilePath);
						BufferedOutputStream bos = new BufferedOutputStream(fos,
								1024);

						while ((b = bis.read(buffer, 0, 1024)) != -1) {
							bos.write(buffer, 0, b);
						}

						bos.flush();
						bos.close();
						bis.close();
					}
				}
			} catch (IOException ioe) {
				System.out.println("IOError :" + ioe);
			}

		}

		public void copyFolder(File src, File dest) throws IOException {

			if (src.isDirectory()) {

				// if directory not exists, create it
				if (!dest.exists()) {
					dest.mkdir();
				}

				// list all the directory contents
				String files[] = src.list();

				for (String file : files) {
					// construct the src and dest file structure
					File srcFile = new File(src, file);
					File destFile = new File(dest, file);
					// recursive copy
					copyFolder(srcFile, destFile);
				}

			} else {
				// if file, then copy it
				// Use bytes stream to support all file types
				if (src.exists()) {
					InputStream in = new FileInputStream(src);
					OutputStream out = new FileOutputStream(dest);

					byte[] buffer = new byte[1024];

					int length;
					// copy the file content in bytes
					while ((length = in.read(buffer)) > 0) {
						out.write(buffer, 0, length);
					}

					in.close();
					out.close();
				}
			}
		}


		public void copyFile(File src, File dest) throws IOException {
			if (src.exists()) {
				InputStream in = new FileInputStream(src);
				OutputStream out = new FileOutputStream(dest);

				byte[] buffer = new byte[1024];

				int length;
				// copy the file content in bytes
				while ((length = in.read(buffer)) > 0) {
					out.write(buffer, 0, length);
				}

				in.close();
				out.close();
			}
		}
		
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6897832855341265019L;

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */

	double downloadedPerc;
	Settings settings = new Settings();	
	
	/**
	 * Create the frame.
	 */
	
	
	public ModManager(JFrame owner, Boolean model) {
		super(owner, model);
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

			@Override
			public void windowActivated(WindowEvent e) { }
			@Override
			public void windowClosed(WindowEvent e) { }
			@Override
			public void windowClosing(WindowEvent e) { }
			@Override
			public void windowDeactivated(WindowEvent e) { }
			@Override
			public void windowDeiconified(WindowEvent e) { }
			@Override
			public void windowIconified(WindowEvent e) { }
		});
	}


}
