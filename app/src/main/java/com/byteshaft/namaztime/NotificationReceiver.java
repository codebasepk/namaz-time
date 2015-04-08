package com.byteshaft.namaztime;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;


public class NotificationReceiver extends BroadcastReceiver {

    private int ONE_SECOND = 1000;
    private int ONE_MINUTE = ONE_SECOND * 60;
    private int THIRTY_MINUTE = ONE_MINUTE * 30;

    @Override
    public void onReceive(Context context, Intent intent) {
        String namaz = intent.getExtras().getString("namaz");
        Notifications notifications = new Notifications(context);
        notifications.startUpcomingNamazNotification(namaz);
        alarmToForTwelveMinutes(context, THIRTY_MINUTE);
    }

    private void alarmToForTwelveMinutes(Context context, long time) {
        Log.i("NAMAZ_TIME", "Setting Alarm FOR :" + time);
        AlarmManager alarmManager = getAlarmManager(context);
        Intent intent = new Intent("com.byteshaft.setnextalarm");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + time, pendingIntent);
    }

    private AlarmManager getAlarmManager(Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }
}
