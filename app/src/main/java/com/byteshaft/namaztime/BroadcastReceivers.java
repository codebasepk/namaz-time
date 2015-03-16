package com.byteshaft.namaztime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;

public class BroadcastReceivers extends ContextWrapper {

    public BroadcastReceivers(Context context) {
        super(context);
    }

    public void registerRingtoneSettingsRestoreReceiver() {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                WidgetHelpers widgetHelpers = new WidgetHelpers(context);
                widgetHelpers.setRingtoneMode(WidgetGlobals.getRingtoneModeBackup());
                widgetHelpers.createToast("Phone ringer setting restored");
                WidgetReceiver.sNotifications.clearPhoneSilentNotification();
                WidgetGlobals.resetRingtoneBackup();
                WidgetGlobals.setIsPhoneSilent(false);
            }
        };

        getApplicationContext().registerReceiver(broadcastReceiver,
                new IntentFilter(WidgetGlobals.SILENT_INTENT));
    }
}
