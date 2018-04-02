package com.example.lemoncream.myapplication.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.lemoncream.myapplication.Activity.NewCoinActivity;
import com.example.lemoncream.myapplication.Activity.SearchCoinActivity;
import com.example.lemoncream.myapplication.Model.RealmModels.Pair;
import com.example.lemoncream.myapplication.R;

import io.realm.Case;
import io.realm.RealmList;
import io.realm.RealmResults;

/**
 * Created by LemonCream on 2018-02-08.
 */

public class CoinListAdapter extends RecyclerView.Adapter<CoinListAdapter.ViewHolder> {

    private static final String TAG = CoinListAdapter.class.getSimpleName();
    private RealmResults<Pair> data;
    private RealmList<Pair> filteredData;
    private Context mContext;

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        String pairName;
        Context context;
        TextView pairTextView;
        View layout;

        ViewHolder(View v, Context context) {
            super(v);
            this.context = context;
            this.layout = v;
            this.pairTextView = v.findViewById(R.id.item_coin_list_text);
            this.layout.setOnClickListener(this);
        }

        void setPairName(String pairName) {
            this.pairName = pairName;
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(context, NewCoinActivity.class);
            intent.putExtra(NewCoinActivity.EXTRA_PAIR_KEY, pairName);
            ((Activity) context).startActivityForResult(intent, SearchCoinActivity.REQUEST_CODE_SAVE_SUCCESSFUL);
        }
    }

    public CoinListAdapter(RealmResults<Pair> data, Context mContext) {
        this.mContext = mContext;
        this.data = data;
        this.filteredData = new RealmList<>();
        filteredData.addAll(data);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_coin_list, parent, false);
        return new ViewHolder(v, mContext);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: " + position);
        Pair currentPair = filteredData.get(position);
        final String pairName = currentPair.getfCoin().getName() + ": " + currentPair.getfCoin().getSymbol() + "/" + currentPair.gettCoin().getSymbol();
        holder.pairTextView.setText(pairName);
        holder.setPairName(currentPair.getPairName());
    }

    public void performFiltering(CharSequence constraint) {
        if (constraint != null && constraint.length() > 0) {

            RealmResults<Pair> unsorted;
            RealmResults<Pair> exactMatch;
            RealmResults<Pair> relevantMatch;
            RealmList<Pair> result = new RealmList<Pair>();

            unsorted = data.where()
                    .contains("fCoin.symbol", constraint.toString(), Case.INSENSITIVE)
                    .or()
                    .contains("fCoin.name", constraint.toString(), Case.INSENSITIVE)
                    .findAll();

            exactMatch = unsorted.where()
                    .equalTo("fCoin.symbol", constraint.toString(), Case.INSENSITIVE)
                    .or()
                    .equalTo("fCoin.name", constraint.toString(), Case.INSENSITIVE)
                    .findAll();

            relevantMatch = unsorted.where()
                    .not()
                    .beginGroup()
                        .equalTo("fCoin.symbol", constraint.toString(), Case.INSENSITIVE)
                        .or()
                        .equalTo("fCoin.name", constraint.toString(), Case.INSENSITIVE)
                    .endGroup()
                    .findAll();

            result.addAll(exactMatch);
            result.addAll(relevantMatch);
            filteredData = result;
        } else {
            filteredData.clear();
            filteredData.addAll(data);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return filteredData.size();
    }

    public RealmResults<Pair> getData() {
        return data;
    }

    public void setData(RealmResults<Pair> data) {
        this.data = data;
    }
}
