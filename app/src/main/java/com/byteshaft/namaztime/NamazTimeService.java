package com.byteshaft.namaztime;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class NamazTimeService extends Service {

    private final String CONSTANT_TIME_LEFT = "0:-10";
    NamazNotification namazNotification = new NamazNotification(this);
    Timer updateTimer;
    public static String diff;

    public static Calendar getCalenderInstance() {
        return Calendar.getInstance();
    }

    public static String getAmPm() {
        return getTimeFormate().format(getCalenderInstance().getTime());
    }

    public static SimpleDateFormat getTimeFormate() {
        return new SimpleDateFormat("h:mm aa");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateTimer = new Timer();
        updateTimer.schedule(new TimerTask() {
            public void run() {
                try {
                    String namazTimeArr[] = {Helpers.mFajr, Helpers.mDhuhr, Helpers.mAsar,
                            Helpers.mMaghrib, Helpers.mIsha};
                    for (String i : namazTimeArr) {
                        Date date1 = getTimeFormate().parse(getAmPm());
                        Date date2 = getTimeFormate().parse(i);
                        if (date1.before(date2)) {
                            long mills = date1.getTime() - date2.getTime();
                            Log.v("Data1", "" + date1.getTime());
                            Log.v("Data2", "" + date2.getTime());
                            int Hours = (int) (mills / (1000 * 60 * 60));
                            int Mins = (int) (mills / (1000 * 60)) % 60;
                            diff = Hours + ":" + Mins; // updated value every1 second
                            System.out.println(diff);
                            if (diff.equals(CONSTANT_TIME_LEFT)) {
                                Log.v("condition match", "" + diff);
                                namazNotification.startNamazNotification();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }, 0, 30000);
        return flags;
    }
}


