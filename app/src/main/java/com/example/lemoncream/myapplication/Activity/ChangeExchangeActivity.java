package com.example.lemoncream.myapplication.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.lemoncream.myapplication.Adapter.ExchangeListAdapter;
import com.example.lemoncream.myapplication.Model.RealmModels.Exchange;
import com.example.lemoncream.myapplication.Model.RealmModels.Pair;
import com.example.lemoncream.myapplication.R;
import com.example.lemoncream.myapplication.Utils.Callbacks.ExchangeSelectionCallback;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

public class ChangeExchangeActivity extends AppCompatActivity implements ExchangeSelectionCallback {

    private static final String TAG = ChangeExchangeActivity.class.getSimpleName();
    public static final String EXTRA_EXCHANGE_KEY = "extra_exchange_key";

    @BindView(R.id.change_exchange_list)
    RecyclerView mExchangeRecyclerView;
    private ExchangeListAdapter mAdapter;
    private Pair mCurrentPair;
    private Realm mRealm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_exchange);
        ButterKnife.bind(this);

        mRealm = Realm.getDefaultInstance();
        mCurrentPair = mRealm.where(Pair.class)
                .equalTo("pairName", getIntent().getStringExtra(EXTRA_EXCHANGE_KEY))
                .findFirst();

        if (mCurrentPair != null && mCurrentPair.getExchanges() != null) populateExchangeList();

    }

    private void populateExchangeList() {
        LinearLayoutManager lm = new LinearLayoutManager(this);
        mAdapter = new ExchangeListAdapter(mCurrentPair.getExchanges(), this);
        mExchangeRecyclerView.setHasFixedSize(true);
        mExchangeRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mExchangeRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mExchangeRecyclerView.setLayoutManager(lm);
        mExchangeRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onExchangeSelected(Exchange exchange) {
        Intent intent = new Intent();
        Log.d(TAG, "onExchangeSelected: " + exchange.getName());
        intent.putExtra(NewCoinActivity.EXTRA_RESULT_EXCHANGE_KEY, exchange.getName());
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        mRealm.close();
    }
}