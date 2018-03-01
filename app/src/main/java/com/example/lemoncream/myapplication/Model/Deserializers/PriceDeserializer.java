package com.example.lemoncream.myapplication.Model.Deserializers;

import android.util.Log;

import com.example.lemoncream.myapplication.Model.GsonModels.PriceFull;
import com.example.lemoncream.myapplication.Model.GsonModels.PriceDetail;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by LemonCream on 2018-02-28.
 */

public class PriceDeserializer implements JsonDeserializer {

    private static final String TAG = PriceDeserializer.class.getSimpleName();

    @Override
    public Object deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json != null) {

            final JsonObject dataset = json.getAsJsonObject().getAsJsonObject("RAW");
            PriceFull result = new PriceFull();

            for (Map.Entry<String, JsonElement> outerEntry : dataset.entrySet()) {
                String key = outerEntry.getKey();
                result.setFsym(key);
                JsonObject priceObjects = outerEntry.getValue().getAsJsonObject();

                for (Map.Entry<String, JsonElement> innerEntry : priceObjects.entrySet()) {

                    JsonObject tsymInfo = innerEntry.getValue().getAsJsonObject();
                    PriceDetail detail = new PriceDetail();

                    detail.setExchange(tsymInfo.get("MARKET").getAsString());
                    detail.setFsym(tsymInfo.get("FROMSYMBOL").getAsString());
                    detail.setTsym(tsymInfo.get("TOSYMBOL").getAsString());
                    detail.setPrice(tsymInfo.get("PRICE").getAsFloat());
                    detail.setLastUpdated(tsymInfo.get("LASTUPDATE").getAsLong());
                    detail.setVolume24hr(tsymInfo.get("VOLUME24HOUR").getAsFloat());
                    detail.setVolumeTo24hr(tsymInfo.get("VOLUME24HOURTO").getAsFloat());
                    detail.setOpen24hr(tsymInfo.get("OPEN24HOUR").getAsFloat());
                    detail.setHigh24hr(tsymInfo.get("HIGH24HOUR").getAsFloat());
                    detail.setLow24hr(tsymInfo.get("LOW24HOUR").getAsFloat());
                    detail.setChange24hr(tsymInfo.get("CHANGE24HOUR").getAsFloat());
                    detail.setChangePercent24hr(tsymInfo.get("CHANGEPCT24HOUR").getAsFloat());

                    if (priceObjects.entrySet().size() == 1) {
                        result.setTsymPriceDetail(detail);
                        result.setBasePriceDetail(detail);
                        result.setBtcPriceDetail(detail);
                    } else {
                        if (detail.getTsym().equals("BTC")) {
                            result.setBtcPriceDetail(detail);
                        } else {
                            result.setBasePriceDetail(detail);
                        }
                    }
                }
            }

            return result;
        }
        Log.d(TAG, "deserialize: The result is null");
        return null;
    }
}

