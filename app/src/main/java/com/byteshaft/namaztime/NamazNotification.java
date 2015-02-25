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

    public  void startNotification() {
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
        notificationBuilder.setSmallIcon(R.drawable.ic_launcher);
        // disable slide to remove for the notification.
        notificationBuilder.setOngoing(true);
    }

    private void addPendingNotify() {
        Intent intent = new Intent("android.intent.NAMAZ.TIME");
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        notificationBuilder.setContentIntent(pIntent);
    }

    private void showNotification() {
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(ID, notificationBuilder.build());
    }
}
