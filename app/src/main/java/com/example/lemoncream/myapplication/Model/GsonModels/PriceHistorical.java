package com.example.lemoncream.myapplication.Model.GsonModels;

import java.util.HashMap;

/**
 * Created by LemonCream on 2018-02-19.
 */

public class PriceHistorical {

    private String fsym;
    private String tsym;
    private HashMap<String, Float> prices;

    public PriceHistorical() {
    }

    public PriceHistorical(String fsym, HashMap<String, Float> prices) {
        this.fsym = fsym;
        this.prices = prices;
    }

    public String getFsym() {
        return fsym;
    }

    public void setFsym(String fsym) {
        this.fsym = fsym;
    }

    public String getTsym() {
        return tsym;
    }

    public void setTsym(String tsym) {
        this.tsym = tsym;
    }

    public HashMap<String, Float> getPrices() {
        return prices;
    }

    public void setPrices(HashMap<String, Float> prices) {
        this.prices = prices;
    }
}
