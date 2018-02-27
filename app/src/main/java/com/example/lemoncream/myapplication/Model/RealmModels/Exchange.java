package com.example.lemoncream.myapplication.Model.RealmModels;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.PrimaryKey;

/**
 * Created by LemonCream on 2018-02-03.
 */

public class Exchange extends RealmObject {

    @PrimaryKey
    private String name;
    @LinkingObjects("exchanges")
    private final RealmResults<Pair> pairs = null;

    public Exchange() {
    }

    public Exchange(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RealmResults<Pair> getPairs() {
        return pairs;
    }
}
