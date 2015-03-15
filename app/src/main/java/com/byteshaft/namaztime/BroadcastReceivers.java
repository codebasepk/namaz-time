package com.byteshaft.namaztime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;

public class BroadcastReceivers extends ContextWrapper {

    private WidgetHelpers mWidgetHelpers = null;

    public BroadcastReceivers(Context context) {
        super(context);
        mWidgetHelpers = new WidgetHelpers(this);
    }

    public void registerRingtoneSettingsRestoreReceiver() {
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
}
