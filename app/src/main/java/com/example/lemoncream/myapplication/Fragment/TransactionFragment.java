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

import com.example.lemoncream.myapplication.Activity.CoinDetailActivity;
import com.example.lemoncream.myapplication.Adapter.BagListAdapter;
import com.example.lemoncream.myapplication.Adapter.TxListAdapter;
import com.example.lemoncream.myapplication.Model.RealmModels.Bag;
import com.example.lemoncream.myapplication.Model.RealmModels.TxHistory;
import com.example.lemoncream.myapplication.Model.TempModels.BagPriceData;
import com.example.lemoncream.myapplication.Model.TempModels.TxPriceData;
import com.example.lemoncream.myapplication.R;
import com.example.lemoncream.myapplication.Utils.Callbacks.OnUnitToggleListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * A simple {@link Fragment} subclass.
 */
public class TransactionFragment extends Fragment {

    private static final String TAG = TransactionFragment.class.getSimpleName();

    private OnUnitToggleListener mCallback;

    private Realm mRealm;
    private List<TxPriceData> mDataset;
    private int mBagId;
    private Bag mCurrentBag;
    private String mFsym, mTsym;

    @BindView(R.id.transaction_frag_recycler_view)
    RecyclerView mTxRecyclerView;
    private TxListAdapter mAdapter;

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
            //TODO Display error message
        }
    }

    public void unpackInitialData(Bundle args) {
        if (args != null) {
            int receivedId = getArguments().getInt(CoinDetailActivity.EXTRA_PAIR_KEY, -1);
            mCurrentBag = mRealm.where(Bag.class).equalTo("_id", receivedId).findFirst();
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
        // load realm
        loadDataset();
        // config recyclerview
        populateRecyclerView();
        // fill it up with realm data
        // make an api call. should get price data of specified time at a specified exchange.
        // realm will give you 1) holdings, 2) time of the transaction, 3) exchange
        // Observable.zip(index, observable. -> pricehistorical object)
    }

    private void loadDataset() {
        mDataset = new ArrayList<>();
        RealmResults<TxHistory> txHistories =  mRealm.where(TxHistory.class)
                .equalTo("txHolder._id", mBagId)
                .sort("date", Sort.ASCENDING)
                .findAll();
        for (TxHistory txHistory : txHistories) {
            mDataset.add(new TxPriceData(txHistory, -1, -1, -1, -1));
        }
    }

    private void populateRecyclerView() {
        if (mDataset == null) {
            // TODO Show error message
        } else if (mDataset.size() == 0) {
            // TODO Show 'add first transaction' message
        } else {
            // Passing only values of the Map as ArrayList to fetch each element easier
            mAdapter = new TxListAdapter(getContext(), mDataset);
            mTxRecyclerView.setHasFixedSize(true);
            mTxRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
            mTxRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mTxRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            mTxRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(()
                    -> Log.d(TAG, "populateRecyclerView: "));
            mTxRecyclerView.setAdapter(mAdapter);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }
}
