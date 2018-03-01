package com.example.lemoncream.myapplication.Activity;

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

import com.example.lemoncream.myapplication.Adapter.ViewPagerAdapter;
import com.example.lemoncream.myapplication.Fragment.PortfolioFragment;
import com.example.lemoncream.myapplication.R;
import com.example.lemoncream.myapplication.Fragment.WatchlistFragment;
import com.example.lemoncream.myapplication.Utils.Callbacks.OnTotalValueChangedListener;
import com.example.lemoncream.myapplication.Utils.Formatters.SignSwitcher;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.DecimalFormat;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnTotalValueChangedListener {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.portfolio_spinner)
    Spinner mToolbarSpinner;
    @BindView(R.id.main_tab_layout)
    TabLayout mTabLayout;
    @BindView(R.id.main_view_pager)
    ViewPager mViewPager;
    @BindView(R.id.main_line_chart)
    LineChart mLineChart;
    @BindView(R.id.main_portfolio_value_text)
    TextView mPortfolioValueText;
    @BindView(R.id.main_portfolio_change_text)
    TextView mPortfolioChangeText;

    private static final String TAG = MainActivity.class.getSimpleName();
    private ViewPagerAdapter mAdapter;
    private boolean baseCurrencyDisplayMode = false;
    private boolean pctChangeDisplayMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);

        mPortfolioValueText.setOnClickListener(this);
        mPortfolioChangeText.setOnClickListener(this);

        String[] samplePortfolioList = new String[]{"Main Portfolio", "Pocket money", "Vacation project"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, R.layout.spinner_drowdown_header, samplePortfolioList);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mToolbarSpinner.setAdapter(spinnerAdapter);


        mAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mAdapter.addFragment(new PortfolioFragment(), "Portfolio");
        mAdapter.addFragment(new WatchlistFragment(), "Watchlist");
        mViewPager.setAdapter(mAdapter);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
                Log.d(TAG, "onTabSelected: " + mViewPager.getAdapter().getPageTitle(tab.getPosition()));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        mTabLayout.setupWithViewPager(mViewPager);


        mLineChart.setViewPortOffsets(0f, 80f, 0f, 0f);

        mLineChart.setDrawGridBackground(false);
        mLineChart.setTouchEnabled(false);
        mLineChart.setContentDescription("");
        mLineChart.getDescription().setEnabled(false);

        mLineChart.getLegend().setEnabled(false);
        mLineChart.getAxisLeft().setDrawLabels(false);
        mLineChart.getAxisLeft().setDrawAxisLine(false);
        mLineChart.getAxisLeft().setDrawGridLines(false);
        mLineChart.getAxisLeft().setEnabled(false);

        mLineChart.getAxisRight().setDrawLabels(false);
        mLineChart.getAxisRight().setDrawAxisLine(false);
        mLineChart.getAxisRight().setDrawGridLines(false);
        mLineChart.getAxisRight().setEnabled(false);

        mLineChart.getXAxis().setDrawLabels(false);
        mLineChart.getXAxis().setDrawGridLines(false);
        mLineChart.getXAxis().setDrawAxisLine(false);
        mLineChart.getXAxis().setEnabled(false);

        mLineChart.getXAxis().setDrawGridLines(false);
        ArrayList<Entry> vals = new ArrayList<>();
        int count = 10;
        int range = 100;
        for (int i = 0; i < count; i++) {
            float val = (float) (Math.random() * range) + 3;
            vals.add(new Entry(i, val));
        }


        LineDataSet set1 = new LineDataSet(vals, "Dataset 1");
        set1.setDrawIcons(false);
        set1.setDrawCircles(false);

        set1.setDrawFilled(true);
        set1.setColor(Color.parseColor("#78ffffff"));
        set1.setFillColor(Color.parseColor("#B3E5FC"));
        set1.setFillAlpha(20);
        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set1);
        LineData data = new LineData(dataSets);
        data.setDrawValues(false);
        mLineChart.setData(data);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.main_portfolio_value_text:
                baseCurrencyDisplayMode = !baseCurrencyDisplayMode;
                ((PortfolioFragment) mAdapter.getFragment(mTabLayout.getSelectedTabPosition())).changeDisplayCurrency(baseCurrencyDisplayMode);
                break;
            case R.id.main_portfolio_change_text:
                pctChangeDisplayMode = !pctChangeDisplayMode;
                ((PortfolioFragment) mAdapter.getFragment(mTabLayout.getSelectedTabPosition())).changeDisplayChangeUnit(pctChangeDisplayMode);
                break;
        }
    }

    @Override
    public void onTotalValueChanged(float totalPortfolioValue, float totalPortfolioValue24hr) {
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
}
