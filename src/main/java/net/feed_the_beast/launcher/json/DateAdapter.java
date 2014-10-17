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

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;

public class DateAdapter implements JsonDeserializer<Date>, JsonSerializer<Date>
{
    private final DateFormat enUsFormat = DateFormat.getDateTimeInstance(2, 2, Locale.US);
    private final DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    @Override
    public JsonElement serialize (Date value, Type type, JsonSerializationContext context)
    {
        synchronized (enUsFormat)
        {
            String ret = this.iso8601Format.format(value);
            return new JsonPrimitive(ret.substring(0, 22) + ":" + ret.substring(22));
        }
    }

    @Override
    public Date deserialize (JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException
    {
        if (!(json instanceof JsonPrimitive))
        {
            throw new JsonParseException("Date was not string: " + json);
        }
        if (type != Date.class)
        {
            throw new IllegalArgumentException(getClass() + " cannot deserialize to " + type);
        }
        String value = json.getAsString();
        synchronized (enUsFormat)
        {
            try
            {
                return enUsFormat.parse(value);
            }
            catch (ParseException e)
            {
                try
                {
                    return iso8601Format.parse(value);
                }
                catch (ParseException e2)
                {
                    try
                    {
                        String tmp = value.replace("Z", "+00:00");
                        return iso8601Format.parse(tmp.substring(0, 22) + tmp.substring(23));
                    }
                    catch (ParseException e3)
                    {
                        throw new JsonSyntaxException("Invalid date: " + value, e3);
                    }
                }
            }
        }
    }
}
