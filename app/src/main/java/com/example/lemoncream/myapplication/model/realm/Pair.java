package com.example.lemoncream.myapplication.model.realm;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by LemonCream on 2018-02-03.
 */

public class Pair extends RealmObject {
    @PrimaryKey
    private String pairName;
    private Coin fCoin;
    private Coin tCoin;
    private RealmList<Exchange> exchanges;

    public Pair() {
    }

    public Pair(String pairName) {
        this.pairName = pairName;
    }

    public Pair(Coin fCoin, Coin tCoin) {
        this.pairName = fCoin.getName() + "_" + tCoin.getName();
        this.fCoin = fCoin;
        this.tCoin = tCoin;
    }

    public String getPairName() {
        return pairName;
    }

    public void setPairName(String pairName) {
        this.pairName = pairName;
    }

    public Coin getfCoin() {
        return fCoin;
    }

    public void setfCoin(Coin fCoin) {
        this.fCoin = fCoin;
    }

    public Coin gettCoin() {
        return tCoin;
    }

    public void settCoin(Coin tCoin) {
        this.tCoin = tCoin;
    }

    public RealmList<Exchange> getExchanges() {
        return exchanges;
    }

    public void setExchanges(RealmList<Exchange> exchanges) {
        this.exchanges = exchanges;
    }
}
