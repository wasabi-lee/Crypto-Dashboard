package com.example.lemoncream.myapplication.Utils.Formatters;

/**
 * Created by LemonCream on 2018-02-27.
 */

public class NumberFormatter {

    public static float convertUserInputIntoFloat(String inputString) {
        float inputFloat;
        try {
            inputFloat = Float.valueOf(inputString);
        } catch (NumberFormatException e) {
            inputFloat = 0;
        }
        return inputFloat >= 0 ? inputFloat : 0;
    }

}
