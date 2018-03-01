package com.example.lemoncream.myapplication.Network;

import com.example.lemoncream.myapplication.Model.GsonModels.PriceFull;

import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by LemonCream on 2018-02-19.
 */

public interface PriceService {
    //https://min-api.cryptocompare.com/data/pricehistorical?fsym=ETH&tsyms=BTC,USD,EUR&ts=1452680400&extraParams=your_app_name
    //https://min-api.cryptocompare.com/data/pricemultifull?fsyms=BTC,ETH&tsyms=USD,EUR&e=Coinbase&extraParams=your_app_name
    @GET("data/pricemultifull")
    Single<PriceFull> getSingleCurrentPrice(@Query("fsyms") String fsym,
                                            @Query("tsyms") String tsyms,
                                            @Query("e") String exchange);

    @GET("data/pricemultifull")
    Observable<PriceFull> getMultipleCurrentPrices(@Query("fsyms") String fsym,
                                                   @Query("tsyms") String tsyms,
                                                   @Query("e") String exchange);

    @GET("data/pricemultifull")
    Observable<PriceFull> getMultipleCurrentPrices(@Query("fsyms") String fsym,
                                                   @Query("tsyms") String tsyms);

}
