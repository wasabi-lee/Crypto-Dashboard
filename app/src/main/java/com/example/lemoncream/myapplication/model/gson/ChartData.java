package com.example.lemoncream.myapplication.model.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by LemonCream on 2018-02-27.
 */

public class ChartData {
    @SerializedName("Response")
    public String response;
    @SerializedName("Type")
    public Integer type;
    @SerializedName("Aggregated")
    public Boolean aggregated;
    @SerializedName("Data")
    public List<Datum> data = null;
    @SerializedName("TimeTo")
    public Integer timeTo;
    @SerializedName("TimeFrom")
    public Integer timeFrom;
    @SerializedName("FirstValueInArray")
    public Boolean firstValueInArray;
    @SerializedName("ConversionType")
    public transient ConversionType conversionType;


    public ChartData() {
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Boolean getAggregated() {
        return aggregated;
    }

    public void setAggregated(Boolean aggregated) {
        this.aggregated = aggregated;
    }

    public List<Datum> getData() {
        return data;
    }

    public void setData(List<Datum> data) {
        this.data = data;
    }

    public Integer getTimeTo() {
        return timeTo;
    }

    public void setTimeTo(Integer timeTo) {
        this.timeTo = timeTo;
    }

    public Integer getTimeFrom() {
        return timeFrom;
    }

    public void setTimeFrom(Integer timeFrom) {
        this.timeFrom = timeFrom;
    }

    public Boolean getFirstValueInArray() {
        return firstValueInArray;
    }

    public void setFirstValueInArray(Boolean firstValueInArray) {
        this.firstValueInArray = firstValueInArray;
    }

    public ConversionType getConversionType() {
        return conversionType;
    }

    public void setConversionType(ConversionType conversionType) {
        this.conversionType = conversionType;
    }
}
