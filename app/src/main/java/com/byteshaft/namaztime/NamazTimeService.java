package com.byteshaft.namaztime;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import java.text.ParseException;
import java.util.Date;

public class NamazTimeService extends Service {

    private final long ONE_SECOND = 1000;
    private final long ONE_MINUTE = ONE_SECOND * 60;
    private final long TEN_MINUTES = ONE_MINUTE * 10;
    private Helpers mHelpers = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mHelpers = new Helpers(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String namazTimes[] = mHelpers.getNamazTimesArray();
        for (String namaz : namazTimes) {
            try {
                Date presentTime = mHelpers.getTimeFormat().parse(mHelpers.getAmPm());
                Date namazTime = mHelpers.getTimeFormat().parse(namaz);
                if (presentTime.before(namazTime)) {
                    long difference = namazTime.getTime() - presentTime.getTime();
                    if (difference < TEN_MINUTES) {
                        setNotificationAlarmForNamaz(namaz, difference);
                    } else {
                        long tenMinutesBeforeEvent = difference - TEN_MINUTES;
                        setNotificationAlarmForNamaz(namaz, tenMinutesBeforeEvent);
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private AlarmManager getAlarmManager() {
        return (AlarmManager) getSystemService(ALARM_SERVICE);
    }

    private void setNotificationAlarmForNamaz(String namaz, long time) {
        Log.i("ALARM", String.format("Setting alarm for %s", namaz));
        Intent intent = new Intent("com.byteshaft.namazupcoming");
        intent.putExtra("namaz", namaz);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        AlarmManager alarmManager = getAlarmManager();
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + time,
                pendingIntent);
    }
}


