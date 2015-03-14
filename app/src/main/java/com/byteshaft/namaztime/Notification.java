package com.byteshaft.namaztime;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;


public class Notification extends ContextWrapper {


    private final int ID = 56;
    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager manager;

    public Notification(Context context) {
        super(context);
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
        notificationBuilder = new NotificationCompat.Builder(this);

        notificationBuilder.setContentTitle("Namaz Time");
        notificationBuilder.setContentText("Tap to remove");
        notificationBuilder.setSmallIcon(R.drawable.ic_notification);
        // dismiss notification when its tapped.
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setOngoing(true);
    }

    private void addPendingNotify() {
        Intent intent = new Intent(WidgetGlobals.NOTIFICATION_INTENT);
        PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        notificationBuilder.setContentIntent(pIntent);
    }

    private void showNotification() {
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        System.out.println("Showing notification");
        manager.notify(ID, notificationBuilder.build());
    }
}
