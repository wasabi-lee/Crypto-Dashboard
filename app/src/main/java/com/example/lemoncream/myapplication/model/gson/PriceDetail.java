package com.example.lemoncream.myapplication.model.gson;

/**
 * Created by LemonCream on 2018-02-28.
 */

public class PriceDetail {

    private String exchange;
    private String fsym;
    private String tsym;
    private float price;
    private long lastUpdated;
    private float volume24hr;
    private float volumeTo24hr;
    private float open24hr;
    private float high24hr;
    private float low24hr;
    private float change24hr;
    private float changePercent24hr;
    private float baseCurrencyPrice;
    private float btcCoversionPrice;

    public PriceDetail() {
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
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

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public float getVolume24hr() {
        return volume24hr;
    }

    public void setVolume24hr(float volume24hr) {
        this.volume24hr = volume24hr;
    }

    public float getVolumeTo24hr() {
        return volumeTo24hr;
    }

    public void setVolumeTo24hr(float volumeTo24hr) {
        this.volumeTo24hr = volumeTo24hr;
    }

    public float getOpen24hr() {
        return open24hr;
    }

    public void setOpen24hr(float open24hr) {
        this.open24hr = open24hr;
    }

    public float getHigh24hr() {
        return high24hr;
    }

    public void setHigh24hr(float high24hr) {
        this.high24hr = high24hr;
    }

    public float getLow24hr() {
        return low24hr;
    }

    public void setLow24hr(float low24hr) {
        this.low24hr = low24hr;
    }

    public float getChange24hr() {
        return change24hr;
    }

    public void setChange24hr(float change24hr) {
        this.change24hr = change24hr;
    }

    public float getChangePercent24hr() {
        return changePercent24hr;
    }

    public void setChangePercent24hr(float changePercent24hr) {
        this.changePercent24hr = changePercent24hr;
    }

    public float getBaseCurrencyPrice() {
        return baseCurrencyPrice;
    }

    public void setBaseCurrencyPrice(float baseCurrencyPrice) {
        this.baseCurrencyPrice = baseCurrencyPrice;
    }

    public float getBtcCoversionPrice() {
        return btcCoversionPrice;
    }

    public void setBtcCoversionPrice(float btcCoversionPrice) {
        this.btcCoversionPrice = btcCoversionPrice;
    }
}
