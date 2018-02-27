package com.example.lemoncream.myapplication.Model.GsonModels;

import java.util.HashMap;

/**
 * Created by LemonCream on 2018-02-27.
 */

public class PriceCurrent {

    private HashMap<String, Float> prices;

    public PriceCurrent() {
    }

    public PriceCurrent(String fsym, HashMap<String, Float> prices) {
        this.prices = prices;
    }

    public HashMap<String, Float> getPrices() {
        return prices;
    }

    public void setPrices(HashMap<String, Float> prices) {
        this.prices = prices;
    }


}
