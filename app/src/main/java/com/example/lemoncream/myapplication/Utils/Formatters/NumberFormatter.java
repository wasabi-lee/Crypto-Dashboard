package com.example.lemoncream.myapplication.Utils.Formatters;

import android.graphics.Color;

import java.text.DecimalFormat;

/**
 * Created by LemonCream on 2018-02-27.
 */

public class NumberFormatter {

    public static final DecimalFormat largeDf = new DecimalFormat("#,###,###.##");
    public static final DecimalFormat smallDf = new DecimalFormat("#,###,###.#######");

    public static float convertUserInputIntoFloat(String inputString) {
        float inputFloat;
        try {
            inputFloat = Float.valueOf(inputString);
        } catch (NumberFormatException e) {
            inputFloat = 0;
        }
        return inputFloat >= 0 ? inputFloat : 0;
    }

    public static String formatDecimals(float number) {
        if (Math.abs(number) > 9) {
            return largeDf.format(number);
        } return smallDf.format(number);
    }

    public static String formatProfitDecimals(float number) {
        if (number > 0) {
            return "+" + formatDecimals(number);
        }  else {
            return formatDecimals(number);
        }
    }

    public static int getProfitTextColor(float number) {
        if (number > 0) {
            return Color.GREEN;
        } else if (number < 0) {
            return Color.RED;
        } else {
            return Color.DKGRAY;
        }
    }

}
