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

package com.byteshaft.namaztime;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.media.AudioManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.widget.Toast;

public class WidgetHelpers extends ContextWrapper {

    private static Toast sToast;
    private static AlarmManager alarmManager;

    public WidgetHelpers(Context context) {
        super(context);
    }

    private AudioManager getAudioManager() {
        return (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    AlarmManager getAlarmManager() {
        return (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    }

    private Vibrator getVibrator() {
        return (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void setAlarm(int time, PendingIntent pendingIntent) {
        alarmManager = getAlarmManager();
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + time,
                pendingIntent);
    }

    void removePreviousAlarm(PendingIntent pendingIntent) {
        System.out.println(alarmManager == null);
        alarmManager = getAlarmManager();
        alarmManager.cancel(pendingIntent);
        alarmManager = null;
    }

    public int getCurrentRingtoneMode() {
        AudioManager audioManager = getAudioManager();
        return audioManager.getRingerMode();
    }

    public void setRingtoneMode(int ringtoneSetting) {
        AudioManager audioManager = getAudioManager();
        audioManager.setRingerMode(ringtoneSetting);
    }

    public void vibrate(int TIME) {
        Vibrator vibrator = getVibrator();
        vibrator.vibrate(TIME);
    }

    public void createToast(String message) {
        cancelPreviousToastIfVisible();
        sToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        sToast.show();
    }

    private void cancelPreviousToastIfVisible() {
        if (sToast != null && sToast.getView().isShown()) {
            sToast.cancel();
        }
    }
}
