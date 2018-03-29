package com.example.lemoncream.myapplication.Utils.Database;

import android.content.Context;
import android.util.Log;

import com.example.lemoncream.myapplication.Model.GsonModels.ExchangeData;
import com.example.lemoncream.myapplication.Model.RealmModels.Coin;
import com.example.lemoncream.myapplication.Model.RealmModels.Exchange;
import com.example.lemoncream.myapplication.Model.RealmModels.Pair;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.exceptions.RealmException;

/**
 * Created by LemonCream on 2018-02-08.
 */

public class RealmHelper {

    private static final String TAG = RealmHelper.class.getSimpleName();

    public interface RealmTransactionListener {
        void onExchangeTransactionFinished(boolean result);
    }

    private RealmTransactionListener mCallback;
    private Context context;

    public RealmHelper(Context context) {
        this.context = context;
    }

    public void saveLatestCoinDataToRealm(Realm realm, final List<Coin> coinList) {
        // Save coins to DB
        realm.executeTransaction(realm1 -> realm1.copyToRealmOrUpdate(coinList));
        realm.close();
    }

    public void syncPairsWithCoinData(Realm realm, final List<Pair> pairs) {
        try {
            realm.executeTransaction(realm1 -> {
                for (int i = 0; i < pairs.size(); i++) {
                    Pair currentPair = pairs.get(i);
                    String[] pairNames = currentPair.getPairName().split("_");
                    String fSym = pairNames[0];
                    String tSym = pairNames[1];

                    RealmResults<Coin> results = null;
                    try {
                        results = realm.where(Coin.class)
                                .equalTo("symbol", fSym)
                                .or()
                                .equalTo("symbol", tSym)
                                .findAll();
                    } catch (RealmException e) {
                        e.printStackTrace();
                    }

                    if (results == null || results.size() == 0) {
                        currentPair.setfCoin(new Coin(fSym, fSym, null));
                        currentPair.settCoin(new Coin(tSym, tSym, null));
                    } else if (results.size() == 1) {
                        if (results.get(0).getSymbol().equals(fSym)) {
                            currentPair.setfCoin(results.get(0));
                            currentPair.settCoin(new Coin(tSym, tSym, null));
                        } else {
                            currentPair.setfCoin(new Coin(fSym, fSym, null));
                            currentPair.settCoin(results.get(0));
                        }
                    } else {
                        if (results.get(0).getSymbol().equals(pairNames[0])) {
                            currentPair.setfCoin(results.get(0));
                            currentPair.settCoin(results.get(1));
                        } else {
                            currentPair.setfCoin(results.get(1));
                            currentPair.settCoin(results.get(0));
                        }
                        pairs.set(i, currentPair);
                    }
                }
                realm.copyToRealmOrUpdate(pairs);
            });
        } catch (RealmException e) {
            e.printStackTrace();
        } finally {
            realm.close();
        }

    }


    public void saveLatestPairDataToRealm(Realm realm, final List<Exchange> exchanges) {
        realm.executeTransactionAsync(realm1 -> realm1.copyToRealmOrUpdate(exchanges), () -> {
            mCallback = (RealmTransactionListener) context;
            mCallback.onExchangeTransactionFinished(true);
            realm.close();
        }, error -> {
            error.printStackTrace();
            mCallback = (RealmTransactionListener) context;
            mCallback.onExchangeTransactionFinished(false);
            realm.close();
        });
    }


}
