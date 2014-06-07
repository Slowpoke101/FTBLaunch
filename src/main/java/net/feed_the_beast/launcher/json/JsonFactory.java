package net.feed_the_beast.launcher.json;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

import net.feed_the_beast.launcher.json.assets.AssetIndex;
import net.feed_the_beast.launcher.json.launcher.Update;
import net.feed_the_beast.launcher.json.versions.Library;
import net.feed_the_beast.launcher.json.versions.Version;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.io.IOUtils;

public class JsonFactory {
    public static final Gson GSON;
    static {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapterFactory(new EnumAdaptorFactory());
        builder.registerTypeAdapter(Date.class, new DateAdapter());
        builder.registerTypeAdapter(File.class, new FileAdapter());
        builder.enableComplexMapKeySerialization();
        builder.setPrettyPrinting();
        GSON = builder.create();
    }

    public static Version loadVersion (File json) throws JsonSyntaxException, JsonIOException, IOException {
        FileReader reader = new FileReader(json);
        Version v =  GSON.fromJson(reader, Version.class);
        reader.close();
        return v;
    }

    public static AssetIndex loadAssetIndex (File json) throws JsonSyntaxException, JsonIOException, IOException {
        FileReader reader = new FileReader(json);
        AssetIndex a =  GSON.fromJson(reader, AssetIndex.class);
        reader.close();
        return a;
    }
    public static Update getUpdate(String name, String url) throws IOException{
        Library l = new Library();
        l.name = name;
        return GSON.fromJson(IOUtils.toString(new URL(url + l.getPath())), Update.class);
    }
}
