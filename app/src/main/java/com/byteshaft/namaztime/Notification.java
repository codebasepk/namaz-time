package com.byteshaft.namaztime;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;


public class Notification {


    private final int ID = 1;
    private Context context;
    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager manager;

    public Notification(Context context) {
        this.context = context;
    }

    public void startNotification() {
        buildNotification();
        addPendingNotify();
        showNotification();
    }

    public void endNotification() {
        if (manager != null) {
            manager.cancel(ID);
        }
    }

    private void buildNotification() {
        notificationBuilder = new NotificationCompat.Builder(context);

        notificationBuilder.setContentTitle("Namaz Time");
        notificationBuilder.setContentText("Tap to remove");
        // dismiss notification when its tapped.
        notificationBuilder.setAutoCancel(true);
        //notificationBuilder.setSmallIcon(R.drawable.ic_notify);
        // disable slide to remove for the notification.
        notificationBuilder.setOngoing(true);
    }

    private void addPendingNotify() {
        Intent intent = new Intent("android.intent.CLOSE_ACTIVITY");
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        notificationBuilder.setContentIntent(pIntent);
    }

    private void showNotification() {
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(ID, notificationBuilder.build());
    }


}
