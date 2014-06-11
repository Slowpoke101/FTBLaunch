package net.feed_the_beast.launcher.json;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.*;
import net.feed_the_beast.launcher.json.assets.AssetIndex;
import net.feed_the_beast.launcher.json.launcher.Update;
import net.feed_the_beast.launcher.json.versions.Library;
import net.feed_the_beast.launcher.json.versions.Version;

import net.ftb.log.Logger;
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

    @SuppressWarnings("unchecked")
    public static Map<String, Object> decode (String s) {
        try {
            Map<String, Object> ret;
            JsonObject jso = new JsonParser().parse(s).getAsJsonObject();
            ret = (Map<String, Object>) decodeElement(jso);
            return ret;
        } catch (Exception e) {
            Logger.logError("Error decoding JSON", e);
            return null;
        }
    }

    public static Object decodeElement (JsonElement e) {
        if (e instanceof JsonObject) {
            Map<String, Object> ret = Maps.newLinkedHashMap();
            for (Map.Entry<String, JsonElement> jse : ((JsonObject) e).entrySet()) {
                ret.put(jse.getKey(), decodeElement(jse.getValue()));
            }
            return ret;
        }
        if (e instanceof JsonArray) {
            List<Object> ret = Lists.newArrayList();
            for (JsonElement jse : e.getAsJsonArray()) {
                ret.add(decodeElement(jse));
            }
            return ret;

        }
        return e.getAsString();
    }

    public static String encode (Map<String, Object> m) {
        try {
            return GSON.toJson(m);
        } catch (Exception e) {
            Logger.logError("Error encoding JSON", e);
            return null;
        }
    }
    public static String encodeStrListMap (Map<String, List<String>> m) {
        try {
            return GSON.toJson(m);
        } catch (Exception e) {
            Logger.logError("Error encoding JSON", e);
            return null;
        }
    }

}
