package com.example.lemoncream.myapplication.custom;

import android.content.Context;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;

/**
 * Created by LemonCream on 2018-02-28.
 */

public class CandleChart extends CandleStickChart {

    public CandleChart(Context context) {
        super(context);
    }

    public CandleChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CandleChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void configSelf() {
        setPinchZoom(false);
        setMaxVisibleValueCount(90);
        getDescription().setEnabled(false);
        setDrawGridBackground(true);
        getLegend().setEnabled(false);
    }

    public void configAxis() {
        XAxis xAxis = getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(true);
        xAxis.setDrawLabels(false);
//        xAxis.setLabelCount(5);
//        xAxis.setValueFormatter(new IAxisValueFormatter() {
//            private SimpleDateFormat format = new SimpleDateFormat("MMM, dd");
//            @Override
//            public String getFormattedValue(float value, AxisBase axis) {
//                return format.format(value * 1000);
//            }
//        });

        YAxis leftAxis = getAxisLeft();
        leftAxis.setLabelCount(5, false);
        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawAxisLine(true);

        YAxis rightAxis = getAxisRight();
        rightAxis.setEnabled(false);
    }

}
