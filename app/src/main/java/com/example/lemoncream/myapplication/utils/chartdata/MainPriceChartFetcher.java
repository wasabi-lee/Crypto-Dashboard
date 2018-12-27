package com.example.lemoncream.myapplication.utils.chartdata;

import android.util.Log;

import com.example.lemoncream.myapplication.custom.PriceLineChart;
import com.example.lemoncream.myapplication.model.gson.ChartData;
import com.example.lemoncream.myapplication.model.realm.Bag;
import com.example.lemoncream.myapplication.model.realm.Pair;
import com.example.lemoncream.myapplication.model.realm.TxHistory;
import com.example.lemoncream.myapplication.model.temp.ChartParams;
import com.example.lemoncream.myapplication.network.ChartDataService;
import com.example.lemoncream.myapplication.network.RetrofitHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Wasabi on 3/31/2018.
 */

public class MainPriceChartFetcher {

    /**
     * Pulls chart data for MainActivity' header chart every 7 seconds.
     */

    private static final String TAG = MainPriceChartFetcher.class.getSimpleName();

    private Realm mRealm;
    private PriceLineChart mLineChart;
    private List<ChartParams> mChartParams;

    private int currentBagIndex = 0;
    private static final String BASE_URL = "https://min-api.cryptocompare.com/";
    private Disposable disposable;

    public MainPriceChartFetcher(Realm mRealm, PriceLineChart mLineChart) {
        this.mRealm = mRealm;
        this.mLineChart = mLineChart;
        this.mChartParams = createParams();
    }

    private ArrayList<ChartParams> createParams() {
        RealmResults<Bag> bags = mRealm.where(Bag.class).findAll();
        ArrayList<ChartParams> params = new ArrayList<>();
        for (Bag bag : bags) {
            String exchange = getLatestExchange(bag);
            Pair tradePair = bag.getTradePair();
            params.add(new ChartParams(exchange,
                    tradePair.getfCoin().getSymbol(),
                    tradePair.gettCoin().getSymbol()));
        }
        return params;
    }

    private String getLatestExchange(Bag bag) {
        TxHistory latestTx = mRealm.where(TxHistory.class)
                .equalTo("txHolder._id", bag.get_id())
                .sort("date", Sort.DESCENDING)
                .findFirst();
        if (latestTx != null && latestTx.getExchange() != null)
            return latestTx.getExchange().getName();
        return "CCCAGG";
    }

    public void startPriceChartRequestCycle() {
        if (mChartParams == null || mChartParams.size() == 0) {
            Log.d(TAG, "startPriceChartRequestCycle:  Null!");
            return;
        }
        Log.d(TAG, "startPriceChartRequestCycle: no null!!");
        ChartTimeframe timeframe = getTimeFrame();
        ChartDataService chartDataService = RetrofitHelper
                .createRetrofitWithRxConverter(BASE_URL)
                .create(ChartDataService.class);

        disposable = Observable.interval(0,7, TimeUnit.SECONDS)
                .flatMap(aLong -> Observable.zip(Observable.just(mChartParams.get(currentBagIndex)),
                        chartDataService.getChartData(timeframe.getChartUrlPath(),
                                mChartParams.get(currentBagIndex).getmFsym(),
                                mChartParams.get(currentBagIndex).getmTsym(),
                                timeframe.getAggregate(),
                                mChartParams.get(currentBagIndex).getmExchange(),
                                1440),
                        (BiFunction<ChartParams, ChartData, Object>) (chartParams, chartData) -> {
                            chartParams.setmChartData(chartData);
                            return chartParams;
                        }))
                .doOnError(Throwable::printStackTrace)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(chartParams -> {
                    ChartParams result = (ChartParams) chartParams;
                    mLineChart.setNewData(result);
                    if (currentBagIndex == (mChartParams.size() -1)) {
                        currentBagIndex = 0;
                    } else {
                        currentBagIndex ++;
                    }
                });

    }

    private ChartTimeframe getTimeFrame() {
        return ChartTimeframeConfig
                .getDefaultTimeframeSettings()
                .get(ChartTimeframeConfig.TIMEFRAME_ONE_DAY);
    }

    public void refresh() {
        this.mChartParams = createParams();
        startPriceChartRequestCycle();
    }

    public void cancelPriceChartRequestCycle() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
            currentBagIndex = 0;
        }
    }

}
