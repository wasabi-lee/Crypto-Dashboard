package com.example.lemoncream.myapplication.Background;

import android.content.Context;
import android.util.Log;

import com.example.lemoncream.myapplication.Model.RealmModels.Alert;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Wasabi on 3/26/2018.
 */

public class AlertDispatchHelper {

    private static final String TAG = AlertDispatchHelper.class.getSimpleName();

    public static void scheduleJob(Context context) {
        Log.d(TAG, "scheduleJob: Scheduling the job...");
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        Job job = createJob(dispatcher);
        dispatcher.mustSchedule(job);
    }

    public static Job createJob(FirebaseJobDispatcher dispatcher) {
        Log.d(TAG, "createJob: New job created");
        return dispatcher.newJobBuilder()
                .setService(AlertService.class)
                .setTag(AlertService.JOB_TAG)
                .setRecurring(true)
                .setLifetime(Lifetime.FOREVER)
                .setTrigger(Trigger.executionWindow(30, 60))
                .setReplaceCurrent(true)
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .build();
    }
    public static void cancelJob(Context context) {
        Log.d(TAG, "cancelJob: Job cancelled");
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        dispatcher.cancelAll();
    }
}
