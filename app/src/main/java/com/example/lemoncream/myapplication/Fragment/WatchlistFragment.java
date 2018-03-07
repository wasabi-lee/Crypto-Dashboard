package com.example.lemoncream.myapplication.Fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.lemoncream.myapplication.Adapter.BagListAdapter;
import com.example.lemoncream.myapplication.Model.Deserializers.PriceDeserializer;
import com.example.lemoncream.myapplication.Model.GsonModels.PriceFull;
import com.example.lemoncream.myapplication.Model.RealmModels.Bag;
import com.example.lemoncream.myapplication.Model.RealmModels.TxHistory;
import com.example.lemoncream.myapplication.Model.TempModels.BagPriceData;
import com.example.lemoncream.myapplication.Model.TempModels.PriceParams;
import com.example.lemoncream.myapplication.Network.GsonHelper;
import com.example.lemoncream.myapplication.Network.PriceService;
import com.example.lemoncream.myapplication.Network.RetrofitHelper;
import com.example.lemoncream.myapplication.R;
import com.example.lemoncream.myapplication.Utils.Callbacks.OnTotalValueChangedListener;
import com.example.lemoncream.myapplication.Utils.Formatters.SignSwitcher;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class WatchlistFragment extends Fragment {

    private static final String TAG = WatchlistFragment.class.getSimpleName();

    private boolean mFirstRun;

    @BindView(R.id.watchlist_frag_progress_bar)
    ProgressBar mProgressBar;
    @BindView(R.id.watchlist_frag_recycler_view)
    RecyclerView mWatchlistRecyclerView;

    private ArrayList<BagPriceData> mDataset;
    private BagListAdapter mAdapter;

    Realm mRealm;

    public WatchlistFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_watchlist, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRealm = Realm.getDefaultInstance();

        loadBagData();
        requestPriceData(createPriceApiCallParams());

    }

    private ArrayList<BagPriceData> loadBagData() {
        mProgressBar.setVisibility(View.VISIBLE);
        return createDataset(mRealm.where(Bag.class).equalTo("watchOnly", true).findAll());
    }

    private ArrayList<BagPriceData> createDataset(RealmResults<Bag> bags) {
        // Using LinkedHashMap to keep the order of elements due to the complexity of the API response design.
        if (bags.size() == 0) {
            mProgressBar.setVisibility(View.GONE);
            return null;
        }
        ArrayList<BagPriceData> dataset = new ArrayList<>();
        for (Bag bag : bags) {
            if (bag == null || bag.getTradePair() == null) continue;
//            String pairName = bag.getTradePair().getPairName();
            dataset.add(new BagPriceData(bag, null));
        }
        mDataset = dataset;
        return dataset;
    }


    private void populateBagList(ArrayList<BagPriceData> dataset) {
        if (dataset == null) {
            // TODO Show error message
        } else if (dataset.size() == 0) {
            // TODO Show 'add first transaction' message
        } else {
            // Passing only values of the Map as ArrayList to fetch each element easier
            mAdapter = new BagListAdapter(getContext(), mDataset);
            mWatchlistRecyclerView.setHasFixedSize(true);
            mWatchlistRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
            mWatchlistRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mWatchlistRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            mWatchlistRecyclerView.setAdapter(mAdapter);
        }
    }

    private void requestPriceData(ArrayList<PriceParams> params) {
        if (mDataset == null || mDataset.size() == 0) {
            mProgressBar.setVisibility(View.GONE);
            return;
        }
        if (params == null || params.size() == 0) {
            mProgressBar.setVisibility(View.GONE);
            return;
        }

        Retrofit retrofitTsym = RetrofitHelper
                .createRetrofitWithRxConverter(getResources().getString(R.string.base_url),
                        GsonHelper.createGsonBuilder(PriceFull.class, new PriceDeserializer()).create());
        PriceService currentPriceService = retrofitTsym.create(PriceService.class);

        Observable.fromIterable(params)
                .flatMap((Function<PriceParams, ObservableSource<?>>) priceParams ->
                        Observable.zip(Observable.just(priceParams),
                                currentPriceService.getMultipleCurrentPrices(priceParams.getFsym(),
                                        priceParams.getTsym(),
                                        priceParams.getExchangeName()),
                                currentPriceService.getMultipleCurrentPrices(priceParams.getFsym(),
                                        SignSwitcher.BASE_CURRENCY.toUpperCase() + ",BTC"),
                                (param, tsymPrice, baseBtcPrice) -> {
                                    tsymPrice.setPosition(param.getPosition());
                                    tsymPrice.setBasePriceDetail(baseBtcPrice.getBasePriceDetail());
                                    tsymPrice.setBtcPriceDetail(baseBtcPrice.getBtcPriceDetail());
                                    return tsymPrice;
                                }))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(priceResponse -> {
                            PriceFull priceFull = (PriceFull) priceResponse;
                            BagPriceData dataToFix = mDataset.get(priceFull.getPosition());
                            dataToFix.setTsymPriceDetail(priceFull.getTsymPriceDetail());
                            dataToFix.setBasePriceDetail(priceFull.getBasePriceDetail());
                            dataToFix.setBtcPriceDetail(priceFull.getBtcPriceDetail());
                        }, Throwable::printStackTrace,
                        () -> {
                            mProgressBar.setVisibility(View.GONE);
                            mAdapter.setData(mDataset);
                            mAdapter.notifyDataSetChanged();
                        }
                );
    }

    private ArrayList<PriceParams> createPriceApiCallParams() {
        if (mDataset == null || mDataset.size() == 0) return null;

        ArrayList<PriceParams> params = new ArrayList<>();
        for (int i = 0; i < mDataset.size(); i++) {
            BagPriceData data = mDataset.get(i);
            if (data == null) continue;
            String fsym = data.getBag().getTradePair().getfCoin().getSymbol();
            String tsyms = data.getBag().getTradePair().gettCoin().getSymbol(); //TODO Fix this to Base currency later
            TxHistory exchange = mRealm.where(TxHistory.class)
                    .equalTo("txHolder.tradePair.pairName", data.getBag().getTradePair().getPairName())
                    .sort("date", Sort.DESCENDING)
                    .findFirst();
            String exchangeName = exchange == null ? "CCCAGG" : exchange.getExchange().getName();

            params.add(new PriceParams(i, fsym, tsyms, exchangeName));
        }
        return params;
    }

    public void changeDisplayCurrency(boolean baseCurrencyDisplayMode) {
        mAdapter.setBaseCurrencyDisplayMode(baseCurrencyDisplayMode);
        mAdapter.notifyDataSetChanged();
    }

    public void changeDisplayChangeUnit(boolean pctChangeDisplayMode) {
        mAdapter.setPctChangeDisplayMode(pctChangeDisplayMode);
        mAdapter.notifyDataSetChanged();
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mFirstRun) {
            mFirstRun = false;
        } else {
            if (mAdapter == null) {
                populateBagList(loadBagData());
            } else {
                loadBagData();
            }
            requestPriceData(createPriceApiCallParams());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }
}
