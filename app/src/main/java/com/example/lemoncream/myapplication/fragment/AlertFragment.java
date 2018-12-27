package com.example.lemoncream.myapplication.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.lemoncream.myapplication.activity.CoinDetailActivity;
import com.example.lemoncream.myapplication.activity.NewAlertActivity;
import com.example.lemoncream.myapplication.adapter.AlertListAdapter;
import com.example.lemoncream.myapplication.background.AlertDispatchHelper;
import com.example.lemoncream.myapplication.model.realm.Alert;
import com.example.lemoncream.myapplication.model.realm.Bag;
import com.example.lemoncream.myapplication.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class AlertFragment extends Fragment implements View.OnClickListener, AlertListAdapter.OnAlertActiveSwitchClickedListener {

    private static final String TAG = AlertFragment.class.getSimpleName();
    public static final int REQUEST_CODE_ALERT_FRAG = 43464;

    private boolean mFirstRun = true;

    @BindView(R.id.alert_frag_recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.alert_frag_add_fab)
    FloatingActionButton mAddAlertFab;

    private Realm mRealm;
    private RealmResults<Alert> mDataset;
    private AlertListAdapter mAdapter;

    private int mBagId;
    private Bag mCurrentBag;
    private String mFsym, mTsym;

    public AlertFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRealm = Realm.getDefaultInstance();
        if (getArguments() != null) {
            unpackInitialData(getArguments());
        } else {
            Toast.makeText(getContext(), "Unexpected error occurred. Please try again later.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_alert, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //1) Inflate recycler view
        populateRecyclerView(loadDataset());

        //2) Connect the button to AddAlertActivity along with the bag details.
        mAddAlertFab.setOnClickListener(this);

    }

    private RealmResults<Alert> loadDataset() {
        mDataset = mRealm.where(Alert.class).equalTo("bag._id", mBagId).findAll();
        return mDataset;
    }

    private void populateRecyclerView(RealmResults<Alert> dataset) {
        mAdapter = new AlertListAdapter(getContext(), dataset, this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);
    }

    public void unpackInitialData(Bundle args) {
        if (args != null) {
            mBagId = getArguments().getInt(CoinDetailActivity.EXTRA_PAIR_KEY, -1);
            mCurrentBag = mRealm.where(Bag.class).equalTo("_id", mBagId).findFirst();
            if (mCurrentBag != null && mCurrentBag.getTradePair() != null) {
                mFsym = mCurrentBag.getTradePair().getfCoin().getSymbol();
                mTsym = mCurrentBag.getTradePair().gettCoin().getSymbol();
            }
        }
    }


    private void repopulateList() {
        if (mAdapter == null) {
            populateRecyclerView(loadDataset());
        } else {
            loadDataset();
        }
        mAdapter.notifyDataSetChanged();
    }

    private void scheduleAlertJobService() {
        if (isAlertInDB(mRealm)) {
            AlertDispatchHelper.scheduleJob(getContext());
        } else {
            AlertDispatchHelper.cancelJob(getContext());
        }
    }

    private boolean isAlertInDB(Realm realm) {
        RealmResults<Alert> alerts = realm.where(Alert.class).findAll();
        boolean isAlertInDB = alerts.size() != 0;
        Log.d(TAG, "isAlertInDB: " + isAlertInDB);
        for (int i = 0; i < alerts.size(); i++) {
            Log.d(TAG, "isAlertInDB: " + alerts.get(i).toString());
        }
        return isAlertInDB;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.alert_frag_add_fab:
                Intent intent = new Intent(getContext(), NewAlertActivity.class);
                intent.putExtra(NewAlertActivity.EXTRA_BAG_ID_KEY, mBagId);
                startActivityForResult(intent, REQUEST_CODE_ALERT_FRAG);
                break;
        }
    }

    @Override
    public void onAlertActiveSwitchClicked(Alert currentAlert, boolean active) {
        mRealm.executeTransaction(realm -> {
            currentAlert.setActive(!currentAlert.isActive());
            realm.copyToRealmOrUpdate(currentAlert);
        });
        String toastMessage = (active ? "Enabled " : "Paused ") + "the alert";
        Toast.makeText(getContext(), toastMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ALERT_FRAG) {
            if (resultCode == RESULT_OK) {
                repopulateList();
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mFirstRun) {
            mFirstRun = false;
        } else {
            scheduleAlertJobService();
            repopulateList();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }
}
