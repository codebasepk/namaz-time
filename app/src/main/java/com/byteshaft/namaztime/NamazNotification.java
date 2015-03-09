package com.byteshaft.namaztime;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class NamazNotification {

    private final int ID = 21;
    private Context context;
    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager manager;

    public NamazNotification(Context context) {
        this.context = context;
    }

    public void startNamazNotification(String namazz) {
        buildNamazNotification(namazz);
        addPendingNamazNotification();
        showNamazNotification();
    }

    public void endNamazNotification() {
        if (manager != null) {
            manager.cancel(ID);
        }
    }

    private void buildNamazNotification(String namaz) {
        notificationBuilder = new NotificationCompat.Builder(context);
        notificationBuilder.setContentTitle(" Namaz Time at "+ namaz);
        notificationBuilder.setContentText("Tap to remove");
        // dismiss notification when its tapped.
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setSmallIcon(R.drawable.ic_notification);
        // disable slide to remove for the notification.
        notificationBuilder.setOngoing(true);
    }

    private void addPendingNamazNotification() {
        Intent intent = new Intent("android.intent.NAMAZ.TIME");
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        notificationBuilder.setContentIntent(pIntent);
    }

    private void showNamazNotification() {
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(ID, notificationBuilder.build());
    }
}
