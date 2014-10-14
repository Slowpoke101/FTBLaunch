package net.ftb.gui.panes;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import lombok.Getter;
import net.ftb.util.OSUtils;

public class ObjectInfoSplitPane extends JSplitPane {

	
	// Space between items in scrolling list
	static final int verticalItemPadding = 4; 

	
	@Getter
	protected JPanel packs;
	@Getter
	protected JScrollPane packsScroll;	
	@Getter
	protected JEditorPane packInfo;
	@Getter
	protected JScrollPane infoScroll;
	
	
	public ObjectInfoSplitPane()
	{
        packs = new JPanel();
        packs.setLayout(new FlowLayout(FlowLayout.LEFT, 0, verticalItemPadding));
        packs.setOpaque(false);

        packsScroll = new JScrollPane();
        packsScroll.setBorder(null);
        packsScroll.setMinimumSize(new Dimension(420, 283));
        packsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        packsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        packsScroll.setWheelScrollingEnabled(true);
        packsScroll.setOpaque(false);
        packsScroll.setViewportView(packs);
        packsScroll.getVerticalScrollBar().setUnitIncrement(19);

        packInfo = new JEditorPane();
        packInfo.setEditable(false);
        packInfo.setContentType("text/html");
        packInfo.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent event) {
                if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    OSUtils.browse(event.getURL().toString());
                }
            }
        });
        // TODO: Fix darker background for text area? Or is it better blending in?
        packInfo.setBackground(UIManager.getColor("control").darker().darker());

        infoScroll = new JScrollPane();
        infoScroll.setMinimumSize(new Dimension(430,290));
        infoScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        infoScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        infoScroll.setWheelScrollingEnabled(true);
        infoScroll.setViewportView(packInfo);
        infoScroll.setOpaque(false);
        
        
        this.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        this.setLeftComponent(packsScroll);
        this.setRightComponent(infoScroll);
        this.setDividerSize(4);
	}
	
	
}
