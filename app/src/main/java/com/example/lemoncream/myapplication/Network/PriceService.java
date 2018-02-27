package com.example.lemoncream.myapplication.Network;

import com.example.lemoncream.myapplication.Model.GsonModels.Price;
import com.example.lemoncream.myapplication.Model.RealmModels.Pair;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by LemonCream on 2018-02-19.
 */

public interface PriceService {
    //https://min-api.cryptocompare.com/data/pricehistorical?fsym=ETH&tsyms=BTC,USD,EUR&ts=1452680400&extraParams=your_app_name
    @GET("data/price")
    Observable<Price> getCurrentPrice(@Query("fsym") String fsym,
                                @Query("tsyms") String tsyms,
                                @Query("e") String exchange);

    @GET("data/pricehistorical")
    Observable<Price> getHistoricalPrice(@Query("fsym") String fsym,
                                       @Query("tsyms") String tsyms,
                                       @Query("ts") long timestamp,
                                       @Query("e") String exchange);
}
