package net.ftb.ui.tab;

import net.ftb.data.ModPack;
import net.ftb.events.PackChangeEvent;
import net.ftb.events.PackChangeEvent.TYPE;
import net.ftb.main.Main;
import net.ftb.ui.LightBarScrollPane;
import net.ftb.ui.panel.ModPackPanel;

import com.google.common.eventbus.Subscribe;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JPanel;

public final class FTBMPTab
extends JPanel
implements Tab{
    private final GridBagConstraints gbc = new GridBagConstraints();
    private final JPanel content = new JPanel(new GridBagLayout());

    public FTBMPTab(){
        super(new BorderLayout());

        this.gbc.fill = GridBagConstraints.BOTH;
        this.gbc.weightx = 1.0;
        this.gbc.weighty = 1.0;
        this.gbc.gridx = 0;
        this.gbc.gridy = 0;
        this.gbc.insets.set(15, 15, 15, 15);

        this.add(new LightBarScrollPane(this.content), BorderLayout.CENTER);

        Main.getEventBus().register(this);
    }

    @Subscribe
    public void onPackChange(PackChangeEvent e){
        if(e.getType() == TYPE.ADD){
            for(ModPack mp : e.getPacks()){
                if(!mp.isThirdPartyTab()){
                    this.content.add(new ModPackPanel(mp), this.gbc);
                    this.next();
                }
            }
        }
    }

    private void next(){
        if(this.gbc.gridx == 2){
            this.gbc.gridx = 0;
            this.gbc.gridy++;
        } else{
            this.gbc.gridx++;
        }
    }

    @Override
    public String id(){
        return "ftb-mp";
    }
}