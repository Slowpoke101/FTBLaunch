package net.ftb.data.events;

import net.ftb.data.Map;

public interface MapListener {
	/*
	 * Fired by the Map Singleton once a map has been added.
	 * Beware its called for EVERY map thats added!
	 */
	public void onMapAdded(Map map);
}
