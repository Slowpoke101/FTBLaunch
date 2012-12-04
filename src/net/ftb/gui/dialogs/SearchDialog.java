package net.ftb.gui.dialogs;

import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.ftb.gui.panes.MapsPane;
import net.ftb.gui.panes.ModpacksPane;
import net.ftb.gui.panes.TexturepackPane;

public class SearchDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	public JTextField searchBar = new JTextField();

	public SearchDialog(final ModpacksPane instance) {
		setUpGui();
		searchBar.getDocument().addDocumentListener(new DocumentListener() {
			@Override public void removeUpdate(DocumentEvent arg0) {
				instance.searchPacks(searchBar.getText());
			}

			@Override public void insertUpdate(DocumentEvent arg0) {
				instance.searchPacks(searchBar.getText());
			}

			@Override public void changedUpdate(DocumentEvent arg0) { }
		});
	}

	public SearchDialog(final MapsPane instance) {
		setUpGui();
		searchBar.getDocument().addDocumentListener(new DocumentListener() {
			@Override public void removeUpdate(DocumentEvent arg0) {
				instance.searchMaps(searchBar.getText());
			}

			@Override public void insertUpdate(DocumentEvent arg0) {
				instance.searchMaps(searchBar.getText());
			}

			@Override public void changedUpdate(DocumentEvent arg0) { }
		});
	}

	public SearchDialog(final TexturepackPane instance) {
		setUpGui();
		searchBar.getDocument().addDocumentListener(new DocumentListener() {
			@Override public void removeUpdate(DocumentEvent arg0) {
				instance.searchTexturePacks(searchBar.getText());
			}

			@Override public void insertUpdate(DocumentEvent arg0) {
				instance.searchTexturePacks(searchBar.getText());
			}

			@Override public void changedUpdate(DocumentEvent arg0) { }
		});
	}

	public void setUpGui() {
		this.setTitle("Searcher!");
		this.setBounds(300, 300, 220, 90);
		this.setResizable(false);
		this.setAlwaysOnTop(true);
		this.getContentPane().setLayout(null);
		searchBar.setBounds(10, 10, 200, 30);
		this.getContentPane().add(searchBar);
	}
}
