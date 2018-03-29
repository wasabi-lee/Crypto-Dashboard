package com.example.lemoncream.myapplication.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.example.lemoncream.myapplication.Activity.NewAlertActivity;
import com.example.lemoncream.myapplication.Model.RealmModels.Alert;
import com.example.lemoncream.myapplication.R;
import com.example.lemoncream.myapplication.Utils.Formatters.NumberFormatter;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Wasabi on 3/18/2018.
 */

public class AlertListAdapter extends RecyclerView.Adapter<AlertListAdapter.ViewHolder> {

    public interface OnAlertActiveSwitchClickedListener {
        void onAlertActiveSwitchClicked(Alert currentAlert, boolean active);
    }

    private Context mContext;
    private RealmResults<Alert> mDataset;
    private OnAlertActiveSwitchClickedListener mListener;

    public AlertListAdapter(Context mContext, RealmResults<Alert> mDataset, OnAlertActiveSwitchClickedListener mListener) {
        this.mContext = mContext;
        this.mDataset = mDataset;
        this.mListener = mListener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        Context context;
        View layout;
        TextView mConditionText, mExchangeText, mOneTimeText;
        Switch mActiveSwitch;

        ViewHolder(View itemView, Context context) {
            super(itemView);
            this.context = context;
            layout = itemView;
            mConditionText = itemView.findViewById(R.id.item_alert_condition_text);
            mExchangeText = itemView.findViewById(R.id.item_alert_exchange_text);
            mOneTimeText = itemView.findViewById(R.id.item_alert_one_time_text);
            mActiveSwitch = itemView.findViewById(R.id.item_alert_active_switch);
        }

        void bind(Alert currentAlert, OnAlertActiveSwitchClickedListener listener) {
            layout.setOnClickListener(view -> {
                Intent intent = new Intent(context, NewAlertActivity.class);
                intent.putExtra(NewAlertActivity.EXTRA_ALERT_ID_KEY, currentAlert.get_id());
                context.startActivity(intent);
            });

            String conditionText = "";
            if (currentAlert != null) {
                if (currentAlert.getMoreThan() >= 0)
                    conditionText += ("\u2265" + currentAlert.getMoreThan());
                if (currentAlert.getLessThan() >= 0) {
                    if (!conditionText.isEmpty()) conditionText += "\n";
                    conditionText += ("\u2264" + currentAlert.getLessThan());
                }
                mConditionText.setText(conditionText);
                mExchangeText.setText(currentAlert.getExchange() == null ? "No exchange data" : currentAlert.getExchange().getName());
                mOneTimeText.setText(currentAlert.isOneTime() ? "One Time" : "Persistent");
                mActiveSwitch.setChecked(currentAlert.isActive());
                mActiveSwitch.setOnCheckedChangeListener((compoundButton, b) ->
                        listener.onAlertActiveSwitchClicked(currentAlert, b)
                );
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alert_list, parent, false);
        return new ViewHolder(v, mContext);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (mDataset == null || mDataset.size() == 0) return;
        if (holder == null || mDataset.get(position) == null) return;

        Alert currentAlert = mDataset.get(position);
        holder.bind(currentAlert, mListener);
    }


    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
