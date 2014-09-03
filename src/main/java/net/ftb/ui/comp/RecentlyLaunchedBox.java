package net.ftb.ui.comp;

import net.ftb.events.PackChangeEvent;
import net.ftb.events.PackChangeEvent.TYPE;
import net.ftb.main.Main;
import net.ftb.ui.panel.ModPackPanel;

import com.google.common.eventbus.Subscribe;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public final class RecentlyLaunchedBox
extends JPanel{
    private final GridBagConstraints gbc = new GridBagConstraints();
    private boolean loaded = false;

    public RecentlyLaunchedBox(){
        super(new GridBagLayout());
        this.gbc.gridx = 0;
        this.gbc.gridy = 0;
        this.gbc.weightx = 1.0;
        this.gbc.weighty = 1.0;
        this.gbc.anchor = GridBagConstraints.LINE_START;
        this.gbc.insets.set(5, 15, 5, 5);
        this.add(new JLabel("Recently Launched"), this.gbc);
        this.gbc.insets.set(15, 15, 15, 15);
        this.gbc.gridy++;

        Main.getEventBus().register(this);
    }

    @Subscribe
    public void onPackLoaded(PackChangeEvent e){
        if(e.getType() == TYPE.ADD){
            for(int i = 0; i < 3; i++){
                this.add(new ModPackPanel(e.getPacks().get(i)), this.gbc);
            }
            this.loaded = true;
        }
    }
}