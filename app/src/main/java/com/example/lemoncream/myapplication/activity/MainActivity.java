package com.example.lemoncream.myapplication.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.lemoncream.myapplication.adapter.ViewPagerAdapter;
import com.example.lemoncream.myapplication.custom.PriceLineChart;
import com.example.lemoncream.myapplication.fragment.PortfolioFragment;
import com.example.lemoncream.myapplication.model.realm.Portfolio;
import com.example.lemoncream.myapplication.R;
import com.example.lemoncream.myapplication.fragment.WatchlistFragment;
import com.example.lemoncream.myapplication.utils.callbacks.OnTotalValueChangedListener;
import com.example.lemoncream.myapplication.utils.chartdata.MainPriceChartFetcher;
import com.example.lemoncream.myapplication.utils.formatters.SignSwitcher;
import com.example.lemoncream.myapplication.utils.notification.NotificationUtils;
import com.example.lemoncream.myapplication.utils.settings.SharedPreferenceManager;

import java.text.DecimalFormat;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnTotalValueChangedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.portfolio_spinner)
    Spinner mToolbarSpinner;
    @BindView(R.id.main_tab_layout)
    TabLayout mTabLayout;
    @BindView(R.id.main_view_pager)
    ViewPager mViewPager;
    @BindView(R.id.main_line_chart)
    PriceLineChart mLineChart;
    @BindView(R.id.main_portfolio_value_text)
    TextView mPortfolioValueText;
    @BindView(R.id.main_portfolio_change_text)
    TextView mPortfolioChangeText;

    private int mCurrentPortfolioId = 0;

    private boolean mFirstRun = true;
    private Realm mRealm;
    private MainPriceChartFetcher mFetcher;

    private ViewPagerAdapter mAdapter;
    private boolean baseCurrencyDisplayMode = false;
    private boolean pctChangeDisplayMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initToolbar();
        initSharedPref();
        initNotificationChannel();
        initRaelm();
        initFragments();
        initChart();
        initChartFetcher();
        initSpinner();
        setListeners();

    }


    public void initToolbar() {
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
    }

    private void initSharedPref() {
        mCurrentPortfolioId = SharedPreferenceManager
                .getIntSharedPref(this, getString(R.string.shared_pref_file_key),
                        getString(R.string.shared_pref_portfolio_id_key), 0);
    }

    public void initNotificationChannel() {
        NotificationUtils notificationUtils = new NotificationUtils();
        notificationUtils.createChannel(this);
    }

    public void initRaelm() {
        mRealm = Realm.getDefaultInstance();
    }

    public void initChart() {
        mLineChart.initChart();
    }

    public void setListeners() {
        mPortfolioValueText.setOnClickListener(this);
        mPortfolioChangeText.setOnClickListener(this);
    }

    public void initChartFetcher() {
        mFetcher = new MainPriceChartFetcher(mRealm, mLineChart);
        mFetcher.startPriceChartRequestCycle();
    }

    public void initSpinner() {
        ArrayList<Portfolio> portfolios =
                (ArrayList<Portfolio>) mRealm.copyFromRealm(mRealm.where(Portfolio.class)
                        .sort("_id", Sort.ASCENDING).findAll());
        ArrayAdapter<Portfolio> spinnerAdapter =
                new ArrayAdapter<>(this, R.layout.spinner_drowdown_header, portfolios);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mToolbarSpinner.setAdapter(spinnerAdapter);
        mToolbarSpinner.setSelection(findCurrentPortfolioPosition(portfolios, mCurrentPortfolioId));
    }

    public int findCurrentPortfolioPosition(ArrayList<Portfolio> data, int currentPortfolioId) {
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).get_id() == currentPortfolioId)
                return i;
        }
        return 0;
    }

    public void initFragments() {
        mAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mAdapter.addFragment(ViewPagerAdapter.FRAG_POSITION_PORTFOLIO, new PortfolioFragment(), "Portfolio");
        mAdapter.addFragment(ViewPagerAdapter.FRAG_POSITION_WATCHLIST, new WatchlistFragment(), "Watchlist");
        mViewPager.setAdapter(mAdapter);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Log.d(TAG, "onTabUnselected: ");
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Log.d(TAG, "onTabReselected: ");
            }
        });
        mTabLayout.setupWithViewPager(mViewPager);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.main_portfolio_value_text:
                baseCurrencyDisplayMode = !baseCurrencyDisplayMode;
                ((PortfolioFragment) mAdapter.getFragment(ViewPagerAdapter.FRAG_POSITION_PORTFOLIO)).changeDisplayCurrency(baseCurrencyDisplayMode);
                ((WatchlistFragment) mAdapter.getFragment(ViewPagerAdapter.FRAG_POSITION_WATCHLIST)).changeDisplayCurrency(baseCurrencyDisplayMode);
                break;
            case R.id.main_portfolio_change_text:
                pctChangeDisplayMode = !pctChangeDisplayMode;
                ((PortfolioFragment) mAdapter.getFragment(ViewPagerAdapter.FRAG_POSITION_PORTFOLIO)).changeDisplayChangeUnit(pctChangeDisplayMode);
                ((WatchlistFragment) mAdapter.getFragment(ViewPagerAdapter.FRAG_POSITION_WATCHLIST)).changeDisplayChangeUnit(baseCurrencyDisplayMode);
                break;
        }
    }

    @Override
    public void onTotalValueChanged(float totalPortfolioValue, float totalPortfolioValue24hr) {
        Log.d(TAG, "onTotalValueChanged: " + totalPortfolioValue + "," + totalPortfolioValue24hr);
        DecimalFormat df = new DecimalFormat("#,###,###.##");
        String portfolioValueStr = String.valueOf(df.format(totalPortfolioValue));
        portfolioValueStr = baseCurrencyDisplayMode ?
                SignSwitcher.getBaseCurrencySign() + " " + portfolioValueStr :
                SignSwitcher.getBtcSign() + " " + portfolioValueStr; //TODO Change here to accept sharedpref

        float change = pctChangeDisplayMode ?
                ((totalPortfolioValue - totalPortfolioValue24hr) / totalPortfolioValue24hr * 100)
                : (totalPortfolioValue - totalPortfolioValue24hr);
        String portfolioChangeStr = pctChangeDisplayMode ?
                (String.valueOf(df.format(change)) + "%")
                : String.valueOf(df.format(change));
        portfolioChangeStr = baseCurrencyDisplayMode ?
                SignSwitcher.getBaseCurrencySign() + " " + portfolioChangeStr :
                SignSwitcher.getBtcSign() + " " + portfolioChangeStr; //TODO Change here to accept sharedpref

        mPortfolioValueText.setText(portfolioValueStr);
        if (change > 0) {
            mPortfolioChangeText.setTextColor(Color.GREEN);
            mPortfolioChangeText.setText(portfolioChangeStr);
        } else if (change < 0) {
            mPortfolioChangeText.setTextColor(Color.RED);
            mPortfolioChangeText.setText(portfolioChangeStr);
        }
        mPortfolioChangeText.setText(portfolioChangeStr);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_new_coin) {
            Intent intent = new Intent(this, SearchCoinActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mFirstRun) {
            mFirstRun = false;
        } else {
            mFetcher.refresh();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFetcher.cancelPriceChartRequestCycle();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }
}
