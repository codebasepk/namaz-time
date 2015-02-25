package com.byteshaft.namaztime;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.media.AudioManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.widget.Toast;

public class WidgetHelpers {

    private static Toast toast;
    private Context mContext;

    public WidgetHelpers(Context context) {
        mContext = context;
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

    public void setAlarm(int time, PendingIntent pendingIntent) {
        AlarmManager alarmManager = getAlarmManager();
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + time,
                pendingIntent);
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
        toast = Toast.makeText(mContext, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void cancelPreviousToastIfVisible() {
        if (toast != null && toast.getView().isShown()) {
            toast.cancel();
        }
    }
}
