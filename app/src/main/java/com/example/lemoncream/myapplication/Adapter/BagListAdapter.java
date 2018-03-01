package com.example.lemoncream.myapplication.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.lemoncream.myapplication.Activity.CoinDetailActivity;
import com.example.lemoncream.myapplication.Model.RealmModels.Bag;
import com.example.lemoncream.myapplication.Model.TempModels.BagPriceData;
import com.example.lemoncream.myapplication.R;
import com.example.lemoncream.myapplication.Utils.Formatters.SignSwitcher;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by LemonCream on 2018-02-23.
 */

public class BagListAdapter extends RecyclerView.Adapter<BagListAdapter.ViewHolder> {

    private static final String TAG = BagListAdapter.class.getSimpleName();
    private static final DecimalFormat largeDf = new DecimalFormat("#,###,###.##");
    private static final DecimalFormat smallDf = new DecimalFormat("#,###,###.#######");

    private boolean baseCurrencyDisplayMode = false;
    private boolean pctChangeDisplayMode = false;
    private float totalPortfolioValue = 0;
    private float totalPortfolioValue24hr = 0;

    private ArrayList<BagPriceData> data;
    private Context mContext;
    private String mBaseUrl;

    public BagListAdapter(Context mContext, ArrayList<BagPriceData> data) {
        this.mContext = mContext;
        this.data = data;
        this.mBaseUrl = mContext.getResources().getString(R.string.image_base_url);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        View layout;
        ImageView mCoinLogo;
        TextView mPairText;
        TextView mHoldingsValueText;
        TextView mHoldingsText;
        TextView mPriceText;
        TextView mPriceChangeText;

        ViewHolder(View itemView) {
            super(itemView);
            layout = itemView;
            mCoinLogo = layout.findViewById(R.id.item_bag_coin_logo);
            mPairText = layout.findViewById(R.id.item_bag_coin_pair_name);
            mHoldingsValueText = layout.findViewById(R.id.item_bag_holdings_value);
            mHoldingsText = layout.findViewById(R.id.item_bag_holdings);
            mPriceText = layout.findViewById(R.id.item_bag_price);
            mPriceChangeText = layout.findViewById(R.id.item_bag_price_change);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_bag_list, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        if (position == 0) {
            totalPortfolioValue = 0;
            totalPortfolioValue24hr = 0;
        }

        BagPriceData currentData = data.get(position);
        Bag currentBag = currentData.getBag();
        String fSym = currentBag.getTradePair().getfCoin().getSymbol();
        String tSym = currentBag.getTradePair().gettCoin().getSymbol();
        String fSymImageUrl = currentBag.getTradePair().getfCoin().getImageUrl();
        String pairName = fSym + "/" + tSym;

        float holdings = currentBag.getBalance();
        holder.mPairText.setText(pairName);
        holder.mHoldingsText.setText(largeDf.format(holdings) + " " + fSym);

        if (currentData.getTsymPriceDetail() != null && currentData.getBasePriceDetail() != null && currentData.getBtcPriceDetail() != null) {
            float priceCurrent = currentData.getTsymPriceDetail().getPrice();
            float priceCurrentBase = currentData.getBasePriceDetail().getPrice();
            float priceCurrentBtc = currentData.getBtcPriceDetail().getPrice();

            float priceBase24hr = currentData.getBasePriceDetail().getOpen24hr();
            float priceBtc24hr = currentData.getBtcPriceDetail().getOpen24hr();

            float changePct24hr = currentData.getTsymPriceDetail().getChangePercent24hr();
            float change24hr = currentData.getTsymPriceDetail().getChange24hr();
            float changePct24hrBase = currentData.getBasePriceDetail().getChangePercent24hr();
            float change24hrBase = currentData.getBasePriceDetail().getChange24hr();


            String priceSign = baseCurrencyDisplayMode ? SignSwitcher.getBaseCurrencySign() : SignSwitcher.getCurrencySign(tSym);
            float price = baseCurrencyDisplayMode ? priceCurrentBase : priceCurrent;
            String priceStr = price > 99 ? largeDf.format(price) : smallDf.format(price);
            holder.mPriceText.setText(priceSign + priceStr);

            String holdingsValueSign = baseCurrencyDisplayMode ? SignSwitcher.getBaseCurrencySign() : SignSwitcher.getBtcSign();
            float holdingsValue = baseCurrencyDisplayMode ? priceCurrentBase * holdings : (fSym.equals("BTC") ? holdings : priceCurrentBtc * holdings);
            float holdingsValue24hrAgo = baseCurrencyDisplayMode ? priceBase24hr * holdings : priceBtc24hr * holdings;
            String holdingsValueStr = holdingsValue > 99 ? largeDf.format(holdingsValue) : smallDf.format(holdingsValue);
            holder.mHoldingsValueText.setText(holdingsValueSign + holdingsValueStr);

            totalPortfolioValue += holdingsValue; totalPortfolioValue24hr += holdingsValue24hrAgo;

            float change = pctChangeDisplayMode ?
                    (baseCurrencyDisplayMode ? changePct24hrBase : changePct24hr) :
                    (baseCurrencyDisplayMode ? change24hrBase : change24hr);

            String changeStr = Math.abs(change) > 99 ? largeDf.format(change) : smallDf.format(change);
            holder.mPriceChangeText.setText(String.valueOf(pctChangeDisplayMode ? changeStr + "%" : changeStr));
            if (change != 0) holder.mPriceChangeText.setTextColor(change > 0 ? Color.GREEN : Color.RED);

        }

        if (holder.mCoinLogo.getDrawable() == null) {
            // These views don't need to be updated upon unit change.
            Picasso.with(mContext)
                    .load(mBaseUrl + fSymImageUrl)
                    .resize(40, 40)
                    .centerCrop()
                    .into(holder.mCoinLogo);

            holder.layout.setOnClickListener(view -> {
                Intent intent = new Intent(mContext, CoinDetailActivity.class);
                intent.putExtra(CoinDetailActivity.EXTRA_PAIR_KEY, currentBag.get_id());
                mContext.startActivity(intent);
            });
        }
    }

    public boolean isBaseCurrencyDisplayMode() {
        return baseCurrencyDisplayMode;
    }

    public void setBaseCurrencyDisplayMode(boolean baseCurrencyDisplayMode) {
        this.baseCurrencyDisplayMode = baseCurrencyDisplayMode;
    }

    public boolean isPctChangeDisplayMode() {
        return pctChangeDisplayMode;
    }

    public void setPctChangeDisplayMode(boolean pctChangeDisplayMode) {
        this.pctChangeDisplayMode = pctChangeDisplayMode;
    }

    public float getTotalPortfolioValue() {
        return totalPortfolioValue;
    }

    public void setTotalPortfolioValue(float totalPortfolioValue) {
        this.totalPortfolioValue = totalPortfolioValue;
    }

    public float getTotalPortfolioValue24hr() {
        return totalPortfolioValue24hr;
    }

    public void setTotalPortfolioValue24hr(float totalPortfolioValue24hr) {
        this.totalPortfolioValue24hr = totalPortfolioValue24hr;
    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    public ArrayList<BagPriceData> getData() {
        return data;
    }

    public void setData(ArrayList<BagPriceData> data) {
        this.data = data;
    }
}
