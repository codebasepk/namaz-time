package com.byteshaft.namaztime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StandardAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Helpers helpers = new Helpers(context);
        AlarmHelpers alarmHelpers = new AlarmHelpers(context);
        helpers.setTimesFromDatabase(false);
        alarmHelpers.setAlarmForNextNamaz();
    }
}
