package com.example.lemoncream.myapplication.fragment;


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
import android.widget.Toast;

import com.example.lemoncream.myapplication.adapter.BagListAdapter;
import com.example.lemoncream.myapplication.model.deserializers.PriceDeserializer;
import com.example.lemoncream.myapplication.model.gson.PriceFull;
import com.example.lemoncream.myapplication.model.realm.TxHistory;
import com.example.lemoncream.myapplication.model.temp.BagPriceData;
import com.example.lemoncream.myapplication.model.temp.PriceParams;
import com.example.lemoncream.myapplication.model.realm.Bag;
import com.example.lemoncream.myapplication.network.GsonHelper;
import com.example.lemoncream.myapplication.network.PriceService;
import com.example.lemoncream.myapplication.network.RetrofitHelper;
import com.example.lemoncream.myapplication.R;
import com.example.lemoncream.myapplication.utils.formatters.SignSwitcher;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
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
    @BindView(R.id.portfolio_frag_progress_bar)
    ProgressBar mProgressBar;

    private ArrayList<BagPriceData> mDataset;

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
        mRealm = Realm.getDefaultInstance();

        populateBagList(loadBagData());
        requestPriceData(createPriceApiCallParams());
    }


    private ArrayList<BagPriceData> loadBagData() {
        mProgressBar.setVisibility(View.VISIBLE);
        return createDataset(mRealm.where(Bag.class).equalTo("watchOnly", false).findAll());
    }

    private ArrayList<BagPriceData> createDataset(RealmResults<Bag> bags) {
        ArrayList<BagPriceData> dataset = new ArrayList<>();
        if (bags.size() == 0) { // the realm query result is never null
            mProgressBar.setVisibility(View.GONE);
            mDataset = dataset;
            return dataset;
        }
        for (Bag bag : bags) {
            if (bag == null || bag.getTradePair() == null) continue;
            dataset.add(new BagPriceData(bag, null));
        }
        mDataset = dataset;
        return dataset;
    }


    private void populateBagList(ArrayList<BagPriceData> dataset) {
        mAdapter = new BagListAdapter(getContext(), dataset);
        mBagRecyclerView.setHasFixedSize(true);
        mBagRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        mBagRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mBagRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mBagRecyclerView.setAdapter(mAdapter);
    }

    private void requestPriceData(ArrayList<PriceParams> params) {
        if (mDataset == null || params == null) {
            mProgressBar.setVisibility(View.GONE);
            return;
        }
        if (mDataset.size() == 0 || params.size() == 0) {
            mProgressBar.setVisibility(View.GONE);
            mAdapter.setData(mDataset);
            mAdapter.notifyDataSetChanged();
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
                            mDataset.set(priceFull.getPosition(), dataToFix);
                        },
                        Throwable::printStackTrace,
                        () -> {
                            mProgressBar.setVisibility(View.GONE);
                            mAdapter.setData(mDataset);
                            mAdapter.notifyDataSetChanged();
                        });
    }

    private ArrayList<PriceParams> createPriceApiCallParams() {
        ArrayList<PriceParams> params = new ArrayList<>();

        if (mDataset == null) return params;

        for (int i = 0; i < mDataset.size(); i++) {
            BagPriceData data = mDataset.get(i);
            if (data == null) continue;
            String fsym = data.getBag().getTradePair().getfCoin().getSymbol();
            String tsyms = data.getBag().getTradePair().gettCoin().getSymbol() + "," + SignSwitcher.getBaseCurrencySign(); //TODO Fix this to Base currency later
            RealmResults<TxHistory> exchangeList = mRealm.where(TxHistory.class)
                    .equalTo("txHolder.tradePair.pairName", data.getBag().getTradePair().getPairName())
                    .findAllSorted("date");
            if (exchangeList == null || exchangeList.size() == 0) return null;
            String exchange = exchangeList.last().getExchange().getName();

            params.add(new PriceParams(i, fsym, tsyms, exchange));
        }
        return params;
    }

    public void changeDisplayCurrency(boolean baseCurrencyDisplayMode) {
        try {
            mAdapter.setBaseCurrencyDisplayMode(baseCurrencyDisplayMode);
            mAdapter.notifyDataSetChanged();
        } catch (NullPointerException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), getResources().getString(R.string.error_message), Toast.LENGTH_SHORT).show();
        }
    }

    public void changeDisplayChangeUnit(boolean pctChangeDisplayMode) {
        try {
            mAdapter.setPctChangeDisplayMode(pctChangeDisplayMode);
            mAdapter.notifyDataSetChanged();
        } catch (NullPointerException e) {
            Toast.makeText(getContext(), getResources().getString(R.string.error_message), Toast.LENGTH_SHORT).show();
        }
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

}
