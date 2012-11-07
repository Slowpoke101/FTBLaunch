package net.ftb.gui.panes;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.ftb.data.Map;
import net.ftb.data.events.MapListener;

public class MapsPane extends JPanel implements ILauncherPane, MapListener {
	private static final long serialVersionUID = 1L;

	public static boolean loaded = false;
	private static JLabel splash;
	
	public MapsPane() {
		super();
		this.setBorder(new EmptyBorder(5, 5, 5, 5));
		this.setLayout(null);
		
		
	}

	@Override
	public void onVisible() {

	}

	@Override
	public void onMapAdded(Map map) {

	}
}
