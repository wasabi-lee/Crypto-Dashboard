package com.example.lemoncream.myapplication.model.temp;

/**
 * Created by LemonCream on 2018-03-03.
 */

public class TxPriceData {

    int position;
    private float currentPrice = -1;
    private float currentBasePrice = -1;
    private float previousBasePrice = -1;

    public TxPriceData() {
    }

    public TxPriceData(int position) {
        this.position = position;
    }

    public TxPriceData(int position, float currentPrice, float currentBasePrice, float previousBasePrice) {
        this.position = position;
        this.currentPrice = currentPrice;
        this.currentBasePrice = currentBasePrice;
        this.previousBasePrice = previousBasePrice;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public float getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(float currentPrice) {
        this.currentPrice = currentPrice;
    }

    public float getCurrentBasePrice() {
        return currentBasePrice;
    }

    public void setCurrentBasePrice(float currentBasePrice) {
        this.currentBasePrice = currentBasePrice;
    }

    public float getPreviousBasePrice() {
        return previousBasePrice;
    }

    public void setPreviousBasePrice(float previousBasePrice) {
        this.previousBasePrice = previousBasePrice;
    }
}
