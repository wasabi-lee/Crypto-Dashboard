package com.example.lemoncream.myapplication.Utils.ChartData;

import java.util.HashMap;

/**
 * Created by LemonCream on 2018-02-27.
 */

public class ChartTimeframeConfig {

    public static final String TIMEFRAME_ONE_DAY = "1d";
    public static final int TIMEFRAME_ONE_DAY_AGGREGATE = 20;
    public static final int TIMEFRAME_ONE_DAY_NUM_OF_CANDLES = 72;
    public static final String TIMEFRAME_ONE_DAY_URL_PATH = ChartUrlPath.CHART_URL_HISTOMINUTE;

    public static final String TIMEFRAME_THREE_DAY = "3d";
    public static final int TIMEFRAME_THREE_DAY_AGGREGATE = 1;
    public static final int TIMEFRAME_THREE_DAY_NUM_OF_CANDLES = 72;
    public static final String TIMEFRAME_THREE_DAY_URL_PATH = ChartUrlPath.CHART_URL_HISTOHOUR;

    public static final String TIMEFRAME_ONE_WEEK = "1w";
    public static final int TIMEFRAME_ONE_WEEK_AGGREGATE = 2;
    public static final int TIMEFRAME_ONE_WEEK_NUM_OF_CANDLES = 84;
    public static final String TIMEFRAME_ONE_WEEK_URL_PATH = ChartUrlPath.CHART_URL_HISTOHOUR;

    public static final String TIMEFRAME_ONE_MONTH = "1m";
    public static final int TIMEFRAME_ONE_MONTH_AGGREGATE = 8;
    public static final int TIMEFRAME_ONE_MONTH_NUM_OF_CANDLES = 90;
    public static final String TIMEFRAME_ONE_MONTH_URL_PATH = ChartUrlPath.CHART_URL_HISTOHOUR;

    public static final String TIMEFRAME_THREE_MONTHS = "3m";
    public static final int TIMEFRAME_THREE_MONTHS_AGGREGATE = 1;
    public static final int TIMEFRAME_THREE_MONTH_NUM_OF_CANDLES = 90;
    public static final String TIMEFRAME_THREE_MONTHS_URL_PATH = ChartUrlPath.CHART_URL_HISTODAY;

    public static final String TIMEFRAME_SIX_MONTHS = "6m";
    public static final int TIMEFRAME_SIX_MONTHS_AGGREGATE = 2;
    public static final int TIMEFRAME_SIX_MONTH_NUM_OF_CANDLES = 90;
    public static final String TIMEFRAME_SIX_MONTHS_URL_PATH = ChartUrlPath.CHART_URL_HISTODAY;

    public static final String TIMEFRAME_ONE_YEAR = "1y";
    public static final int TIMEFRAME_ONE_YERR_AGGREGATE = 5;
    public static final int TIMEFRAME_ONE_YEAR_NUM_OF_CANDLES = 73;
    public static final String TIMEFRAME_ONE_YEAR_URL_PATH = ChartUrlPath.CHART_URL_HISTODAY;


    public static HashMap<String, ChartTimeframe> getDefaultTimeframeSettings() {

        // Generates API call components. Key: Timeframe name / Value: Timeframe data
        HashMap<String, ChartTimeframe> timeframeSettings = new HashMap<>();
        timeframeSettings.put(TIMEFRAME_ONE_DAY, new ChartTimeframe(TIMEFRAME_ONE_DAY_AGGREGATE, TIMEFRAME_ONE_DAY_NUM_OF_CANDLES, TIMEFRAME_ONE_DAY_URL_PATH));
        timeframeSettings.put(TIMEFRAME_THREE_DAY, new ChartTimeframe(TIMEFRAME_THREE_DAY_AGGREGATE, TIMEFRAME_THREE_DAY_NUM_OF_CANDLES, TIMEFRAME_THREE_DAY_URL_PATH));
        timeframeSettings.put(TIMEFRAME_ONE_WEEK, new ChartTimeframe(TIMEFRAME_ONE_WEEK_AGGREGATE, TIMEFRAME_ONE_WEEK_NUM_OF_CANDLES, TIMEFRAME_ONE_WEEK_URL_PATH));
        timeframeSettings.put(TIMEFRAME_ONE_MONTH, new ChartTimeframe(TIMEFRAME_ONE_MONTH_AGGREGATE, TIMEFRAME_ONE_MONTH_NUM_OF_CANDLES, TIMEFRAME_ONE_MONTH_URL_PATH));
        timeframeSettings.put(TIMEFRAME_THREE_MONTHS, new ChartTimeframe(TIMEFRAME_THREE_MONTHS_AGGREGATE, TIMEFRAME_THREE_MONTH_NUM_OF_CANDLES, TIMEFRAME_THREE_MONTHS_URL_PATH));
        timeframeSettings.put(TIMEFRAME_SIX_MONTHS, new ChartTimeframe(TIMEFRAME_SIX_MONTHS_AGGREGATE, TIMEFRAME_SIX_MONTH_NUM_OF_CANDLES, TIMEFRAME_SIX_MONTHS_URL_PATH));
        timeframeSettings.put(TIMEFRAME_ONE_YEAR, new ChartTimeframe(TIMEFRAME_ONE_YERR_AGGREGATE, TIMEFRAME_ONE_YEAR_NUM_OF_CANDLES, TIMEFRAME_ONE_YEAR_URL_PATH));

        return timeframeSettings;
    }

}


