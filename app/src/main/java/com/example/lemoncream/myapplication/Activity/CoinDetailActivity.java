package com.example.lemoncream.myapplication.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.lemoncream.myapplication.Adapter.ViewPagerAdapter;
import com.example.lemoncream.myapplication.Fragment.AlertFragment;
import com.example.lemoncream.myapplication.Fragment.ChartFragment;
import com.example.lemoncream.myapplication.Fragment.TransactionFragment;
import com.example.lemoncream.myapplication.Model.RealmModels.Alert;
import com.example.lemoncream.myapplication.Model.RealmModels.Bag;
import com.example.lemoncream.myapplication.Model.RealmModels.Exchange;
import com.example.lemoncream.myapplication.Model.RealmModels.TxHistory;
import com.example.lemoncream.myapplication.R;
import com.example.lemoncream.myapplication.Utils.Callbacks.OnUnitToggleListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class CoinDetailActivity extends AppCompatActivity implements OnUnitToggleListener, TabLayout.OnTabSelectedListener {

    private static final String TAG = CoinDetailActivity.class.getSimpleName();
    public static final String EXTRA_PAIR_KEY = "extra_pair_key";

    private boolean baseCurrencyDisplayMode = false;
    private boolean pctChangeDisplayMode = false;

    private Realm mRealm;
    private Bag mCurrentBag;
    private int mBagId;
    private String mFsym;
    private String mTsym;
    private String mExchange;

    private ViewPagerAdapter mTabLayoutAdapter;


    @BindView(R.id.coin_detail_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.coin_detail_tab_layout)
    TabLayout mTabLayout;
    @BindView(R.id.coin_detail_view_pager)
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin_detail);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        initializeData();
        initializeLayout();

    }

    public void initializeData() {
        mRealm = Realm.getDefaultInstance();
        mBagId = getIntent().getIntExtra(EXTRA_PAIR_KEY, -1);
        mCurrentBag = mRealm.where(Bag.class).equalTo("_id", mBagId).findFirst();
        if (mCurrentBag != null && mCurrentBag.getTradePair() != null) {
            mFsym = mCurrentBag.getTradePair().getfCoin().getSymbol();
            mTsym = mCurrentBag.getTradePair().gettCoin().getSymbol();
        }

         TxHistory latestTx = mRealm.where(TxHistory.class)
                .equalTo("txHolder.tradePair.pairName", mCurrentBag.getTradePair().getPairName())
                 .sort("date", Sort.DESCENDING).findFirst();
        if (latestTx == null) return;
        mExchange = latestTx.getExchange().getName();
    }

    public void initializeLayout() {

        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_PAIR_KEY, mBagId);

        ChartFragment chartFragment = new ChartFragment();
        TransactionFragment txFragment = new TransactionFragment();
        AlertFragment alertFragment = new AlertFragment();

        chartFragment.setArguments(bundle);
        txFragment.setArguments(bundle);
        alertFragment.setArguments(bundle);

        mToolbar.setTitle(mFsym + "/" + mTsym);
        mTabLayoutAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mTabLayoutAdapter.addFragment(ViewPagerAdapter.FRAG_POSITION_CHART, chartFragment, "Chart");
        mTabLayoutAdapter.addFragment(ViewPagerAdapter.FRAG_POSITION_TRANSACTION, txFragment, "Transactions");
        mTabLayoutAdapter.addFragment(ViewPagerAdapter.FRAG_POSITION_ALERT,alertFragment, "Alerts");
        mViewPager.setAdapter(mTabLayoutAdapter);
        mViewPager.setOffscreenPageLimit(2);
        mTabLayout.addOnTabSelectedListener(this);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private void showDeletionWarningDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(CoinDetailActivity.this).create();
        alertDialog.setTitle("Delete?");
        alertDialog.setMessage("All transactions and alerts for this pair will be deleted.");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            deleteThisBag();
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL", (dialogInterface, i) -> dialogInterface.dismiss());
        alertDialog.show();
    }

    private void deleteThisBag() {
        try {
            mRealm.executeTransaction(realm -> {
                RealmResults<TxHistory> txs = realm.where(TxHistory.class).equalTo("txHolder._id", mCurrentBag.get_id()).findAll();
                RealmResults<Alert> alerts = realm.where(Alert.class).equalTo("bag._id", mCurrentBag.get_id()).findAll();
                txs.deleteAllFromRealm();
                alerts.deleteAllFromRealm();
                mCurrentBag.deleteFromRealm();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        Toast.makeText(this,  "Successfully deleted " + mFsym + "/" + mTsym + " data", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onUnitToggled(boolean baseCurrencyDisplayMode, boolean pctChangeDisplayMode) {
        this.baseCurrencyDisplayMode = baseCurrencyDisplayMode;
        this.pctChangeDisplayMode = pctChangeDisplayMode;
        ((ChartFragment) mTabLayoutAdapter.getFragment(ViewPagerAdapter.FRAG_POSITION_CHART))
                .parseData(this.baseCurrencyDisplayMode, this.pctChangeDisplayMode);
        ((TransactionFragment) mTabLayoutAdapter.getFragment(ViewPagerAdapter.FRAG_POSITION_TRANSACTION))
                .togglePriceDisplay(this.baseCurrencyDisplayMode, this.pctChangeDisplayMode);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {}

    @Override
    public void onTabReselected(TabLayout.Tab tab) {}


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_coin_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_coin_detail_add_tx:
                Intent intent = new Intent(this, NewCoinActivity.class);
                intent.putExtra(NewCoinActivity.EXTRA_PAIR_KEY, mCurrentBag.getTradePair().getPairName());
                startActivity(intent);
                return true;
            case R.id.menu_coin_detail_delete:
                // Delete this bag and histories
                showDeletionWarningDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }
}
