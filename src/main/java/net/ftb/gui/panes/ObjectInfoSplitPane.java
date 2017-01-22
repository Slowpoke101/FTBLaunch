/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2017, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
 * FTB Launcher is licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ftb.gui.panes;

import lombok.Getter;
import net.ftb.util.OSUtils;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

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

    public ObjectInfoSplitPane () {
        packs = new JPanel();
        packs.setLayout(new FlowLayout(FlowLayout.LEFT, 0, verticalItemPadding));
        packs.setOpaque(false);

        packsScroll = new JScrollPane();
        packsScroll.setBorder(null);
        packsScroll.setMinimumSize(new Dimension(405, 283));
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
            public void hyperlinkUpdate (HyperlinkEvent event) {
                if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    OSUtils.browse(event.getURL().toString());
                }
            }
        });
        // TODO: Fix darker background for text area? Or is it better blending in?
        packInfo.setBackground(UIManager.getColor("control").darker().darker());
        packInfo.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        infoScroll = new JScrollPane();
        infoScroll.setMinimumSize(new Dimension(430, 290));
        infoScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        infoScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        infoScroll.setWheelScrollingEnabled(true);
        infoScroll.setViewportView(packInfo);
        infoScroll.setOpaque(false);
        infoScroll.setBorder(null);

        this.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        this.setLeftComponent(packsScroll);
        this.setRightComponent(infoScroll);
        this.setDividerSize(4);
        this.setBorder(null);
    }

}
