package com.example.lemoncream.myapplication.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.lemoncream.myapplication.Adapter.ViewPagerAdapter;
import com.example.lemoncream.myapplication.Fragment.PortfolioFragment;
import com.example.lemoncream.myapplication.R;
import com.example.lemoncream.myapplication.Fragment.WatchlistFragment;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.portfolio_spinner) Spinner toolbarSpinner;
    @BindView(R.id.main_tab_layout) TabLayout tabLayout;
    @BindView(R.id.main_view_pager) ViewPager viewPager;
    @BindView(R.id.main_line_chart) LineChart lineChart;
    @BindView(R.id.fab) FloatingActionButton fab;

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        String[] samplePortfolioList = new String[]{"Main Portfolio", "Pocket money", "Vacation project"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, R.layout.spinner_drowdown_header, samplePortfolioList);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        toolbarSpinner.setAdapter(spinnerAdapter);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new PortfolioFragment(), "Portfolio");
        adapter.addFragment(new WatchlistFragment(), "Watchlist");
        viewPager.setAdapter(adapter);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                Log.d(TAG, "onTabSelected: " + viewPager.getAdapter().getPageTitle(tab.getPosition()));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        tabLayout.setupWithViewPager(viewPager);


        lineChart.setViewPortOffsets(0f, 80f, 0f, 0f);

        lineChart.setDrawGridBackground(false);
        lineChart.setTouchEnabled(false);
        lineChart.setContentDescription("");
        lineChart.getDescription().setEnabled(false);

        lineChart.getLegend().setEnabled(false);
        lineChart.getAxisLeft().setDrawLabels(false);
        lineChart.getAxisLeft().setDrawAxisLine(false);
        lineChart.getAxisLeft().setDrawGridLines(false);
        lineChart.getAxisLeft().setEnabled(false);

        lineChart.getAxisRight().setDrawLabels(false);
        lineChart.getAxisRight().setDrawAxisLine(false);
        lineChart.getAxisRight().setDrawGridLines(false);
        lineChart.getAxisRight().setEnabled(false);

        lineChart.getXAxis().setDrawLabels(false);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getXAxis().setDrawAxisLine(false);
        lineChart.getXAxis().setEnabled(false);

        lineChart.getXAxis().setDrawGridLines(false);
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
        lineChart.setData(data);

        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());






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
