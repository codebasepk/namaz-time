package com.byteshaft.namaztime;

import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

public class NamazNotification extends ContextWrapper {

    private NotificationCompat.Builder notificationBuilder = null;

    public NamazNotification(Context context) {
        super(context);
    }

    public void NamazNotificationStart(String namazz) {
        buildNamazNotification(namazz);
        addPendingNamazNotification();
    }

    private void buildNamazNotification(String namaz) {
        notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setContentTitle(" Namaz Time at " + namaz);
        notificationBuilder.setContentText("Slide to remove");
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setSmallIcon(R.drawable.ic_notification);
        notificationBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
        notificationBuilder.setLights(Color.RED, 3000, 3000);
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notificationBuilder.setSound(uri);
    }

    private void addPendingNamazNotification() {
        Intent intent = new Intent("android.intent.NAMAZ.TIME");
        PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        notificationBuilder.setContentIntent(pIntent);
    }
}
