package net.ftb.ui;

import net.ftb.laf.FTBLookAndFeel;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class UITest{
    static
    {
        try{
            UIManager.setLookAndFeel(new FTBLookAndFeel());
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public static void main(String... args){
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                new LauncherFrame().setVisible(true);
            }
        });
    }
}