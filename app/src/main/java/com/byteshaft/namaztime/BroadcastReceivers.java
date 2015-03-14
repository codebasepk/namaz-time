package com.byteshaft.namaztime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class BroadcastReceivers extends ContextWrapper {

    private WidgetHelpers mWidgetHelpers = null;

    public BroadcastReceivers(Context context) {
        super(context);
        mWidgetHelpers = new WidgetHelpers(this);
    }

    public void registerReceiver() {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mWidgetHelpers.setRingtoneMode(WidgetGlobals.getRingtoneModeBackup());
                mWidgetHelpers.createToast("Phone ringer setting restored");
                WidgetReceiver.sNotifications.clearPhoneSilentNotification();
                WidgetGlobals.resetRingtoneBackup();
                WidgetGlobals.setIsPhoneSilent(false);
            }
        };

        getApplicationContext().registerReceiver(broadcastReceiver,
                new IntentFilter(WidgetGlobals.SILENT_INTENT));
    }

    public void registerNotificationReceiver() {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mWidgetHelpers.setRingtoneMode(WidgetGlobals.getRingtoneModeBackup());
                mWidgetHelpers.createToast("Phone ringer setting restored");
                WidgetGlobals.resetRingtoneBackup();
                WidgetGlobals.setIsPhoneSilent(false);
            }
        };

        getApplicationContext().registerReceiver(broadcastReceiver,
                new IntentFilter((WidgetGlobals.NOTIFICATION_INTENT)));
    }
}
