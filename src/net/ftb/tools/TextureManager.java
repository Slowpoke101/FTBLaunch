package net.ftb.tools;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

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
import net.ftb.data.TexturePack;
import net.ftb.gui.LaunchFrame;
import net.ftb.log.Logger;
import net.ftb.util.DownloadUtils;
import net.ftb.util.ErrorUtils;

public class TextureManager extends JDialog {
	private static TexturePack updateTexture;
	private static ModPack updateModPack;
	private JPanel contentPane;
	private double downloadedPerc;
	private final JProgressBar progressBar;
	private final JLabel label;
	public static boolean updating = false;
	private static String sep = File.separator;

	private class TexturePackManagerWorker extends SwingWorker<Boolean, Void> {
		@Override
		protected Boolean doInBackground() throws Exception {
			String installPath = Settings.getSettings().getInstallPath();
			TexturePack texturePack = TexturePack.getSelectedTexturePack();
			String compDir = texturePack.getSelectedCompatible();
			ModPack compPack = TexturePack.getModPackComp(compDir);
			if(updating) {
				texturePack = updateTexture;
				compDir = updateModPack.getDir();
				compPack = updateModPack;
			}
			if(compPack == null) {
				ErrorUtils.tossError("Error: Invalid Mod Pack selected.");
				return false;
			}
			String packVer = (Settings.getSettings().getPackVer(compDir).equalsIgnoreCase("Recommended Version") ? TexturePack.getModPackComp(compDir).getVersion() : Settings.getSettings().getPackVer(compDir)).replace(".", "_");
			String url = DownloadUtils.getCreeperhostLink("texturepacks%5E" + texturePack.getName().replace(" ", "_") + "%5E" + compDir + "%5E" + packVer + "%5E" + texturePack.getUrl());
			if(DownloadUtils.fileExists(url)) {
				File oldFile = new File(installPath, texturePack.getSelectedCompatible() + sep + "minecraft" + sep + "texturepacks" + sep + texturePack.getUrl());
				if(oldFile.exists()) {
					oldFile.delete();
				}
				return downloadTexturePack(texturePack.getUrl(), texturePack.getName(), compDir, packVer, url);
			} else {
				ErrorUtils.tossError("Error: Texture Pack not found for select mod pack's version!");
				return false;
			}
		}

		public boolean downloadUrl(String filename, String urlString) {
			BufferedInputStream in = null;
			FileOutputStream fout = null;
			try {
				URL url_ = new URL(urlString);
				in = new BufferedInputStream(url_.openStream());
				fout = new FileOutputStream(filename);
				byte data[] = new byte[1024];
				int count, amount = 0, steps = 0, mapSize = url_.openConnection().getContentLength();
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
			} catch (MalformedURLException e) {
				Logger.logError("Texture Download Error", e);
				return false;
			} catch (IOException e) {
				Logger.logError("Texture Download Error", e);
				return false;
			} finally {
				try {
					in.close();
					fout.flush();
					fout.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return true;
		}

		protected boolean downloadTexturePack(String texturePackName, String dir, String compDir, String packVer, String url) throws IOException {
			Logger.logInfo("Downloading Texture Pack");
			String installPath = Settings.getSettings().getInstallPath();
			new File(installPath, compDir + sep + "minecraft" + sep + "texturepacks" + sep).mkdirs();
			new File(installPath, compDir + sep + "minecraft" + sep + "texturepacks" + sep + texturePackName).createNewFile();
			if(downloadUrl(installPath + sep + compDir + sep + "minecraft" + sep + "texturepacks" + sep + texturePackName, url)) {
				File versionFile = new File(installPath, compDir + sep + "minecraft" + sep + "texturepacks" + sep + "textureVersions");
				HashMap<String, String> installedTextures = new HashMap<String, String>();
				if(versionFile.exists()) {
					BufferedReader in = new BufferedReader(new FileReader(versionFile));
					String line;
					while((line = in.readLine()) != null) {
						String[] split = line.toLowerCase().split(":");
						if(split.length == 2) {
							installedTextures.put(split[0], split[1]);
						}
					}
					in.close();
				}
				installedTextures.put(dir.toLowerCase(), packVer);
				BufferedWriter out = new BufferedWriter(new FileWriter(versionFile));
				for(int i = 0; i < installedTextures.size(); i++) {
					out.write(installedTextures.keySet().toArray()[i] + ":" + installedTextures.values().toArray()[i]);
					out.newLine();
				}
				out.flush();
				out.close();
				if(!Settings.getSettings().getSnooper()) {
					LaunchFrame.tracker.trackPageViewFromReferrer(dir + " Install", dir + " / " + compDir + " / " + packVer, "Feed The Beast", "http://www.feed-the-beast.com", "/");
				}
				return true;
			}
			return false;
		}
	}

	public TextureManager(JFrame owner, Boolean model) {
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

		JLabel lblDownloadingTexture = new JLabel("<html><body><center>Downloading texture pack...<br/>Please Wait</center></body></html>");
		lblDownloadingTexture.setHorizontalAlignment(SwingConstants.CENTER);
		lblDownloadingTexture.setBounds(0, 5, 313, 30);
		contentPane.add(lblDownloadingTexture);

		label = new JLabel("");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setBounds(0, 42, 313, 14);
		contentPane.add(label);

		addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent arg0) {
				TexturePackManagerWorker worker = new TexturePackManagerWorker() {
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

	public static void updateTextures() throws NoSuchAlgorithmException, IOException {
		boolean removed = false;
		ModPack pack = ModPack.getSelectedPack();
		String installDir = Settings.getSettings().getInstallPath();
		File textureVersionFile = new File(installDir, pack.getDir() + sep + "minecraft" + sep + "texturepacks" + sep + "textureVersions");
		HashMap<String, String> installedTextures = new HashMap<String, String>();
		if(textureVersionFile.exists()) {
			try {
				BufferedReader in = new BufferedReader(new FileReader(textureVersionFile));
				String line;
				while((line = in.readLine()) != null) {
					String[] split = line.toLowerCase().split(":");
					if(split.length == 2) {
						installedTextures.put(split[0], split[1]);
					}
				}
				in.close();
			} catch (FileNotFoundException e) {
				Logger.logError("Update Textures Error", e);
			} catch (IOException e) {
				Logger.logError("Update Textures Error", e);
			}
			if(installedTextures.size() > 0) {
				for(TexturePack tp : TexturePack.getTexturePackArray()) {
					if(installedTextures.containsKey(tp.getName().toLowerCase()) && tp.isCompatible(pack.getDir())) {
						File texturePackFile = new File(installDir, pack.getDir() + sep + "minecraft" + sep + "texturepacks" + sep + tp.getUrl());
						if(texturePackFile.exists()) {
							String version = (Settings.getSettings().getPackVer().equalsIgnoreCase("Recommended Version") ? pack.getVersion() : Settings.getSettings().getPackVer()).replace(".", "_");
							if(!installedTextures.get(tp.getName().toLowerCase()).equalsIgnoreCase(version)) {
								String url = DownloadUtils.getCreeperhostLink("texturepacks%5E" + tp.getName().replace(" ", "_") + "%5E" + pack.getDir() + "%5E" + version + "%5E" + tp.getUrl());
								if(DownloadUtils.fileExists(url)) {
									updating = true;
									TextureManager man = new TextureManager(new JFrame(), true);
									man.updateTexture = tp;
									man.updateModPack = pack;
									man.setVisible(true);
								}
							}
						} else {
							installedTextures.remove(tp.getName().toLowerCase());
							removed = true;
						}
					}
				}
				if(removed) {
					BufferedWriter out = new BufferedWriter(new FileWriter(textureVersionFile));
					for(int i = 0; i < installedTextures.size(); i++) {
						out.write(installedTextures.keySet().toArray()[i] + ":" + installedTextures.values().toArray()[i]);
						out.newLine();
					}
					out.flush();
					out.close();
				}
			}
		}
	}
}
