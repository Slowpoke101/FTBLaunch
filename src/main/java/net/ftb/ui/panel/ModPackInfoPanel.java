package net.ftb.ui.panel;

import net.ftb.data.ModPack;
import net.ftb.laf.utils.UIUtils;
import net.ftb.ui.tab.Tab;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.JPanel;

public class ModPackInfoPanel
extends JPanel
implements Tab{
    private final ModPack pack;
    private final Image splash;

    public ModPackInfoPanel(ModPack pack){
        this.pack = pack;
        this.splash = pack.getImage();
    }

    @Override
    public String id(){
        return this.pack.getName();
    }

    @Override
    public void paint(Graphics g){
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(UIUtils.GRAY);
        g2.fillRect(0, 0, this.getWidth(), this.getHeight());
        g2.drawImage(this.splash, 0, 0, this.getWidth(), 150, null);
        Font f = g2.getFont();
        g2.setFont(f.deriveFont(24.0F));
        g2.setColor(Color.white);
        g2.drawString(this.pack.getName(), 5, 5 + g2.getFontMetrics().getHeight());
        g2.setFont(f);
    }
}
