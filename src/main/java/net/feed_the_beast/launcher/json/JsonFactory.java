/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2014, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
 * FTB Launcher is licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import net.feed_the_beast.launcher.json.launcher.RetiredPacks;
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

    public static RetiredPacks getRetiredPacks (File json) throws JsonSyntaxException, JsonIOException, IOException {
        FileReader reader = new FileReader(json);
        RetiredPacks packs = GSON.fromJson(reader, RetiredPacks.class);
        reader.close();
        return packs;
    }

    public static Version loadVersion (File json) throws JsonSyntaxException, JsonIOException, IOException {
        FileReader reader = new FileReader(json);
        Version v = GSON.fromJson(reader, Version.class);
        reader.close();
        return v;
    }

    public static AssetIndex loadAssetIndex (File json) throws JsonSyntaxException, JsonIOException, IOException {
        FileReader reader = new FileReader(json);
        AssetIndex a = GSON.fromJson(reader, AssetIndex.class);
        reader.close();
        return a;
    }

    public static Library loadLibrary (String libJsonObject) throws JsonSyntaxException, JsonIOException {
        return GSON.fromJson(libJsonObject, Library.class);
    }

    public static Update getUpdate (String name, String url) throws IOException {
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
