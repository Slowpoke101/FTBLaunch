package net.ftb.ui.tab;

import net.ftb.laf.utils.UIUtils;
import net.ftb.util.NewsUtils;
import net.ftb.util.OSUtils;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;

public class HomeTab
extends JPanel
implements Tab{
    private final HTMLEditorKit html_kit = new HTMLEditorKit();
    private final JEditorPane news_pane = new JEditorPane("text/html", "");

    public HomeTab(){
        this.setBackground(UIUtils.DARK_GRAY);

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
    }

    @Override
    public String id(){
        return "home";
    }
}