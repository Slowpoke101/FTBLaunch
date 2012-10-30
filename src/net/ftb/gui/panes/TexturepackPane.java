package net.ftb.gui.panes;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class TexturepackPane extends JPanel implements ILauncherPane {
	
	private static final long serialVersionUID = 1L;


	public TexturepackPane() {
		super();
		
		this.setBorder(new EmptyBorder(5, 5, 5, 5));
		this.setLayout(null);
		//tpPane.add(backgroundImage5);
		//tpPane.setBackground(back);
	}
	
	
	@Override
	public void onVisible() {
		// TODO Auto-generated method stub
		
	}

}
