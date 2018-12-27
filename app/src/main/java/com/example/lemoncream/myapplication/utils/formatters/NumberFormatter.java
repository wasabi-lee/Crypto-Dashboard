package com.example.lemoncream.myapplication.utils.formatters;

import android.graphics.Color;
import android.util.Log;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Created by LemonCream on 2018-02-27.
 */

public class NumberFormatter {

    private static final String TAG = NumberFormatter.class.getSimpleName();

    public static final DecimalFormat largeDf = new DecimalFormat("#,###,###.##");
    public static final DecimalFormat smallDf = new DecimalFormat("#,###,###.#######");

    public static float convertUserInputIntoFloat(String inputString) {
        float inputFloat;
        try {
            inputFloat = NumberFormat.getNumberInstance().parse(inputString).floatValue();
            Log.d(TAG, "convertUserInputIntoFloat: InputString: " + inputString);
        } catch (ParseException e) {
            inputFloat = 0;
        } catch (NumberFormatException e) {
            inputFloat = 0;
        }
        return inputFloat >= 0 ? inputFloat : 0;
    }

    public static String formatDecimalsRaw(float number) {
        return smallDf.format(number);
    }

    public static String formatDecimals(float number) {
        if (Math.abs(number) > 9) {
            return largeDf.format(number);
        }
        return smallDf.format(number);
    }

    public static String formatProfitDecimals(float number) {
        if (number > 0) {
            return "+" + formatDecimals(number);
        } else {
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
