package com.example.lemoncream.myapplication.Model.TempModels;

import com.example.lemoncream.myapplication.Model.RealmModels.Bag;

/**
 * Created by LemonCream on 2018-02-27.
 */

public class BagPriceData {
    private Bag bag;
    private float currentPrice;
    private float previousPrice;

    public BagPriceData(Bag bag, float currentPrice, float previousPrice) {
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

    public float getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(float currentPrice) {
        this.currentPrice = currentPrice;
    }

    public float getPreviousPrice() {
        return previousPrice;
    }

    public void setPreviousPrice(float previousPrice) {
        this.previousPrice = previousPrice;
    }
}
