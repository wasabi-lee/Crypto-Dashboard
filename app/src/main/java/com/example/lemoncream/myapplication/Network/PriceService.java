package com.example.lemoncream.myapplication.Network;

import com.example.lemoncream.myapplication.Model.GsonModels.PriceCurrent;
import com.example.lemoncream.myapplication.Model.GsonModels.PriceHistorical;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by LemonCream on 2018-02-19.
 */

public interface PriceService {
    //https://min-api.cryptocompare.com/data/pricehistorical?fsym=ETH&tsyms=BTC,USD,EUR&ts=1452680400&extraParams=your_app_name
    @GET("data/price")
    Observable<PriceCurrent> getCurrentPrice(@Query("fsym") String fsym,
                                             @Query("tsyms") String tsyms,
                                             @Query("e") String exchange);

    @GET("data/pricehistorical")
    Observable<PriceHistorical> getHistoricalPrice(@Query("fsym") String fsym,
                                                   @Query("tsyms") String tsyms,
                                                   @Query("ts") long timestamp,
                                                   @Query("e") String exchange);
}
