package com.example.lemoncream.myapplication.Network;

import com.google.gson.GsonBuilder;

/**
 * Created by LemonCream on 2018-02-22.
 */

public class GsonHelper {
    public static <T> GsonBuilder createGsonBuilder(Class<?> clazz, T deserializer) {
        return new GsonBuilder().registerTypeAdapter(clazz, deserializer);
    }
}
