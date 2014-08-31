package net.ftb.events;

import net.ftb.data.ModPack;

public final class OpenInfoPanelEvent{
    public final ModPack pack;

    public OpenInfoPanelEvent(ModPack pack){
        this.pack = pack;
    }
}