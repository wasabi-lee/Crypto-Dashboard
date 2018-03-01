package com.example.lemoncream.myapplication.Model.TempModels;

import com.example.lemoncream.myapplication.Model.RealmModels.TxHistory;

/**
 * Created by LemonCream on 2018-03-02.
 */

public class TxPriceData {

    private TxHistory txHistory;
    private float currentPrice;
    private float previousPrice;
    private float currentBasePrice;
    private float previousBasePrice;

    public TxPriceData() {
    }

    public TxPriceData(TxHistory txHistory, float currentPrice, float previousPrice, float currentBasePrice, float previousBasePrice) {
        this.txHistory = txHistory;
        this.currentPrice = currentPrice;
        this.previousPrice = previousPrice;
        this.currentBasePrice = currentBasePrice;
        this.previousBasePrice = previousBasePrice;
    }

    public TxHistory getTxHistory() {
        return txHistory;
    }

    public void setTxHistory(TxHistory txHistory) {
        this.txHistory = txHistory;
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
