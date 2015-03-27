package com.byteshaft.namaztime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class NextNamazTimeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmHelpers alarmHelpers = new AlarmHelpers(context);
        alarmHelpers.setAlarmForNextNamaz(context);


    }
}
