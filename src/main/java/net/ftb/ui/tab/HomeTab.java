package net.ftb.ui.tab;

import net.ftb.laf.utils.UIUtils;
import net.ftb.ui.comp.NewsBox;
import net.ftb.ui.comp.RecentlyLaunchedBox;

import java.awt.BorderLayout;
import javax.swing.JPanel;

public class HomeTab
extends JPanel
implements Tab{
    public HomeTab(){
        super(new BorderLayout());
        this.setBackground(UIUtils.DARK_GRAY);

        this.add(new RecentlyLaunchedBox(), BorderLayout.NORTH);
        this.add(new NewsBox(), BorderLayout.CENTER);
    }

    @Override
    public String id(){
        return "home";
    }
}