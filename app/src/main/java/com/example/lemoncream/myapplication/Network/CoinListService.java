package com.example.lemoncream.myapplication.Network;

import com.example.lemoncream.myapplication.Model.GsonModels.CoinData;
import com.example.lemoncream.myapplication.Model.GsonModels.ExchangeData;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by LemonCream on 2018-02-15.
 */

public interface CoinListService {
    @GET("data/all/coinlist")
    Call<CoinData> requestCoinList();

    @GET("data/all/exchanges")
    Call<ExchangeData> requestExchangeData();
}
