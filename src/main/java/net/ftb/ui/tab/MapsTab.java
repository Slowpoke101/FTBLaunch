package net.ftb.ui.tab;

import net.ftb.data.Map;
import net.ftb.data.events.MapListener;
import net.ftb.ui.LightBarScrollPane;
import net.ftb.ui.panel.MapPanel;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JPanel;

public final class MapsTab
extends JPanel
implements Tab,
           MapListener{
    private final JPanel content = new JPanel(new GridBagLayout());
    private final GridBagConstraints gbc = new GridBagConstraints();

    public MapsTab(){
        super(new BorderLayout());

        this.gbc.fill = GridBagConstraints.BOTH;
        this.gbc.weightx = 1.0;
        this.gbc.weighty = 1.0;
        this.gbc.gridx = 0;
        this.gbc.gridy = 0;
        this.gbc.insets.set(2, 2, 2, 2);

        this.add(new LightBarScrollPane(this.content), BorderLayout.CENTER);

        Map.addListener(this);
    }

    private void next(){
        this.gbc.gridy++;
    }

    @Override
    public String id(){
        return "maps";
    }

    @Override
    public void onMapAdded(Map map){
        this.content.add(new MapPanel(map), this.gbc);
        this.next();
    }
}