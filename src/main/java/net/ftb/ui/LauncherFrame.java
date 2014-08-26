package net.ftb.ui;

import net.ftb.data.ModPack;
import net.ftb.events.PackChangeEvent;
import net.ftb.laf.comp.LightBarScrollPane;
import net.ftb.laf.utils.UIUtils;
import net.ftb.main.Main;
import net.ftb.ui.panel.ModPackPanel;

import com.google.common.eventbus.Subscribe;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

public final class LauncherFrame
extends JFrame{
    private final JPanel centerPanel = new JPanel();
    private final JPanel leftPanel = new JPanel();
    private final GridBagConstraints gbc = new GridBagConstraints();

    private final JToggleButton homeButton = new JToggleButton("Home");
    private final JToggleButton ftbMpButton = new JToggleButton("FTB Mod Packs");
    private final JToggleButton tpMpButton = new JToggleButton("Third Party Mod Packs");

    public LauncherFrame(){
        super("FTB Launcher");
        this.setLayout(new BorderLayout());
        this.setMinimumSize(new Dimension(830, 500));
        this.add(new LightBarScrollPane(this.centerPanel), BorderLayout.CENTER);
        this.add(this.leftPanel, BorderLayout.WEST);
        this.setupCenterPanel();
        this.setupLeftPanel();
    }

    private void setupLeftPanel(){
        this.leftPanel.setBackground(UIUtils.WHITE);

        this.gbc.fill = GridBagConstraints.BOTH;
        this.gbc.weighty = 0.1;
        this.gbc.gridx = 0;
        this.gbc.gridy = 0;
        this.leftPanel.add(this.homeButton, this.gbc);
        this.gbc.gridy++;
        this.leftPanel.add(this.ftbMpButton, this.gbc);
    }

    private void setupCenterPanel(){
        this.centerPanel.setLayout(new GridBagLayout());
        this.centerPanel.setBackground(UIUtils.WHITE);

        this.gbc.weightx = 1.0;
        this.gbc.weighty = 1.0;
        this.gbc.gridx = 0;
        this.gbc.gridy = 0;
        this.gbc.insets.set(15, 15, 15, 15);

        Main.getEventBus().register(this);
    }

    @Subscribe
    public void onPackChange(PackChangeEvent e){
        try{
            this.centerPanel.removeAll();
            List<ModPack> packs = ModPack.getPackArray();
            for(ModPack pack : packs){
                this.centerPanel.add(new ModPackPanel(pack), this.gbc);
                this.next();
            }
            this.repaint();
        } catch(Exception ex){
            ex.printStackTrace(System.err);
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
}