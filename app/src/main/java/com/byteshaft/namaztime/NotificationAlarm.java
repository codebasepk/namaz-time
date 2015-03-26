package com.byteshaft.namaztime;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class NotificationAlarm extends BroadcastReceiver {

    private static Helpers mHelpers = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mHelpers == null) {
            mHelpers = new Helpers(context);
        }
        setAlarmForNextNamaz(context);
    }

    private void setAlarmForNextNamaz(Context context) {
        final int ONE_SECOND = 1000;
        final int ONE_MINUTE = ONE_SECOND * 60;
        final int TEN_MINUTES = ONE_MINUTE * 10;

        String[] namazTimes = mHelpers.getNamazTimesArray();

        for (String namazTime : namazTimes) {
            try {
                Date presentTime = mHelpers.getTimeFormat().parse(mHelpers.getAmPm());
                Date namaz = mHelpers.getTimeFormat().parse(namazTime);
                if (presentTime.before(namaz)) {
                    long difference = namaz.getTime() - presentTime.getTime();
                    long subtractTenMinutes = difference - TEN_MINUTES;
                    setAlarmsForNamaz(context, subtractTenMinutes);
                    break;
                } else {
                    mHelpers.setTimesFromDatabase(false);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private void setAlarmsForNamaz(Context context, long time) {
        AlarmManager alarmManager = getAlarmManager(context);
        Intent intent = new Intent("com.byteshaft.fireNotification");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context , 0 , intent , 0);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME , SystemClock.elapsedRealtime() + time , pendingIntent);
    }

    private AlarmManager getAlarmManager(Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }
}
