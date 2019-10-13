/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2018, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
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

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import net.feed_the_beast.launcher.json.versions.Arguments;
import net.feed_the_beast.launcher.json.versions.Game;
import net.feed_the_beast.launcher.json.versions.GameRule;

import java.lang.reflect.Type;
import java.util.List;

public class GameAdapter implements JsonDeserializer<Game> {
    @Override
    public Game deserialize (JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        List<String> values = Lists.newArrayList();
        List<GameRule> gamerules = Lists.newArrayList();
        boolean use = false;
        String textstr = null;
        final JsonObject root = json.getAsJsonObject();
        final JsonElement rules = root.get("rules");
        final JsonElement value = root.get("value");
        final JsonPrimitive text = root.getAsJsonPrimitive("text");
        final JsonPrimitive useText = root.getAsJsonPrimitive("useText");
        if (useText != null ) {
            use = useText.getAsBoolean();
        }
        if (text != null) {
            textstr = text.getAsString();
        }
        if (rules != null) {
            if (rules.isJsonArray()) {
                for (JsonElement e : rules.getAsJsonArray()) {
                    gamerules.add(JsonFactory.GSON.fromJson(e.getAsJsonObject(), GameRule.class));
                }
            }

        }
        if (value != null) {
            if (value.isJsonArray()) {
                for (JsonElement gameArg : value.getAsJsonArray()) {
                    if (gameArg.isJsonPrimitive()) {
                        values.add(gameArg.getAsString());
                    }
                }
            } else if (value.isJsonPrimitive()) {
                values.add(value.getAsString());
            }

        }
        return new Game(values, gamerules, textstr, use);
    }
}