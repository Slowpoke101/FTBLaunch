package net.feed_the_beast.launcher.json;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Date;

import net.feed_the_beast.launcher.json.assets.AssetIndex;
import net.feed_the_beast.launcher.json.versions.Version;

import com.google.gson.*;

public class JsonFactory
{
    public static final Gson GSON;
    static
    {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapterFactory(new EnumAdaptorFactory());
        builder.registerTypeAdapter(Date.class, new DateAdapter());
        builder.registerTypeAdapter(File.class, new FileAdapter());
        builder.enableComplexMapKeySerialization();
        builder.setPrettyPrinting();
        GSON = builder.create();
    }

    public static Version loadVersion(File json) throws JsonSyntaxException, JsonIOException, FileNotFoundException
    {
        return GSON.fromJson(new FileReader(json), Version.class);
    }

    public static AssetIndex loadAssetIndex(File json) throws JsonSyntaxException, JsonIOException, FileNotFoundException
    {
        return GSON.fromJson(new FileReader(json), AssetIndex.class);
    }
}
