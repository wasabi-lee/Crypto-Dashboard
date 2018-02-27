package com.example.lemoncream.myapplication.Model.TempModels;

import com.example.lemoncream.myapplication.Model.RealmModels.Bag;

import java.util.HashMap;

/**
 * Created by LemonCream on 2018-02-27.
 */

public class BagPriceData {
    private Bag bag;
    private HashMap<String, Float> currentPrice;
    private HashMap<String, Float> previousPrice;

    public BagPriceData(Bag bag, HashMap<String, Float> currentPrice, HashMap<String, Float> previousPrice) {
        this.bag = bag;
        this.currentPrice = currentPrice;
        this.previousPrice = previousPrice;
    }

    public Bag getBag() {
        return bag;
    }

    public void setBag(Bag bag) {
        this.bag = bag;
    }

    public HashMap<String, Float> getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(HashMap<String, Float> currentPrice) {
        this.currentPrice = currentPrice;
    }

    public HashMap<String, Float> getPreviousPrice() {
        return previousPrice;
    }

    public void setPreviousPrice(HashMap<String, Float> previousPrice) {
        this.previousPrice = previousPrice;
    }
}
