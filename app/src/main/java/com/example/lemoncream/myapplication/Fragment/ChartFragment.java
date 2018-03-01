package com.example.lemoncream.myapplication.Fragment;


import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.lemoncream.myapplication.Activity.CoinDetailActivity;
import com.example.lemoncream.myapplication.CustomViews.CandleChart;
import com.example.lemoncream.myapplication.Model.GsonModels.ChartData;
import com.example.lemoncream.myapplication.Model.GsonModels.Datum;
import com.example.lemoncream.myapplication.Model.GsonModels.PriceFull;
import com.example.lemoncream.myapplication.Model.GsonModels.PriceDetail;
import com.example.lemoncream.myapplication.Model.RealmModels.Bag;
import com.example.lemoncream.myapplication.Model.RealmModels.Pair;
import com.example.lemoncream.myapplication.Model.RealmModels.TxHistory;
import com.example.lemoncream.myapplication.Network.ChartDataService;
import com.example.lemoncream.myapplication.Network.RetrofitHelper;
import com.example.lemoncream.myapplication.R;
import com.example.lemoncream.myapplication.Utils.Callbacks.OnUnitToggleListener;
import com.example.lemoncream.myapplication.Utils.ChartData.ChartTimeframe;
import com.example.lemoncream.myapplication.Utils.ChartData.ChartTimeframeConfig;
import com.example.lemoncream.myapplication.Utils.Formatters.NumberFormatter;
import com.example.lemoncream.myapplication.Utils.Formatters.SignSwitcher;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import retrofit2.Retrofit;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChartFragment extends Fragment implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private static final String TAG = ChartFragment.class.getSimpleName();
    private OnUnitToggleListener mCallback;

    private Bag mCurrentBag;
    private PriceDetail mTsymPriceDetail;
    private String mExchange, mFsym, mTsym;
    private Realm mRealm;

    @BindView(R.id.chart_header_exchange_pair_text)  TextView mChartHeaderText;
    @BindView(R.id.chart_current_price_text)  TextView mCurrentPriceText;
    @BindView(R.id.chart_change_text)  TextView mChangeText;

    @BindView(R.id.chart_candlestick_chart)  CandleChart mCandleChart;

    @BindView(R.id.chart_timeframe_radio_group)  RadioGroup mTimeframeRadioGroup;
    @BindView(R.id.chart_timeframe_radio_one_day)  RadioButton mTimeframeOneDay;
    @BindView(R.id.chart_timeframe_radio_three_day)  RadioButton mTimeframeThreeDay;
    @BindView(R.id.chart_timeframe_radio_one_week)  RadioButton mTimeframeOneWeek;
    @BindView(R.id.chart_timeframe_radio_one_month)  RadioButton mTimeframeOneMonth;
    @BindView(R.id.chart_timeframe_raio_three_months)  RadioButton mTimeframeThreeMonth;
    @BindView(R.id.chart_timeframe_radio_six_months)  RadioButton mTimeframeSixMonths;
    @BindView(R.id.chart_timeframe_radio_one_year)  RadioButton mTimeframeOneYear;

    @BindView(R.id.chart_high_text)  TextView mHighText;
    @BindView(R.id.chart_low_text)  TextView mLowText;
    @BindView(R.id.chart_volume_text)  TextView mVolumeText;

    private HashMap<String, ChartTimeframe> mTimeframeSettings;
    private String mCurrentTimeframe = ChartTimeframeConfig.TIMEFRAME_ONE_DAY;

    DecimalFormat dfVol = new DecimalFormat("#.##");
    DecimalFormat dfPrice = new DecimalFormat("#.#######");

    private boolean baseCurrencyDisplayMode = false;
    private boolean pctChangeDisplayMode = false;

    public ChartFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (OnUnitToggleListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnUnitToggledListener");}
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRealm = Realm.getDefaultInstance();
        if (getArguments() != null) {
            unpackInitialData(getArguments());
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
        setInitialData();
        setListeners();
        configCandleStickChart();

        requestChartData(mTimeframeSettings.get(ChartTimeframeConfig.TIMEFRAME_ONE_DAY));

    }

    public void unpackInitialData(Bundle args) {
        if (args != null) {
            int receivedId = getArguments().getInt(CoinDetailActivity.EXTRA_PAIR_KEY, -1);
            mCurrentBag = mRealm.where(Bag.class).equalTo("_id", receivedId).findFirst();

            if (mCurrentBag != null && mCurrentBag.getTradePair() != null) {
                mFsym = mCurrentBag.getTradePair().getfCoin().getSymbol();
                mTsym = mCurrentBag.getTradePair().gettCoin().getSymbol();
            }

            TxHistory latestTx = mRealm.where(TxHistory.class)
                    .equalTo("txHolder.tradePair.pairName", mCurrentBag.getTradePair().getPairName())
                    .findAllSorted("date").last();
            if (latestTx == null) return;
            mExchange = latestTx.getExchange().getName();
        }
    }

    public void setInitialData() {
        String currentPageInfo = mExchange + " - " + mFsym + "/" + mTsym;
        mChartHeaderText.setText(currentPageInfo);
    }

    public void setListeners() {
        mTimeframeRadioGroup.setOnCheckedChangeListener(this);
        mTimeframeOneDay.setOnClickListener(this);
        mTimeframeThreeDay.setOnClickListener(this);
        mTimeframeOneWeek.setOnClickListener(this);
        mTimeframeOneMonth.setOnClickListener(this);
        mTimeframeThreeMonth.setOnClickListener(this);
        mTimeframeSixMonths.setOnClickListener(this);
        mTimeframeOneYear.setOnClickListener(this);
        mCurrentPriceText.setOnClickListener(this);
    }

    public void configCandleStickChart() {
        mCandleChart.configSelf();
        mCandleChart.configAxis();
    }


    public void requestChartData(ChartTimeframe timeframe) {
        if (mCurrentBag == null) return;
        Pair currentPair = mCurrentBag.getTradePair();

        if (currentPair == null) return;
        Retrofit retrofit = RetrofitHelper
                .createRetrofitWithRxConverter(getResources().getString(R.string.base_url));
        ChartDataService chartDataService = retrofit.create(ChartDataService.class);
        Observable<ChartData> chartDataObservable = chartDataService
                .getChartData(timeframe.getChartUrlPath(),
                        mFsym, mTsym, timeframe.getAggregate(), mExchange, 1440);

        chartDataObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(chartData -> parseChartData(chartData.getData()),
                        Throwable::printStackTrace);
    }


    private void parseChartData(List<Datum> data) {
        ArrayList<CandleEntry> yVals = new ArrayList<>();
        int numOfBars = mTimeframeSettings.get(mCurrentTimeframe).getNumOfBars();
        for (int i = data.size() - 1; i >= (data.size() - numOfBars); i--) {
            Datum curruntData = data.get(i);
            yVals.add(0, new CandleEntry(i,
                    curruntData.getHigh(), curruntData.getLow(),
                    curruntData.getOpen(), curruntData.getClose()));
        }
        populateCandleStickChart(yVals);
    }

    public void parseData(PriceFull priceInfo, boolean baseCurrencyDisplayMode, boolean pctChangeDisplayMode) {
        float price = baseCurrencyDisplayMode ? priceInfo.getBasePriceDetail().getPrice() : priceInfo.getTsymPriceDetail().getPrice();
        float change = pctChangeDisplayMode ?
                (baseCurrencyDisplayMode ? priceInfo.getBasePriceDetail().getChangePercent24hr() : priceInfo.getTsymPriceDetail().getChangePercent24hr()) :
                (baseCurrencyDisplayMode ? priceInfo.getBasePriceDetail().getChange24hr() : priceInfo.getTsymPriceDetail().getChange24hr());

        float volume24hr = baseCurrencyDisplayMode ? priceInfo.getBasePriceDetail().getVolume24hr() : priceInfo.getTsymPriceDetail().getVolume24hr();
        float high24hr = baseCurrencyDisplayMode ? priceInfo.getBasePriceDetail().getHigh24hr() : priceInfo.getTsymPriceDetail().getHigh24hr();
        float low24hr = baseCurrencyDisplayMode ? priceInfo.getBasePriceDetail().getLow24hr() : priceInfo.getTsymPriceDetail().getLow24hr();

        parsePriceData(price, change);
        parse24hrMarketData(volume24hr, high24hr, low24hr);
    }

    private void parsePriceData(float price, float change) {
        String sign = baseCurrencyDisplayMode ? SignSwitcher.getBaseCurrencySign() : SignSwitcher.getCurrencySign(mTsym);
        String priceStr = price > 99 ? NumberFormatter.largeDf.format(price) : NumberFormatter.smallDf.format(price);
        mCurrentPriceText.setText(sign + " " + priceStr);

        String changeStr = change > 99 ? NumberFormatter.largeDf.format(change) : NumberFormatter.smallDf.format(change);
        mChangeText.setText(pctChangeDisplayMode ? changeStr + " %" : changeStr);
        if (change < 0) {
            mChangeText.setTextColor(Color.RED);
        } else if (change > 0) {
            mChangeText.setTextColor(Color.GREEN);
        }
    }

    private void parse24hrMarketData(float volume, float high, float low) {
        Log.d(TAG, "parse24hrMarketData: " + volume + ", " + high + ", " + low);
        mVolumeText.setText(String.valueOf(volume > 99 ? NumberFormatter.largeDf.format(volume) : NumberFormatter.smallDf.format(volume)));
        mHighText.setText(String.valueOf(high > 99 ? NumberFormatter.largeDf.format(high) : NumberFormatter.smallDf.format(high)));
        mLowText.setText(String.valueOf(low > 99 ? NumberFormatter.largeDf.format(low) : NumberFormatter.smallDf.format(low)));
    }

    private void populateCandleStickChart(ArrayList<CandleEntry> yVals) {
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
            case R.id.chart_current_price_text:
                baseCurrencyDisplayMode = !baseCurrencyDisplayMode;
                pctChangeDisplayMode = !pctChangeDisplayMode;
                mCallback.onUnitToggled(baseCurrencyDisplayMode, pctChangeDisplayMode);
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
        switch (checkedId) {
            case R.id.chart_timeframe_radio_one_day:
                onTimeframeChanged(ChartTimeframeConfig.TIMEFRAME_ONE_DAY);
                break;
            case R.id.chart_timeframe_radio_three_day:
                onTimeframeChanged(ChartTimeframeConfig.TIMEFRAME_THREE_DAY);
                break;
            case R.id.chart_timeframe_radio_one_week:
                onTimeframeChanged(ChartTimeframeConfig.TIMEFRAME_ONE_WEEK);
                break;
            case R.id.chart_timeframe_radio_one_month:
                onTimeframeChanged(ChartTimeframeConfig.TIMEFRAME_ONE_MONTH);
                break;
            case R.id.chart_timeframe_raio_three_months:
                onTimeframeChanged(ChartTimeframeConfig.TIMEFRAME_THREE_MONTHS);
                break;
            case R.id.chart_timeframe_radio_six_months:
                onTimeframeChanged(ChartTimeframeConfig.TIMEFRAME_SIX_MONTHS);
                break;
            case R.id.chart_timeframe_radio_one_year:
                onTimeframeChanged(ChartTimeframeConfig.TIMEFRAME_ONE_YEAR);
                break;
        }
    }
}
