package net.ftb.data.events;

import net.ftb.data.ModPack;

public interface ModPackListener {
	
	/*
	 * Fired by the ModPack Singleton once a modpack has been added.
	 * Beware its called for EVERY pack thats added!
	 */
	public void onModPackAdded(ModPack pack);
}
