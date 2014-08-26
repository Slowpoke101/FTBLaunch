package net.ftb.ui.panel;

import net.ftb.data.ModPack;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JPanel;

public final class ModPackPanel
extends JPanel
implements MouseListener{
    private final ModPack pack;

    private boolean rollover = false;

    public ModPackPanel(ModPack pack){
        this.pack = pack;
        this.setPreferredSize(new Dimension(128, 128));
    }

    @Override
    public void paint(Graphics g){
        Graphics2D g2 = (Graphics2D) g;
    }

    @Override
    public void mouseClicked(MouseEvent e){

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

    @Override public void mousePressed(MouseEvent e){}
    @Override public void mouseReleased(MouseEvent e){}
}