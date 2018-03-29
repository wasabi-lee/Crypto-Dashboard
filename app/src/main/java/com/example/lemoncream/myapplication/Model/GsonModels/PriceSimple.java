package com.example.lemoncream.myapplication.Model.GsonModels;

/**
 * Created by Wasabi on 3/26/2018.
 */

public class PriceSimple {
    private float price;

    public PriceSimple(float price) {
        this.price = price;
    }

    public PriceSimple() {
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public float getPrice() {
        return price;
    }
}