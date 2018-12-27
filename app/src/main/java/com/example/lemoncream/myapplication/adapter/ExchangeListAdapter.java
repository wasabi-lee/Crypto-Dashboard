package com.example.lemoncream.myapplication.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.lemoncream.myapplication.model.realm.Exchange;
import com.example.lemoncream.myapplication.R;
import com.example.lemoncream.myapplication.utils.callbacks.ExchangeSelectionCallback;

import io.realm.RealmList;

/**
 * Created by LemonCream on 2018-02-19.
 */

public class ExchangeListAdapter extends RecyclerView.Adapter<ExchangeListAdapter.ViewHolder> {

    private ExchangeSelectionCallback mCallback;
    private static final String TAG = ExchangeListAdapter.class.getSimpleName();
    private RealmList<Exchange> data;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View layout;
        TextView exchangeTextView;

        ViewHolder(View v) {
            super(v);
            exchangeTextView = v.findViewById(R.id.item_exchange_text);
            layout = v;
        }
    }

    public ExchangeListAdapter(RealmList<Exchange> data, ExchangeSelectionCallback callback) {
        this.data = data;
        this.mCallback = callback;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exchange_list, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Exchange currentExchange = data.get(position);
        holder.exchangeTextView.setText(currentExchange.getName());
        holder.layout.setOnClickListener(view -> mCallback.onExchangeSelected(currentExchange));

    }



    @Override
    public int getItemCount() {
        return data.size();
    }
}
