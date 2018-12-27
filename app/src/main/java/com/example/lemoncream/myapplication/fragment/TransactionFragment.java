package com.example.lemoncream.myapplication.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lemoncream.myapplication.activity.CoinDetailActivity;
import com.example.lemoncream.myapplication.adapter.TxListAdapter;
import com.example.lemoncream.myapplication.model.deserializers.PriceDeserializer;
import com.example.lemoncream.myapplication.model.deserializers.PriceHistoricalDeserializer;
import com.example.lemoncream.myapplication.model.gson.PriceFull;
import com.example.lemoncream.myapplication.model.gson.PriceHistorical;
import com.example.lemoncream.myapplication.model.realm.Bag;
import com.example.lemoncream.myapplication.model.realm.TxHistory;
import com.example.lemoncream.myapplication.model.temp.PriceParams;
import com.example.lemoncream.myapplication.model.temp.TxListData;
import com.example.lemoncream.myapplication.model.temp.TxPriceData;
import com.example.lemoncream.myapplication.network.GsonHelper;
import com.example.lemoncream.myapplication.network.PriceService;
import com.example.lemoncream.myapplication.network.RetrofitHelper;
import com.example.lemoncream.myapplication.R;
import com.example.lemoncream.myapplication.utils.callbacks.OnUnitToggleListener;
import com.example.lemoncream.myapplication.utils.formatters.NumberFormatter;
import com.example.lemoncream.myapplication.utils.formatters.SignSwitcher;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import retrofit2.Retrofit;

/**
 * A simple {@link Fragment} subclass.
 */
public class TransactionFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = TransactionFragment.class.getSimpleName();

    private boolean mFirstRun = true;

    private OnUnitToggleListener mCallback;

    private Realm mRealm;
    private List<TxListData> mDataset;
    private int mBagId;
    private Bag mCurrentBag;
    private String mFsym, mTsym;

    @BindView(R.id.transaction_frag_recycler_view)
    RecyclerView mTxRecyclerView;
    private TxListAdapter mAdapter;

    @BindView(R.id.tx_frag_header_profit_loss_text)
    TextView mProfitText;
    @BindView(R.id.tx_frag_header_total_holdings_text)
    TextView mHoldingsText;
    @BindView(R.id.tx_frag_header_info_image_view)
    ImageView mInfoImageView;

    @BindView(R.id.tx_frag_header_net_cost_text)
    TextView mCostText;
    @BindView(R.id.tx_frag_header_market_value_text)
    TextView mValueText;

    private boolean baseCurrencyDisplayMode = false;
    private boolean pctChangeDisplayMode = false;

    public TransactionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (OnUnitToggleListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnUnitToggledListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRealm = Realm.getDefaultInstance();
        if (getArguments() != null) {
            unpackInitialData(getArguments());
        } else {
            Toast.makeText(getContext(), "Unexpected error occurred. Please try again later.", Toast.LENGTH_SHORT).show();
        }
    }

    public void unpackInitialData(Bundle args) {
        if (args != null) {
            mBagId = getArguments().getInt(CoinDetailActivity.EXTRA_PAIR_KEY, -1);
            mCurrentBag = mRealm.where(Bag.class).equalTo("_id", mBagId).findFirst();
            if (mCurrentBag != null && mCurrentBag.getTradePair() != null) {
                mFsym = mCurrentBag.getTradePair().getfCoin().getSymbol();
                mTsym = mCurrentBag.getTradePair().gettCoin().getSymbol();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setListeners();
        // load realm
        loadDataset();
        if (mDataset == null || mDataset.size() == 0) return;
        // config recyclerview, fill it up with realm data
        populateRecyclerView();
        // make an api call. should get price data of specified time at a specified exchange.
        requestPriceData(createPriceRequestParams());
    }

    private void setListeners() {
        mInfoImageView.setOnClickListener(this);
    }

    private void loadDataset() {
        mDataset = new ArrayList<>();
        RealmResults<TxHistory> txHistories = mRealm.where(TxHistory.class)
                .equalTo("txHolder._id", mBagId)
                .not()
                .equalTo("orderType", TxHistory.ORDER_TYPE_WATCH)
                .sort("date", Sort.DESCENDING)
                .findAll();
        for (int i = 0; i < txHistories.size(); i++) {
            mDataset.add(new TxListData(txHistories.get(i), new TxPriceData(i)));
        }
    }

    private void populateRecyclerView() {
        mAdapter = new TxListAdapter(getContext(), mDataset, mBagId);
        mAdapter.setFsym(mFsym);
        mAdapter.setTsym(mTsym);

        mTxRecyclerView.setHasFixedSize(true);
        mTxRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        mTxRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mTxRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mTxRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(()
                -> {
            mHoldingsText.setText(NumberFormatter.formatDecimals(mAdapter.getTotalHoldings()) + " " + mFsym);
            mValueText.setText(NumberFormatter.formatDecimals(mAdapter.getTotalHoldingsValue()));
            mCostText.setText(NumberFormatter.formatDecimals(mAdapter.getTotalNetCost()));
            mProfitText.setText(NumberFormatter.formatProfitDecimals(mAdapter.getTotalProfit()));
            mProfitText.setTextColor(NumberFormatter.getProfitTextColor(mAdapter.getTotalProfit()));

        });
        mTxRecyclerView.setAdapter(mAdapter);
    }

    private void requestPriceData(ArrayList<PriceParams> params) {
        Retrofit priceHistoRetrofit = RetrofitHelper.createRetrofitWithRxConverter(getResources().getString(R.string.base_url),
                GsonHelper.createGsonBuilder(PriceHistorical.class, new PriceHistoricalDeserializer()).create());
        PriceService priceHistoricalService = priceHistoRetrofit.create(PriceService.class);
        Retrofit priceCurrentRetrofit = RetrofitHelper.createRetrofitWithRxConverter(getResources().getString(R.string.base_url),
                GsonHelper.createGsonBuilder(PriceFull.class, new PriceDeserializer()).create());
        PriceService priceCurrentService = priceCurrentRetrofit.create(PriceService.class);

        if (params != null && params.size() != 0) {
            Observable.fromIterable(params)
                    .flatMap((Function<PriceParams, ObservableSource<?>>) param ->
                            Observable.zip(Observable.just(param),
                                    priceCurrentService.getMultipleCurrentPrices(param.getFsym(), param.getTsym(), param.getExchangeName()),
                                    priceHistoricalService.getHistoricalPrice(param.getTsym(), SignSwitcher.BASE_CURRENCY),
                                    priceHistoricalService.getHistoricalPrice(param.getTsym(), SignSwitcher.BASE_CURRENCY,
                                            param.getTimestamp()),
                                    (currentParam, priceCurrent, priceCurrentBase, priceHistoBase) -> {
                                        TxPriceData txPriceData = new TxPriceData();
                                        txPriceData.setPosition(currentParam.getPosition());
                                        if (priceCurrent != null)
                                            txPriceData.setCurrentPrice(priceCurrent.getTsymPriceDetail().getPrice());
                                        if (priceCurrentBase != null)
                                            txPriceData.setCurrentBasePrice(priceCurrentBase.getPrice());
                                        if (priceHistoBase != null)
                                            txPriceData.setPreviousBasePrice(priceHistoBase.getPrice());
                                        return txPriceData;
                                    }
                            )).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(txPriceData -> {
                                TxPriceData castedPriceData = (TxPriceData) txPriceData;
                                mDataset.get(castedPriceData.getPosition())
                                        .setTxPriceData(castedPriceData);
                            }, throwable -> {
                                throwable.printStackTrace();
                                mAdapter.notifyDataSetChanged();
                            },
                            () -> {
                                mAdapter.setmData(mDataset);
                                mAdapter.notifyDataSetChanged();
                            });
        } else if (params.size() == 0) {
            mAdapter.setmData(mDataset);
            mAdapter.notifyDataSetChanged();
        }
    }

    private ArrayList<PriceParams> createPriceRequestParams() {
        ArrayList<PriceParams> params = new ArrayList<>();
        for (int i = 0; i < mDataset.size(); i++) {
            params.add(new PriceParams(i, mFsym, mTsym,
                    mDataset.get(i).getTxHistory().getExchange().getName(),
                    mDataset.get(i).getTxHistory().getDate().getTime() / 1000));
        }
        return params;
    }

    public void togglePriceDisplay(boolean baseCurrencyDisplayMode, boolean pctChangeDisplayMode) {
        if (mAdapter != null && mTxRecyclerView != null) {
            mAdapter.setBaseCurrencyDisplayMode(baseCurrencyDisplayMode);
            mAdapter.setPctChangeDisplayMode(pctChangeDisplayMode);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void launchInfoDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setTitle("This information may not be accurate");
        alertDialogBuilder.setMessage("The historical prices are based on the daily average of the specified dates. " +
                "These are not calculated in an accurate way. " +
                "Use this information for your reference only.")
                .setCancelable(true)
                .setPositiveButton("GOT IT!", (dialogInterface, i) -> dialogInterface.cancel());
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tx_frag_header_info_image_view:
                launchInfoDialog();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mFirstRun) {
            mFirstRun = false;
        } else {
            if (mAdapter == null) {
                loadDataset();
                populateRecyclerView();
            } else {
                loadDataset();
            }
            requestPriceData(createPriceRequestParams());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }
}
