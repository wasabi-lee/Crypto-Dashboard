package com.example.lemoncream.myapplication.Model.TempModels;

import com.example.lemoncream.myapplication.Model.GsonModels.PriceDetail;
import com.example.lemoncream.myapplication.Model.RealmModels.Bag;

import java.util.HashMap;

/**
 * Created by LemonCream on 2018-02-27.
 */

public class BagPriceData {
    private Bag bag;
    private PriceDetail tsymPriceDetail;
    private PriceDetail basePriceDetail;
    private PriceDetail btcPriceDetail;

    public BagPriceData() {
    }

    public BagPriceData(Bag bag, PriceDetail tsymPriceDetail) {
        this.bag = bag;
        this.tsymPriceDetail = tsymPriceDetail;
    }

    public Bag getBag() {
        return bag;
    }

    public void setBag(Bag bag) {
        this.bag = bag;
    }

    public PriceDetail getTsymPriceDetail() {
        return tsymPriceDetail;
    }

    public void setTsymPriceDetail(PriceDetail tsymPriceDetail) {
        this.tsymPriceDetail = tsymPriceDetail;
    }

    public PriceDetail getBasePriceDetail() {
        return basePriceDetail;
    }

    public void setBasePriceDetail(PriceDetail basePriceDetail) {
        this.basePriceDetail = basePriceDetail;
    }

    public PriceDetail getBtcPriceDetail() {
        return btcPriceDetail;
    }

    public void setBtcPriceDetail(PriceDetail btcPriceDetail) {
        this.btcPriceDetail = btcPriceDetail;
    }
}
