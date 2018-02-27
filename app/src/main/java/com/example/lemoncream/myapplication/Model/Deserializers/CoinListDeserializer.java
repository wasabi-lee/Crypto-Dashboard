package com.example.lemoncream.myapplication.Model.Deserializers;

import android.util.Log;

import com.example.lemoncream.myapplication.Model.GsonModels.CoinData;
import com.example.lemoncream.myapplication.Model.RealmModels.Coin;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by LemonCream on 2018-02-09.
 */

public class CoinListDeserializer implements JsonDeserializer {

    private static final String TAG = CoinListDeserializer.class.getSimpleName();

    @Override
    public Object deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        if (json != null) {

            final JsonObject dataset = json.getAsJsonObject().getAsJsonObject("Data");
            CoinData result = new CoinData();
            List<Coin> coinList = new ArrayList<>();

            for (Map.Entry<String, JsonElement> entry : dataset.entrySet()) {

                Coin newCoin = new Coin();
                Log.d(TAG, "deserialize: " + entry.toString());

                JsonElement name = entry.getValue().getAsJsonObject().get("CoinName");
                JsonElement symbol = entry.getValue().getAsJsonObject().get("Symbol");
                JsonElement imageUrl = entry.getValue().getAsJsonObject().get("ImageUrl");

                newCoin.setName(name.getAsString());
                newCoin.setSymbol(symbol.getAsString());
                newCoin.setImageUrl(imageUrl != null ? imageUrl.getAsString() : null);

                coinList.add(newCoin);
            }
            result.setCoinList(coinList);
            return result;
        }
        Log.d(TAG, "deserialize: The result is null");
        return null;
    }

}
