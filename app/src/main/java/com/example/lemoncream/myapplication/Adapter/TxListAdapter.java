package com.example.lemoncream.myapplication.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.lemoncream.myapplication.Model.RealmModels.TxHistory;
import com.example.lemoncream.myapplication.Model.TempModels.TxPriceData;
import com.example.lemoncream.myapplication.R;

import java.text.DateFormat;
import java.util.List;

/**
 * Created by LemonCream on 2018-03-02.
 */

public class TxListAdapter extends RecyclerView.Adapter<TxListAdapter.ViewHolder> {

    private Context mContext;
    private List<TxPriceData> mData;

    private boolean baseCurrencyDisplayMode = false;
    private boolean pctChangeDisplayMode = false;

    public TxListAdapter(Context mContext, List<TxPriceData> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        View layout;
        TextView exchangeText, dateText, orderTypeText, amountText,
                priceText, costText, profitText, noteText;

        ViewHolder(View itemView) {
            super(itemView);
            layout = itemView;
            exchangeText = itemView.findViewById(R.id.tx_item_exchange_text);
            dateText = itemView.findViewById(R.id.tx_item_date_text);
            orderTypeText = itemView.findViewById(R.id.tx_item_order_type_text);
            amountText = itemView.findViewById(R.id.tx_item_amount_text);
            priceText = itemView.findViewById(R.id.tx_item_price_text);
            costText = itemView.findViewById(R.id.tx_item_cost_text);
            profitText = itemView.findViewById(R.id.tx_item_profit_text);
            noteText = itemView.findViewById(R.id.tx_item_note_text);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction_list, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TxHistory currentTx = mData.get(position).getTxHistory();
        String orderType = currentTx.getOrderType().equals(TxHistory.ORDER_TYPE_BUY) ? "BUY" : "SELL";
        float amount = currentTx.getAmount();

        holder.exchangeText.setText(currentTx.getExchange().getName());
        holder.dateText.setText(DateFormat.getDateInstance().format(currentTx.getDate()));
        holder.orderTypeText.setText(orderType);
        holder.amountText.setText(String.valueOf(amount));


    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public List<TxPriceData> getmData() {
        return mData;
    }

    public void setmData(List<TxPriceData> mData) {
        this.mData = mData;
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
}
