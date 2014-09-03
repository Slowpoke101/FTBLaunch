package net.ftb.ui.tab;

import net.ftb.laf.comp.LightBarScrollPane;

import java.awt.BorderLayout;
import javax.swing.JPanel;

public class ScrollPaneWrapperTab<T extends JPanel & Tab>
extends JPanel
implements Tab{
    private final T tab;

    public ScrollPaneWrapperTab(T tab){
        super(new BorderLayout());
        this.tab = tab;
        this.add(new LightBarScrollPane(tab), BorderLayout.CENTER);
    }

    @Override
    public String id(){
        return this.tab.id();
    }
}