package com.moreoutlines.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.util.Identifier;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;

public final class GsonUtil {
    private GsonUtil() {}

    public static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(Identifier.class, new IdentifierTypeAdapter())
        .create();

    private static class IdentifierTypeAdapter implements JsonSerializer<Identifier>, JsonDeserializer<Identifier> {
        @Override
        public JsonElement serialize(Identifier src, Type typeOfSrc, com.google.gson.JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }

        @Override
        public Identifier deserialize(JsonElement json, Type typeOfT, com.google.gson.JsonDeserializationContext context)
                throws JsonParseException {
            try {
                return Identifier.of(json.getAsString());
            } catch (Exception e) {
                throw new JsonParseException("Invalid identifier: " + json.getAsString(), e);
            }
        }
    }
}
