package com.byteshaft.namaztime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;

public class RingtoneRestoreReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        WidgetHelpers widgetHelpers = new WidgetHelpers(context);
        widgetHelpers.setRingtoneMode(WidgetGlobals.getRingtoneModeBackup());
        widgetHelpers.createToast("Phone ringer setting restored");
        WidgetReceiver.sNotifications.clearPhoneSilentNotification();
        WidgetGlobals.resetRingtoneBackup();
        WidgetGlobals.setIsPhoneSilent(false);
    }
}
