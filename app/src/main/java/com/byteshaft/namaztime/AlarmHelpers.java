package com.byteshaft.namaztime;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class AlarmHelpers extends ContextWrapper {

    static AlarmManager alarmManager;
    static PendingIntent pendingIntent;
    static PendingIntent pIntent;
    Helpers mHelpers;

    public AlarmHelpers(Context base) {
        super(base);
        mHelpers = new Helpers(this);
    }

    private AlarmManager getAlarmManager(Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    void setAlarmForNextNamaz() {
        final int ONE_SECOND = 1000;
        final int ONE_MINUTE = ONE_SECOND * 60;
        final int TEN_MINUTES = ONE_MINUTE * 10;
        settingAlarm(TEN_MINUTES);
    }

    void settingAlarm(int TEN_MINUTES) {
        String[] namazTimes = mHelpers.getNamazTimesArray();
        for (String namazTime : namazTimes) {
            try {
                Date presentTime = mHelpers.getTimeFormat().parse(mHelpers.getAmPm());
                Date namaz = mHelpers.getTimeFormat().parse(namazTime);
                String item = namazTimes[4];
                Date lastItem = mHelpers.getTimeFormat().parse(item);
                if (presentTime.before(namaz)) {
                    long difference = namaz.getTime() - presentTime.getTime();
                    long subtractTenMinutes = difference - TEN_MINUTES;
                    setAlarmsForNamaz(subtractTenMinutes, namazTime);
                    break;
                } else if (presentTime.after(lastItem)) {
                    alarmIfNoNamazTimeAvailable(this);
                    break;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private void setAlarmsForNamaz(long time, String namaz) {
        Log.i("NAMAZ_TIME",
                String.format("Setting alarm for: %d", TimeUnit.MILLISECONDS.toMinutes(time)));
        alarmManager = getAlarmManager(this);
        Intent intent = new Intent("com.byteshaft.shownotification");
        intent.putExtra("namaz", namaz);
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + time, pendingIntent);
    }

    private void alarmIfNoNamazTimeAvailable(Context context) {
        alarmManager = getAlarmManager(context);
        Intent intent = new Intent("com.byteShaft.standardalarm");
        pIntent = PendingIntent.getBroadcast(context, 1, intent, 0);
        Calendar timeOff = Calendar.getInstance();
        timeOff.add(Calendar.DATE, 1);
        timeOff.set(Calendar.HOUR_OF_DAY, 0);
        timeOff.set(Calendar.MINUTE, 5);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                timeOff.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pIntent);
        Log.i("NAMAZ_TIME", "setting alarm of :" + timeOff.getTime());
    }

    void removePreviousAlarams() {
        try {
            if (pendingIntent != null) {
                Log.i("NAMAZ_TIME", "removing namaz Alarm");
                alarmManager.cancel(pendingIntent);
            } else {
                Log.i("NAMAZ_TIME", "removing");
                alarmManager.cancel(pIntent);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }


}
