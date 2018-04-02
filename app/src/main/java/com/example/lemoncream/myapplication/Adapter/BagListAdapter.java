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
import com.example.lemoncream.myapplication.Utils.Callbacks.OnTotalValueChangedListener;
import com.example.lemoncream.myapplication.Utils.Formatters.NumberFormatter;
import com.example.lemoncream.myapplication.Utils.Formatters.SignSwitcher;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;

import io.realm.Realm;

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

    private OnTotalValueChangedListener mCallback;

    public BagListAdapter(Context mContext, ArrayList<BagPriceData> data) {
        this.mContext = mContext;
        this.data = data;
        this.mBaseUrl = mContext.getResources().getString(R.string.image_base_url);
        this.mCallback = (OnTotalValueChangedListener) mContext;
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

        // ----------------------------------- Unpacking data ---------------------------------------
        BagPriceData currentData = data.get(position);
        Bag currentBag = currentData.getBag();
        String fSym = currentBag.getTradePair().getfCoin().getSymbol();
        String tSym = currentBag.getTradePair().gettCoin().getSymbol();
        String fSymImageUrl = currentBag.getTradePair().getfCoin().getImageUrl();
        String pairName = fSym + "/" + tSym;
        String priceSign = baseCurrencyDisplayMode ? SignSwitcher.getBaseCurrencySign() : SignSwitcher.getCurrencySign(tSym);
        String holdingsValueSign = baseCurrencyDisplayMode ? SignSwitcher.getBaseCurrencySign() : SignSwitcher.getBtcSign();

        float priceCurrent = 0, changePct24hr = 0, change24hr = 0;
        float priceCurrentBtc = 0, priceBtc24hr = 0;
        float priceCurrentBase = 0, priceBase24hr = 0, changePct24hrBase = 0, change24hrBase = 0;

        if (currentData.getTsymPriceDetail() != null) {
            priceCurrent = currentData.getTsymPriceDetail().getPrice();
            changePct24hr = currentData.getTsymPriceDetail().getChangePercent24hr();
            change24hr = currentData.getTsymPriceDetail().getChange24hr();
        }
        if (currentData.getBtcPriceDetail() != null) {
            priceCurrentBtc = currentData.getBtcPriceDetail().getPrice();
            priceBtc24hr = currentData.getBtcPriceDetail().getOpen24hr();
        }
        if (currentData.getBasePriceDetail() != null) {
            priceCurrentBase = currentData.getBasePriceDetail().getPrice();
            priceBase24hr = currentData.getBasePriceDetail().getOpen24hr();
            changePct24hrBase = currentData.getBasePriceDetail().getChangePercent24hr();
            change24hrBase = currentData.getBasePriceDetail().getChange24hr();
        }

        float price = baseCurrencyDisplayMode ? priceCurrentBase : priceCurrent;
        float change = pctChangeDisplayMode ?
                (baseCurrencyDisplayMode ? changePct24hrBase : changePct24hr) :
                (baseCurrencyDisplayMode ? change24hrBase : change24hr);


        // ----------------------------------- Parsing data ---------------------------------------

        holder.mPairText.setText(pairName);

        if (currentBag.isWatchOnly()) {
            holder.mHoldingsValueText.setText("");
            holder.mHoldingsText.setText("");
            holder.mPriceText.setText(priceSign + NumberFormatter.formatDecimals(price));
            holder.mPriceChangeText.setText(String.valueOf(pctChangeDisplayMode ? NumberFormatter.formatProfitDecimals(change) + "%" : NumberFormatter.formatProfitDecimals(change)));
            holder.mPriceChangeText.setTextColor(NumberFormatter.getProfitTextColor(change));

        } else {
            if (currentData.getTsymPriceDetail() != null && currentData.getBasePriceDetail() != null && currentData.getBtcPriceDetail() != null) {
                float holdings = currentBag.getBalance();

                float holdingsValue = baseCurrencyDisplayMode ? priceCurrentBase * holdings : (fSym.equals("BTC") ? holdings : priceCurrentBtc * holdings);
                float holdingsValue24hrAgo = baseCurrencyDisplayMode ? priceBase24hr * holdings : priceBtc24hr * holdings;

                String priceStr = NumberFormatter.formatDecimals(price);
                String holdingsValueStr = NumberFormatter.formatDecimals(holdingsValue);
                String changeStr = NumberFormatter.formatDecimals(change);

                holder.mHoldingsText.setText(holdings > 0 ? largeDf.format(holdings) + " " + fSym : "");
                holder.mPriceText.setText(priceSign + priceStr);
                holder.mHoldingsValueText.setText(holdingsValueSign + holdingsValueStr);
                holder.mPriceChangeText.setText(String.valueOf(pctChangeDisplayMode ? changeStr + "%" : changeStr));
                holder.mPriceChangeText.setTextColor(NumberFormatter.getProfitTextColor(change));

                totalPortfolioValue += holdingsValue;
                totalPortfolioValue24hr += holdingsValue24hrAgo;
            }
        }

            // These views don't need to be updated upon unit change.
            Log.d(TAG, "onBindViewHolder: " + (currentBag.isWatchOnly() ? "WATCH " : "PORTFOLIO ") + position + ":"  + fSymImageUrl);
            Picasso.with(mContext)
                    .load(mBaseUrl + fSymImageUrl)
                    .resize(50, 50)
                    .centerCrop()
                    .into(holder.mCoinLogo);

            holder.layout.setOnClickListener(view -> {
                Intent intent = new Intent(mContext, CoinDetailActivity.class);
                intent.putExtra(CoinDetailActivity.EXTRA_PAIR_KEY, currentBag.get_id());
                mContext.startActivity(intent);
            });

        if (!currentBag.isWatchOnly() && position == data.size()-1) {
            Log.d(TAG, "onBindViewHolder: " + (currentBag.isWatchOnly() ? "WATCH " : "PORTFOLIO ") + "Callback!");
            mCallback.onTotalValueChanged(totalPortfolioValue, totalPortfolioValue24hr);
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
