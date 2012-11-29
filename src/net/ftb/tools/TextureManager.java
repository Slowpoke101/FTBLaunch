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

import net.ftb.data.Settings;
import net.ftb.data.TexturePack;
import net.ftb.gui.LaunchFrame;
import net.ftb.log.Logger;
import net.ftb.util.DownloadUtils;
import net.ftb.util.FileUtils;
import net.ftb.util.OSUtils;

public class TextureManager extends JDialog {
	private static final long serialVersionUID = 6897832855341265019L;

	private JPanel contentPane;
	private double downloadedPerc;
	private final JProgressBar progressBar;
	private final JLabel label;
	public static boolean overwrite = false;
	private static String sep = File.separator;
	public static String installDir = "FTBBETAA";

	private class TexturePackManagerWorker extends SwingWorker<Boolean, Void> {
		@Override
		protected Boolean doInBackground() throws Exception {
			String installPath = OSUtils.getDynamicStorageLocation();
			TexturePack texturePack = TexturePack.getTexturePack(LaunchFrame.getSelectedTexturePackIndex());
			if(new File(installPath, installDir + sep + "minecraft" + sep + "texturepacks" + sep + texturePack.getUrl()).exists()) {
				new File(installPath, installDir + sep + "minecraft" + sep + "texturepacks" + sep + texturePack.getUrl()).delete();
			}
			downloadTexturePack(texturePack.getUrl(), texturePack.getName());
			return false;
		}

		public void downloadUrl(String filename, String urlString) throws MalformedURLException, IOException, NoSuchAlgorithmException {
			BufferedInputStream in = null;
			FileOutputStream fout = null;
			try {
				in = new BufferedInputStream(new URL(urlString).openStream());
				fout = new FileOutputStream(filename);
				byte data[] = new byte[1024];
				int count, amount = 0, steps = 0;
				URL url_ = new URL(DownloadUtils.getCreeperhostLink(TexturePack.getTexturePack(LaunchFrame.getSelectedTexturePackIndex()).getUrl()));
				int mapSize = url_.openConnection().getContentLength();
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

		protected void downloadTexturePack(String texturePackName, String dir) throws IOException, NoSuchAlgorithmException {
			Logger.logInfo("Downloading");
			String installPath = OSUtils.getDynamicStorageLocation();
			new File(installPath + "/TexturePacks/" + dir + "/").mkdirs();
			new File(installPath + "/TexturePacks/" + dir + "/" + texturePackName).createNewFile();
			downloadUrl(installPath + "/TexturePacks/" + dir + "/" + texturePackName, DownloadUtils.getCreeperhostLink(texturePackName));
			installTexturePack(texturePackName, dir);
		}

		protected void installTexturePack(String texturePackName, String dir) throws IOException {
			Logger.logInfo("Installing");
			String installPath = Settings.getSettings().getInstallPath();
			String tempPath = OSUtils.getDynamicStorageLocation();
			TexturePack texturePack = TexturePack.getTexturePack(LaunchFrame.getSelectedTexturePackIndex());
			new File(installPath, installDir + "/minecraft/texturepacks/").mkdirs();
			FileUtils.copyFile(new File(tempPath, "TexturePacks/" + dir + "/" + texturePackName), new File(installPath, installDir + "/minecraft/texturepacks/" + texturePackName));
			FileUtils.copyFile(new File(tempPath, "TexturePacks/" + dir + "/" + "version"), new File(installPath, installDir + "/minecraft/texturepacks/" + dir + "_version"));
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

	public static void cleanUp() {
		TexturePack texturePack = TexturePack.getTexturePack(LaunchFrame.getSelectedTexturePackIndex());
		File tempFolder = new File(OSUtils.getDynamicStorageLocation(), "TexturePacks" + sep + texturePack.getName() + sep);
		for(String file: tempFolder.list()) {
			if(!file.equals(texturePack.getLogoName()) && !file.equals(texturePack.getImageName()) && !file.equalsIgnoreCase("version")) {
				try {
					FileUtils.delete(new File(tempFolder, file));
				} catch (IOException e) { }
			}
		}
	}
}
