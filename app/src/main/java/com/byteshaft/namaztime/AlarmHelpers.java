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
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class AlarmHelpers extends ContextWrapper {
    Helpers mHelpers;
    AlarmManager mAlarmManager;
    PendingIntent mPendingIntent, mPIntent;

    public AlarmHelpers(Context base) {
        super(base);
        mHelpers = new Helpers(this);
        mAlarmManager = getAlarmManager(this);
    }

    void setAlarmForNextNamaz() {
        NotificationReceiver.sNotificationDisplayed = false;
        final int ONE_SECOND = 1000;
        final int ONE_MINUTE = ONE_SECOND * 60;
        final int TEN_MINUTES = ONE_MINUTE * 10;
        settingAlarm(TEN_MINUTES);
    }

    void settingAlarm(int TEN_MINUTES) {
        boolean namazTimeSet = false;
        String[] namazTimes = mHelpers.getNamazTimesArray();
        int count = 0;
        for (String raw : namazTimes) {
            Log.i("TAG", namazTimes[count]);
            String[] rawNamazTime = raw.split(" ");
            String namazTime = null;
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
            SimpleDateFormat sdfs = new SimpleDateFormat("hh:mm aa");
            Date dt;
            try {
                dt = sdf.parse(rawNamazTime[0]);
                namazTime = namazTimes[count];
                System.out.println("Time Display: " + sdfs.format(dt)); // <-- I got result here
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            try {
                Date presentTime = mHelpers.getTimeFormat().parse(mHelpers.getAmPm());
                Date namaz = mHelpers.getTimeFormat().parse(namazTime);
                Log.i("TAG", "present time " + presentTime);
                Log.i("TAG", "namaz time " + namaz);
                String item = namazTimes[4].split(" ")[0];
                Date lastDate = sdf.parse(item);
                Date lastItem = mHelpers.getTimeFormat().parse(sdfs.format(lastDate));
                Log.i("TAG", "condition check " + presentTime.before(namaz));
                if (presentTime.before(namaz)) {
                    long difference = namaz.getTime() - presentTime.getTime();
                    long subtractTenMinutes = difference - TEN_MINUTES;
                    setAlarmsForNamaz(subtractTenMinutes, namazTime);
                    namazTimeSet = true;
                    break;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            count++;
        }
        if (count >= 5 && !namazTimeSet) {
            alarmIfNoNamazTimeAvailable(this);
        }
    }

    private void setAlarmsForNamaz(long time, String namaz) {
        Log.i("NAMAZ_TIME",
                String.format("Setting alarm for: %d", TimeUnit.MILLISECONDS.toMinutes(time)));
        Intent intent = new Intent("com.byteshaft.shownotification");
        intent.putExtra("namaz", namaz);
        mPendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + time, mPendingIntent);
    }

    private void alarmIfNoNamazTimeAvailable(Context context) {

        Intent intent = new Intent("com.byteShaft.standardalarm");
        mPIntent = PendingIntent.getBroadcast(context, 1, intent, 0);
        Calendar timeOff = Calendar.getInstance();
        timeOff.add(Calendar.DATE, 1);
        timeOff.set(Calendar.HOUR_OF_DAY, 0);
        timeOff.set(Calendar.MINUTE, 2);
        mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                timeOff.getTimeInMillis(), AlarmManager.INTERVAL_DAY, mPIntent);
        Log.i("NAMAZ_TIME", "setting alarm of :" + timeOff.getTime());
    }

    private AlarmManager getAlarmManager(Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public void removePreviousAlarams() {
        try {
            if (mPendingIntent != null) {
                Log.i("NAMAZ_TIME", "removing namaz Alarm");
                mAlarmManager.cancel(mPendingIntent);
            } else if (mPIntent != null) {
                Log.i("NAMAZ_TIME", "removing");
                mAlarmManager.cancel(mPIntent);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
