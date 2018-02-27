package com.example.lemoncream.myapplication.Activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;

import com.example.lemoncream.myapplication.Adapter.ViewPagerAdapter;
import com.example.lemoncream.myapplication.Fragment.AlertFragment;
import com.example.lemoncream.myapplication.Fragment.ChartFragment;
import com.example.lemoncream.myapplication.Fragment.TransactionFragment;
import com.example.lemoncream.myapplication.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CoinDetailActivity extends AppCompatActivity {

    private static final String TAG = CoinDetailActivity.class.getSimpleName();

    @BindView(R.id.coin_detail_toolbar)  Toolbar toolbar;
    @BindView(R.id.coin_detail_tab_layout)  TabLayout mTabLayout;
    @BindView(R.id.coin_detail_view_pager) ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin_detail);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        initializeLayout();

    }

    public void initializeLayout(){
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new ChartFragment(), "Chart");
        adapter.addFragment(new AlertFragment(), "Alerts");
        adapter.addFragment(new TransactionFragment(), "Transactions");
        mViewPager.setAdapter(adapter);
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
    }


}
