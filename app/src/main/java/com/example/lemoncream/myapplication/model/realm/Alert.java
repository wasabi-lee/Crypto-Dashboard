package com.example.lemoncream.myapplication.model.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Wasabi on 3/18/2018.
 */

public class Alert extends RealmObject {

    @PrimaryKey
    private int _id;
    private Bag bag;
    private Exchange exchange;
    private float lessThan;
    private float moreThan;
    private boolean isOneTime;
    private boolean isActive;

    public Alert(int _id, Bag bag, Exchange exchange, float lessThan, float moreThan, boolean isOneTime, boolean isActive) {
        this._id = _id;
        this.bag = bag;
        this.exchange = exchange;
        this.lessThan = lessThan;
        this.moreThan = moreThan;
        this.isOneTime = isOneTime;
        this.isActive = isActive;
    }

    public Alert() {
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public Bag getBag() {
        return bag;
    }

    public void setBag(Bag bag) {
        this.bag = bag;
    }

    public Exchange getExchange() {
        return exchange;
    }

    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }

    public float getLessThan() {
        return lessThan;
    }

    public void setLessThan(float lessThan) {
        this.lessThan = lessThan;
    }

    public float getMoreThan() {
        return moreThan;
    }

    public void setMoreThan(float moreThan) {
        this.moreThan = moreThan;
    }

    public boolean isOneTime() {
        return isOneTime;
    }

    public void setOneTime(boolean oneTime) {
        isOneTime = oneTime;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
