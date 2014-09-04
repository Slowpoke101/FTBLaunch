package net.ftb.ui.comp;


import net.ftb.ui.utils.UIUtils;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.imageio.ImageIO;
import javax.swing.JButton;

public final class FTBLogoButton
extends JButton{
    private static final Image logo;
    static
    {
        try{
            logo = ImageIO.read(System.class.getResourceAsStream("/image/logo_ftb.png"));
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public FTBLogoButton(){
        super();
        this.setPreferredSize(new Dimension(191, 50));
    }

    @Override
    public void paint(Graphics g){
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(UIUtils.WHITE);
        g2.fillRect(0, 0, this.getWidth(), this.getHeight());
        int x = (this.getWidth() - logo.getWidth(null)) / 2;
        int y = (this.getHeight() - logo.getHeight(null)) / 2;
        g2.drawImage(logo, x, y, logo.getWidth(null), logo.getHeight(null), null);
    }
}