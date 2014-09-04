package net.ftb.ui;

import net.ftb.events.OpenInfoPanelEvent;
import net.ftb.main.Main;
import net.ftb.ui.comp.FTBLogoButton;
import net.ftb.ui.comp.SelectProfileButton;
import net.ftb.ui.comp.ToggleButtonGroup;
import net.ftb.ui.panel.ModPackInfoPanel;
import net.ftb.ui.tab.FTBMPTab;
import net.ftb.ui.tab.HomeTab;
import net.ftb.ui.tab.MapsTab;
import net.ftb.ui.tab.OptionsTab;
import net.ftb.ui.tab.ScrollPaneWrapperTab;
import net.ftb.ui.tab.TPMPTab;
import net.ftb.ui.tab.Tab;
import net.ftb.ui.tab.TexturesTab;
import net.ftb.ui.utils.UIUtils;

import com.google.common.eventbus.Subscribe;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.WindowConstants;

public final class LauncherFrame
extends JFrame{
    private final JPanel centerPanel = new JPanel(new CardLayout());
    private final JPanel leftPanel = new JPanel();
    private final JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    private final GridBagConstraints gbc = new GridBagConstraints();
    private final Set<String> infoPanels = new HashSet<String>();

    private final TPMPTab tpmpTab = new TPMPTab();
    private final FTBMPTab ftbMpTab = new FTBMPTab();

    private final ToggleButtonGroup tbg = new ToggleButtonGroup();

    private final JToggleButton homeButton = new JToggleButton("Home", true);
    private final JToggleButton ftbMpButton = new JToggleButton("FTB Mod Packs");
    private final JToggleButton tpMpButton = new JToggleButton("Third Party Mod Packs");
    private final JToggleButton mapsButton = new JToggleButton("Maps");
    private final JToggleButton texturesButton = new JToggleButton("Textures");
    private final JToggleButton optionsButton = new JToggleButton("Options");

    public LauncherFrame(){
        super("FTB Launcher");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.setResizable(false);
        this.setMinimumSize(new Dimension(830, 500));
        this.add(this.centerPanel, BorderLayout.CENTER);
        this.add(this.leftPanel, BorderLayout.WEST);
        this.add(this.topPanel, BorderLayout.NORTH);
        this.setupLeftPanel();
        this.setupTopPanel();
        this.addActionListeners();
        this.addTabs();

        Main.getEventBus().register(this);
    }

    private void addTabs(){
        this.addTab(new HomeTab());
        this.addTab(this.ftbMpTab);
        this.addTab(this.tpmpTab);
        this.addTab(new MapsTab());
        this.addTab(new TexturesTab());
        this.addTab(new OptionsTab());
    }

    @Subscribe
    public void onInfoOpen(OpenInfoPanelEvent e){
        if(!this.infoPanels.contains(e.pack.getName())){
            this.addTab(new ScrollPaneWrapperTab<ModPackInfoPanel>(new ModPackInfoPanel(e.pack)));
            this.infoPanels.add(e.pack.getName());
        }

        this.showTab(e.pack.getName());
    }

    private void addActionListeners(){
        this.tbg.add(this.homeButton);
        this.tbg.add(this.ftbMpButton);
        this.tbg.add(this.tpMpButton);
        this.tbg.add(this.mapsButton);
        this.tbg.add(this.texturesButton);
        this.tbg.add(this.optionsButton);

        this.homeButton.addItemListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent e){
                if(homeButton.isSelected()){
                    showTab("home");
                    repaint();
                }
            }
        });
        this.ftbMpButton.addItemListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent e){
                if(ftbMpButton.isSelected()){
                    showTab("ftb-mp");
                    repaint();
                }
            }
        });
        this.tpMpButton.addItemListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent e){
                if(tpMpButton.isSelected()){
                    showTab("tp-mp");
                    repaint();
                }
            }
        });
        this.mapsButton.addItemListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent e){
                if(mapsButton.isSelected()){
                    showTab("maps");
                }
            }
        });
        this.texturesButton.addItemListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent e){
                if(texturesButton.isSelected()){
                    showTab("textures");
                    repaint();
                }
            }
        });
        this.optionsButton.addItemListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent e){
                if(optionsButton.isSelected()){
                    showTab("options");
                    repaint();
                }
            }
        });
    }

    private void showTab(String id){
        ((CardLayout) this.centerPanel.getLayout()).show(this.centerPanel, id);
        this.centerPanel.repaint();
    }

    private <T extends JPanel & Tab> void addTab(T t){
        this.centerPanel.add(t, t.id());
    }

    private void setupLeftPanel(){
        this.leftPanel.setBackground(UIUtils.WHITE);
        this.leftPanel.setLayout(new GridBagLayout());

        this.gbc.fill = GridBagConstraints.BOTH;
        this.gbc.weighty = 0.1;
        this.gbc.gridx = 0;
        this.gbc.gridy = 0;
        this.leftPanel.add(this.homeButton, this.gbc);
        this.gbc.gridy++;
        this.leftPanel.add(this.ftbMpButton, this.gbc);
        this.gbc.gridy++;
        this.leftPanel.add(this.tpMpButton, this.gbc);
        this.gbc.gridy++;
        this.leftPanel.add(this.mapsButton, this.gbc);
        this.gbc.gridy++;
        this.leftPanel.add(this.texturesButton, this.gbc);
        this.gbc.gridy++;
        this.leftPanel.add(Box.createRigidArea(new Dimension(125, 50)), this.gbc);
        this.gbc.gridy++;
        this.leftPanel.add(this.optionsButton, this.gbc);
    }

    private void setupTopPanel(){
        this.topPanel.setBackground(UIUtils.WHITE);
        this.topPanel.setLayout(new GridBagLayout());
        this.gbc.gridx = 0;
        this.gbc.gridy = 0;
        this.gbc.weightx = 0.1;
        this.gbc.weighty = 0.1;
        this.gbc.fill = GridBagConstraints.NONE;
        this.gbc.insets.set(0, 0, 0, 0);
        this.gbc.anchor = GridBagConstraints.LINE_START;
        this.topPanel.add(new FTBLogoButton(), this.gbc);
        this.gbc.anchor = GridBagConstraints.CENTER;
        this.topPanel.add(Box.createHorizontalGlue());
        this.gbc.anchor = GridBagConstraints.LINE_END;
        this.topPanel.add(new SelectProfileButton(), this.gbc);
    }
}