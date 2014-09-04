package net.ftb.ui.panel;

import net.ftb.data.Map;
import net.ftb.laf.utils.UIUtils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JPanel;

public final class MapPanel
extends JPanel
implements MouseListener{
    private final Map map;
    private final Image image;

    private boolean rollover = false;

    public MapPanel(Map map){
        this.map = map;
        this.image = map.getImage();
        this.setPreferredSize(new Dimension(128, 128));
        this.addMouseListener(this);
    }

    @Override
    public void paint(Graphics g){
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(UIUtils.WHITE);
        g2.fillRect(0, 0, this.getWidth(), this.getHeight());
        g2.drawImage(this.image, 5, 5, this.getWidth() / 4 - 5, this.getHeight() - 5, null);
        g2.setColor(Color.black);
        g2.drawString(this.map.getName(), this.getWidth() / 4 + 15, 5 + g2.getFontMetrics().getHeight());

        int y = 5 + g2.getFontMetrics().getHeight() * 2;
        int x = this.getWidth() / 4 + 15;
        String[] wrap = UIUtils.wrap(this.map.getInfo(), 75).split("\n");
        for(String str : wrap){
            g2.drawString(str, x, y += g2.getFontMetrics().getHeight());
        }

        if(this.rollover){
            g2.setColor(Color.black);
            g2.setComposite(UIUtils.alpha(0.25F));
            g2.fillRect(0, 0, this.getWidth(), this.getHeight());
        }
    }

    @Override
    public void mouseClicked(MouseEvent e){

    }

    @Override
    public void mousePressed(MouseEvent e){

    }

    @Override
    public void mouseReleased(MouseEvent e){

    }

    @Override
    public void mouseEntered(MouseEvent e){
        this.rollover = true;
        this.repaint();
    }

    @Override
    public void mouseExited(MouseEvent e){
        this.rollover = false;
        this.repaint();
    }
}