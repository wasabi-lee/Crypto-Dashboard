package com.example.lemoncream.myapplication.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LemonCream on 2018-01-05.
 */

public class ViewPagerAdapter extends FragmentPagerAdapter {

    public static final int FRAG_POSITION_PORTFOLIO = 0;
    public static final int FRAG_POSITION_WATCHLIST = 1;

    public static final int FRAG_POSITION_CHART = 0;
    public static final int FRAG_POSITION_TRANSACTION = 1;
    public static final int FRAG_POSITION_ALERT = 2;

    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    public void addFragment(int position, Fragment fragment, String title) {
        mFragmentList.add(position, fragment);
        mFragmentTitleList.add(position,title);
    }

    public Fragment getFragment(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }
}
