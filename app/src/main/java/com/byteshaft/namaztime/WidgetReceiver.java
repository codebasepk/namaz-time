package com.byteshaft.namaztime;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

public class WidgetReceiver extends BroadcastReceiver {

    public static Notifications sNotifications = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        final int FIFTEEN_MINUTES = 15 * 60000;
        WidgetHelpers widgetHelpers = new WidgetHelpers(context);
        BroadcastReceivers broadcastReceivers = new BroadcastReceivers(context);
        if (sNotifications == null) {
            sNotifications = new Notifications(context);
        }

        if (WidgetGlobals.isPhoneSilent()) {
            widgetHelpers.setRingtoneMode(WidgetGlobals.getRingtoneModeBackup());
            sNotifications.clearPhoneSilentNotification();
            widgetHelpers.createToast("Phone ringer setting restored");
            WidgetGlobals.resetRingtoneBackup();
            WidgetGlobals.setIsPhoneSilent(false);
        } else {
            WidgetGlobals.setRingtoneModeBackup(widgetHelpers.getCurrentRingtoneMode());
            widgetHelpers.setRingtoneMode(AudioManager.RINGER_MODE_VIBRATE);
            widgetHelpers.vibrate(500);
            widgetHelpers.createToast("Phone set to vibrate");
            WidgetGlobals.setIsPhoneSilent(true);
            broadcastReceivers.registerReceiver();
            broadcastReceivers.registerNotificationReceiver();
            sNotifications.startPhoneSilentNotification();
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, 0, new Intent(WidgetGlobals.SILENT_INTENT), 0);
            widgetHelpers.setAlarm(FIFTEEN_MINUTES, pendingIntent);
        }

        WidgetProvider.setupWidget(context);
    }
}
