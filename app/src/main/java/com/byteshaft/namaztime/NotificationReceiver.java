package com.byteshaft.namaztime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String namaz = intent.getExtras().getString("namaz");
        Notifications notifications = new Notifications(context);
        notifications.startUpcomingNamazNotification(namaz);
    }
}
