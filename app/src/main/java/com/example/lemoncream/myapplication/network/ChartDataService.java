package com.example.lemoncream.myapplication.network;

import com.example.lemoncream.myapplication.model.gson.ChartData;


import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by LemonCream on 2018-02-27.
 */

public interface ChartDataService {
//    https://min-api.cryptocompare.com/data/histominute?fsym=BTC&tsym=USD&limit=10&aggregate=3&e=CCCAGG
    @GET("data/{urlpath}")
    Observable<ChartData> getChartData(@Path("urlpath") String urlPath,
                                       @Query("fsym") String fsym,
                                       @Query("tsym") String tsym,
                                       @Query("aggregate") int aggregate,
                                       @Query("e") String exchange,
                                       @Query("limit") int limit);
}
