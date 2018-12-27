package com.example.lemoncream.myapplication.utils.formatters;

/**
 * Created by LemonCream on 2018-03-01.
 */

public class SignSwitcher {

    public static String BASE_CURRENCY = "USD"; // TODO Change later

    private static final String SYMBOL_USD = "\u0024";
    private static final String SYMBOL_CNY = "\u00A5";
    private static final String SYMBOL_KRW = "\u20A9";
    private static final String SYMBOL_JPY = "\u00A5";
    private static final String SYMBOL_EUR = "\u20AC";
    private static final String SYMBOL_GBP = "\u00A3";
    private static final String SYMBOL_BTC = "\u0E3F";

    public static String getBaseCurrencySign() {
        switch (BASE_CURRENCY.toUpperCase()) {
            case "USD":
                return SYMBOL_USD;
            case "CNY":
                return SYMBOL_CNY;
            case "KRW":
                return SYMBOL_KRW;
            case "JPY":
                return SYMBOL_JPY;
            case "EUR":
                return SYMBOL_EUR;
            case "GBP":
                return SYMBOL_GBP;
            case "BTC":
                return SYMBOL_BTC;
        }
        return "";
    }

    public static String getCurrencySign(String symbol) {
        switch (symbol.toUpperCase()) {
            case "USD":
                return SYMBOL_USD;
            case "CNY":
                return SYMBOL_CNY;
            case "KRW":
                return SYMBOL_KRW;
            case "JPY":
                return SYMBOL_JPY;
            case "EUR":
                return SYMBOL_EUR;
            case "GBP":
                return SYMBOL_GBP;
            case "BTC":
                return SYMBOL_BTC;
        }
        return "";
    }



    public static String getBtcSign() {
        return SYMBOL_BTC;
    }
}
