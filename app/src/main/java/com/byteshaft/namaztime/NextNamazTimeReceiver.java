package com.byteshaft.namaztime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class NextNamazTimeReceiver extends BroadcastReceiver {
    Notifications notifications;

    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmHelpers alarmHelpers = new AlarmHelpers(context);
        notifications = new Notifications(context);
        notifications.removeNotification();
        alarmHelpers.setAlarmForNextNamaz();
    }
}
