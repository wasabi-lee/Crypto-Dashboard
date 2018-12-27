package com.example.lemoncream.myapplication.custom;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.example.lemoncream.myapplication.model.gson.ChartData;
import com.example.lemoncream.myapplication.model.gson.Datum;
import com.example.lemoncream.myapplication.model.temp.ChartParams;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wasabi on 3/31/2018.
 */

public class PriceLineChart extends LineChart {

    private static final String TAG = PriceLineChart.class.getSimpleName();

    public PriceLineChart(Context context) {
        super(context);
    }

    public PriceLineChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PriceLineChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void initChart() {
        setViewPortOffsets(0f, 80f, 0f, 0f);
        setDrawGridBackground(false);
        setTouchEnabled(false);
        getDescription().setTextColor(Color.WHITE);
        getDescription().setText("");
        setContentDescription("");
        setNoDataText("");
        getLegend().setEnabled(false);

        getAxisLeft().setDrawLabels(false);
        getAxisLeft().setDrawAxisLine(false);
        getAxisLeft().setDrawGridLines(false);
        getAxisLeft().setEnabled(false);

        getAxisRight().setDrawLabels(false);
        getAxisRight().setDrawAxisLine(false);
        getAxisRight().setDrawGridLines(false);
        getAxisRight().setEnabled(false);

        getXAxis().setDrawLabels(false);
        getXAxis().setDrawGridLines(false);
        getXAxis().setDrawGridLines(false);
        getXAxis().setEnabled(false);
    }

    public void setNewData(ChartParams data) {

        fade(true, data);


    }

    private void fade(boolean fadeOut, ChartParams data) {
        float from = fadeOut ? 1f : 0f;
        float to = fadeOut ? 0f : 1f;
        AlphaAnimation anim = new AlphaAnimation(from, to);
        anim.setDuration(1000);
        anim.setRepeatCount(0);
        if (fadeOut) {
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    ChartData chartData = data.getmChartData();
                    List<Datum> ochls = chartData.getData();

                    ArrayList<Entry> vals = new ArrayList<>();
                    for (int i = 0; i < ochls.size(); i++) {
                        vals.add(new Entry(i, ochls.get(i).getClose()));
                    }

                    LineDataSet set1 = new LineDataSet(vals, "Dataset 1");
                    set1.setDrawIcons(false);
                    set1.setDrawCircles(false);
                    set1.setDrawFilled(true);
                    set1.setColor(Color.parseColor("#78ffffff"));
                    set1.setFillColor(Color.parseColor("#B3E5FC"));
                    set1.setFillAlpha(20);

                    ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                    dataSets.add(set1);

                    LineData finalData = new LineData(dataSets);
                    finalData.setDrawValues(false);
                    setData(finalData);

                    getDescription().setText(data.getmFsym() + "/" + data.getmTsym() + " in " + data.getmExchange());

                    invalidate();

                    fade(false, data);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }

            });
        }

        startAnimation(anim);
    }
}
