package com.example.lemoncream.myapplication.model.gson;

/**
 * Created by LemonCream on 2018-02-28.
 */

public class PriceFull {

    /**
     * PriceMultiFull call's return model.
     *
     * String fsym: From symbol of this pair i.e) ZRX (from ZRX - ETH pair)
     * PriceDetail tsymPriceDetail: PriceFull detail based on the request ToSymbol i.e) (ZRX-ETH price data)
     * PriceDetail btcPriceDetail: PriceFull detail based on the BTC conversion i.e) (ZRX-BTC price data)
     * PriceDetail basePriceDetail: PriceFull detail based on the base currency conversion i.e) (ZRX-USD if the base currency is set as USD)
     *
     * btcPriceDetail and basePriceDetail is based on CCCAGG (default, aggregated data)
     * because the api doesn't support the price conversion
     * if the pair doesn't exist for the specified exchange.
     */

    private int position;
    private String fsym;
    private PriceDetail tsymPriceDetail;
    private PriceDetail btcPriceDetail;
    private PriceDetail basePriceDetail;

    public PriceFull() {
    }

    public String getFsym() {
        return fsym;
    }

    public void setFsym(String fsym) {
        this.fsym = fsym;
    }

    public PriceDetail getTsymPriceDetail() {
        return tsymPriceDetail;
    }

    public void setTsymPriceDetail(PriceDetail tsymPriceDetail) {
        this.tsymPriceDetail = tsymPriceDetail;
    }

    public PriceDetail getBtcPriceDetail() {
        return btcPriceDetail;
    }

    public void setBtcPriceDetail(PriceDetail btcPriceDetail) {
        this.btcPriceDetail = btcPriceDetail;
    }

    public PriceDetail getBasePriceDetail() {
        return basePriceDetail;
    }

    public void setBasePriceDetail(PriceDetail basePriceDetail) {
        this.basePriceDetail = basePriceDetail;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
