package com.byteshaft.namaztime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class CustomBroadcastReceivers {

    Context mContext;
    WidgetHelpers mWidgetHelpers;

    public CustomBroadcastReceivers(Context context) {
        mContext = context;
        mWidgetHelpers = new WidgetHelpers(context);
    }

    public void registerReceiver() {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mWidgetHelpers.setRingtoneMode(WidgetGlobals.getRingtoneModeBackup());
                mWidgetHelpers.createToast("Phone ringer setting restored");
                WidgetReceiver.notification.endNotification();
                WidgetGlobals.resetRingtoneBackup();
                WidgetGlobals.setIsPhoneSilent(false);
            }
        };

        mContext.getApplicationContext().registerReceiver(broadcastReceiver,
                new IntentFilter(WidgetGlobals.SILENT_INTENT));
    }

    public void registerNotificationReceiver() {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("NAMAZ", "Phone is in silent mode already.");
                mWidgetHelpers.setRingtoneMode(WidgetGlobals.getRingtoneModeBackup());
                mWidgetHelpers.createToast("Phone ringer setting restored");
                WidgetGlobals.resetRingtoneBackup();
                WidgetGlobals.setIsPhoneSilent(false);
            }
        };

        mContext.getApplicationContext().registerReceiver(broadcastReceiver,
                new IntentFilter((WidgetGlobals.NOTIFICATION_INTENT)));
    }
}
