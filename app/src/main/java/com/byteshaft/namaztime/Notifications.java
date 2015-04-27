package com.byteshaft.namaztime;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

public class Notifications extends ContextWrapper {

    private final int SILENT_NOTIFICATION_ID = 56;
    private final int UPCOMING_NAMAZ_NOTIFICATION_ID = 57;
    private NotificationManager mNotificationManager = null;


    public Notifications(Context context) {
        super(context);
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    public void startPhoneSilentNotification() {
        NotificationCompat.Builder notification = buildPhoneSilentNotification();
        showNotification(SILENT_NOTIFICATION_ID, notification);
    }

    public void clearPhoneSilentNotification() {
        if (mNotificationManager != null) {
            mNotificationManager.cancel(SILENT_NOTIFICATION_ID);
        }
    }

    public void startUpcomingNamazNotification(String namaz) {
        NotificationCompat.Builder notification = buildUpcomingNamazNotification(namaz);
        showNotification(UPCOMING_NAMAZ_NOTIFICATION_ID, notification);
    }

    private void showNotification(int id, NotificationCompat.Builder notification) {
        mNotificationManager.notify(id, notification.build());
    }

    private NotificationCompat.Builder buildPhoneSilentNotification() {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        Intent intent = new Intent(WidgetGlobals.SILENT_INTENT);
        PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        notificationBuilder.setContentTitle("Namaz Time");
        notificationBuilder.setContentText("Swipe to restore Ringtone");
        notificationBuilder.setSmallIcon(R.drawable.ic_notification);
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setDeleteIntent(pIntent);
        notificationBuilder.setContentIntent(pIntent);
        return notificationBuilder;
    }

    private NotificationCompat.Builder buildUpcomingNamazNotification(String namaz) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notificationBuilder.setContentTitle("Namaz Time at " + namaz);
        notificationBuilder.setContentText("Slide to remove");
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setSmallIcon(R.drawable.ic_notification);
        notificationBuilder.setVibrate(new long[]{250, 175, 250, 175, 250});
        notificationBuilder.setLights(Color.GREEN, 3000, 3000);
        notificationBuilder.setSound(uri);
        return notificationBuilder;
    }

    void removeNotification() {
        mNotificationManager.cancel(UPCOMING_NAMAZ_NOTIFICATION_ID);
    }
}
