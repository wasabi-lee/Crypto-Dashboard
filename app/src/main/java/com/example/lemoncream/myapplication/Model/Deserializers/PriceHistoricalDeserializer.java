package com.example.lemoncream.myapplication.Model.Deserializers;

import android.util.Log;

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
 * Created by LemonCream on 2018-02-19.
 */

public class PriceHistoricalDeserializer implements JsonDeserializer {

    private static final String TAG = PriceHistoricalDeserializer.class.getSimpleName();

    @Override
    public Object deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        if (json != null) {

            PriceHistorical priceHistorical = new PriceHistorical();
            HashMap<String, Float> prices = new HashMap<>();

            try {
                JsonObject jsonObj = json.getAsJsonObject();
                for (Map.Entry<String, JsonElement> outerEntry : jsonObj.entrySet()) {
                    String fsym = outerEntry.getKey();
                    JsonObject priceData = outerEntry.getValue().getAsJsonObject();
                    priceHistorical.setFsym(fsym);

                    for (Map.Entry<String, JsonElement> innerEntry : priceData.entrySet()) {
                        String currentTsym = innerEntry.getKey();
                        priceHistorical.setTsym(currentTsym); // todo change it to the valid base currency
                        Float currentPrice = innerEntry.getValue().getAsFloat();
                        prices.put(currentTsym, currentPrice);
                    }
                }
            } catch (JsonParseException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            priceHistorical.setPrices(prices);
            Log.d(TAG, "deserialize: " + json.toString());
            return priceHistorical;

        }

        Log.d(TAG, "deserialize: The result is null");
        return null;

    }
}
