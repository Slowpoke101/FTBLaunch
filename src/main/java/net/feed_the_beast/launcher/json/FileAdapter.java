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

import java.io.File;
import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class FileAdapter extends TypeAdapter<File> {

    @Override
    public File read (JsonReader json) throws IOException {
        if (json.hasNext()) {
            String value = json.nextString();
            return value == null ? null : new File(value);
        }
        return null;
    }

    @Override
    public void write (JsonWriter json, File value) throws IOException {
        if (value == null) {
            json.nullValue();
        } else {
            json.value(value.getAbsolutePath());
        }
    }
}
