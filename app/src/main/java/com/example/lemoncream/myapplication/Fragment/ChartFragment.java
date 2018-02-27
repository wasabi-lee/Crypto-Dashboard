package com.example.lemoncream.myapplication.Fragment;


import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.lemoncream.myapplication.Activity.CoinDetailActivity;
import com.example.lemoncream.myapplication.CustomViews.CandleChart;
import com.example.lemoncream.myapplication.Model.GsonModels.ChartData;
import com.example.lemoncream.myapplication.Model.GsonModels.Datum;
import com.example.lemoncream.myapplication.Model.RealmModels.Bag;
import com.example.lemoncream.myapplication.Model.RealmModels.Pair;
import com.example.lemoncream.myapplication.Model.RealmModels.TxHistory;
import com.example.lemoncream.myapplication.Network.ChartDataService;
import com.example.lemoncream.myapplication.Network.RetrofitHelper;
import com.example.lemoncream.myapplication.R;
import com.example.lemoncream.myapplication.Utils.ChartData.ChartTimeframe;
import com.example.lemoncream.myapplication.Utils.ChartData.ChartTimeframeConfig;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import retrofit2.Retrofit;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChartFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = ChartFragment.class.getSimpleName();
    private Bag mCurrentBag;
    private Realm mRealm;

    @BindView(R.id.chart_header_exchange_pair_text)
    TextView mChartHeaderText;
    @BindView(R.id.chart_current_price_text)
    TextView mCurrentPriceText;
    @BindView(R.id.chart_change_text)
    TextView mChangeText;

    @BindView(R.id.chart_candlestick_chart)
    CandleChart mCandleChart;
    @BindView(R.id.chart_timeframe_text_one_day)
    TextView mTimeframeOneDay;
    @BindView(R.id.chart_timeframe_text_three_day)
    TextView mTimeframeThreeDay;
    @BindView(R.id.chart_timeframe_text_one_week)
    TextView mTimeframeOneWeek;
    @BindView(R.id.chart_timeframe_text_one_month)
    TextView mTimeframeOneMonth;
    @BindView(R.id.chart_timeframe_text_three_months)
    TextView mTimeframeThreeMonth;
    @BindView(R.id.chart_timeframe_text_six_months)
    TextView mTimeframeSixMonths;
    @BindView(R.id.chart_timeframe_text_one_year)
    TextView mTimeframeOneYear;

    @BindView(R.id.chart_high_text)
    TextView mHighText;
    @BindView(R.id.chart_low_text)
    TextView mLowText;
    @BindView(R.id.chart_volume_text)
    TextView mVolumeText;

    private HashMap<String, ChartTimeframe> mTimeframeSettings;
    private String mCurrentTimeframe = ChartTimeframeConfig.TIMEFRAME_ONE_DAY;

    public ChartFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRealm = Realm.getDefaultInstance();

        if (getArguments() != null) {
            int receivedId = getArguments().getInt(CoinDetailActivity.EXTRA_PAIR_KEY, -1);
            mCurrentBag = mRealm.where(Bag.class).equalTo("_id", receivedId).findFirst();
        } else {
            //TODO Display error message
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chart, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTimeframeSettings = ChartTimeframeConfig.getDefaultTimeframeSettings();
        setOnClickListeners();
        configCandleStickChart();
        requestChartData(mTimeframeSettings.get(ChartTimeframeConfig.TIMEFRAME_ONE_DAY));

    }

    public void setOnClickListeners() {
        mTimeframeOneDay.setOnClickListener(this);
        mTimeframeThreeDay.setOnClickListener(this);
        mTimeframeOneWeek.setOnClickListener(this);
        mTimeframeOneMonth.setOnClickListener(this);
        mTimeframeThreeMonth.setOnClickListener(this);
        mTimeframeSixMonths.setOnClickListener(this);
        mTimeframeOneYear.setOnClickListener(this);
    }

    public void configCandleStickChart() {
        mCandleChart.configSelf();
        mCandleChart.configAxis();
    }

    public void requestChartData(ChartTimeframe timeframe) {

        if (mCurrentBag == null) return;
        Pair currentPair = mCurrentBag.getTradePair();
        if (currentPair == null) return;
        TxHistory latestTx = mRealm.where(TxHistory.class)
                .equalTo("txHolder.tradePair.pairName", mCurrentBag.getTradePair().getPairName())
                .findAllSorted("date").last();
        if (latestTx == null) return;
        String exchange = latestTx.getExchange().getName();
        Log.d(TAG, "requestChartData: " + exchange);

        Retrofit retrofit = RetrofitHelper
                .createRetrofitWithRxConverter(getResources().getString(R.string.base_url));
        ChartDataService chartDataService = retrofit.create(ChartDataService.class);
        Observable<ChartData> chartDataObservable = chartDataService
                .getChartData(timeframe.getChartUrlPath(),
                        currentPair.getfCoin().getSymbol(), currentPair.gettCoin().getSymbol(),
                        timeframe.getAggregate(), exchange, 1440);

        chartDataObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<ChartData>() {
                    @Override
                    public void onNext(ChartData chartData) {
                        parseChartResponse(chartData.getData());
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }
                });
    }

    private void parseChartResponse(List<Datum> data) {
        Log.d(TAG, "parseChartResponse: ");
        final DateFormat df = DateFormat.getDateInstance();
        ArrayList<CandleEntry> yVals = new ArrayList<>();
        int numOfBars = mTimeframeSettings.get(mCurrentTimeframe).getNumOfBars();

        for (int i = data.size()-1; i >= (data.size() - numOfBars); i--) {
            Datum curruntData = data.get(i);
            yVals.add(0, new CandleEntry(i,
                    curruntData.getHigh(), curruntData.getLow(),
                    curruntData.getOpen(), curruntData.getClose()));
        }

        CandleDataSet dataset = new CandleDataSet(yVals, "");
        configDataset(dataset);

        CandleData resultData = new CandleData(dataset);
        mCandleChart.setData(resultData);
        mCandleChart.invalidate();
    }

    public void configDataset(CandleDataSet dataSet) {
        dataSet.setDrawValues(false);
        dataSet.setDrawIcons(false);
        dataSet.setShadowColor(Color.DKGRAY);
        dataSet.setShadowWidth(0.7f);
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSet.setDecreasingColor(Color.RED);
        dataSet.setDecreasingPaintStyle(Paint.Style.FILL);
        dataSet.setIncreasingColor(Color.GREEN);
        dataSet.setIncreasingPaintStyle(Paint.Style.FILL);
        dataSet.setNeutralColor(Color.BLUE);
    }


    private void onTimeframeChanged(String timeframeKey) {
        mCurrentTimeframe = timeframeKey;
        requestChartData(mTimeframeSettings.get(timeframeKey));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.chart_timeframe_text_one_day:
                onTimeframeChanged(ChartTimeframeConfig.TIMEFRAME_ONE_DAY);
                break;
            case R.id.chart_timeframe_text_three_day:
                onTimeframeChanged(ChartTimeframeConfig.TIMEFRAME_THREE_DAY);
                break;
            case R.id.chart_timeframe_text_one_week:
                onTimeframeChanged(ChartTimeframeConfig.TIMEFRAME_ONE_WEEK);
                break;
            case R.id.chart_timeframe_text_one_month:
                onTimeframeChanged(ChartTimeframeConfig.TIMEFRAME_ONE_MONTH);
                break;
            case R.id.chart_timeframe_text_three_months:
                onTimeframeChanged(ChartTimeframeConfig.TIMEFRAME_THREE_MONTHS);
                break;
            case R.id.chart_timeframe_text_six_months:
                onTimeframeChanged(ChartTimeframeConfig.TIMEFRAME_SIX_MONTHS);
                break;
            case R.id.chart_timeframe_text_one_year:
                onTimeframeChanged(ChartTimeframeConfig.TIMEFRAME_ONE_YEAR);
                break;
        }
    }
}
