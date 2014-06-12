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

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class EnumAdaptorFactory implements TypeAdapterFactory {

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create (Gson gson, TypeToken<T> type) {
        if (!type.getRawType().isEnum())
            return null;
        final Map<String, T> map = Maps.newHashMap();
        for (T c : (T[]) type.getRawType().getEnumConstants()) {
            map.put(c.toString().toLowerCase(Locale.US), c);
        }

        return new TypeAdapter<T>() {
            @Override
            public T read (JsonReader reader) throws IOException {
                if (reader.peek() == JsonToken.NULL) {
                    reader.nextNull();
                    return null;
                }
                String name = reader.nextString();
                if (name == null)
                    return null;
                return map.get(name.toLowerCase(Locale.US));
            }

            @Override
            public void write (JsonWriter writer, T value) throws IOException {
                if (value == null) {
                    writer.nullValue();
                } else {
                    writer.value(value.toString().toLowerCase(Locale.US));
                }
            }
        };
    }
}
