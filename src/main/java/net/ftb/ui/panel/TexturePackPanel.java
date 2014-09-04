package net.ftb.ui.panel;

import net.ftb.data.TexturePack;
import net.ftb.ui.utils.UIUtils;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JPanel;

//TODO: Convert to Card
public final class TexturePackPanel
extends JPanel
implements MouseListener{
    private final TexturePack pack;
    private final Image image;

    private boolean rollover = false;

    public TexturePackPanel(TexturePack pack){
        this.pack = pack;
        this.image = pack.getLogo();
        this.setPreferredSize(new Dimension(128, 128));
        this.addMouseListener(this);
    }


    @Override
    public void paint(Graphics g){
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(UIUtils.WHITE);
        g2.fillRect(0, 0, this.getWidth(), this.getHeight());
        g2.drawImage(this.image, 0, 0, this.getWidth(), this.getHeight(), null);

        if(this.rollover){
            // Draw Fade
            Composite comp = g2.getComposite();
            g2.setComposite(UIUtils.alpha(0.75F));
            g2.setColor(Color.black);
            g2.fillRect(0, 0, this.getWidth(), this.getHeight());
            g2.setComposite(comp);

            // Draw Title
            g2.setColor(Color.white);
            g2.drawString(this.pack.getName(), 5, 5 + g2.getFontMetrics().getHeight());

            String wrapped = UIUtils.wrap(this.pack.getInfo(), 25);
            int x = 15;
            int y = 5 + g2.getFontMetrics().getHeight();
            for(String str : wrapped.split("\n")){
                g2.drawString(str, x, y += g2.getFontMetrics().getHeight());
            }
        }
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