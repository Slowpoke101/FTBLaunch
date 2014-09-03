package net.ftb.ui.panel;

import net.ftb.data.Map;
import net.ftb.laf.utils.UIUtils;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.JPanel;

public final class MapPanel
extends JPanel{
    private final Map map;
    private final Image image;

    public MapPanel(Map map){
        this.map = map;
        this.image = map.getImage();
        this.setPreferredSize(new Dimension(128, 128));
    }

    @Override
    public void paint(Graphics g){
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(UIUtils.WHITE);
        g2.fillRect(0, 0, this.getWidth(), this.getHeight());
        g2.drawImage(this.image, 0, 0, this.getWidth(), this.getHeight(), null);
    }
}