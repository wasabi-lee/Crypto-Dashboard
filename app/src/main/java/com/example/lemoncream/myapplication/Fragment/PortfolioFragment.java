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
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.example.lemoncream.myapplication.Adapter.BagListAdapter;
import com.example.lemoncream.myapplication.Model.Deserializers.PriceDeserializer;
import com.example.lemoncream.myapplication.Model.GsonModels.PriceFull;
import com.example.lemoncream.myapplication.Model.RealmModels.TxHistory;
import com.example.lemoncream.myapplication.Model.TempModels.BagPriceData;
import com.example.lemoncream.myapplication.Model.TempModels.PriceParams;
import com.example.lemoncream.myapplication.Model.RealmModels.Bag;
import com.example.lemoncream.myapplication.Network.GsonHelper;
import com.example.lemoncream.myapplication.Network.PriceService;
import com.example.lemoncream.myapplication.Network.RetrofitHelper;
import com.example.lemoncream.myapplication.R;
import com.example.lemoncream.myapplication.Utils.Callbacks.OnTotalValueChangedListener;
import com.example.lemoncream.myapplication.Utils.Formatters.SignSwitcher;

import java.util.ArrayList;
import java.util.LinkedHashMap;

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
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class PortfolioFragment extends Fragment {

    private static final String TAG = PortfolioFragment.class.getSimpleName();

    private OnTotalValueChangedListener mCallback;

    private boolean mFirstRun = true;

    @BindView(R.id.portfolio_frag_add_new_container)
    LinearLayout mAddNewLayout;
    @BindView(R.id.portfolio_frag_recycler_view)
    RecyclerView mBagRecyclerView;
    @BindView(R.id.portfolio_frag_progress_bar)
    ProgressBar mProgressBar;


    private LinkedHashMap<String, BagPriceData> mDataset;

    private BagListAdapter mAdapter;
    private Realm mRealm;


    public PortfolioFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (OnTotalValueChangedListener) context;
        } catch (ClassCastException e) {
                throw new ClassCastException(context.toString()
                        + " must implement OnUnitChangedListener");}
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
        populateBagList(loadBagData());
        requestPriceData(createPriceApiCallParams());

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
            dataset.put(pairName, new BagPriceData(bag, null));
        }
        mDataset = dataset;
        return dataset;
    }


    private void populateBagList(LinkedHashMap<String, BagPriceData> dataset) {
        if (dataset == null) {
            // TODO Show error message
        } else if (dataset.size() == 0) {
            // TODO Show 'add first transaction' message
        } else {
            // Passing only values of the Map as ArrayList to fetch each element easier
            mAdapter = new BagListAdapter(getContext(), new ArrayList<>(dataset.values()));
            mBagRecyclerView.setHasFixedSize(true);
            mBagRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
            mBagRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mBagRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            mBagRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(()
                    -> mCallback.onTotalValueChanged(mAdapter.getTotalPortfolioValue(), mAdapter.getTotalPortfolioValue24hr()));
            mBagRecyclerView.setAdapter(mAdapter);
        }
    }

    private void requestPriceData(ArrayList<PriceParams> params) {
        if (params == null) return;

        Retrofit retrofitTsym = RetrofitHelper
                .createRetrofitWithRxConverter(getResources().getString(R.string.base_url),
                        GsonHelper.createGsonBuilder(PriceFull.class, new PriceDeserializer()).create());
        PriceService currentPriceService = retrofitTsym.create(PriceService.class);

        Observable.fromIterable(params)
                .flatMap((Function<PriceParams, ObservableSource<?>>) priceParams -> {
                    Observable<PriceFull> tsymPriceObservable = currentPriceService
                            .getMultipleCurrentPrices(priceParams.getFsym(),
                                    priceParams.getTsym(),
                                    priceParams.getExchangeName());
                    Observable<PriceFull> baseBtcPriceObservable = currentPriceService
                            .getMultipleCurrentPrices(priceParams.getFsym(),
                                    SignSwitcher.BASE_CURRENCY.toUpperCase() + ",BTC");
                    return Observable.zip(tsymPriceObservable, baseBtcPriceObservable, (tsymPrice, baseBtcPrice) -> {
                        tsymPrice.setBasePriceDetail(baseBtcPrice.getBasePriceDetail());
                        tsymPrice.setBtcPriceDetail(baseBtcPrice.getBtcPriceDetail());
                        return tsymPrice;
                    });
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(Object o) {
                        PriceFull priceFull = (PriceFull) o;
                        String pairName = priceFull.getFsym() + "_" + priceFull.getTsymPriceDetail().getTsym();
                        BagPriceData dataToFix = mDataset.get(pairName);
                        dataToFix.setTsymPriceDetail(priceFull.getTsymPriceDetail());
                        dataToFix.setBasePriceDetail(priceFull.getBasePriceDetail());
                        dataToFix.setBtcPriceDetail(priceFull.getBtcPriceDetail());
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                        mProgressBar.setVisibility(View.GONE);
                        mAdapter.setData(new ArrayList<>(mDataset.values()));
                        mAdapter.notifyDataSetChanged();
                    }
                });
    }

    private ArrayList<PriceParams> createPriceApiCallParams() {
        ArrayList<PriceParams> params = new ArrayList<>();
        for (BagPriceData data : mDataset.values()) {
            if (data == null) continue;
            String fsym = data.getBag().getTradePair().getfCoin().getSymbol();
            String tsyms = data.getBag().getTradePair().gettCoin().getSymbol(); //TODO Fix this to Base currency later
            String exchange = mRealm.where(TxHistory.class)
                    .equalTo("txHolder.tradePair.pairName", data.getBag().getTradePair().getPairName())
                    .findAllSorted("date")
                    .last().getExchange().getName(); // Getting the latest exchange that has been added
            params.add(new PriceParams(fsym, tsyms, exchange));
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
        Log.d(TAG, "onResume: ");
        if (mFirstRun) {
            mFirstRun = false;
        } else {
            loadBagData();
            requestPriceData(createPriceApiCallParams());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }
}
