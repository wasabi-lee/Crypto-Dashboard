package com.example.lemoncream.myapplication.Model.Deserializers;

import android.util.Log;

import com.example.lemoncream.myapplication.Model.GsonModels.Price;
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

public class PriceDeserializer implements JsonDeserializer {

    private static final String TAG = PriceDeserializer.class.getSimpleName();

    @Override
    public Object deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        if (json != null) {

            Price price = new Price();
            HashMap<String, Float> prices = new HashMap<>();
            JsonObject jsonObj = json.getAsJsonObject();
            for (Map.Entry<String, JsonElement> outerEntry : jsonObj.entrySet()) {
                String fsym = outerEntry.getKey();
                JsonObject priceData = outerEntry.getValue().getAsJsonObject();
                price.setFsym(fsym);

                for (Map.Entry<String, JsonElement> innerEntry : priceData.entrySet()) {
                    String currentTsym = innerEntry.getKey();
                    Float currentPrice = innerEntry.getValue().getAsFloat();
                    prices.put(currentTsym, currentPrice);
                }
            }

            price.setPrices(prices);
            Log.d(TAG, "deserialize: " + json.toString());
            return price;
        }

        return null;
    }
}
