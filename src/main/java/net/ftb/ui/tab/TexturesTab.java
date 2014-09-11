package net.ftb.ui.tab;

import net.ftb.data.TexturePack;
import net.ftb.data.events.TexturePackListener;
import net.ftb.ui.LightBarScrollPane;
import net.ftb.ui.panel.TexturePackPanel;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JPanel;

public class TexturesTab
extends JPanel
implements Tab,
           TexturePackListener{
    private final GridBagConstraints gbc = new GridBagConstraints();
    private final JPanel content = new JPanel(new GridBagLayout());

    public TexturesTab(){
        super(new BorderLayout());

        this.gbc.fill = GridBagConstraints.BOTH;
        this.gbc.weightx = 1.0;
        this.gbc.weighty = 1.0;
        this.gbc.gridx = 0;
        this.gbc.gridy = 0;
        this.gbc.insets.set(2, 2, 2, 2);

        this.add(new LightBarScrollPane(this.content), BorderLayout.CENTER);

        TexturePack.addListener(this);
    }

    private void next(){
        this.gbc.gridy++;
    }

    @Override
    public String id(){
        return "textures";
    }

    @Override
    public void onTexturePackAdded(TexturePack texturePack){
        this.content.add(new TexturePackPanel(texturePack), this.gbc);
        this.next();
    }
}
