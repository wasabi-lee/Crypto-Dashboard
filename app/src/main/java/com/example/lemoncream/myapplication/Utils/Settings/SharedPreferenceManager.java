package com.example.lemoncream.myapplication.Utils.Settings;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceManager {

    private static SharedPreferences getSharedPref(Context context, String fileKey) {
        return context.getSharedPreferences(fileKey, Context.MODE_PRIVATE);
    }

    public static int getIntSharedPref(Context context, String fileKey, String valueKey, int defaultValue) {
        return getSharedPref(context, fileKey).getInt(valueKey, defaultValue);
    }

    public static String getStringSharedPref(Context context, String fileKey, String valueKey, String defaultValue) {
        return getSharedPref(context, fileKey).getString(valueKey, defaultValue);
    }

    public static float getFloatSharedPref(Context context, String fileKey, String valueKey, float defaultValue) {
        return getSharedPref(context, fileKey).getFloat(valueKey, defaultValue);
    }

    public static boolean getBooleanSharedPref(Context context, String fileKey, String valueKey, boolean defaultValue) {
        return getSharedPref(context, fileKey).getBoolean(valueKey, defaultValue);
    }


}
