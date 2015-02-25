package com.byteshaft.namaztime;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

public class WidgetReceiver extends BroadcastReceiver {

    final int TEN_SECONDS = 10000;
    Context mContext;
    WidgetHelpers mWidgetHelpers;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        mWidgetHelpers = new WidgetHelpers(context);
        CustomBroadcastReceivers customBroadcastReceivers = new CustomBroadcastReceivers(context);
        if (WidgetGlobals.isPhoneSilent()) {
            Log.i("NAMAZ", "Phone is in silent mode already.");
            mWidgetHelpers.setRingtoneMode(WidgetGlobals.getRingtoneModeBackup());
            mWidgetHelpers.createToast("Phone ringer setting restored");
            WidgetGlobals.resetRingtoneBackup();
            WidgetGlobals.setIsPhoneSilent(false);
        } else {
            WidgetGlobals.setRingtoneModeBackup(mWidgetHelpers.getCurrentRingtoneMode());
            mWidgetHelpers.setRingtoneMode(AudioManager.RINGER_MODE_VIBRATE);
            mWidgetHelpers.vibrate();
            mWidgetHelpers.createToast("Phone set to vibrate");
            WidgetGlobals.setIsPhoneSilent(true);
            customBroadcastReceivers.registerReceiver();
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    mContext, 0, new Intent(WidgetGlobals.SILENT_INTENT), 0);
            mWidgetHelpers.setAlarm(TEN_SECONDS, pendingIntent);
        }
        WidgetProvider.setupWidget(mContext);
    }
}
