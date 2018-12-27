package com.example.lemoncream.myapplication.network;

import com.example.lemoncream.myapplication.model.gson.CoinData;
import com.example.lemoncream.myapplication.model.gson.ExchangeData;


import io.reactivex.Observable;
import retrofit2.http.GET;

/**
 * Created by LemonCream on 2018-02-15.
 */

public interface CoinListService {
    @GET("data/all/coinlist")
    Observable<CoinData> requestCoinList();

    @GET("data/all/exchanges")
    Observable<ExchangeData> requestExchangeData();
}
