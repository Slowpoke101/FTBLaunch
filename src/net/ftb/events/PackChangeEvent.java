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
    /*
     * make sure to null check when using getter !!!!
     */
    @Getter
    private ArrayList<ModPack> packs;
    public PackChangeEvent(TYPE type, boolean xml ,String... name){
        this.type = type;
        this.names = name;
        this.xml = xml;
    }
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
