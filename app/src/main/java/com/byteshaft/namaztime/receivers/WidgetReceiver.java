/*
 *
 *  * (C) Copyright 2015 byteShaft Inc.
 *  *
 *  * All rights reserved. This program and the accompanying materials
 *  * are made available under the terms of the GNU Lesser General Public License
 *  * (LGPL) version 2.1 which accompanies this distribution, and is available at
 *  * http://www.gnu.org/licenses/lgpl-2.1.html
 *  
 */

package com.byteshaft.namaztime.receivers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

import com.byteshaft.namaztime.Notifications;
import com.byteshaft.namaztime.widget.WidgetGlobals;
import com.byteshaft.namaztime.widget.WidgetHelpers;
import com.byteshaft.namaztime.widget.WidgetProvider;

import java.util.concurrent.TimeUnit;

public class WidgetReceiver extends BroadcastReceiver {

    public static Notifications sNotifications;
    static PendingIntent pendingIntent;
    WidgetHelpers widgetHelpers;

    @Override
    public void onReceive(Context context, Intent intent) {
        final int FIFTEEN_MINUTES = 15 * 60000;
        widgetHelpers = new WidgetHelpers(context);
        if (sNotifications == null) {
            sNotifications = new Notifications(context);
        }
        if (WidgetGlobals.isPhoneSilent()) {
            backupRingtoneMode(widgetHelpers);
        } else if (widgetHelpers.getCurrentRingtoneMode()
                == AudioManager.RINGER_MODE_SILENT) {
            widgetHelpers.createToast("Phone is already Silent");
            return;
        } else if (widgetHelpers.getCurrentRingtoneMode() == AudioManager.RINGER_MODE_VIBRATE) {
            widgetHelpers.createToast("Phone is already on Vibrate Mode");
        } else {
            setSilentForFifteenMinutes(context, FIFTEEN_MINUTES);
        }
        WidgetProvider.setupWidget(context);
    }

    private void setSilentForFifteenMinutes(Context context, int FIFTEEN_MINUTES) {
        WidgetGlobals.setRingtoneModeBackup(widgetHelpers.getCurrentRingtoneMode());
        widgetHelpers.setRingtoneMode(AudioManager.RINGER_MODE_VIBRATE);
        widgetHelpers.vibrate(500);
        widgetHelpers.createToast(String.format("Phone set to vibrate for %d minutes",
                TimeUnit.MILLISECONDS.toMinutes(FIFTEEN_MINUTES)));
        WidgetGlobals.setIsPhoneSilent(true);
        sNotifications.startPhoneSilentNotification();
        pendingIntent = PendingIntent.getBroadcast(
                context, 0, new Intent(WidgetGlobals.SILENT_INTENT), 0);
        widgetHelpers.setAlarm(FIFTEEN_MINUTES, pendingIntent);
    }

    void removeAlarm(Context context) {
        Log.i("NAMAZ_TIME", "Removing Alarm");
        WidgetHelpers widgetHelper = new WidgetHelpers(context);
        widgetHelper.removePreviousAlarm(pendingIntent);
        pendingIntent = null;
    }

    private void backupRingtoneMode(WidgetHelpers widgetHelpers) {
        widgetHelpers.setRingtoneMode(WidgetGlobals.getRingtoneModeBackup());
        sNotifications.clearPhoneSilentNotification();
        widgetHelpers.createToast("Phone ringer setting restored");
        WidgetGlobals.resetRingtoneBackup();
        WidgetGlobals.setIsPhoneSilent(false);
    }
}
