package com.byteshaft.namaztime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StandardAlarmReceiver extends BroadcastReceiver {
    Helpers mHelpers;
    AlarmHelpers alarmHelpers;

    @Override
    public void onReceive(Context context, Intent intent) {
        mHelpers = new Helpers(context);
        alarmHelpers = new AlarmHelpers(context);
        mHelpers.setTimesFromDatabase(false);
        alarmHelpers.setAlarmForNextNamaz(context);
    }
}
