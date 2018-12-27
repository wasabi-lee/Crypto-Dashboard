package com.example.lemoncream.myapplication.model.gson;

import com.example.lemoncream.myapplication.model.realm.Coin;

import java.util.List;

/**
 * Created by LemonCream on 2018-02-13.
 */

public class CoinData {

    private List<Coin> coinList;

    public CoinData() {
    }

    public List<Coin> getCoinList() {
        return coinList;
    }

    public void setCoinList(List<Coin> coinList) {
        this.coinList = coinList;
    }
}
