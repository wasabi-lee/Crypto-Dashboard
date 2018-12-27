package com.example.lemoncream.myapplication.model.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by LemonCream on 2018-02-27.
 */

public class Datum {
    @SerializedName("time")
    public long time;
    @SerializedName("close")
    public Float close;
    @SerializedName("high")
    public Float high;
    @SerializedName("low")
    public Float low;
    @SerializedName("open")
    public Float open;
    @SerializedName("volumefrom")
    public Float volumefrom;
    @SerializedName("volumeto")
    public Float volumeto;

    public Datum() {
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Float getClose() {
        return close;
    }

    public void setClose(Float close) {
        this.close = close;
    }

    public Float getHigh() {
        return high;
    }

    public void setHigh(Float high) {
        this.high = high;
    }

    public Float getLow() {
        return low;
    }

    public void setLow(Float low) {
        this.low = low;
    }

    public Float getOpen() {
        return open;
    }

    public void setOpen(Float open) {
        this.open = open;
    }

    public Float getVolumefrom() {
        return volumefrom;
    }

    public void setVolumefrom(Float volumefrom) {
        this.volumefrom = volumefrom;
    }

    public Float getVolumeto() {
        return volumeto;
    }

    public void setVolumeto(Float volumeto) {
        this.volumeto = volumeto;
    }
}
