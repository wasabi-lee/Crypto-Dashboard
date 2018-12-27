package com.example.lemoncream.myapplication.utils.database;

import io.realm.Realm;
import io.realm.RealmObject;

/**
 * Created by LemonCream on 2018-02-21.
 */

public class RealmIdAutoIncrementHelper {

    public static int generateItemId(Class<? extends RealmObject> clazz, String fieldName) {
        Realm realm = Realm.getDefaultInstance();
        Number maxIdValue = realm.where(clazz).max(fieldName);
        return maxIdValue == null ? 0 : maxIdValue.intValue() + 1;
    }

}
