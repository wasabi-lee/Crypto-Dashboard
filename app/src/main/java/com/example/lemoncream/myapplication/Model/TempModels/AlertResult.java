package com.example.lemoncream.myapplication.Model.TempModels;

import com.example.lemoncream.myapplication.Model.RealmModels.Alert;

/**
 * Created by Wasabi on 3/26/2018.
 */

public class AlertResult {
    private int alertId;
    private String fsym;
    private String tsym;
    private String exchange;
    private float price;

    public AlertResult() {
    }

    public AlertResult(int alertId, String fsym, String tsym, String exchange) {
        this.alertId = alertId;
        this.fsym = fsym;
        this.tsym = tsym;
        this.exchange = exchange;
    }

    public AlertResult(int alertId, String fsym, String tsym, String exchange, float price) {
        this.alertId = alertId;
        this.fsym = fsym;
        this.tsym = tsym;
        this.exchange = exchange;
        this.price = price;
    }

    public int getAlertId() {
        return alertId;
    }

    public void setAlertId(int alertId) {
        this.alertId = alertId;
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

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }
}
