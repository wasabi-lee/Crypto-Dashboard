package com.example.lemoncream.myapplication.model.gson;

import com.example.lemoncream.myapplication.model.realm.Exchange;
import com.example.lemoncream.myapplication.model.realm.Pair;

import java.util.List;

/**
 * Created by LemonCream on 2018-02-08.
 */

public class ExchangeData {
    private List<Exchange> exchanges;
    private List<Pair> pairs;

    public ExchangeData() {
    }

    public ExchangeData(List<Exchange> exchanges, List<Pair> pairs) {
        this.exchanges = exchanges;
        this.pairs = pairs;
    }

    public List<Exchange> getExchanges() {
        return exchanges;
    }

    public void setExchanges(List<Exchange> exchanges) {
        this.exchanges = exchanges;
    }

    public List<Pair> getPairs() {
        return pairs;
    }

    public void setPairs(List<Pair> pairs) {
        this.pairs = pairs;
    }
}
