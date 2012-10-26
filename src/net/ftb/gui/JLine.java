package net.ftb.gui;

import java.awt.Graphics;

import javax.swing.JPanel;

public class JLine extends JPanel{
	private static final long serialVersionUID = 1L;
	
	private int x1, y1, x2, y2;

	public JLine(int x1, int y1, int x2, int y2) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}
	public void paintComponent (Graphics g) {
		g.drawLine(x1, y1, x2, y2);
	}
}