package com.example.lemoncream.myapplication.Utils.Notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.example.lemoncream.myapplication.Activity.MainActivity;
import com.example.lemoncream.myapplication.Model.RealmModels.Alert;
import com.example.lemoncream.myapplication.R;

/**
 * Created by Wasabi on 3/27/2018.
 */

public class NotificationUtils {

    private static final String TAG = NotificationUtils.class.getSimpleName();
    public static final String ALERT_NOTIFICATION_CHANNEL_ID = "com.example.lemoncream.myapplication.alert_notification_channel_id";
    public static final String ALERT_NOTIFICATION_CHANNEL_NAME = "Alert Channel";
    public static final String ALERT_NOTIFICATION_CHANNEL_DESCRIPTION = "Notifications for price alerts.";


    public NotificationUtils() {
    }

    public void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel alertChannel = new NotificationChannel(ALERT_NOTIFICATION_CHANNEL_ID,
                    ALERT_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            alertChannel.setDescription(ALERT_NOTIFICATION_CHANNEL_DESCRIPTION);
            alertChannel.enableLights(true);
            alertChannel.enableVibration(true);
            alertChannel.setLightColor(Color.BLUE);
            alertChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            getManager(context).createNotificationChannel(alertChannel);
        }
    }

    public NotificationManager getManager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public NotificationCompat.Builder getAlertNotification(Context context, String title, String body) {
        Log.d(TAG, "getAlertNotification: " + title + ", " + body);
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        return new NotificationCompat.Builder(context, ALERT_NOTIFICATION_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_action_line_chart) // TODO Change icon
                .setContentIntent(pendingIntent)
                .setAutoCancel(false);
    }

}
