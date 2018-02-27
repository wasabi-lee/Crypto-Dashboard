package com.example.lemoncream.myapplication.Model.RealmModels;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by LemonCream on 2018-02-21.
 */

public class Bag extends RealmObject {

    @PrimaryKey
    private int _id;
    private Pair tradePair;
    private Date dateAdded;
    private float balance;
    private Portfolio portfolio;

    public Bag() {
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public Pair getTradePair() {
        return tradePair;
    }

    public void setTradePair(Pair tradePair) {
        this.tradePair = tradePair;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    public float getBalance() {
        return balance;
    }

    public void setBalance(float balance) {
        this.balance = balance;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(Portfolio portfolio) {
        this.portfolio = portfolio;
    }
}
