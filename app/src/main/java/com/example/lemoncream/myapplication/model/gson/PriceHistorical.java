package com.example.lemoncream.myapplication.model.gson;

/**
 * Created by LemonCream on 2018-03-02.
 */

public class PriceHistorical {

    private String tsym;
    private float price;

    public PriceHistorical() {
    }

    public PriceHistorical(String tsym, float price) {
        this.tsym = tsym;
        this.price = price;
    }

    public String getTsym() {
        return tsym;
    }

    public void setTsym(String tsym) {
        this.tsym = tsym;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }
}
