package com.example.lemoncream.myapplication.model.temp;

/**
 * Created by LemonCream on 2018-02-27.
 */

public class PriceParams {

    private int position;
    private String fsym;
    private String tsym;
    private String exchangeName;
    private long timestamp;


    public PriceParams(String fsym, String tsym, String exchangeName) {
        this.fsym = fsym;
        this.tsym = tsym;
        this.exchangeName = exchangeName;
    }
    public PriceParams(int position, String fsym, String tsym, String exchangeName) {
        this.position = position;
        this.fsym = fsym;
        this.tsym = tsym;
        this.exchangeName = exchangeName;
    }


    public PriceParams(String fsym, String tsym, String exchangeName, long timestamp) {
        this.fsym = fsym;
        this.tsym = tsym;
        this.exchangeName = exchangeName;
        this.timestamp = timestamp;
    }

    public PriceParams(int position, String fsym, String tsym, String exchangeName, long timestamp) {
        this.position = position;
        this.fsym = fsym;
        this.tsym = tsym;
        this.exchangeName = exchangeName;
        this.timestamp = timestamp;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
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

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}
