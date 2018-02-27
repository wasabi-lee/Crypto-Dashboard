package com.example.lemoncream.myapplication.Model.GsonModels;

import com.example.lemoncream.myapplication.Model.RealmModels.Exchange;
import com.example.lemoncream.myapplication.Model.RealmModels.Pair;

import java.util.List;

/**
 * Created by LemonCream on 2018-02-08.
 */

public class ExchangeData {
    private List<Exchange> exchanges;
    private List<Pair> pairs;

    public ExchangeData() {
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
