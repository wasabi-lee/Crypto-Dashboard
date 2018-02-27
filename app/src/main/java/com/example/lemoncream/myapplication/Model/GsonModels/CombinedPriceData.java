package com.example.lemoncream.myapplication.Model.GsonModels;

import java.util.HashMap;

/**
 * Created by LemonCream on 2018-02-27.
 */

public class CombinedPriceData {

    private String fsym;
    private HashMap<String, Float> previousPrices;
    private HashMap<String, Float> currentPrices;

    public CombinedPriceData() {
    }

    public CombinedPriceData(String fsym, HashMap<String, Float> previousPrices, HashMap<String, Float> currentPrices) {
        this.fsym = fsym;
        this.previousPrices = previousPrices;
        this.currentPrices = currentPrices;
    }

    public String getFsym() {
        return fsym;
    }

    public void setFsym(String fsym) {
        this.fsym = fsym;
    }

    public HashMap<String, Float> getPreviousPrices() {
        return previousPrices;
    }

    public void setPreviousPrices(HashMap<String, Float> previousPrices) {
        this.previousPrices = previousPrices;
    }

    public HashMap<String, Float> getCurrentPrices() {
        return currentPrices;
    }

    public void setCurrentPrices(HashMap<String, Float> currentPrices) {
        this.currentPrices = currentPrices;
    }
}
