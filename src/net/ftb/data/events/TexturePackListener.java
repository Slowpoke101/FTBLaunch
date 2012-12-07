package net.ftb.data.events;

import net.ftb.data.TexturePack;

public interface TexturePackListener {
	/**
	 * Fired by the TexturePack Singleton once a texturepack has been added.
	 * Beware its called for EVERY map thats added!
	 */
	public void onTexturePackAdded(TexturePack texturePack);
}
