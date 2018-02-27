package com.example.lemoncream.myapplication.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.lemoncream.myapplication.Activity.CoinDetailActivity;
import com.example.lemoncream.myapplication.Fragment.ChartFragment;
import com.example.lemoncream.myapplication.Model.GsonModels.ChartData;
import com.example.lemoncream.myapplication.Model.RealmModels.Bag;
import com.example.lemoncream.myapplication.Model.TempModels.BagPriceData;
import com.example.lemoncream.myapplication.R;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by LemonCream on 2018-02-23.
 */

public class BagListAdapter extends RecyclerView.Adapter<BagListAdapter.ViewHolder> {

    private static final String TAG = BagListAdapter.class.getSimpleName();
    private static final DecimalFormat df = new DecimalFormat("#.######");

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

        Bag currentItem = data.get(position).getBag();
        String fSym = currentItem.getTradePair().getfCoin().getSymbol();
        String tSym = currentItem.getTradePair().gettCoin().getSymbol();
        String fSymImageUrl = currentItem.getTradePair().getfCoin().getImageUrl();
        String pairName = fSym + "/" + tSym;

        float holdings = currentItem.getBalance();
        holder.mPairText.setText(pairName);
        holder.mHoldingsText.setText(df.format(holdings) + " " + fSym);

        if (data.get(position).getCurrentPrice() != null && data.get(position).getPreviousPrice() != null) {
            float previousPrice = data.get(position).getPreviousPrice().get(tSym);
            float currentPrice = data.get(position).getCurrentPrice().get(tSym);
            holder.mPriceText.setText(String.valueOf(df.format(currentPrice)));
            holder.mHoldingsValueText.setText(String.valueOf(df.format(currentPrice * holdings)));
            holder.mPriceChangeText.setText(String.valueOf(df.format(currentPrice - previousPrice)));
        }

        Picasso.with(mContext)
                .load(mBaseUrl + fSymImageUrl)
                .resize(50, 50)
                .centerCrop()
                .into(holder.mCoinLogo);

        holder.layout.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, CoinDetailActivity.class);
            intent.putExtra(CoinDetailActivity.EXTRA_PAIR_KEY, currentItem.get_id());
            mContext.startActivity(intent);
        });
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
