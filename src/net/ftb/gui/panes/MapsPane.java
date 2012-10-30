package net.ftb.gui.panes;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class MapsPane extends JPanel implements ILauncherPane {
	private static final long serialVersionUID = 1L;

	public MapsPane() {
		super();
		this.setBorder(new EmptyBorder(5, 5, 5, 5));
		this.setLayout(null);
		//mapsPane.add(backgroundImage4);
		//mapsPane.setBackground(back);
	}
	
	@Override
	public void onVisible() {
		// TODO Auto-generated method stub
		
	}

}
