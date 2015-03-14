package com.byteshaft.namaztime;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

public class WidgetReceiver extends BroadcastReceiver {

    static Notification notification;
    private final int TEN_SECONDS = 15 * 60000;

    @Override
    public void onReceive(Context context, Intent intent) {
        WidgetHelpers widgetHelpers = new WidgetHelpers(context);
        BroadcastReceivers broadcastReceivers = new BroadcastReceivers(context);
        if (notification == null) {
            notification = new Notification(context);
        }

        if (WidgetGlobals.isPhoneSilent()) {
            Log.i("NAMAZ", "Phone is in silent mode already.");
            widgetHelpers.setRingtoneMode(WidgetGlobals.getRingtoneModeBackup());
            notification.endNotification();
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
            notification.startNotification();
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, 0, new Intent(WidgetGlobals.SILENT_INTENT), 0);
            widgetHelpers.setAlarm(TEN_SECONDS, pendingIntent);
        }

        WidgetProvider.setupWidget(context);
    }
}
