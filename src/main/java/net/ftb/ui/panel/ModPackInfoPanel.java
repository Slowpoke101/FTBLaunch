package net.ftb.ui.panel;

import net.ftb.data.ModPack;
import net.ftb.ui.tab.Tab;
import net.ftb.ui.utils.UIUtils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JPanel;

public class ModPackInfoPanel
extends JPanel
implements Tab{
    private final List<String> col1 = new LinkedList<String>();
    private final List<String> col2 = new LinkedList<String>();
    private final List<String> col3 = new LinkedList<String>();

    private final ModPack pack;
    private final Image splash;

    public ModPackInfoPanel(ModPack pack){
        this.pack = pack;
        this.splash = pack.getImage();
        this.setPreferredSize(new Dimension(this.getPreferredSize().width, 500 + (this.pack.getMods().length * 7)));

        try{
            this.layoutColumns();
        } catch(Exception e){
            System.err.println("Error laying out columns of mods with pack " + this.pack.getName());
        }
    }

    @Override
    public String id(){
        return this.pack.getName();
    }

    @Override
    public void paint(Graphics g){
        Graphics2D g2 = (Graphics2D) g;
        // Paint Banner & Background
        g2.setColor(UIUtils.GRAY);
        g2.fillRect(0, 0, this.getWidth(), this.getHeight());
        g2.drawImage(this.splash, 0, 0, this.getWidth(), 150, null);

        // Paint The title
        Font f = g2.getFont();
        g2.setFont(f.deriveFont(24.0F));
        g2.setColor(Color.white);
        g2.drawString(this.pack.getName(), 5, 5 + g2.getFontMetrics().getHeight());
        g2.setFont(f);

        // Paint Description
        int pad = 25;
        g2.setColor(UIUtils.DARK_GRAY);
        int y = 150 + pad + g2.getFontMetrics().getHeight();
        int x = pad + g2.getFontMetrics().charWidth('M');
        String[] wrap = UIUtils.wrap(this.pack.getInfo(), 75).split("\n");
        for(String str : wrap){
            g2.drawString(str, x, y += g2.getFontMetrics().getHeight());
        }

        y += 5;
        g2.drawString("Mods: ", x, y += g2.getFontMetrics().getHeight());
        y += 5;

        int sY = y;

        // Paint Mods
        for(String str : this.col1){
            g2.drawString(str, x, y += g2.getFontMetrics().getHeight());
        }

        y = sY;
        x += 20 * g2.getFontMetrics().charWidth('M');
        for(String str : this.col2){
            g2.drawString(str, x, y += g2.getFontMetrics().getHeight());
        }

        y = sY;
        x += 20 * g2.getFontMetrics().charWidth('M');
        for(String str : this.col3){
            g2.drawString(str, x, y += g2.getFontMetrics().getHeight());
        }

        Stroke stroke = g2.getStroke();
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(pad, 150 + pad, this.getWidth() - pad * 2, y - pad * 4);
        g2.setStroke(stroke);
    }

    private void layoutColumns(){
        for(int i = 0; i < this.pack.getMods().length; i++){
            String mod = this.pack.getMods()[i];
            if(i % 3 == 0){
                this.col1.add(mod.substring(0, mod.indexOf('-') - 1));
            } else if(i % 3 == 1){
                this.col2.add(mod.substring(0, mod.indexOf('-') - 1));
            } else{
                this.col3.add(mod.substring(0, mod.indexOf('-') - 1));
            }
        }
    }
}