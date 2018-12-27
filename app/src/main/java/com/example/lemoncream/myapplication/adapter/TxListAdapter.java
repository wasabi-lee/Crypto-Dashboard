package com.example.lemoncream.myapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.lemoncream.myapplication.activity.NewCoinActivity;
import com.example.lemoncream.myapplication.model.realm.TxHistory;
import com.example.lemoncream.myapplication.model.temp.TxListData;
import com.example.lemoncream.myapplication.model.temp.TxPriceData;
import com.example.lemoncream.myapplication.R;
import com.example.lemoncream.myapplication.utils.formatters.NumberFormatter;
import com.example.lemoncream.myapplication.utils.formatters.SignSwitcher;

import java.text.DateFormat;
import java.util.List;

/**
 * Created by LemonCream on 2018-03-02.
 */

public class TxListAdapter extends RecyclerView.Adapter<TxListAdapter.ViewHolder> {

    private static final String TAG = TxListAdapter.class.getSimpleName();

    private Context mContext;
    private List<TxListData> mData;

    private int mBagId;
    private String fsym;
    private String tsym;

    private float totalNetCost = 0;
    private float totalHoldingsValue = 0;
    private float totalProfit = 0;
    private float totalHoldings = 0;

    private boolean baseCurrencyDisplayMode = false;
    private boolean pctChangeDisplayMode = false;

    public TxListAdapter(Context mContext, List<TxListData> mData, int mBagId) {
        this.mContext = mContext;
        this.mData = mData;
        this.mBagId = mBagId;
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

        if (position == 0) resetTotalData();

        TxListData currentTxListData = mData.get(position);
        TxHistory currentTx = currentTxListData.getTxHistory();
        TxPriceData currentTxPriceData = currentTxListData.getTxPriceData();
        String orderType = currentTx.getOrderType();
        String sign = baseCurrencyDisplayMode ? SignSwitcher.getBaseCurrencySign() : SignSwitcher.getCurrencySign(tsym);


        float currentTsymPrice = currentTxPriceData.getCurrentPrice();
        float previousBasePrice = currentTxPriceData.getPreviousBasePrice() * currentTx.getTradePrice();
        float currentBasePrice = currentTxPriceData.getCurrentBasePrice() * currentTsymPrice;

        float currentPrice = baseCurrencyDisplayMode ? currentBasePrice : currentTsymPrice;
        float tradePrice = baseCurrencyDisplayMode ? previousBasePrice : currentTx.getTradePrice();
        float amount = currentTx.getAmount();
        float cost = tradePrice * amount;
        float profit = calculateProfit(currentPrice, tradePrice, amount);

        String tradePriceStr = sign + " " + (tradePrice <= -1 ? "No Data" : NumberFormatter.formatDecimals(tradePrice));
        String costStr = sign + " " + (cost <= -1 ? "No Data" : NumberFormatter.formatDecimals(cost));
        String profitStr = makeProfitToString(orderType, profit, currentPrice);
        String orderTypeDisplay = orderType.equals(TxHistory.ORDER_TYPE_BUY) ? "BUY" : "SELL";

        calculateTotalData(orderType, amount, currentTsymPrice, currentBasePrice, cost, profit);


        holder.exchangeText.setText(currentTx.getExchange().getName());
        holder.dateText.setText(DateFormat.getDateInstance().format(currentTx.getDate()));
        holder.orderTypeText.setText(orderTypeDisplay);
        holder.amountText.setText(String.valueOf(amount));
        holder.noteText.setText(currentTx.getNote() == null ? "-" : currentTx.getNote());
        holder.priceText.setText(tradePriceStr);
        holder.costText.setText(costStr);
        holder.profitText.setText(profitStr);
        holder.profitText.setTextColor(NumberFormatter.getProfitTextColor(profit));

        holder.layout.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, NewCoinActivity.class);
            intent.putExtra(NewCoinActivity.EXTRA_TX_KEY, currentTx.get_id());
            mContext.startActivity(intent);
        });
    }

    private void resetTotalData() {
        totalHoldings = 0;
        totalNetCost = 0;
        totalHoldingsValue = 0;
        totalProfit = 0;
    }

    private void calculateTotalData(String orderType, float amount, float currentTsymPrice, float currentBasePrice, float cost, float profit) {
        if (orderType.equals(TxHistory.ORDER_TYPE_BUY)) {
            this.totalHoldings += amount;
            this.totalHoldingsValue += (baseCurrencyDisplayMode ? (currentBasePrice * amount) : (currentTsymPrice * amount));
            this.totalNetCost += cost;
            this.totalProfit += profit;
        } else {
            this.totalHoldings -= amount;
        }
    }

    private float calculateProfit(float currentPrice, float tradePrice, float amount) {
            float priceDifference = currentPrice - tradePrice;
            return pctChangeDisplayMode ? priceDifference / tradePrice * 100 : priceDifference * amount;
    }

    private String makeProfitToString(String orderType, float profit, float currentPrice) {
        if (!orderType.equals(TxHistory.ORDER_TYPE_BUY)) return "";
        if (currentPrice < 0) return "No Data";
        return pctChangeDisplayMode ? NumberFormatter.largeDf.format(profit) + "%" : NumberFormatter.formatProfitDecimals(profit);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public List<TxListData> getmData() {
        return mData;
    }

    public void setmData(List<TxListData> mData) {
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

    public String getFsym() {
        return fsym;
    }

    public void setFsym(String fsym) {
        this.fsym = fsym;
    }

    public String getTsym() {
        return tsym;
    }

    public void setTsym(String tsym) {
        this.tsym = tsym;
    }

    public float getTotalNetCost() {
        return totalNetCost;
    }

    public void setTotalNetCost(float totalNetCost) {
        this.totalNetCost = totalNetCost;
    }

    public float getTotalHoldingsValue() {
        return totalHoldingsValue;
    }

    public void setTotalHoldingsValue(float totalHoldingsValue) {
        this.totalHoldingsValue = totalHoldingsValue;
    }

    public float getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(float totalProfit) {
        this.totalProfit = totalProfit;
    }

    public float getTotalHoldings() {
        return totalHoldings;
    }

    public void setTotalHoldings(float totalHoldings) {
        this.totalHoldings = totalHoldings;
    }

    public int getmBagId() {
        return mBagId;
    }

    public void setmBagId(int mBagId) {
        this.mBagId = mBagId;
    }
}
