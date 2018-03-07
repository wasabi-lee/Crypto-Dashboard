package com.example.lemoncream.myapplication.Model.Deserializers;


import android.util.Log;

import com.example.lemoncream.myapplication.Model.GsonModels.ExchangeData;
import com.example.lemoncream.myapplication.Model.RealmModels.Exchange;
import com.example.lemoncream.myapplication.Model.RealmModels.Pair;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.RealmList;

/**
 * Created by LemonCream on 2018-02-03.
 */

public class ExchangeDataDeserializer implements JsonDeserializer {

    /*
     * ExchangeDataDeserializer.class
     * It's for converting the JSON response to Realm object.
     */

    private final String TAG = ExchangeDataDeserializer.class.getSimpleName();

    @Override
    public Object deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        Log.d(TAG, "deserialize: start deserializing");
        if (json != null) {
            final JsonObject jsonObject = json.getAsJsonObject();
            List<Exchange> exchanges = new ArrayList<>();
            List<Pair> pairs;
            HashMap<String, Pair> temp_pairs = new HashMap<>();

            for (Map.Entry<String, JsonElement> outer_entry : jsonObject.entrySet()) {
                String exchangeName = outer_entry.getKey();
                Exchange exchange = new Exchange(exchangeName);
                exchanges.add(exchange);

                JsonObject value = outer_entry.getValue().getAsJsonObject();

                for (Map.Entry<String, JsonElement> inner_entry : value.entrySet()) {
                    String fsym = inner_entry.getKey();
                    String[] tsyms = convertToStringArray(inner_entry.getValue().getAsJsonArray());

                    for (String tsym:tsyms) {
                        String pairName = fsym.toUpperCase() + "_" + tsym.toUpperCase();
                        Pair newPair = new Pair(pairName);
                        if (temp_pairs.get(pairName) != null) {
                            RealmList<Exchange> currentPairExchanges = temp_pairs.get(pairName).getExchanges();
                            currentPairExchanges.add(exchange);
                            newPair.setExchanges(currentPairExchanges);
                        } else {
                            newPair.setExchanges(new RealmList<Exchange>(exchange));
                        }
                        temp_pairs.put(newPair.getPairName(), newPair);
                    }
                }
            }
            pairs = new ArrayList<>(temp_pairs.values());
            ExchangeData result = new ExchangeData();
            result.setExchanges(exchanges);
            result.setPairs(pairs);
            Log.d(TAG, "deserialize: returning result");
            return result;
        } else {
            Log.d(TAG, "deserialize: returning null");
            return null;
        }
    }


    private String[] convertToStringArray(JsonArray jsonArray) {
        String[] stringArray = new String[jsonArray.size()];
        for (int i = 0, count = jsonArray.size(); i < count; i++)

            stringArray[i] = jsonArray.get(i).getAsString();

        return stringArray;


    }
}
