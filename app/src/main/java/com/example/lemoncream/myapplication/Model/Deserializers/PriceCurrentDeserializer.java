package com.example.lemoncream.myapplication.Model.Deserializers;

import android.util.Log;

import com.example.lemoncream.myapplication.Model.GsonModels.PriceCurrent;
import com.example.lemoncream.myapplication.Model.GsonModels.PriceHistorical;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by LemonCream on 2018-02-27.
 */

public class PriceCurrentDeserializer implements JsonDeserializer {

    private static final String TAG = PriceCurrentDeserializer.class.getSimpleName();

    @Override
    public Object deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json != null) {

            PriceCurrent priceCurrent = new PriceCurrent();
            HashMap<String, Float> prices = new HashMap<>();

            try {
                JsonObject jsonObj = json.getAsJsonObject();
                    for (Map.Entry<String, JsonElement> entry : jsonObj.entrySet()) {
                        String currentTsym = entry.getKey();
                        Float currentPrice = entry.getValue().getAsFloat();
                        prices.put(currentTsym, currentPrice);
                    }
            } catch (JsonParseException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            priceCurrent.setPrices(prices);
            Log.d(TAG, "deserialize: " + json.toString());
            return priceCurrent;

        }

        Log.d(TAG, "deserialize: The result is null");
        return null;

    }
}

