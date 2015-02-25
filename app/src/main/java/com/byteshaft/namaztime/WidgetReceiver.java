package com.byteshaft.namaztime;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

public class WidgetReceiver extends BroadcastReceiver {

    final int TEN_SECONDS = 10000;
    final static String SILENT_INTENT = "com.byteshaft.silent";
    Context mContext;
    PendingIntent mPendingIntent;
    BroadcastReceiver mBroadcastReceiver;
    private static Toast toast;

    private void registerReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                setRingtoneMode(WidgetGlobals.getRingtoneModeBackup());
                createToast("Phone ringer setting restored");
                WidgetGlobals.resetRingtoneBackup();
                WidgetGlobals.setIsPhoneSilent(false);
            }
        };

        mContext.getApplicationContext().registerReceiver(mBroadcastReceiver,
                new IntentFilter(SILENT_INTENT));
        mPendingIntent = PendingIntent.getBroadcast(
                mContext, 0, new Intent(SILENT_INTENT), 0);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        if (WidgetGlobals.isPhoneSilent()) {
            Log.i("NAMAZ", "Phone is in silent mode already.");
            setRingtoneMode(WidgetGlobals.getRingtoneModeBackup());
            createToast("Phone ringer setting restored");
            WidgetGlobals.resetRingtoneBackup();
            WidgetGlobals.setIsPhoneSilent(false);
        } else {
            WidgetGlobals.setRingtoneModeBackup(getCurrentRingtoneMode());
            setRingtoneMode(AudioManager.RINGER_MODE_VIBRATE);
            vibrate();
            createToast("Phone set to vibrate");
            WidgetGlobals.setIsPhoneSilent(true);
            registerReceiver();
            setAlarm(TEN_SECONDS, mPendingIntent);
        }
        WidgetProvider.setupWidget(mContext);
    }

    private AudioManager getAudioManager() {
        return (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }

    private AlarmManager getAlarmManager() {
        return (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
    }

    private Vibrator getVibrator() {
        return (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
    }

    private void setAlarm(int time, PendingIntent pendingIntent) {
        AlarmManager alarmManager = getAlarmManager();
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + time,
                pendingIntent);
    }

    private int getCurrentRingtoneMode() {
        AudioManager audioManager = getAudioManager();
        return audioManager.getRingerMode();
    }

    private void setRingtoneMode(int ringtoneSetting) {
        AudioManager audioManager = getAudioManager();
        audioManager.setRingerMode(ringtoneSetting);
    }

    private void vibrate() {
        Vibrator vibrator = getVibrator();
        vibrator.vibrate(500);
    }

    private void createToast(String message) {
        cancelPreviousToastIfVisible();
        toast = Toast.makeText(mContext, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void cancelPreviousToastIfVisible() {
        if (toast != null && toast.getView().isShown()) {
            toast.cancel();
        }
    }
}
