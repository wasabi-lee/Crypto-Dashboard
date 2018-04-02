package com.example.lemoncream.myapplication.Application;

import android.app.Application;

import com.example.lemoncream.myapplication.Model.RealmModels.Portfolio;
import com.example.lemoncream.myapplication.Utils.Database.RealmIdAutoIncrementHelper;

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
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .initialData(realm -> {
                    Portfolio mainPortfolio = new Portfolio();
                    mainPortfolio.set_id(0);
                    mainPortfolio.setName("MainPortfolio");
                    realm.copyToRealmOrUpdate(mainPortfolio);
                })
                .name("portfolio_tracker_db.realm").build();
        Realm.setDefaultConfiguration(realmConfiguration);
    }
}
