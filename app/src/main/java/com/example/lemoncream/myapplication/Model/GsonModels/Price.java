package com.example.lemoncream.myapplication.Model.GsonModels;

import java.util.HashMap;

/**
 * Created by LemonCream on 2018-02-19.
 */

public class Price {

    private String fsym;
    private HashMap<String, Float> prices;

    public Price() {
    }

    public Price(String fsym, HashMap<String, Float> prices) {
        this.fsym = fsym;
        this.prices = prices;
    }

    public String getFsym() {
        return fsym;
    }

    public void setFsym(String fsym) {
        this.fsym = fsym;
    }

    public HashMap<String, Float> getPrices() {
        return prices;
    }

    public void setPrices(HashMap<String, Float> prices) {
        this.prices = prices;
    }
}
