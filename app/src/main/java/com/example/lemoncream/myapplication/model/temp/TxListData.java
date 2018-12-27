package com.example.lemoncream.myapplication.model.temp;

import com.example.lemoncream.myapplication.model.realm.TxHistory;

/**
 * Created by LemonCream on 2018-03-02.
 */

public class TxListData {
    private TxHistory txHistory;
    private TxPriceData txPriceData;

    public TxListData() {
    }

    public TxListData(TxHistory txHistory, TxPriceData txPriceData) {
        this.txHistory = txHistory;
        this.txPriceData = txPriceData;
    }

    public TxHistory getTxHistory() {
        return txHistory;
    }

    public void setTxHistory(TxHistory txHistory) {
        this.txHistory = txHistory;
    }

    public TxPriceData getTxPriceData() {
        return txPriceData;
    }

    public void setTxPriceData(TxPriceData txPriceData) {
        this.txPriceData = txPriceData;
    }

}
