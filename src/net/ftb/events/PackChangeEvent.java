package net.ftb.events;

import lombok.Getter;
import net.ftb.data.ModPack;

import java.util.ArrayList;

public class PackChangeEvent implements ILauncherEvent {
    public enum TYPE{
        ADD, CHANGE, REMOVE
    }

    @Getter
    private TYPE type;

    @Getter
    private boolean xml;
    @Getter
    private String[] names;
    /**
     * make sure to null check when using getter !!!!
     */
    @Getter
    private ArrayList<ModPack> packs;

    /**
     *
     * @param type type of pack change event such as add or remove
     * @param xml are the names XML file names in the repo(true) or pack names(false)?
     * @param name name of pack involved
     */
    public PackChangeEvent(TYPE type, boolean xml ,String... name){
        this.type = type;
        this.names = name;
        this.xml = xml;
    }

    /**
     *
     * @param type type of pack change event such as add or remove
     * @param packs Mod Packs being added/removed/changed
     */
    public PackChangeEvent(TYPE type,ArrayList<ModPack> packs){
        this.type = type;
        this.packs = packs;
        this.xml = false;
        names = new String[packs.size()];
        int cnt = 0;
        for (ModPack pack : packs){
            names[cnt] = (pack.getName());
            cnt ++;
        }
    }
}
