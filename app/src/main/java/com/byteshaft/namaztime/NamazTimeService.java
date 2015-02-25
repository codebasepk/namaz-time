package com.byteshaft.namaztime;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class NamazTimeService extends Service {

    NamazNotification namazNotification = new NamazNotification(this);
    Timer updateTimer;
    private final  String CONSTANT_TIME_LEFT = "0:-10";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateTimer = new Timer();
        updateTimer.schedule(new TimerTask()
        {
            public void run()
            {
                try {
                    String namazTimeArr[] = {Helpers.mFajr , Helpers.mDhuhr , Helpers.mAsar , Helpers.mMaghrib , Helpers.mIsha};
                    for (int i = 0; i < namazTimeArr.length; i++) {

                        Date date1 = Helpers.getTimeFormate().parse(Helpers.getAmPm());
                        Date date2 = Helpers.getTimeFormate().parse(namazTimeArr[i]);

                        if (date1.before(date2)) {
                            long mills = date1.getTime() - date2.getTime();
                            Log.v("Data1", "" + date1.getTime());
                            Log.v("Data2", "" + date2.getTime());
                            int Hours = (int) (mills / (1000 * 60 * 60));
                            int Mins = (int) (mills / (1000 * 60)) % 60;

                            String diff = Hours + ":" + Mins; // updated value every1 second
                            System.out.println(diff);
                            if (diff.equalsIgnoreCase(CONSTANT_TIME_LEFT)){
                                namazNotification.startNamazNotification();
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

        }, 0, 100000);
        return flags;
    }
}


