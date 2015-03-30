package com.byteshaft.namaztime;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class AlarmHelpers extends ContextWrapper {
    Helpers mHelpers;

    public AlarmHelpers(Context base) {
        super(base);
    }

    void setAlarmForNextNamaz(Context context) {
        final int ONE_SECOND = 1000;
        final int ONE_MINUTE = ONE_SECOND * 60;
        final int TEN_MINUTES = ONE_MINUTE * 10;
        mHelpers = new Helpers(context);
        settingAlarm(context, TEN_MINUTES);
    }

    void settingAlarm(Context context, int TEN_MINUTES) {
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
                    setAlarmsForNamaz(context, subtractTenMinutes, namazTime);
                    break;
                } else if (presentTime.after(lastItem)) {
                    alarmIfNoNamazTimeAvailable(context);
                    break;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private void setAlarmsForNamaz(Context context, long time, String namaz) {
        Log.i("Setting Alarm For ", ":" + TimeUnit.MILLISECONDS.toMinutes(time));
        AlarmManager alarmManager = getAlarmManager(context);
        Intent intent = new Intent("com.byteshaft.fireNotification");
        intent.putExtra("namaz", namaz);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + time, pendingIntent);
    }

    private AlarmManager getAlarmManager(Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    private void alarmIfNoNamazTimeAvailable(Context context) {
        AlarmManager alarmMgr = getAlarmManager(context);
        Intent intent = new Intent("com.byteShaft.StandardAlarm");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, 0);
        Calendar timeOff = Calendar.getInstance();
        timeOff.add(Calendar.DATE, 1);
        timeOff.set(Calendar.HOUR_OF_DAY, 0);
        timeOff.set(Calendar.MINUTE, 5);
        alarmMgr.setInexactRepeating(AlarmManager.RTC,
                timeOff.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        Log.i("TAG", "setting alarm of :" + timeOff.getTime());
    }
}
