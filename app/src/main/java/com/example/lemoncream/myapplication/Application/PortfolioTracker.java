package com.example.lemoncream.myapplication.Application;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by LemonCream on 2018-02-03.
 */

public class PortfolioTracker extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().name("portfolio_tracker_db.realm").build();
        Realm.setDefaultConfiguration(realmConfiguration);
    }
}
