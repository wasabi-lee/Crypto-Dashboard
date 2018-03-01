package com.example.lemoncream.myapplication.Activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.example.lemoncream.myapplication.Adapter.ViewPagerAdapter;
import com.example.lemoncream.myapplication.Fragment.AlertFragment;
import com.example.lemoncream.myapplication.Fragment.ChartFragment;
import com.example.lemoncream.myapplication.Fragment.TransactionFragment;
import com.example.lemoncream.myapplication.Model.Deserializers.PriceDeserializer;
import com.example.lemoncream.myapplication.Model.GsonModels.PriceFull;
import com.example.lemoncream.myapplication.Model.RealmModels.Bag;
import com.example.lemoncream.myapplication.Model.RealmModels.TxHistory;
import com.example.lemoncream.myapplication.Network.GsonHelper;
import com.example.lemoncream.myapplication.Network.PriceService;
import com.example.lemoncream.myapplication.Network.RetrofitHelper;
import com.example.lemoncream.myapplication.R;
import com.example.lemoncream.myapplication.Utils.Callbacks.OnUnitToggleListener;
import com.example.lemoncream.myapplication.Utils.Formatters.SignSwitcher;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import retrofit2.Retrofit;

public class CoinDetailActivity extends AppCompatActivity implements OnUnitToggleListener {

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

    private PriceFull mCurrentPriceInfo;

    private ViewPagerAdapter mTabLayoutAdapter;


    @BindView(R.id.coin_detail_toolbar)
    Toolbar toolbar;
    @BindView(R.id.coin_detail_tab_layout)
    TabLayout mTabLayout;
    @BindView(R.id.coin_detail_view_pager)
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin_detail);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        initializeData();
        initializeLayout();
        requestCurrentPrice();

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
                .findAllSorted("date").last();
        if (latestTx == null) return;
        mExchange = latestTx.getExchange().getName();
    }

    public void initializeLayout() {

        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_PAIR_KEY, mBagId);

        ChartFragment chartFragment = new ChartFragment();
        chartFragment.setArguments(bundle);
        TransactionFragment txFragment = new TransactionFragment();
        txFragment.setArguments(bundle);
        AlertFragment alertFragment = new AlertFragment();
        alertFragment.setArguments(bundle);

        mTabLayoutAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mTabLayoutAdapter.addFragment(chartFragment, "Chart");
        mTabLayoutAdapter.addFragment(txFragment, "Transactions");
        mTabLayoutAdapter.addFragment(alertFragment, "Alerts");
        mViewPager.setAdapter(mTabLayoutAdapter);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public void onUnitToggled(boolean baseCurrencyDisplayMode, boolean pctChangeDisplayMode) {
        this.baseCurrencyDisplayMode = baseCurrencyDisplayMode;
        this.pctChangeDisplayMode = pctChangeDisplayMode;
        ((ChartFragment) mTabLayoutAdapter.getFragment(mTabLayout.getSelectedTabPosition()))
                .parseData(mCurrentPriceInfo, this.baseCurrencyDisplayMode, this.pctChangeDisplayMode);
        //TODO Add tx layout also
    }

    public void requestCurrentPrice() {
        Retrofit retrofit = RetrofitHelper
                .createRetrofitWithRxConverter(getResources().getString(R.string.base_url),
                        GsonHelper.createGsonBuilder(PriceFull.class, new PriceDeserializer()).create());
        Observable<PriceFull> tsymPriceObservable = retrofit.create(PriceService.class).getMultipleCurrentPrices(mFsym, mTsym, mExchange);
        Observable<PriceFull> baseBtcPriceObservable = retrofit.create(PriceService.class).getMultipleCurrentPrices(mFsym, SignSwitcher.BASE_CURRENCY + ",BTC");

        Observable.zip(tsymPriceObservable, baseBtcPriceObservable, (tsymPrice, baseBtcPrice) -> {
            tsymPrice.setBasePriceDetail(baseBtcPrice.getBasePriceDetail());
            tsymPrice.setBtcPriceDetail(baseBtcPrice.getBtcPriceDetail());
            return tsymPrice;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(price -> {
                            if (price == null) return;
                             mCurrentPriceInfo = price;
                            ((ChartFragment) mTabLayoutAdapter
                                    .getFragment(mTabLayout.getSelectedTabPosition()))
                                    .parseData(price, baseCurrencyDisplayMode, pctChangeDisplayMode);
                        }, Throwable::printStackTrace,
                        () -> Log.d(TAG, "requestCurrentPrice: onComplete"));
    }
}
