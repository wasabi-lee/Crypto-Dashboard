package com.example.lemoncream.myapplication.Network;

import com.example.lemoncream.myapplication.Model.GsonModels.PriceFull;
import com.example.lemoncream.myapplication.Model.GsonModels.PriceHistorical;
import com.example.lemoncream.myapplication.Model.GsonModels.PriceSimple;

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
    //https://min-api.cryptocompare.com/data/price?fsym=ETH&tsyms=BTC,USD,EUR

    @GET("data/price")
    Observable<PriceSimple> getSingleSimplePrice(@Query("fsym") String fsym,
                                                 @Query("tsyms") String tsyms,
                                                 @Query("e") String exchange);

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

    @GET("data/pricehistorical")
    Observable<PriceHistorical> getHistoricalPrice(@Query("fsym") String fsym,
                                                            @Query("tsyms") String tsyms);
    @GET("data/pricehistorical")
    Observable<PriceHistorical> getHistoricalPrice(@Query("fsym") String fsym,
                                                            @Query("tsyms") String tsyms,
                                                            @Query("ts") long timestamp);
    @GET("data/pricehistorical")
    Observable<PriceHistorical> getHistoricalPrice(@Query("fsym") String fsym,
                                                            @Query("tsyms") String tsyms,
                                                            @Query("e") String exchange);
}
