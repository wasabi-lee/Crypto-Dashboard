package com.example.lemoncream.myapplication.Model.RealmModels;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.PrimaryKey;

/**
 * Created by LemonCream on 2018-02-21.
 */

public class Portfolio extends RealmObject {

    @PrimaryKey
    int _id;
    String name;

    public Portfolio() {
    }

    public Portfolio(int _id, String name) {
        this._id = _id;
        this.name = name;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    // Overriding toString() to let the Spinner ArrayAdapter read the name for its dropdown list value
    @Override
    public String toString() {
        return name;
    }
}
