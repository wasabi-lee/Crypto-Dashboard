package com.example.lemoncream.myapplication.model.deserializers;

import com.example.lemoncream.myapplication.model.gson.PriceSimple;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by Wasabi on 3/26/2018.
 */

public class PriceSimpleDeserializer implements JsonDeserializer {

    @Override
    public Object deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        PriceSimple result = new PriceSimple();
        for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
            result.setPrice(entry.getValue().getAsFloat());
        }
        return result;
    }
}
