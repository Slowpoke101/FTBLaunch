package net.feed_the_beast.launcher.json.assets;

import java.util.Map;

public class AssetIndex
{
    public Map<String, Asset> objects;
    public boolean virtual;

    public static class Asset
    {
        public String hash;
        public long size;
    }
}
