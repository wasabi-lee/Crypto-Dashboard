package com.example.lemoncream.myapplication.Model.RealmModels;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by LemonCream on 2018-02-09.
 */

public class Coin extends RealmObject {

    @PrimaryKey
    private String name;
    private String symbol;
    private String imageUrl;

    public Coin() {
    }

    public Coin(String name, String symbol, String imageUrl) {
        this.name = name;
        this.symbol = symbol;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
