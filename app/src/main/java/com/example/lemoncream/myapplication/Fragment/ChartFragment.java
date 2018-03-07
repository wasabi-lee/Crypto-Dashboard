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
import com.example.lemoncream.myapplication.Adapter.ViewPagerAdapter;
import com.example.lemoncream.myapplication.CustomViews.CandleChart;
import com.example.lemoncream.myapplication.Model.Deserializers.PriceDeserializer;
import com.example.lemoncream.myapplication.Model.GsonModels.ChartData;
import com.example.lemoncream.myapplication.Model.GsonModels.Datum;
import com.example.lemoncream.myapplication.Model.GsonModels.PriceFull;
import com.example.lemoncream.myapplication.Model.GsonModels.PriceDetail;
import com.example.lemoncream.myapplication.Model.RealmModels.Bag;
import com.example.lemoncream.myapplication.Model.RealmModels.Pair;
import com.example.lemoncream.myapplication.Model.RealmModels.TxHistory;
import com.example.lemoncream.myapplication.Network.ChartDataService;
import com.example.lemoncream.myapplication.Network.GsonHelper;
import com.example.lemoncream.myapplication.Network.PriceService;
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
import io.realm.RealmResults;
import io.realm.Sort;
import retrofit2.Retrofit;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChartFragment extends Fragment implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private static final String TAG = ChartFragment.class.getSimpleName();
    private OnUnitToggleListener mCallback;

    private Bag mCurrentBag;
    private String mExchange, mFsym, mTsym;
    private Realm mRealm;

    @BindView(R.id.chart_header_exchange_pair_text)
    TextView mChartHeaderText;
    @BindView(R.id.chart_current_price_text)
    TextView mCurrentPriceText;
    @BindView(R.id.chart_change_text)
    TextView mChangeText;

    @BindView(R.id.chart_candlestick_chart)
    CandleChart mCandleChart;

    @BindView(R.id.chart_timeframe_radio_group)
    RadioGroup mTimeframeRadioGroup;
    @BindView(R.id.chart_timeframe_radio_one_day)
    RadioButton mTimeframeOneDay;
    @BindView(R.id.chart_timeframe_radio_three_day)
    RadioButton mTimeframeThreeDay;
    @BindView(R.id.chart_timeframe_radio_one_week)
    RadioButton mTimeframeOneWeek;
    @BindView(R.id.chart_timeframe_radio_one_month)
    RadioButton mTimeframeOneMonth;
    @BindView(R.id.chart_timeframe_raio_three_months)
    RadioButton mTimeframeThreeMonth;
    @BindView(R.id.chart_timeframe_radio_six_months)
    RadioButton mTimeframeSixMonths;
    @BindView(R.id.chart_timeframe_radio_one_year)
    RadioButton mTimeframeOneYear;

    @BindView(R.id.chart_high_text)
    TextView mHighText;
    @BindView(R.id.chart_low_text)
    TextView mLowText;
    @BindView(R.id.chart_volume_text)
    TextView mVolumeText;

    private HashMap<String, ChartTimeframe> mTimeframeSettings;
    private String mCurrentTimeframe = ChartTimeframeConfig.TIMEFRAME_ONE_DAY;
    private PriceFull mPriceInfo;

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
                    + " must implement OnUnitToggledListener");
        }
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
        requestCurrentPrice();
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
                    .sort("date", Sort.ASCENDING).findFirst();
            mExchange = latestTx == null ? "CCCAGG" : latestTx.getExchange().getName();
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

    public void requestCurrentPrice() {
        Retrofit retrofit = RetrofitHelper
                .createRetrofitWithRxConverter(getResources().getString(R.string.base_url),
                        GsonHelper.createGsonBuilder(PriceFull.class, new PriceDeserializer()).create());
        Observable<PriceFull> tsymPriceObservable = retrofit.create(PriceService.class).getMultipleCurrentPrices(mFsym, mTsym, mExchange);
        Observable<PriceFull> baseBtcPriceObservable = retrofit.create(PriceService.class).getMultipleCurrentPrices(mFsym, SignSwitcher.BASE_CURRENCY + ",BTC");

        Observable.zip(tsymPriceObservable, baseBtcPriceObservable, (tsymPrice, baseBtcPrice) -> {
            tsymPrice.setBasePriceDetail(baseBtcPrice.getBasePriceDetail());
            tsymPrice.setBtcPriceDetail(baseBtcPrice.getBtcPriceDetail());
            return tsymPrice;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(price -> {
                            if (price == null) return;
                            mPriceInfo = price;
                            parseData(baseCurrencyDisplayMode, pctChangeDisplayMode);
                        }, Throwable::printStackTrace,
                        () -> Log.d(TAG, "requestCurrentPrice: onComplete"));
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

    public void parseData(boolean baseCurrencyDisplayMode, boolean pctChangeDisplayMode) {
        if (mPriceInfo == null) return;
        float price = baseCurrencyDisplayMode ? mPriceInfo.getBasePriceDetail().getPrice() : mPriceInfo.getTsymPriceDetail().getPrice();
        float change = pctChangeDisplayMode ?
                (baseCurrencyDisplayMode ? mPriceInfo.getBasePriceDetail().getChangePercent24hr() : mPriceInfo.getTsymPriceDetail().getChangePercent24hr()) :
                (baseCurrencyDisplayMode ? mPriceInfo.getBasePriceDetail().getChange24hr() : mPriceInfo.getTsymPriceDetail().getChange24hr());

        float volume24hr = baseCurrencyDisplayMode ? mPriceInfo.getBasePriceDetail().getVolume24hr() : mPriceInfo.getTsymPriceDetail().getVolume24hr();
        float high24hr = baseCurrencyDisplayMode ? mPriceInfo.getBasePriceDetail().getHigh24hr() : mPriceInfo.getTsymPriceDetail().getHigh24hr();
        float low24hr = baseCurrencyDisplayMode ? mPriceInfo.getBasePriceDetail().getLow24hr() : mPriceInfo.getTsymPriceDetail().getLow24hr();

        parsePriceData(price, change);
        parse24hrMarketData(volume24hr, high24hr, low24hr);
    }

    private void parsePriceData(float price, float change) {
        String sign = baseCurrencyDisplayMode ? SignSwitcher.getBaseCurrencySign() : SignSwitcher.getCurrencySign(mTsym);
        String priceStr = NumberFormatter.formatDecimals(price);
        mCurrentPriceText.setText(sign + " " + priceStr);

        String changeStr = NumberFormatter.formatDecimals(change);
        mChangeText.setText(pctChangeDisplayMode ? changeStr + " %" : changeStr);
        mChangeText.setTextColor(NumberFormatter.getProfitTextColor(change));
    }

    private void parse24hrMarketData(float volume, float high, float low) {
        Log.d(TAG, "parse24hrMarketData: " + volume + ", " + high + ", " + low);
        mVolumeText.setText(NumberFormatter.formatDecimals(volume));
        mHighText.setText(NumberFormatter.formatDecimals(high));
        mLowText.setText(NumberFormatter.formatDecimals(low));
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
                mCallback.onUnitToggled(baseCurrencyDisplayMode, pctChangeDisplayMode);
                break;
            case R.id.chart_change_text:
                pctChangeDisplayMode = !pctChangeDisplayMode;
                mCallback.onUnitToggled(baseCurrencyDisplayMode, pctChangeDisplayMode);
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
