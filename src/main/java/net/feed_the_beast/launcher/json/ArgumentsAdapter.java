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
import net.feed_the_beast.launcher.json.versions.Arguments;
import net.feed_the_beast.launcher.json.versions.Game;
import net.feed_the_beast.launcher.json.versions.JVM;

import java.lang.reflect.Type;
import java.util.List;

public class ArgumentsAdapter implements JsonDeserializer<Arguments> {
    @Override
    public Arguments deserialize (JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        List<Game> gameList = Lists.newArrayList();
        List<JVM> jvmList = Lists.newArrayList();

        final JsonObject root = json.getAsJsonObject();
        final JsonArray game = root.getAsJsonArray("game");
        final JsonArray jvm = root.getAsJsonArray("jvm");

        for (JsonElement gameArg : game) {
            if (gameArg.isJsonObject()) {
                gameList.add(JsonFactory.GSON.fromJson(gameArg.getAsJsonObject(), Game.class));
            } else {
                gameList.add(new Game(gameArg.getAsString(), true));
            }
        }

        for (JsonElement gameArg : jvm) {
            if (gameArg.isJsonObject()) {
                jvmList.add(JsonFactory.GSON.fromJson(gameArg.getAsJsonObject(), JVM.class));
            } else {
                jvmList.add(new JVM(gameArg.getAsString(), true));

            }
        }
        return new Arguments(gameList, jvmList);

    }
}