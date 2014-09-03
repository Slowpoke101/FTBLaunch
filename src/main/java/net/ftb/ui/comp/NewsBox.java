package net.ftb.ui.comp;

import net.ftb.laf.comp.LightBarScrollPane;
import net.ftb.util.NewsUtils;
import net.ftb.util.OSUtils;

import java.awt.BorderLayout;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;

public final class NewsBox
extends JPanel{
    private final HTMLEditorKit html_kit = new HTMLEditorKit();
    private final JEditorPane news_pane = new JEditorPane("text/html", "");

    public NewsBox(){
        super(new BorderLayout());

        this.html_kit.setStyleSheet(OSUtils.makeStyleSheet("news"));
        this.news_pane.setEditorKit(this.html_kit);
        this.news_pane.setEditable(false);
        this.news_pane.addHyperlinkListener(new HyperlinkListener(){
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e){
                if(e.getEventType() == EventType.ACTIVATED){
                    OSUtils.browse(e.getURL().toString());
                }
            }
        });
        this.news_pane.setText(NewsUtils.getNewsHTML());
        this.news_pane.setCaretPosition(0);

        JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new LightBarScrollPane(this.news_pane), new JPanel());
        splitter.setDividerLocation(0.5F);
        splitter.setEnabled(false);
        splitter.setResizeWeight(0.5F);
        this.add(splitter, BorderLayout.CENTER);
    }
}