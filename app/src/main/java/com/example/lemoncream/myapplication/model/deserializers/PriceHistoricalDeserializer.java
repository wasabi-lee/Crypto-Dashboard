package com.example.lemoncream.myapplication.model.deserializers;

import android.util.Log;

import com.example.lemoncream.myapplication.model.gson.PriceHistorical;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by LemonCream on 2018-03-02.
 */

public class PriceHistoricalDeserializer implements JsonDeserializer {

    /**
     * Json response comes in this form
     {
     "BTC": {
         "USD": 10300,
         "EUR": 8379.99
         }
     }
     */

    private static final String TAG = PriceHistoricalDeserializer.class.getSimpleName();

    @Override
    public Object deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Log.d(TAG, "deserialize: ");
        if (json != null) {
            
            PriceHistorical priceHistorical = new PriceHistorical();

            for (Map.Entry<String, JsonElement> ignored : json.getAsJsonObject().entrySet()) {
                for (Map.Entry<String, JsonElement> innerEntry : ignored.getValue().getAsJsonObject().entrySet()) {
                    priceHistorical.setTsym(innerEntry.getKey());
                    priceHistorical.setPrice(innerEntry.getValue().getAsFloat());
                }
            }

            return priceHistorical;

        } else {
            return null;
        }
    }
}
