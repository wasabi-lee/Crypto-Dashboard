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

/**
 * Created by LemonCream on 2018-02-08.
 */

public class RealmHelper {

    private static final String TAG = RealmHelper.class.getSimpleName();

    public interface RealmTransactionListener {
        void onCoinListTransactionFinished(boolean result);

        void onPairTransactionFinished(boolean result);

        void onExchangeTransactionFinished(boolean result);
    }

    private RealmTransactionListener mCallback;
    private Context context;

    public RealmHelper(Context context) {
        this.context = context;
    }

    public void saveLatestCoinDataToRealm(Realm realm, final List<Coin> coinList) {
        // Save coins to DB
        realm.executeTransactionAsync(realm1 -> realm1.copyToRealmOrUpdate(coinList), () -> {
            mCallback = (RealmTransactionListener) context;
            mCallback.onCoinListTransactionFinished(true);
        }, error -> {
            mCallback = (RealmTransactionListener) context;
            mCallback.onCoinListTransactionFinished(false);
            Log.d(TAG, "onError: " + error.toString());
        });
    }

    public void syncPairsWithCoinData(Realm realm, final List<Pair> pairs) {
        mCallback = (RealmTransactionListener) context;

        realm.executeTransactionAsync(realm1 -> {
            for (int i = 0; i < pairs.size(); i++) {
                Pair currentPair = pairs.get(i);
                String[] pairNames = currentPair.getPairName().split("_");
                String fSym = pairNames[0];
                String tSym = pairNames[1];

                RealmResults<Coin> results = realm1.where(Coin.class)
                        .equalTo("symbol", fSym)
                        .or()
                        .equalTo("symbol", tSym)
                        .findAll();

                if (results.size() == 0) {
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
            realm1.copyToRealmOrUpdate(pairs);
        }, () -> {
            mCallback.onPairTransactionFinished(true);
        }, error -> {
            error.printStackTrace();
            mCallback.onPairTransactionFinished(true);
        });

    }

    public void saveLatestPairDataToRealm(Realm realm, final ExchangeData exchangeData) {
        realm.executeTransactionAsync(realm1 -> {
            List<Exchange> exchanges = exchangeData.getExchanges();
            realm1.copyToRealmOrUpdate(exchanges);
        }, () -> {
            mCallback = (RealmTransactionListener) context;
            mCallback.onExchangeTransactionFinished(true);
        }, error -> {
            error.printStackTrace();
            mCallback = (RealmTransactionListener) context;
            mCallback.onExchangeTransactionFinished(false);
        });
    }


}
