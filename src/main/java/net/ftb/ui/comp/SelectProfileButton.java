package net.ftb.ui.comp;

import net.ftb.laf.utils.UIUtils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JButton;

public final class SelectProfileButton
extends JButton{
    public SelectProfileButton(){
        super("Select Profile");
        this.setRolloverEnabled(true);
        this.setPreferredSize(new Dimension(191, 50));
    }

    @Override
    public void paint(Graphics g){
        Graphics2D g2 = (Graphics2D) g;
        UIUtils.antialiasOn(g2);
        g2.setColor(UIUtils.WHITE);
        g2.fillRect(0, 0, this.getWidth(), this.getHeight());

        if(this.getModel().isRollover()){
            g2.setColor(UIUtils.RED);
            g2.fillRect(0, 0, this.getWidth(), this.getHeight());
            g2.setColor(Color.white);
        } else{
            g2.setColor(UIUtils.DARK_GRAY);
        }

        int x = (this.getWidth() - g2.getFontMetrics().stringWidth(this.getText())) / 2;
        int y = (this.getHeight() - g2.getFontMetrics().getHeight()) / 2;
        g2.drawString(this.getText(), x, y + g2.getFontMetrics().getHeight());
        UIUtils.antialiasOff(g2);
    }
}