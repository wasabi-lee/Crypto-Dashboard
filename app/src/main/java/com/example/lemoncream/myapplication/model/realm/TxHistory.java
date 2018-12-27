package com.example.lemoncream.myapplication.model.realm;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by LemonCream on 2018-02-21.
 */

public class TxHistory extends RealmObject {

    @PrimaryKey
    private int _id;
    private String orderType;
    private Bag txHolder;
    private float amount;
    private float tradePrice;
    private Exchange exchange;
    private Date date;
    private boolean deductFromAnotherBag;
    private Pair decutedPair;
    private String note;

    public static final String ORDER_TYPE_BUY = "order_buy";
    public static final String ORDER_TYPE_SELL = "order_sell";
    public static final String ORDER_TYPE_WATCH = "order_watch";

    public TxHistory() {
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public Bag getTxHolder() {
        return txHolder;
    }

    public void setTxHolder(Bag txHolder) {
        this.txHolder = txHolder;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public float getTradePrice() {
        return tradePrice;
    }

    public void setTradePrice(float tradePrice) {
        this.tradePrice = tradePrice;
    }

    public Exchange getExchange() {
        return exchange;
    }

    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isDeductFromAnotherBag() {
        return deductFromAnotherBag;
    }

    public void setDeductFromAnotherBag(boolean deductFromAnotherBag) {
        this.deductFromAnotherBag = deductFromAnotherBag;
    }

    public Pair getDecutedPair() {
        return decutedPair;
    }

    public void setDecutedPair(Pair decutedPair) {
        this.decutedPair = decutedPair;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

}
