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


public class SetAlarmReceiver extends BroadcastReceiver {

    Context context;
    Helpers mHelpers;
    int ONE_SECOND = 1000 * 60;
    int ONE_MINUTE = ONE_SECOND * 60;
    int TEN_MINUTES = ONE_MINUTE * 10;
    String[] namazTimes;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
            System.out.println("this is working");
            //calculateNamazTime();
        }
    }
    private AlarmManager getAlarmManager() {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public void calculateNamazTime() {
        mHelpers = new Helpers(context);
        namazTimes = mHelpers.getNamazTimesArray();
        for (String namazTime : namazTimes) {
            try {
                Date presentTime = mHelpers.getTimeFormat().parse(mHelpers.getAmPm());
                Date namaz = mHelpers.getTimeFormat().parse(namazTime);
                if(presentTime.before(namaz)){
                    int indexNumber =  namazTime.charAt(0);
                    if (indexNumber == 0) {
                        long difference = presentTime.getTime() - namaz.getTime();
                        long subtractTenMinutes = difference - TEN_MINUTES;
                        setAlarmsForNamaz(subtractTenMinutes);
                        System.out.println(indexNumber);
                        break;
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
    }

    private void setAlarmsForNamaz(long time) {
        Log.i("Setting alarm  :", "time" + time);
        AlarmManager alarmManager = getAlarmManager();
        Intent intent = new Intent("com.byteshaft.fireNotification");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context , 0 , intent , 0);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME , SystemClock.elapsedRealtime() + time , pendingIntent);
    }





}
