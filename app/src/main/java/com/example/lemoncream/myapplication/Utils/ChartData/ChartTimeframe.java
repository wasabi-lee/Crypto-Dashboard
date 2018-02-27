package com.example.lemoncream.myapplication.Utils.ChartData;

/**
 * Created by LemonCream on 2018-02-27.
 */

public class ChartTimeframe {
    private int aggregate;
    private int numOfBars;
    private String chartUrlPath;

    public ChartTimeframe(int aggregate, int numOfBars, String chartUrlPath) {
        this.aggregate = aggregate;
        this.numOfBars = numOfBars;
        this.chartUrlPath = chartUrlPath;
    }

    public int getNumOfBars() {
        return numOfBars;
    }

    public void setNumOfBars(int numOfBars) {
        this.numOfBars = numOfBars;
    }

    public int getAggregate() {
        return aggregate;
    }

    public void setAggregate(int aggregate) {
        this.aggregate = aggregate;
    }

    public String getChartUrlPath() {
        return chartUrlPath;
    }

    public void setChartUrlPath(String chartUrlPath) {
        this.chartUrlPath = chartUrlPath;
    }
}
