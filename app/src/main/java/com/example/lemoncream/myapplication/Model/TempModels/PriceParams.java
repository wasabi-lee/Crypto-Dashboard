package com.example.lemoncream.myapplication.Model.TempModels;

/**
 * Created by LemonCream on 2018-02-27.
 */

public class PriceParams {

    private String fsym;
    private String tsym;
    private String exchangeName;

    public PriceParams(String fsym, String tsym, String exchangeName) {
        this.fsym = fsym;
        this.tsym = tsym;
        this.exchangeName = exchangeName;
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

}
