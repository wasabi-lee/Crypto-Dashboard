package com.example.lemoncream.myapplication.Fragment;


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
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.example.lemoncream.myapplication.Adapter.BagListAdapter;
import com.example.lemoncream.myapplication.Model.Deserializers.PriceCurrentDeserializer;
import com.example.lemoncream.myapplication.Model.Deserializers.PriceHistoricalDeserializer;
import com.example.lemoncream.myapplication.Model.GsonModels.CombinedPriceData;
import com.example.lemoncream.myapplication.Model.GsonModels.PriceCurrent;
import com.example.lemoncream.myapplication.Model.GsonModels.PriceHistorical;
import com.example.lemoncream.myapplication.Model.RealmModels.TxHistory;
import com.example.lemoncream.myapplication.Model.TempModels.BagPriceData;
import com.example.lemoncream.myapplication.Model.TempModels.PriceParams;
import com.example.lemoncream.myapplication.Model.RealmModels.Bag;
import com.example.lemoncream.myapplication.Network.GsonHelper;
import com.example.lemoncream.myapplication.Network.PriceService;
import com.example.lemoncream.myapplication.Network.RetrofitHelper;
import com.example.lemoncream.myapplication.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmResults;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class PortfolioFragment extends Fragment {

    private static final String TAG = PortfolioFragment.class.getSimpleName();
    private boolean mFirstRun = true;

    @BindView(R.id.portfolio_frag_add_new_container)
    LinearLayout mAddNewLayout;
    @BindView(R.id.portfolio_frag_recycler_view)
    RecyclerView mBagRecyclerView;

    private LinkedHashMap<String, BagPriceData> mDataset;

    private ProgressBar mProgressBar;
    private BagListAdapter mAdapter;
    private Realm mRealm;


    public PortfolioFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        View view = inflater.inflate(R.layout.fragment_portfolio, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: ");
        mRealm = Realm.getDefaultInstance();
        setupProgressBar();
        populateBagList(loadBagData());
        requestPriceData(createPriceApiCallParams());

    }

    private void setupProgressBar() {
        mProgressBar = new ProgressBar(getContext());
        mProgressBar.setIndeterminate(true);
    }

    private LinkedHashMap<String, BagPriceData> loadBagData() {
        mProgressBar.setVisibility(View.VISIBLE);
        return createDataset(mRealm.where(Bag.class).findAll());
    }

    private LinkedHashMap<String, BagPriceData> createDataset(RealmResults<Bag> bags) {
        // Using LinkedHashMap to keep the order of elements due to the complexity of the API response design.
        LinkedHashMap<String, BagPriceData> dataset = new LinkedHashMap<>();
        for (Bag bag : bags) {
            if (bag == null || bag.getTradePair() == null) continue;
            String pairName = bag.getTradePair().getPairName();
            dataset.put(pairName, new BagPriceData(bag, null, null));
        }
        mDataset = dataset;
        return dataset;
    }


    private void populateBagList(LinkedHashMap<String, BagPriceData> dataset) {
        if (dataset == null) {
            // TODO Show error message
        } else if (dataset.size() == 0) {
            // TODO Show 'add first transaction' message
            mAddNewLayout.setVisibility(View.VISIBLE);
        } else {
            // Passing only values of the Map as ArrayList to fetch each element easier
            mAdapter = new BagListAdapter(getContext(), new ArrayList<>(dataset.values()));
            mBagRecyclerView.setHasFixedSize(true);
            mBagRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
            mBagRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mBagRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            mBagRecyclerView.setAdapter(mAdapter);
        }
    }

    private void requestPriceData(ArrayList<PriceParams> params) {
        if (params == null) return;
        Retrofit retrofitCurrent = RetrofitHelper
                .createRetrofitWithRxConverter(getResources().getString(R.string.base_url),
                        GsonHelper.createGsonBuilder(PriceCurrent.class, new PriceCurrentDeserializer()).create());
        Retrofit retrofitPrev = RetrofitHelper
                .createRetrofitWithRxConverter(getResources().getString(R.string.base_url),
                        GsonHelper.createGsonBuilder(PriceHistorical.class, new PriceHistoricalDeserializer()).create());
        Observable.fromIterable(params)
                .flatMap(priceParams -> {
                    PriceService coinListServiceCurrent = retrofitCurrent.create(PriceService.class);
                    PriceService coinListServicePrev = retrofitPrev.create(PriceService.class);
                    Observable<PriceCurrent> priceRequest = coinListServiceCurrent
                            .getCurrentPrice(priceParams.getFsym(), priceParams.getTsym(),
                                    priceParams.getExchangeName());
                    Observable<PriceHistorical> priceRequestHistorical = coinListServicePrev
                            .getHistoricalPrice(priceParams.getFsym(), priceParams.getTsym(),
                                    priceParams.getTimestamp(), priceParams.getExchangeName());

                    return Observable.zip(priceRequest, priceRequestHistorical,
                            (BiFunction<PriceCurrent, PriceHistorical, Object>)
                                    (currentPrice, previousPrice)
                                            -> new CombinedPriceData(previousPrice.getFsym(), previousPrice.getTsym(),
                                            previousPrice.getPrices(), currentPrice.getPrices()));

                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Object price) {
                        if (price != null) {
                            CombinedPriceData data = (CombinedPriceData) price;
                            String key = data.getFsym() + "_" + data.getTsym();
                            Log.d(TAG, "onNext: " + key);
                            mDataset.get(key).setCurrentPrice(data.getCurrentPrices());
                            mDataset.get(key).setPreviousPrice(data.getPreviousPrices());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        mAdapter.setData(new ArrayList<>(mDataset.values()));
                        mAdapter.notifyDataSetChanged();
                    }
                });
    }

    private ArrayList<PriceParams> createPriceApiCallParams() {
        ArrayList<PriceParams> params = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        for (BagPriceData data : mDataset.values()) {
            if (data == null) continue;
            String fsym = data.getBag().getTradePair().getfCoin().getSymbol();
            String tsyms = data.getBag().getTradePair().gettCoin().getSymbol(); //TODO Fix this to Base currency later
            long ts = calendar.getTimeInMillis() - (1000 * 60 * 60 * 24);
            String exchange = mRealm.where(TxHistory.class)
                    .equalTo("txHolder.tradePair.pairName", data.getBag().getTradePair().getPairName())
                    .findAllSorted("date")
                    .last().getExchange().getName(); // Getting the latest exchange that has been added
            params.add(new PriceParams(fsym, tsyms, ts, exchange));
        }
        return params;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        if (mFirstRun) {
            mFirstRun = false;
        } else {
            mAdapter.setData(new ArrayList<>(loadBagData().values()));
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }
}
