package com.byteshaft.namaztime;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.SystemClock;

public class WidgetReceiver extends BroadcastReceiver {

    private static int sRingerModeBackup;
    private final int TEN_SECONDS = 10000;
    AudioManager mAudioManager;
    Context mContext;
    PendingIntent mPendingIntent;
    AlarmManager alarmManager;

    private void setup() {
        BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mAudioManager.setRingerMode(sRingerModeBackup);
            }
        };

        mContext.getApplicationContext().registerReceiver(mBroadcastReceiver,
                new IntentFilter("com.byteshaft.wake"));
        mPendingIntent = PendingIntent.getBroadcast(
                mContext, 0, new Intent("com.byteshaft.wake"), 0);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        setup();
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        sRingerModeBackup = mAudioManager.getRingerMode();
        mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
        alarmManager.set(
                AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + TEN_SECONDS,
                mPendingIntent);
        WidgetProvider.setupWidget();
    }
}
