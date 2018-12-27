package com.example.lemoncream.myapplication.background;


import android.annotation.SuppressLint;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.example.lemoncream.myapplication.model.deserializers.PriceSimpleDeserializer;
import com.example.lemoncream.myapplication.model.gson.PriceSimple;
import com.example.lemoncream.myapplication.model.realm.Alert;
import com.example.lemoncream.myapplication.model.temp.AlertResult;
import com.example.lemoncream.myapplication.network.GsonHelper;
import com.example.lemoncream.myapplication.network.PriceService;
import com.example.lemoncream.myapplication.network.RetrofitHelper;
import com.example.lemoncream.myapplication.utils.notification.NotificationUtils;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import java.util.ArrayList;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.BiFunction;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmResults;
import retrofit2.Retrofit;

/**
 * Created by Wasabi on 3/26/2018.
 */

public class AlertService extends JobService {

    private static final String TAG = AlertService.class.getSimpleName();
    public static final String JOB_TAG = "alert_dispatcher_service_job_tag";
    private static final String BASE_URL = "https://min-api.cryptocompare.com/";

    private NotificationUtils notificationUtils;
    private Random mRandom = new Random();

    private Realm mRealm;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d(TAG, "onStartJob: Job started");
        mRealm = Realm.getDefaultInstance();
        notificationUtils = new NotificationUtils();
        requestPrice(jobParameters);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d(TAG, "onStopJob: Job stopped");
        return true;
    }

    @SuppressLint("CheckResult")
    private void requestPrice(JobParameters jobParameters) {
        RealmResults<Alert> alerts = getAlerts();
        ArrayList<AlertResult> alertParams = createAlertParams(alerts);
        PriceService priceSimpleService = getPriceService();

        Observable.fromIterable(alertParams)
                .flatMap(alertParam -> Observable.zip(Observable.just(alertParam),
                        priceSimpleService.getSingleSimplePrice(alertParam.getFsym(),
                                alertParam.getTsym(),
                                alertParam.getExchange()),
                        (BiFunction<AlertResult, PriceSimple, Object>) (alertResult, priceSimple) -> {
                            alertResult.setPrice(priceSimple.getPrice());
                            return alertResult;
                        }))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(alert -> checkAlert((AlertResult) alert),
                        Throwable::printStackTrace,
                        () -> {
                            mRealm.close();
                            jobFinished(jobParameters, false);
                        });
    }

    private RealmResults<Alert> getAlerts() {
        return mRealm.where(Alert.class).findAll();
    }

    private ArrayList<AlertResult> createAlertParams(RealmResults<Alert> alerts) {
        ArrayList<AlertResult> alertParams = new ArrayList<>();
        for (Alert alert : alerts) {
            int id = alert.get_id();
            String fsym = alert.getBag().getTradePair().getfCoin().getSymbol();
            String tsym = alert.getBag().getTradePair().gettCoin().getSymbol();
            String exchange = alert.getExchange().getName();
            alertParams.add(new AlertResult(id, fsym, tsym, exchange));
        }
        return alertParams;
    }


    private PriceService getPriceService() {
        Retrofit priceSimpleRetrofit = RetrofitHelper.createRetrofitWithRxConverter(BASE_URL,
                GsonHelper.createGsonBuilder(PriceSimple.class, new PriceSimpleDeserializer()).create());
        return priceSimpleRetrofit.create(PriceService.class);
    }

    private void checkAlert(AlertResult alertResult) {
        int alertId = alertResult.getAlertId();
        float price = alertResult.getPrice();
        Alert currentAlert = mRealm.where(Alert.class).equalTo("_id", alertId).findFirst();

        if (currentAlert == null) return;
        if (!currentAlert.isActive()) {
            Log.d(TAG, "checkAlert: Deactivated alert");
            return;}

        float moreThan = currentAlert.getMoreThan();
        float lessThan = currentAlert.getLessThan();

        if (checkIfPriceMeetCondition(price, moreThan, lessThan))
            launchNotification(currentAlert, price);
    }

    private boolean checkIfPriceMeetCondition(float price, float moreThan, float lessThan) {
        return (moreThan != -1 || lessThan != -1) && (price >= moreThan || price <= lessThan);
    }

    private void launchNotification(Alert alert, float price) {
        String fsym = alert.getBag().getTradePair().getfCoin().getSymbol();
        String tsym = alert.getBag().getTradePair().gettCoin().getSymbol();
        String exchange = alert.getExchange().getName();
        String title = fsym + "/" + tsym + " is now " + price + " " + tsym + " in " + exchange + "!";
        String body = "Click here for more details";

        NotificationCompat.Builder notifBuilder = notificationUtils.getAlertNotification(getApplicationContext(), title, body);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(mRandom.nextInt(), notifBuilder.build());

        deactivateAlert(alert);
    }

    private void deactivateAlert(Alert alert) {
        if (alert.isOneTime()) {
            mRealm.executeTransaction(realm -> alert.setActive(false));
        }
        Log.d(TAG, "deactivateAlert: Alert deactivated");
    }
}
