package com.byteshaft.namaztime;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;


public class Notification extends ContextWrapper {

    private final int ID = 56;
    private NotificationCompat.Builder mNotificationBuilder = null;
    private NotificationManager mNotificationManager = null;

    public Notification(Context context) {
        super(context);
    }

    public void startNotification() {
        buildNotification();
        addPendingNotify();
        showNotification();
    }

    public void endNotification() {
        if (mNotificationManager != null) {
            mNotificationManager.cancel(ID);
        }
    }

    private void buildNotification() {
        mNotificationBuilder = new NotificationCompat.Builder(this);

        mNotificationBuilder.setContentTitle("Namaz Time");
        mNotificationBuilder.setContentText("Tap to remove");
        mNotificationBuilder.setSmallIcon(R.drawable.ic_notification);
        // dismiss notification when its tapped.
        mNotificationBuilder.setAutoCancel(true);
        mNotificationBuilder.setOngoing(true);
    }

    private void addPendingNotify() {
        Intent intent = new Intent(WidgetGlobals.NOTIFICATION_INTENT);
        PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        mNotificationBuilder.setContentIntent(pIntent);
    }

    private void showNotification() {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.notify(ID, mNotificationBuilder.build());
    }
}
