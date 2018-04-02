package com.example.lemoncream.myapplication.Activity;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.lemoncream.myapplication.Model.RealmModels.Exchange;
import com.example.lemoncream.myapplication.Network.CoinListService;
import com.example.lemoncream.myapplication.Adapter.CoinListAdapter;
import com.example.lemoncream.myapplication.Model.Deserializers.CoinListDeserializer;
import com.example.lemoncream.myapplication.Model.Deserializers.ExchangeDataDeserializer;
import com.example.lemoncream.myapplication.Model.GsonModels.CoinData;
import com.example.lemoncream.myapplication.Model.GsonModels.ExchangeData;
import com.example.lemoncream.myapplication.Model.RealmModels.Pair;
import com.example.lemoncream.myapplication.Network.GsonHelper;
import com.example.lemoncream.myapplication.Network.RetrofitHelper;
import com.example.lemoncream.myapplication.R;
import com.example.lemoncream.myapplication.Utils.Database.RealmHelper;

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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SearchCoinActivity extends AppCompatActivity
        implements RealmHelper.RealmTransactionListener, SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    private static final String TAG = SearchCoinActivity.class.getSimpleName();
    public static final String EXTRA_SAVE_SUCCESSFUL_KEY = "extra_save_successful_key";
    public static final int REQUEST_CODE_SAVE_SUCCESSFUL = 26;
    private Realm mRealm;

    @BindView(R.id.search_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.search_recycler_view)
    RecyclerView mCoinListView;
    CoinListAdapter mAdapter;
    ProgressDialog mProgressDialog;

    RealmHelper mRealmHelper;

    private String mBaseUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_coin);
        ButterKnife.bind(this);
        Log.d(TAG, "onCreate: ");
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        mBaseUrl = getResources().getString(R.string.base_url);
        mRealm = Realm.getDefaultInstance();
        mProgressDialog = new ProgressDialog(SearchCoinActivity.this);
        mProgressDialog.setCancelable(false);
        mRealmHelper = new RealmHelper(SearchCoinActivity.this);

        pullCoinListDB();
    }


    public void pullCoinListDB() {
        long numOfExchanges = mRealm.where(Exchange.class).count();
        if (numOfExchanges == 0) {
            // The DB doesn't exist. Pull initial database from API.
            requestCoinList();
            setProgressDialog("Loading coin data...");
        } else {
            // Populate the RecyclerView with pairs;
            setProgressDialog("Loading coin data from DB...");
            RealmResults<Pair> pairs = mRealm.where(Pair.class).findAllAsync();
            pairs.addChangeListener(resultPairs -> {
                resultPairs.removeAllChangeListeners();
                populateCoinList(resultPairs);
            });
        }
    }

    private void setProgressDialog(String message) {
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage(message);
        mProgressDialog.show();
    }

    private void handleError(String message) {
        mProgressDialog.dismiss();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void populateCoinList(RealmResults<Pair> pairs) {
        mCoinListView.setHasFixedSize(true);
        mCoinListView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mCoinListView.setItemAnimator(new DefaultItemAnimator());

        LinearLayoutManager lm = new LinearLayoutManager(this);
        mCoinListView.setLayoutManager(lm);
        mAdapter = new CoinListAdapter(pairs, this);
        mCoinListView.setAdapter(mAdapter);

        mProgressDialog.dismiss();
    }

    public void requestCoinList() {
        Retrofit coinListRetrofit = RetrofitHelper.createRetrofitWithRxConverter(mBaseUrl,
                GsonHelper.createGsonBuilder(CoinData.class, new CoinListDeserializer()).create());
        Retrofit exchangeListRetrofit = RetrofitHelper.createRetrofitWithRxConverter(mBaseUrl,
                GsonHelper.createGsonBuilder(ExchangeData.class, new ExchangeDataDeserializer()).create());
        Observable<CoinData> coinListObservable = coinListRetrofit.create(CoinListService.class).requestCoinList();
        Observable<ExchangeData> exchangeDataObservable = exchangeListRetrofit.create(CoinListService.class).requestExchangeData();

        coinListObservable.flatMap(coinData -> {
            mRealmHelper.saveLatestCoinDataToRealm(Realm.getDefaultInstance(), coinData.getCoinList());
            return exchangeDataObservable.flatMap(exchangeData -> Observable.create(emitter -> {
                Realm realm = Realm.getDefaultInstance();
                mRealmHelper.syncPairsWithCoinData(realm, exchangeData.getPairs());
                emitter.onNext(exchangeData.getExchanges());
                emitter.onComplete();
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()))
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(exchanges ->
                        mRealmHelper.saveLatestPairDataToRealm(Realm.getDefaultInstance(), (List<Exchange>) exchanges));

    }

    @Override
    public void onExchangeTransactionFinished(boolean result) {
        if (result) {
            //Transaction was successful.
            RealmResults<Pair> pairs = mRealm.where(Pair.class).findAllAsync();
            pairs.addChangeListener(this::populateCoinList);
        } else {
            // Transaction failed.
            handleError(getResources().getString(R.string.error_message));
        }
    }

    public void initSearchView() {
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) mToolbar.getMenu().findItem(R.id.menu_search).getActionView();
        int searchImgId = android.support.v7.appcompat.R.id.search_button;
        ImageView v = searchView.findViewById(searchImgId);
        v.setImageResource(R.drawable.ic_action_search);

        if (searchManager != null)
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setSubmitButtonEnabled(false);
        searchView.setIconifiedByDefault(true);
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        mAdapter.performFiltering(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mAdapter.performFiltering(newText);
        return true;
    }

    @Override
    public boolean onClose() {
        mAdapter.performFiltering(null);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu: ");
        getMenuInflater().inflate(R.menu.menu_search_bar, menu);
        initSearchView();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            finish();
        }
    }
}
