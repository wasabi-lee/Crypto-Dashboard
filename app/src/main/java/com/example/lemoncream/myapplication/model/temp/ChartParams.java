package com.example.lemoncream.myapplication.model.temp;

import com.example.lemoncream.myapplication.model.gson.ChartData;

/**
 * Created by Wasabi on 3/31/2018.
 */

public class ChartParams {
    private String mExchange;
    private String mFsym;
    private String mTsym;
    private ChartData mChartData;

    public ChartParams() {
    }

    public ChartParams(String mExchange, String mFsym, String mTsym) {
        this.mExchange = mExchange;
        this.mFsym = mFsym;
        this.mTsym = mTsym;
    }

    public String getmExchange() {
        return mExchange;
    }

    public void setmExchange(String mExchange) {
        this.mExchange = mExchange;
    }

    public String getmFsym() {
        return mFsym;
    }

    public void setmFsym(String mFsym) {
        this.mFsym = mFsym;
    }

    public String getmTsym() {
        return mTsym;
    }

    public void setmTsym(String mTsym) {
        this.mTsym = mTsym;
    }

    public ChartData getmChartData() {
        return mChartData;
    }

    public void setmChartData(ChartData mChartData) {
        this.mChartData = mChartData;
    }
}
